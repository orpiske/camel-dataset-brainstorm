#!/bin/bash

install_path=$(dirname "$0")
CODE_DIR=$(echo "$1" | jq -r .path)

logDir=$(mktemp -d)

# Step 1: convert all component documentation to a docbook file
find "$CODE_DIR" -type f -iname '*.adoc' -ipath '*src/main/docs*' -exec "${install_path}"/convert-file.sh {} "$logDir" \; | tee $logDir/conversion.log

numDocs=$(find "$CODE_DIR" -type f -iname '*.adoc' -ipath '*src/main/docs*' | wc -l)
numConverted=$(find "$CODE_DIR" -type f -iname '*.xml' -ipath '*src/main/docs*' | wc -l)
printf "Conversion complete. Checking results: converted %s of %s\n" "$numConverted" "$numDocs"
printf "Check %s for results and failures" "$logDir/conversion.log"