#!/bin/bash
# Wrapper for gradle command
# This ensures Java 21 is used for builds

export JAVA_HOME="C:\Program Files\Java\jdk-21.0.10"
export PATH="$JAVA_HOME/bin:$PATH"

exec gradle "$@"
