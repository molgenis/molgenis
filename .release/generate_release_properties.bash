#!/bin/bash

ARTIFACT_ID=${1}
GROUP_ID=${2}
RELEASE_SCOPE=${3}
VERSION=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)

echo "MAVEN release is prepared"
echo "------------------------------------------"
echo "Group                 : ${GROUP_ID}"
echo "Artifact              : ${ARTIFACT_ID}"
echo "Release scope         : ${RELEASE_SCOPE}"

VERSION_PARSED=(${VERSION//./ })
MAJOR=${VERSION_PARSED[0]}
MINOR=${VERSION_PARSED[1]}
PATCH_PARSED=(${VERSION_PARSED[2]//-/ })
PATCH=${PATCH_PARSED[0]}

git fetch --tags
CANDIDATE_NUMBER=1
TAGS=$(git tag -l "[${MAJOR}]\.[${MINOR}]\.[${PATCH}]-RC*")
for tag in "${TAGS[@]}"; do
    RC_NUMBER=$(echo ${tag: -1})
    if [[ ! -z ${RC_NUMBER} ]]; then
      ((${RC_NUMBER} >= ${CANDIDATE_NUMBER})) && CANDIDATE_NUMBER=$((${RC_NUMBER}+1))
    fi
done

if [[ ${RELEASE_SCOPE} = "candidate" ]]; then
  VERSION_POSTFIX="-RC${CANDIDATE_NUMBER}"
else
  VERSION_POSTFIX="-RELEASE"
fi

RELEASE_VERSION=${MAJOR}.${MINOR}.${PATCH}${VERSION_POSTFIX}

DEV_PATCH=${PATCH}
if [[ ${RELEASE_SCOPE} = "release" ]]; then
  DEV_PATCH=$((${PATCH}+1))
fi
DEV_VERSION="${MAJOR}.${MINOR}.${DEV_PATCH}-SNAPSHOT"
echo "------------------------------------------"
echo "Release version       : ${RELEASE_VERSION}"
echo "New dev version       : ${DEV_VERSION}"

echo "scm.tag=${RELEASE_VERSION}
project.rel.${GROUP_ID}\:${ARTIFACT_ID}=${RELEASE_VERSION}
project.dev.${GROUP_ID}\:${ARTIFACT_ID}=${DEV_VERSION}" >> release.properties