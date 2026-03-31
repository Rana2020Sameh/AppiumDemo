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

    // Appium is installed via NVM at this exact path.
    // Jenkins does NOT source .zshrc so NVM is never activated automatically.
    // We add the NVM node bin directory directly here so every stage can see it.
    PATH = "${env.HOME}/.nvm/versions/node/v20.19.6/bin:${env.HOME}/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.PATH}"
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 30, unit: 'MINUTES')
    timestamps()
  }

  stages {

    // ── 1. Sanity-check every required tool before doing real work ────────────
    stage('Verify Prerequisites') {
      steps {
        sh '''
          echo "=== Checking required tools ==="

          echo "PATH in use: $PATH"

          echo "--- Java ---"
          java -version 2>&1       || { echo "ERROR: java not found";  exit 1; }

          echo "--- Maven ---"
          mvn --version            || { echo "ERROR: mvn not found";   exit 1; }

          echo "--- Node ---"
          node --version           || { echo "ERROR: node not found";  exit 1; }
          npm  --version

          echo "--- Appium ---"
          which appium             || { echo "ERROR: appium not found at: $PATH"; exit 1; }
          appium --version

          echo "--- XCUITest driver ---"
          appium driver list --installed 2>&1 | grep -i xcuitest \
            || { echo "ERROR: xcuitest driver missing. Run: appium driver install xcuitest"; exit 1; }

          echo "--- Xcode ---"
          xcrun --version          || { echo "ERROR: xcrun not found"; exit 1; }
          xcodebuild -version 2>&1 | head -2

          echo "=== All prerequisite checks passed ==="
        '''
      }
    }

    // ── 2. Boot iPhone 16 Pro simulator ──────────────────────────────────────
    stage('Boot iOS Simulator') {
      steps {
        sh '''
          echo "=== Locating simulator: ${SIMULATOR_NAME} ==="

          UDID=$(xcrun simctl list devices available \
                 | grep "${SIMULATOR_NAME}" \
                 | grep -v "Pro Max\\|Plus" \
                 | head -1 \
                 | grep -oE "[A-F0-9a-f-]{36}")

          if [ -z "$UDID" ]; then
            echo "ERROR: No simulator found matching '${SIMULATOR_NAME}'"
            echo "Available devices:"
            xcrun simctl list devices available
            exit 1
          fi

          echo "Simulator UDID: $UDID"
          echo "$UDID" > /tmp/jenkins_sim_udid.txt

          # Patch config.properites with the real UDID for this machine
          sed -i '' "s|iosUdid=.*|iosUdid=$UDID|" \
            src/test/java/resources/config.properites
          echo "config.properites → iosUdid=$UDID"

          # Clean boot
          xcrun simctl shutdown "$UDID" 2>/dev/null || true
          sleep 2
          xcrun simctl boot "$UDID"

          echo "Waiting for Booted state..."
          for i in $(seq 1 30); do
            STATUS=$(xcrun simctl list devices \
                     | grep "$UDID" \
                     | grep -oE "Booted|Shutdown" || true)
            if [ "$STATUS" = "Booted" ]; then
              echo "  → Simulator is Booted (attempt $i)"
              break
            fi
            echo "  attempt $i/30 — $STATUS"
            sleep 3
          done

          open -a Simulator 2>/dev/null || true
        '''
      }
    }

    // ── 3. Start Appium server ────────────────────────────────────────────────
    stage('Start Appium Server') {
      steps {
        sh '''
          echo "=== Starting Appium on port ${APPIUM_PORT} ==="
          echo "Appium path:    $(which appium)"
          echo "Appium version: $(appium --version)"

          # Release the port if something is already on it
          lsof -ti tcp:${APPIUM_PORT} | xargs kill -9 2>/dev/null || true
          sleep 1

          # BUILD_ID=dontKillMe stops Jenkins process-tree killer from
          # terminating Appium when this sh block exits
          BUILD_ID=dontKillMe nohup appium \
            --port    "${APPIUM_PORT}" \
            --log-level info \
            --log     "${APPIUM_LOG}" \
            > /dev/null 2>&1 &

          APPIUM_PID_VAL=$!
          echo "$APPIUM_PID_VAL" > "${APPIUM_PID}"
          echo "Appium started — PID $APPIUM_PID_VAL"

          # Poll HTTP /status until 200 OK
          echo "Waiting for Appium to be ready..."
          APPIUM_READY=false
          for i in $(seq 1 40); do
            CODE=$(curl -s -o /dev/null -w "%{http_code}" \
                   http://127.0.0.1:${APPIUM_PORT}/status 2>/dev/null || echo "000")
            if [ "$CODE" = "200" ]; then
              echo "  → Appium is ready! (attempt $i)"
              APPIUM_READY=true
              break
            fi
            # Also try via localhost (IPv6 fallback)
            CODE6=$(curl -s -o /dev/null -w "%{http_code}" \
                    http://localhost:${APPIUM_PORT}/status 2>/dev/null || echo "000")
            if [ "$CODE6" = "200" ]; then
              echo "  → Appium is ready on localhost! (attempt $i)"
              APPIUM_READY=true
              break
            fi
            # If process died, stop waiting
            kill -0 "$APPIUM_PID_VAL" 2>/dev/null \
              || { echo "ERROR: Appium process died unexpectedly!"; break; }
            echo "  attempt $i/40 — HTTP $CODE"
            sleep 3
          done

          # Always show the Appium log so failures are diagnosable
          echo "=== Appium Server Log ==="
          cat "${APPIUM_LOG}" 2>/dev/null || echo "(log not yet written)"
          echo "========================="

          if [ "$APPIUM_READY" != "true" ]; then
            echo "ERROR: Appium did not become ready in time."
            exit 1
          fi
        '''
      }
    }

    // ── 4. Run TestNG tests via Maven ─────────────────────────────────────────
    stage('Run iOS Tests') {
      steps {
        sh '''
          echo "=== Running Appium tests on ${SIMULATOR_NAME} ==="
          mvn clean test \
            --batch-mode \
            --no-transfer-progress \
            -Dsurefire.failIfNoSpecifiedTests=false
        '''
      }
    }

  }  // end stages

  // ── Post: publish results, stop Appium, shut down simulator ──────────────
  post {

    always {
      echo "=== Post-build cleanup ==="

      junit allowEmptyResults: true,
            testResults: 'target/surefire-reports/*.xml'

      archiveArtifacts artifacts: 'appium-server.log',
                       allowEmptyArchive: true

      sh '''
        if [ -f "${APPIUM_PID}" ]; then
          PID=$(cat "${APPIUM_PID}")
          kill "$PID" 2>/dev/null && echo "Appium (PID $PID) stopped." || true
          rm -f "${APPIUM_PID}"
        fi
        lsof -ti tcp:${APPIUM_PORT} | xargs kill -9 2>/dev/null || true
      '''

      sh '''
        if [ -f /tmp/jenkins_sim_udid.txt ]; then
          UDID=$(cat /tmp/jenkins_sim_udid.txt)
          xcrun simctl shutdown "$UDID" 2>/dev/null || true
          rm -f /tmp/jenkins_sim_udid.txt
          echo "Simulator $UDID shut down."
        fi
      '''
    }

    success  { echo "✅ All iOS Appium tests passed on ${env.SIMULATOR_NAME}!" }
    failure  { echo "❌ Build FAILED — check appium-server.log above for details." }
    unstable { echo "⚠️  Build UNSTABLE — some tests failed. Review the TestNG report." }

  }

}
