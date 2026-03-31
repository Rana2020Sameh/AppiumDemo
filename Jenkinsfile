pipeline {

  agent any

  // ── Trigger: run automatically on every GitHub push ──────────────────────
  triggers {
    githubPush()
  }

  environment {
    SIMULATOR_NAME = 'iPhone 16 Pro'
    APPIUM_PORT    = '4723'
    APPIUM_LOG     = "${WORKSPACE}/appium-server.log"
    APPIUM_PID     = '/tmp/appium_jenkins.pid'
  }

  options {
    // Keep the last 10 builds in Jenkins history
    buildDiscarder(logRotator(numToKeepStr: '10'))
    // Fail the build if it runs longer than 30 minutes
    timeout(time: 30, unit: 'MINUTES')
    // Timestamps in console output
    timestamps()
  }

  stages {

    // ── 1. Source code ────────────────────────────────────────────────────
    stage('Checkout') {
      steps {
        git branch: 'main',
            url: 'https://github.com/Rana2020Sameh/AppiumDemo.git'
      }
    }

    // ── 2. Boot the iPhone 16 Pro simulator ──────────────────────────────
    stage('Boot iOS Simulator') {
      steps {
        sh '''
          echo "=== Locating simulator: ${SIMULATOR_NAME} ==="

          # Grab the UDID of the first matching (non-Max/Plus) device
          UDID=$(xcrun simctl list devices available | \
                 grep "${SIMULATOR_NAME}" | \
                 grep -v "Pro Max\\|Plus" | \
                 head -1 | \
                 grep -oE "[A-F0-9a-f-]{36}")

          if [ -z "$UDID" ]; then
            echo "ERROR: No simulator found matching '${SIMULATOR_NAME}'. Aborting."
            exit 1
          fi
          echo "Simulator UDID: $UDID"

          # Store UDID for later stages
          echo "$UDID" > /tmp/jenkins_sim_udid.txt

          # Shut down cleanly in case a previous build left it running
          xcrun simctl shutdown "$UDID" 2>/dev/null || true
          sleep 2

          # Boot the simulator
          xcrun simctl boot "$UDID"

          # Wait until the simulator reports "Booted"
          echo "Waiting for simulator to be ready..."
          for i in $(seq 1 30); do
            STATUS=$(xcrun simctl list devices | grep "$UDID" | grep -oE "Booted|Shutdown" || true)
            if [ "$STATUS" = "Booted" ]; then
              echo "Simulator is Booted."
              break
            fi
            sleep 3
          done

          # Open Simulator.app so the device is visible (optional but helpful)
          open -a Simulator 2>/dev/null || true
        '''
      }
    }

    // ── 3. Start Appium server ────────────────────────────────────────────
    stage('Start Appium Server') {
      steps {
        sh '''
          echo "=== Starting Appium on port ${APPIUM_PORT} ==="

          # Kill any process already holding the port
          lsof -ti tcp:${APPIUM_PORT} | xargs kill -9 2>/dev/null || true
          sleep 1

          # Launch Appium in the background; redirect stdout+stderr to log file
          nohup appium \
            --port ${APPIUM_PORT} \
            --log-level info \
            --log "${APPIUM_LOG}" \
            --allow-insecure chromedriver_autodownload \
            > "${APPIUM_LOG}.stdout" 2>&1 &
          echo $! > "${APPIUM_PID}"
          echo "Appium PID: $(cat ${APPIUM_PID})"

          # Poll until the /status endpoint returns HTTP 200
          # Try both 127.0.0.1 and localhost to handle IPv4/IPv6 binding
          echo "Waiting for Appium to be ready..."
          APPIUM_READY=false
          for i in $(seq 1 40); do
            HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
              http://127.0.0.1:${APPIUM_PORT}/status 2>/dev/null || echo "000")
            if [ "$HTTP_CODE" = "200" ]; then
              echo "Appium is ready! (HTTP 200 on attempt $i)"
              APPIUM_READY=true
              break
            fi
            # Also try via localhost in case Appium bound to IPv6
            HTTP_CODE_V6=$(curl -s -o /dev/null -w "%{http_code}" \
              http://localhost:${APPIUM_PORT}/status 2>/dev/null || echo "000")
            if [ "$HTTP_CODE_V6" = "200" ]; then
              echo "Appium is ready! (HTTP 200 via localhost on attempt $i)"
              APPIUM_READY=true
              break
            fi
            # Check if Appium process is still alive
            if ! kill -0 $(cat ${APPIUM_PID} 2>/dev/null) 2>/dev/null; then
              echo "ERROR: Appium process died unexpectedly!"
              break
            fi
            echo "  attempt $i / 40 (last HTTP code: $HTTP_CODE)..."
            sleep 3
          done

          # Print Appium log regardless, for visibility
          echo "=== Appium Server Log ==="
          cat "${APPIUM_LOG}" 2>/dev/null || cat "${APPIUM_LOG}.stdout" 2>/dev/null || echo "(no log found)"
          echo "==========================="

          if [ "$APPIUM_READY" != "true" ]; then
            echo "ERROR: Appium did not start in time."
            exit 1
          fi
        '''
      }
    }

    // ── 4. Compile and run Appium/TestNG tests ────────────────────────────
    stage('Run iOS Tests') {
      steps {
        sh '''
          echo "=== Building and running tests on ${SIMULATOR_NAME} ==="
          mvn clean test \
            --batch-mode \
            --no-transfer-progress \
            -Dsurefire.failIfNoSpecifiedTests=false
        '''
      }
    }

  }  // end stages

  // ── Post-build actions ────────────────────────────────────────────────────
  post {

    always {
      echo "=== Post-build: collecting results and cleaning up ==="

      // Publish TestNG / Surefire XML results
      junit allowEmptyResults: true,
            testResults: 'target/surefire-reports/*.xml'

      // Archive Appium server log for debugging
      archiveArtifacts artifacts: 'appium-server.log',
                       allowEmptyArchive: true

      // Stop Appium
      sh '''
        if [ -f "${APPIUM_PID}" ]; then
          PID=$(cat "${APPIUM_PID}")
          kill "$PID" 2>/dev/null || true
          rm -f "${APPIUM_PID}"
          echo "Appium (PID $PID) stopped."
        fi
        lsof -ti tcp:${APPIUM_PORT} | xargs kill -9 2>/dev/null || true
      '''

      // Shutdown the simulator
      sh '''
        if [ -f /tmp/jenkins_sim_udid.txt ]; then
          UDID=$(cat /tmp/jenkins_sim_udid.txt)
          xcrun simctl shutdown "$UDID" 2>/dev/null || true
          rm -f /tmp/jenkins_sim_udid.txt
          echo "Simulator $UDID shut down."
        fi
      '''
    }

    success {
      echo "All iOS Appium tests passed on ${env.SIMULATOR_NAME}!"
    }

    failure {
      echo "Build FAILED. Check the Surefire reports and appium-server.log for details."
    }

    unstable {
      echo "Build UNSTABLE — some tests failed. Review the TestNG report."
    }

  }

}
