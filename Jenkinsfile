pipeline {

  agent any

  triggers {
    githubPush()
  }

  environment {
    SIMULATOR_NAME = 'iPhone 16 Pro'
    APPIUM_PORT    = '4723'
    APPIUM_LOG     = "${WORKSPACE}/appium-server.log"
    APPIUM_PID     = "${WORKSPACE}/appium.pid"

    // ── Extend PATH so Jenkins can find Homebrew tools (Node, Appium, etc.) ──
    // Jenkins runs in a limited shell and often misses /opt/homebrew/bin
    PATH           = "/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.PATH}"
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 30, unit: 'MINUTES')
    timestamps()
  }

  stages {

    // ── 1. Verify all required tools are present before doing any real work ──
    stage('Verify Prerequisites') {
      steps {
        sh '''
          echo "=== Checking required tools ==="

          echo "--- Java ---"
          java -version 2>&1 || { echo "ERROR: java not found!"; exit 1; }

          echo "--- Maven ---"
          mvn --version || { echo "ERROR: mvn not found!"; exit 1; }

          echo "--- Node / npm ---"
          node --version || { echo "ERROR: node not found. Install via: brew install node"; exit 1; }
          npm  --version || { echo "ERROR: npm not found.";  exit 1; }

          echo "--- Appium ---"
          which appium   || { echo "ERROR: appium not found. Install via: npm install -g appium"; exit 1; }
          appium --version

          echo "--- Appium XCUITest driver ---"
          appium driver list --installed 2>/dev/null | grep xcuitest || {
            echo "WARNING: XCUITest driver may not be installed."
            echo "         Run: appium driver install xcuitest"
          }

          echo "--- Xcode CLI tools ---"
          xcrun --version       || { echo "ERROR: xcrun not found. Install Xcode CLT."; exit 1; }
          xcodebuild -version 2>&1 | head -2

          echo "=== All prerequisite checks passed ==="
        '''
      }
    }

    // ── 2. Boot the iPhone 16 Pro simulator ──────────────────────────────────
    stage('Boot iOS Simulator') {
      steps {
        sh '''
          echo "=== Locating simulator: ${SIMULATOR_NAME} ==="

          UDID=$(xcrun simctl list devices available | \
                 grep "${SIMULATOR_NAME}" | \
                 grep -v "Pro Max\\|Plus" | \
                 head -1 | \
                 grep -oE "[A-F0-9a-f-]{36}")

          if [ -z "$UDID" ]; then
            echo "ERROR: No available simulator found matching '${SIMULATOR_NAME}'."
            echo "Available simulators:"
            xcrun simctl list devices available
            exit 1
          fi
          echo "Simulator UDID: $UDID"

          # Save UDID for the cleanup step in post
          echo "$UDID" > /tmp/jenkins_sim_udid.txt

          # ── CRITICAL FIX: Update config.properites with the ACTUAL booted UDID ──
          # The hardcoded UDID in config.properites is only valid on your local machine.
          # On any other machine (or after a Xcode update), the UDID changes.
          sed -i '' "s|iosUdid=.*|iosUdid=$UDID|" \
            src/test/java/resources/config.properites
          echo "config.properites updated → iosUdid=$UDID"

          # Shut down cleanly in case a previous build left it running
          xcrun simctl shutdown "$UDID" 2>/dev/null || true
          sleep 2

          # Boot the simulator
          xcrun simctl boot "$UDID"

          echo "Waiting for simulator to reach Booted state..."
          for i in $(seq 1 30); do
            STATUS=$(xcrun simctl list devices | grep "$UDID" | \
                     grep -oE "Booted|Shutdown" || true)
            if [ "$STATUS" = "Booted" ]; then
              echo "Simulator is Booted (attempt $i)."
              break
            fi
            echo "  waiting... attempt $i / 30"
            sleep 3
          done

          # Open Simulator.app so the UI is visible (helps with XCUITest)
          open -a Simulator 2>/dev/null || true
        '''
      }
    }

    // ── 3. Start Appium server ────────────────────────────────────────────────
    stage('Start Appium Server') {
      steps {
        sh '''
          echo "=== Starting Appium on port ${APPIUM_PORT} ==="
          echo "Appium binary: $(which appium)"
          echo "Appium version: $(appium --version)"

          # Kill anything already sitting on the port
          lsof -ti tcp:${APPIUM_PORT} | xargs kill -9 2>/dev/null || true
          sleep 1

          # ── CRITICAL FIX: BUILD_ID=dontKillMe tells Jenkins NOT to kill ──
          # ── background processes when the sh step ends.                  ──
          # Without this, Jenkins kills Appium the moment this sh block exits,
          # which is why curl never gets a response.
          BUILD_ID=dontKillMe nohup appium \
            --port "${APPIUM_PORT}" \
            --log-level info \
            --log  "${APPIUM_LOG}" \
            > /dev/null 2>&1 &

          APPIUM_PID_VAL=$!
          echo "$APPIUM_PID_VAL" > "${APPIUM_PID}"
          echo "Appium started with PID: $APPIUM_PID_VAL"

          # Poll until /status returns HTTP 200 (works for all Appium 2.x versions)
          echo "Waiting for Appium to be ready..."
          APPIUM_READY=false
          for i in $(seq 1 40); do

            # Try 127.0.0.1 (IPv4)
            CODE=$(curl -s -o /dev/null -w "%{http_code}" \
                   http://127.0.0.1:${APPIUM_PORT}/status 2>/dev/null || echo "000")
            if [ "$CODE" = "200" ]; then
              echo "Appium is ready on 127.0.0.1 (attempt $i)"
              APPIUM_READY=true
              break
            fi

            # Also try localhost (covers IPv6 ::1 binding)
            CODE6=$(curl -s -o /dev/null -w "%{http_code}" \
                    http://localhost:${APPIUM_PORT}/status 2>/dev/null || echo "000")
            if [ "$CODE6" = "200" ]; then
              echo "Appium is ready on localhost (attempt $i)"
              APPIUM_READY=true
              break
            fi

            # If the process died early, stop waiting
            if ! kill -0 "$APPIUM_PID_VAL" 2>/dev/null; then
              echo "ERROR: Appium process (PID $APPIUM_PID_VAL) died unexpectedly!"
              break
            fi

            echo "  attempt $i / 40 — HTTP $CODE (127.0.0.1) / $CODE6 (localhost)"
            sleep 3
          done

          # Always print the Appium log so you can see what happened
          echo "=== Appium Server Log ==="
          cat "${APPIUM_LOG}" 2>/dev/null || echo "(log file not yet written)"
          echo "========================="

          if [ "$APPIUM_READY" != "true" ]; then
            echo "ERROR: Appium did not become ready in time."
            echo "Hints:"
            echo "  1. Run: appium driver list --installed   (XCUITest must be listed)"
            echo "  2. Run: appium driver install xcuitest   (if missing)"
            echo "  3. Check the Appium log above for errors"
            exit 1
          fi
        '''
      }
    }

    // ── 4. Compile and run Appium / TestNG tests ──────────────────────────────
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

  // ── Post-build: publish results and clean up regardless of outcome ─────────
  post {

    always {
      echo "=== Post-build: collecting results and cleaning up ==="

      // Publish TestNG / Surefire XML results
      junit allowEmptyResults: true,
            testResults: 'target/surefire-reports/*.xml'

      // Archive the Appium server log for debugging
      archiveArtifacts artifacts: 'appium-server.log',
                       allowEmptyArchive: true

      // Stop Appium
      sh '''
        if [ -f "${APPIUM_PID}" ]; then
          PID=$(cat "${APPIUM_PID}")
          kill "$PID" 2>/dev/null && echo "Appium (PID $PID) stopped." || true
          rm -f "${APPIUM_PID}"
        fi
        # Belt-and-suspenders: kill anything still on the port
        lsof -ti tcp:${APPIUM_PORT} | xargs kill -9 2>/dev/null || true
      '''

      // Shut down the simulator
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
      echo "✅ All iOS Appium tests passed on ${env.SIMULATOR_NAME}!"
    }

    failure {
      echo "❌ Build FAILED. Check the Surefire reports and appium-server.log above."
    }

    unstable {
      echo "⚠️  Build UNSTABLE — some tests failed. Review the TestNG report."
    }

  }

}
