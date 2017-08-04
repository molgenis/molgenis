#!/bin/bash
if [ "${TRAVIS_REPO_SLUG}" == "fdlk/molgenis" ] && [ "${TRAVIS_PULL_REQUEST}" == "false" ]; then
	echo "Running sonar analysis in maven..."
    ./mvnw sonar:sonar --batch-mode --quiet
    echo "Done."
else
    echo "Skipped Sonar analysis (only runs for committed builds on the fdlk/molgenis repo)"
fi