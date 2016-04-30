#!/bin/bash
set -e

JAVA_URL="https://java.com/download/"

REQUIRED_MAJOR=$1
REQUIRED_MINOR=$2
REQUIRED_BUILD=$3
REQUIRED_UPDATE=$4


echo "---=== REQUIRED JAVA VERSION ===---"
echo
echo "Major version: ${REQUIRED_MAJOR}"
echo "Minor version: ${REQUIRED_MINOR}"
echo "Build version: ${REQUIRED_BUILD}"
echo "Update version: ${REQUIRED_UPDATE}"

echo
echo


commandExists () {
    which "$1" &> /dev/null
}

showWarning() {
  PROMPT="$1"

  echo "$PROMPT"

  if commandExists "zenity"
  then
    zenity --warning --text="$PROMPT" &>/dev/null
  elif commandExists "osascript"
  then
    osascript -e "tell app \"System Events\" to display dialog \"${PROMPT}\" buttons \"OK\" default button 1 with title \"Warning\" with icon caution" &>/dev/null
  fi
}


showJavaWarning() {
    PROMPT="$1"
    showWarning "$PROMPT"

    if commandExists "xdg-open"
    then
        xdg-open "$JAVA_URL"
    elif commandExists "open"
    then
        open "$JAVA_URL"
    fi
}

showCompatibilityWarning() {
  PROMPT="Java"

  if (($REQUIRED_UPDATE > 0))
  then
     PROMPT="$PROMPT ${REQUIRED_MAJOR}.${REQUIRED_MINOR}.${REQUIRED_BUILD}_${REQUIRED_UPDATE}"
  else
     PROMPT="$PROMPT ${REQUIRED_MAJOR}.${REQUIRED_MINOR}.${REQUIRED_BUILD}"
  fi

  PROMPT="$PROMPT or later is required to run this program."

  showJavaWarning "$PROMPT"

  exit 1
}


if ! commandExists "java"
then
  showJavaWarning "Cannot detect Java. Is it installed and in your PATH variable?"
  exit 1
fi




if ! JAVA_VERSION_OUTPUT=$(java -version 2>&1 >/dev/null)
then
  showJavaWarning "Error while detecting Java version. Is it correctly installed?"
  exit 1
fi

if [[ $JAVA_VERSION_OUTPUT =~ ([0-9]+)\.([0-9]+)(\.([0-9]+)(_([0-9]+))?)?  ]]
then
  MAJOR_VERSION=${BASH_REMATCH[1]}
  MINOR_VERSION=${BASH_REMATCH[2]}
  BUILD_VERSION=${BASH_REMATCH[4]}
  if [ -z "$BUILD_VERSION" ]
  then
    BUILD_VERSION="0"
  fi

  UPDATE_VERSION=${BASH_REMATCH[6]}
  if [ -z "$UPDATE_VERSION" ]
  then
    UPDATE_VERSION="0"
  fi

  echo "---=== JAVA VERSION ===---"
  echo
  echo "Major version: $MAJOR_VERSION"
  echo "Minor version: $MINOR_VERSION"
  echo "Build version: $BUILD_VERSION"
  echo "Update version: $UPDATE_VERSION"


  if (($MAJOR_VERSION > $REQUIRED_MAJOR))
  then
    exit 0
  fi

  if (($MAJOR_VERSION < $REQUIRED_MAJOR))
  then
    showCompatibilityWarning
  fi

  if (($MINOR_VERSION > $REQUIRED_MINOR))
  then
    exit 0
  fi

  if (($MINOR_VERSION < $REQUIRED_MINOR))
  then
    showCompatibilityWarning
  fi


  if (($BUILD_VERSION > $REQUIRED_BUILD))
  then
    exit 0
  fi

  if (($BUILD_VERSION < $REQUIRED_BUILD))
  then
    showCompatibilityWarning
  fi


  if (($UPDATE_VERSION > $REQUIRED_UPDATE))
  then
    exit 0
  fi

  if (($UPDATE_VERSION < $REQUIRED_UPDATE))
  then
    showCompatibilityWarning
  fi
else
    showJavaWarning "Error while retrieving Java version. Is Java correctly installed?"
    exit 1
fi
