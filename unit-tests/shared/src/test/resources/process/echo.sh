#!/bin/sh

while IFS='$\n' read -r line ; do
  if [ $line = "quit" ]; then
    exit 0
  fi
  printf $line
done
