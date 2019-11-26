#! /bin/bash

./gradlew clean build

cf delete -f populate_app

cf push -f manifest.yml
