#!/usr/bin/env bash
# this script updates the json file with new version that BraveNewPipe is fetching regulary

set -e

if [[ $# -ne 2 ]]; then
  echo "This needs a release tag and a apk file:"
  echo "e.g. $0 v0.22.0-1.0.5 /path/to/BraveNewPipe_v0.22.0-1.0.5.apk"
  exit 1
fi

if [[ -z "$GITHUB_SUPER_TOKEN" ]]; then
  echo "This script needs a GitHub personal access token."
  exit 1
fi

TAG=$1
APK_FILE=$2

BNP_R_MGR_REPO="bnp-r-mgr"

GITHUB_USER="bravenewpipe"
RELEASE_REPO="NewPipe"
RELEASE_BODY="Apk available at ${GITHUB_USER}/${RELEASE_REPO}@${TAG}](https://github.com/${GITHUB_USER}/${RELEASE_REPO}/releases/tag/${TAG})."

PRERELEASE="false"
if [[ "$TAG" == "latest" ]]; then
  PRERELEASE="true"
fi

if [[ "$GITHUB_REPOSITORY" != "${GITHUB_USER}/${RELEASE_REPO}" ]]; then
  echo "This mirror script is only meant to be run from ${GITHUB_USER}/${RELEASE_REPO}, not ${GITHUB_REPOSITORY}. Nothing to do here."
  exit 0
fi

create_tagged_release() {
  local L_REPO=$1
  local L_BRANCH=$2
  local L_COMMIT_MSG=$3
  pushd /tmp/${L_REPO}/

  # Set the local git identity
  git config user.email "${GITHUB_USER}@users.noreply.github.com"
  git config user.name "$GITHUB_USER"

  # Obtain the release ID for the previous release of $TAG (if present)
  local previous_release_id=$(curl --user ${GITHUB_USER}:${GITHUB_SUPER_TOKEN} --request GET --silent https://api.github.com/repos/${GITHUB_USER}/${L_REPO}/releases/tags/${TAG} | jq '.id')

  # Delete the previous release (if present)
  if [[ -n "$previous_release_id" ]]; then
    echo "Deleting previous release: ${previous_release_id}"
    curl \
      --user ${GITHUB_USER}:${GITHUB_SUPER_TOKEN} \
      --request DELETE \
      --silent \
      https://api.github.com/repos/${GITHUB_USER}/${L_REPO}/releases/${previous_release_id}
  fi

  # Delete previous identical tags, if present
  git tag -d $TAG || true
  git push origin :$TAG || true

  # Add all the changed files and push the changes upstream
  git add -f .
  git commit -m "${L_COMMIT_MSG}" || true
  git push -f origin ${L_BRANCH}:${L_BRANCH}
  git tag $TAG
  git push origin $TAG

# evermind -- we don't want any release entries there  # Generate a skeleton release on GitHub
# evermind -- we don't want any release entries there  curl \
# evermind -- we don't want any release entries there    --user ${GITHUB_USER}:${GITHUB_SUPER_TOKEN} \
# evermind -- we don't want any release entries there    --request POST \
# evermind -- we don't want any release entries there    --silent \
# evermind -- we don't want any release entries there    --data @- \
# evermind -- we don't want any release entries there    https://api.github.com/repos/${GITHUB_USER}/${L_REPO}/releases <<END
# evermind -- we don't want any release entries there  {
# evermind -- we don't want any release entries there    "tag_name": "$TAG",
# evermind -- we don't want any release entries there    "name": "Auto-generated release for tag $TAG",
# evermind -- we don't want any release entries there    "body": "$RELEASE_BODY",
# evermind -- we don't want any release entries there    "draft": false,
# evermind -- we don't want any release entries there    "prerelease": $PRERELEASE
# evermind -- we don't want any release entries there  }
# evermind -- we don't want any release entries thereEND
  popd
}

addAlternative() {
        local L_ALTERNATIVE_FLAVOR="$1"
        local L_URL_ALTERNATIVE_LEGACY="$2"
        if [ "a${L_URL_ALTERNATIVE_LEGACY}b" != "ab" ] ; then
            jq '.flavors.github.stable.alternative_apks += [{
	            "alternative": "'${L_ALTERNATIVE_FLAVOR}'",
	            "url": "'${L_URL_ALTERNATIVE_LEGACY}'"
                    }]'
        else
            jq
        fi
}

updateJsonFile() {
    local L_URL_STABLE="$1"
    local L_URL_ALTERNATIVE="$2"
    local L_URL_ALTERNATIVE_LEGACY="$3"
    cat $JSON_FILE \
        | jq '.flavors.github.stable.version_code = '${VERSION_CODE}'' \
        | jq '.flavors.github.stable.version = "'${VERSION_NAME}'"' \
        | jq '.flavors.github.stable.apk = "'${L_URL_STABLE}'"' \
        | jq '( .flavors.github.stable.alternative_apks[] | select(.alternative == "conscrypt") ).url |= "'${L_URL_ALTERNATIVE}'"' \
        | addAlternative "braveLegacy" "${L_URL_ALTERNATIVE_LEGACY}"
}
create_json_file_and_create_tagged_release() {
    local L_BRANCH="$1"
    local L_URL_STABLE="$2"
    local L_URL_ALTERNATIVE="$3"
    # checkout json release file repo
    rm -rf "/tmp/${BNP_R_MGR_REPO}"
    git clone --branch "${L_BRANCH}" "https://${GITHUB_USER}:${GITHUB_SUPER_TOKEN}@github.com/${GITHUB_USER}/${BNP_R_MGR_REPO}.git" /tmp/${BNP_R_MGR_REPO}
    # update version{code,name} and download url
    updateJsonFile "$L_URL_STABLE" "$L_URL_ALTERNATIVE" "$L_URL_ALTERNATIVE_LEGACY" > "$TEMPFILE"
    mv $TEMPFILE $JSON_FILE

    create_tagged_release "$BNP_R_MGR_REPO" "$L_BRANCH" "\"version\": \"$VERSION_NAME\""
}

detect_build_tools_version() {
    ls /usr/local/lib/android/sdk/build-tools/ | tail -n 1
}

BUILD_TOOLS_VERSION="${BUILD_TOOLS_VERSION:-$(detect_build_tools_version)}"

AAPT=$ANDROID_HOME/build-tools/$BUILD_TOOLS_VERSION/aapt

URL_PREFIX="https://github.com/${GITHUB_USER}/${RELEASE_REPO}/releases/download/${TAG}"
URL="$URL_PREFIX/BraveNewPipe_${TAG}.apk"
URL_CONSCRYPT="$URL_PREFIX/BraveNewPipe_conscrypt_${TAG}.apk"
URL_LEGACY="$URL_PREFIX/BraveNewPipe_legacy_${TAG}.apk"
VERSION_NAME=${TAG/v/}
VERSION_CODE="$($AAPT d badging $APK_FILE | grep -Po "(?<=\sversionCode=')([0-9.-]+)")"

TEMPFILE="$(mktemp  -p /tmp -t sdflhXXXXXXXXX)"
JSON_FILE=/tmp/${BNP_R_MGR_REPO}/api/data.json

# We have two different json files for now:
# The first is used within the flavors brave and braveConscrypt
# and the second is used in braveLegacy. The json files
# are stored in the same repo but in different branches.
# We call kitkat stuff first as each call tags and delete same exising
# tags before and we want the master branch to have the actual tag.
create_json_file_and_create_tagged_release "kitkat" "$URL_LEGACY" "$URL_LEGACY"
create_json_file_and_create_tagged_release "master" "$URL" "$URL_CONSCRYPT" "$URL_LEGACY"
