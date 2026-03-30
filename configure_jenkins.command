#!/bin/bash
set -e

JENKINS_JOB_DIR="$HOME/.jenkins/jobs/Appium"
CONFIG_FILE="$JENKINS_JOB_DIR/config.xml"

echo "=== Configuring Jenkins 'Appium' Pipeline job ==="

# Back up existing config
cp "$CONFIG_FILE" "$CONFIG_FILE.bak" 2>/dev/null && echo "Backup saved: $CONFIG_FILE.bak" || true

# Write the correct Pipeline config
cat > "$CONFIG_FILE" << 'XML'
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <description>Appium iOS tests – iPhone 16 Pro simulator – triggers on every GitHub push</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <com.coravy.hudson.plugins.github.GithubProjectProperty plugin="github@1.29.5">
      <projectUrl>https://github.com/Rana2020Sameh/AppiumDemo/</projectUrl>
      <displayName></displayName>
    </com.coravy.hudson.plugins.github.GithubProjectProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.90">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.7.1">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/Rana2020Sameh/AppiumDemo.git</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers>
    <com.cloudbees.jenkins.GitHubPushTrigger plugin="github@1.29.5">
      <spec></spec>
    </com.cloudbees.jenkins.GitHubPushTrigger>
  </triggers>
  <disabled>false</disabled>
</flow-definition>
XML

echo "config.xml updated successfully."
echo ""

# Ask Jenkins to reload its configuration from disk
echo "Reloading Jenkins configuration..."
RELOAD_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "http://localhost:8080/reload" \
  --user "ranasameh:$(cat $HOME/.jenkins_api_token 2>/dev/null || echo '')" 2>/dev/null || echo "000")

if [ "$RELOAD_STATUS" = "302" ] || [ "$RELOAD_STATUS" = "200" ]; then
  echo "Jenkins reloaded successfully (HTTP $RELOAD_STATUS)."
else
  echo "Note: Auto-reload returned HTTP $RELOAD_STATUS."
  echo "Jenkins will pick up the new config on next restart,"
  echo "or go to: http://localhost:8080/manage → Reload Configuration from Disk"
fi

echo ""
echo "=== Done! The 'Appium' job is now a Pipeline job. ==="
echo "It will trigger automatically on every GitHub push."
echo ""
echo "Press any key to close..."
read -n1
