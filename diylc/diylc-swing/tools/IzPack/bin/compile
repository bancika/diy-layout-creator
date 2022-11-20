#!/bin/sh

#   Copyright 2001-2005 The Apache Software Foundation
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

# allow command line pre-set opts
if [ -z "$IZPACK_OPTS" ] ; then 
  IZPACK_OPTS=""
fi

# load user izpack configuration (may specify IZPACK_HOME
if [ -f "$HOME/.izpackrc" ] ; then
  . $HOME/.izpackrc
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
             echo "Using Java version: $JAVA_VERSION"
           fi
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
           fi
           ;;
esac

if [ -z "$IZPACK_HOME" ] ; then
  # try to find IZPACK
  if [ -d /opt/izpack ] ; then 
    IZPACK_HOME=/opt/izpack
  fi

  if [ -d "${HOME}/izpack" ] ; then 
    IZPACK_HOME="${HOME}/izpack"
  fi

  ## resolve links - $0 may be a link to izpack's home
  PRG="$0"
  progname=`basename "$0"`
  saveddir=`pwd`

  # need this for relative symlinks
  dirname_prg=`dirname "$PRG"`
  cd "$dirname_prg"

  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
	PRG="$link"
    else
	PRG=`dirname "$PRG"`"/$link"
    fi
  done

  IZPACK_HOME=`dirname "$PRG"`/..

  cd "$saveddir"

  # make it fully qualified
  IZPACK_HOME=`cd "$IZPACK_HOME" && pwd`
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$IZPACK_HOME" ] &&
    IZPACK_HOME=`cygpath --unix "$IZPACK_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

if [ -z "$JAVACMD" ] ; then 
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then 
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then 
        JAVACMD=java
    fi
  fi
fi
 
if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
  echo "  If build fails because sun.* classes could not be found"
  echo "  you will need to set the JAVA_HOME environment variable"
  echo "  to the installation directory of java."
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$IZPACK_HOME" ] &&
    IZPACK_HOME=`cygpath --windows "$IZPACK_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# For Darwin, use classes.jar for TOOLS_JAR
TOOLS_JAR="${JAVA_HOME}/lib/tools.jar"
if $darwin; then
  TOOLS_JAR="/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Classes/classes.jar"
fi

MAIN_CLASS=com.izforge.izpack.compiler.bootstrap.CompilerLauncher

"$JAVACMD" -Xmx512m \
  $IZPACK_OPTS \
  -classpath "${IZPACK_HOME}/lib/*" \
  "-Dtools.jar=$TOOLS_JAR" \
  "-Dizpack.home=${IZPACK_HOME}" \
  $MAIN_CLASS "$@"
