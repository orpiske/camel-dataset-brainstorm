#!/bin/bash

install_path=$(dirname "$0")
VERSION=4.8.0

JAR_FILE=${install_path}/camel-jbang-plugin-explain-${VERSION}-jar-with-dependencies.jar
BRAINSTORM_HOME=/opt/brainstorm/
BASE_DATA_DIR=${BRAINSTORM_HOME}/data
DATA_DIR=${BASE_DATA_DIR}/work/camel/

echo "Generating the dataset"
java -jar "${JAR_FILE}" data dump --data-type component-documentation --source-path ${DATA_DIR}

if [[ -d ${BASE_DATA_DIR}/dataset ]] ; then
  echo "Removing old dataset"
  rm -rf ${BASE_DATA_DIR}/dataset
fi

echo "Moving the dataset"
mv dataset ${BASE_DATA_DIR}
