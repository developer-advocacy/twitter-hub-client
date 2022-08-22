#!/usr/bin/env bash
set -e
set -o pipefail
cd $ROOT_DIR
./mvnw -DskipTests=true spring-javaformat:apply clean deploy spring-boot:build-image -Dspring-boot.build-image.imageName=$IMAGE_NAME
