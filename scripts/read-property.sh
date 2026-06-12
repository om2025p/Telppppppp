#!/bin/bash
set -e

FILE=$1
VAR=$2

if [ ! -f "$FILE" ]; then
  echo "Properties file $FILE not found!"
  exit 1
fi

if [[ ! "$VAR" =~ ^[a-z][a-zA-Z0-9_.]*$ ]]; then
  echo "Invalid property name: $VAR"
  exit 1
fi

while IFS='=' read -r key value; do
  if [[ "$key" == "$VAR" ]]; then
    echo "$value"
    exit
  fi
done < "$FILE"
echo "$VAR not found in $FILE!"
exit 1