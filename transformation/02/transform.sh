#!/bin/bash

install_path=$(dirname "$0")
VERSION=4.8.0

JAR_FILE=${install_path}/camel-jbang-plugin-explain-${VERSION}-jar-with-dependencies.jar
SOURCE_DIR=${DATA_DIR}/camel/

echo "Generating the dataset"
java -jar "${JAR_FILE}" data dump --data-type component-documentation --source-path "${SOURCE_DIR}"

if [[ -d ${DATA_DIR}/dataset ]] ; then
  echo "Removing old dataset"
  rm -rf "${DATA_DIR}"/dataset
fi

echo "Moving the dataset"
mv dataset "${DATA_DIR}"
