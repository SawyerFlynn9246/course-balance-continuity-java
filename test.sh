#!/usr/bin/env sh
set -eu
mkdir -p out-test
javac -d out-test $(find src/main/java src/test/java -name '*.java')
java -cp out-test learning.continuity.CourseContinuityServiceTest
