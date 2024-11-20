#!/bin/bash
JAR_FILE=$HOME/code/java/camel-jbang-explain/target/camel-jbang-plugin-explain-4.8.0-jar-with-dependencies.jar
DATA_DIR=$HOME/tmp/brainstorm-data-3

java -jar ${JAR_FILE} data dump --data-type component-documentation --source-path ${DATA_DIR}/camel/
