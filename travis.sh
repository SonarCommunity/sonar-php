#!/bin/bash

set -euo pipefail

function cleanPhpInMavenRepository {
  rm -rf ~/.m2/repository/org/sonarsource/php
}

function configureTravis {
  mkdir ~/.local
  curl -sSL https://github.com/SonarSource/travis-utils/tarball/v27 | tar zx --strip-components 1 -C ~/.local
  source ~/.local/bin/install
}

configureTravis

case "$TEST" in

ci)
  git fetch --unshallow || true

  # Analyze with SNAPSHOT version as long as SQ does not correctly handle
  # purge of release data
  SONAR_PROJECT_VERSION=`maven_expression "project.version"`

  # Do not deploy a SNAPSHOT version but the release version related to this build
  set_maven_build_version $TRAVIS_BUILD_NUMBER

  export MAVEN_OPTS="-Xmx1536m -Xms128m"
  mvn org.jacoco:jacoco-maven-plugin:prepare-agent deploy sonar:sonar \
      -Pcoverage-per-test,deploy-sonarsource \
      -Dmaven.test.redirectTestOutputToFile=false \
      -Dsonar.host.url=$SONAR_HOST_URL \
      -Dsonar.login=$SONAR_TOKEN \
      -Dsonar.projectVersion=$SONAR_PROJECT_VERSION \
      -B -e -V

  ;;

plugin|ruling)  

  cleanPhpInMavenRepository # make sure we don't use an old version from the travis cache
  mvn install -Dsource.skip=true -Denforcer.skip=true -Danimal.sniffer.skip=true -Dmaven.test.skip=true

  if [ "$SQ_VERSION" = "DEV" ] ; then
    build_snapshot "SonarSource/sonarqube"
  fi

  cd its/$TEST
  mvn -Dsonar.runtimeVersion="$SQ_VERSION" -Dmaven.test.redirectTestOutputToFile=false package
  cleanPhpInMavenRepository # avoid adding snapshot to the travis cache
  ;;

*)
  echo "Unexpected TEST mode: $TEST"
  exit 1
  ;;

esac
