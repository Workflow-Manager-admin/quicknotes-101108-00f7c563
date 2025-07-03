#!/bin/bash
cd /home/kavia/workspace/code-generation/quicknotes-101108-00f7c563/notes_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

