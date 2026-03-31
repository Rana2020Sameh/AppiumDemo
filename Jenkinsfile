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

    // Base PATH — covers Homebrew (Apple Silicon + Intel) and user-local npm installs
    // $HOME/bin is where npm puts global binaries when prefix = $HOME
    PATH = "${env.HOME}/bin:${env.HOME}/.npm-global/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.PATH}"
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 30, unit: 'MINUTES')
    timestamps()
  }

  stages {

    // ── 1. Extend PATH with every place npm may have installed Appium ─────────
    // Jenkins runs in a stripped shell with a different npm config than your
    // terminal. Appium can end up in several locations depending on how npm
    // prefix was set when it was installed:
    //   • $HOME/bin          — when prefix = $HOME (most common user install)
    //   • $HOME/.npm-global/bin — when prefix = ~/.npm-global
    //   • /opt/homebrew/bin  — when installed via Homebrew-managed npm
    //   • /usr/local/bin     — Intel Mac Homebrew
    // We probe all of them and pick the first one that actually contains appium.
    stage('Setup Environment') {
      steps {
        script {
          def appiumPath = sh(
            script: '''
              for candidate in \
                  "$HOME/bin/appium" \
                  "$HOME/.npm-global/bin/appium" \
                  "/opt/homebrew/bin/appium" \
                  "/usr/local/bin/appium"; do
                if [ -x "$candidate" ]; then
                  dirname "$candidate"
                  exit 0
                fi
              done
              # Last resort: ask npm where its globals live
              npm config get prefix 2>/dev/null && echo "/bin" | tr -d "\\n" || echo "/opt/homebrew/bin"
            ''',
            returnStdout: true
          ).trim()

          env.APPIUM_BIN_DIR = appiumPath
          env.PATH = "${appiumPath}:${env.HOME}/bin:${env.HOME}/.npm-global/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.PATH}"
          echo "Appium found in: ${appiumPath}"
          echo "Effective PATH:  ${env.PATH}"
        }
      }
    }

    // ── 2. Verify all required tools are present ──────────────────────────────
    stage('Verify Prerequisites') {
      steps {
        sh '''
          echo "=== Checking required tools ==="

          echo "--- Java ---"
          java -version 2>&1 || { echo "ERROR: java not found!"; exit 1; }

          echo "--- Maven ---"
          mvn --version || { echo "ERROR: mvn not found! Install via: brew install maven"; exit 1; }

          echo "--- Node / npm ---"
          node --version || { echo "ERROR: node not found. Install via: brew install node"; exit 1; }
          npm  --version || { echo "ERROR: npm not found."; exit 1; }

          echo "--- Appium ---"
          which appium || {
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "ERROR: Appium not found in PATH."
            echo "Run these commands in your Terminal, then retry:"
            echo "  npm install -g appium"
            echo "  appium driver install xcuitest"
            echo "npm global bin is: $(npm config get prefix)/bin"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            exit 1
          }
          appium --version

          echo "--- XCUITest driver ---"
          appium driver list --installed 2>/dev/null | grep -i xcuitest || {
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "ERROR: XCUITest driver is not installed."
            echo "Run in your Terminal, then retry:"
            echo "  appium driver install xcuitest"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            exit 1
          }

          echo "--- Xcode CLI tools ---"
          xcrun --version       || { echo "ERROR: xcrun not found. Install Xcode from the App Store."; exit 1; }
          xcodebuild -version 2>&1 | head -2

          echo "=== All prerequisite checks passed ==="
        '''
      }
    }

    // ── 3. Boot the iPhone 16 Pro simulator ──────────────────────────────────
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

          # Save UDID so the post block can shut it down
          echo "$UDID" > /tmp/jenkins_sim_udid.txt

          # Sync the real UDID into config.properites so the test driver connects
          # to the correct simulator (the hardcoded value only works on one machine)
          sed -i '' "s|iosUdid=.*|iosUdid=$UDID|" \
            src/test/java/resources/config.properites
          echo "config.properites updated → iosUdid=$UDID"

          # Shut down cleanly in case a previous build left it running
          xcrun simctl shutdown "$UDID" 2>/dev/null || true
          sleep 2

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

          open -a Simulator 2>/dev/null || true
        '''
      }
    }

    // ── 4. Start Appium server ────────────────────────────────────────────────
    stage('Start Appium Server') {
      steps {
        sh '''
          echo "=== Starting Appium on port ${APPIUM_PORT} ==="
          echo "Appium path:    $(which appium)"
          echo "Appium version: $(appium --version)"

          # Kill anything already on the port
          lsof -ti tcp:${APPIUM_PORT} | xargs kill -9 2>/dev/null || true
          sleep 1

          # BUILD_ID=dontKillMe prevents Jenkins from killing the background
          # process when this sh block exits (its process-tree killer is aggressive)
          BUILD_ID=dontKillMe nohup appium \
            --port "${APPIUM_PORT}" \
            --log-level info \
            --log  "${APPIUM_LOG}" \
            > /dev/null 2>&1 &

          APPIUM_PID_VAL=$!
          echo "$APPIUM_PID_VAL" > "${APPIUM_PID}"
          echo "Appium started → PID $APPIUM_PID_VAL"

          # Poll until /status returns HTTP 200
          echo "Waiting for Appium to be ready..."
          APPIUM_READY=false
          for i in $(seq 1 40); do

            CODE=$(curl -s -o /dev/null -w "%{http_code}" \
                   http://127.0.0.1:${APPIUM_PORT}/status 2>/dev/null || echo "000")
            if [ "$CODE" = "200" ]; then
              echo "Appium is ready on 127.0.0.1 (attempt $i)"
              APPIUM_READY=true; break
            fi

            CODE6=$(curl -s -o /dev/null -w "%{http_code}" \
                    http://localhost:${APPIUM_PORT}/status 2>/dev/null || echo "000")
            if [ "$CODE6" = "200" ]; then
              echo "Appium is ready on localhost (attempt $i)"
              APPIUM_READY=true; break
            fi

            if ! kill -0 "$APPIUM_PID_VAL" 2>/dev/null; then
              echo "ERROR: Appium process died unexpectedly!"
              break
            fi

            echo "  attempt $i / 40 — HTTP $CODE"
            sleep 3
          done

          echo "=== Appium Server Log ==="
          cat "${APPIUM_LOG}" 2>/dev/null || echo "(log not yet written)"
          echo "========================="

          if [ "$APPIUM_READY" != "true" ]; then
            echo "ERROR: Appium did not become ready. See log above."
            exit 1
          fi
        '''
      }
    }

    // ── 5. Run Appium / TestNG tests ──────────────────────────────────────────
    stage('Run iOS Tests') {
      steps {
        sh '''
          echo "=== Running tests on ${SIMULATOR_NAME} ==="
          mvn clean test \
            --batch-mode \
            --no-transfer-progress \
            -Dsurefire.failIfNoSpecifiedTests=false
        '''
      }
    }

  }  // end stages

  // ── Post-build: publish results and clean up ──────────────────────────────
  post {

    always {
      echo "=== Post-build: collecting results and cleaning up ==="

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

    success  { echo "✅ All iOS tests passed on ${env.SIMULATOR_NAME}!" }
    failure  { echo "❌ Build FAILED. Check appium-server.log and Surefire reports." }
    unstable { echo "⚠️  Build UNSTABLE — some tests failed. Review the TestNG report." }

  }

}
