#!/usr/bin/env bash
export GOVBR_API_URL=""
export GOVBR_CLIENTID=""
export GOVBR_SECRET=""
export GOVBR_BASIC=""
export GOVBR_CODEVERIFY=""
export GOVBR_SCOPE=""
export GOVBR_CODE=""

gradle wrapper --gradle-version 8.10
# ./gradlew run
./gradlew clean build
java -jar app/build/libs/govbrsso-1.0.jar