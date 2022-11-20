#!/bin/bash
##################################################################################
#                                                                                #
# universalJavaApplicationStub                                                   #
#                                                                                #
#                                                                                #
# A shellscript JavaApplicationStub for Java Apps on Mac OS X                    #
# that works with both Apple's and Oracle's plist format.                        #
#                                                                                #
# Inspired by Ian Roberts stackoverflow answer                                   #
# at http://stackoverflow.com/a/17546508/1128689                                 #
#                                                                                #
#                                                                                #
# @author    Tobias Fischer                                                      #
# @url       https://github.com/tofi86/universalJavaApplicationStub              #
# @date      2017-11-27                                                          #
# @version   2.0.2                                                               #
#                                                                                #
#                                                                                #
##################################################################################
#                                                                                #
#                                                                                #
# The MIT License (MIT)                                                          #
#                                                                                #
# Copyright (c) 2016 Tobias Fischer                                              #
#                                                                                #
# Permission is hereby granted, free of charge, to any person obtaining a copy   #
# of this software and associated documentation files (the "Software"), to deal  #
# in the Software without restriction, including without limitation the rights   #
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell      #
# copies of the Software, and to permit persons to whom the Software is          #
# furnished to do so, subject to the following conditions:                       #
#                                                                                #
# The above copyright notice and this permission notice shall be included in all #
# copies or substantial portions of the Software.                                #
#                                                                                #
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR     #
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,       #
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE    #
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER         #
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  #
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  #
# SOFTWARE.                                                                      #
#                                                                                #
##################################################################################



#
# resolve symlinks
############################################

PRG=$0

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done

# set the directory abspath of the current shell script
PROGDIR=`dirname "$PRG"`



#
# set files and folders
############################################

# the absolute path of the app package
cd "$PROGDIR"/../../
AppPackageFolder=`pwd`

# the base path of the app package
cd ..
AppPackageRoot=`pwd`

# set Apple's Java folder
AppleJavaFolder="${AppPackageFolder}"/Contents/Resources/Java

# set Apple's Resources folder
AppleResourcesFolder="${AppPackageFolder}"/Contents/Resources

# set Oracle's Java folder
OracleJavaFolder="${AppPackageFolder}"/Contents/Java

# set Oracle's Resources folder
OracleResourcesFolder="${AppPackageFolder}"/Contents/Resources

# set path to Info.plist in bundle
InfoPlistFile="${AppPackageFolder}"/Contents/Info.plist

# set the default JVM Version to a null string
JVMVersion=""



#
# read Info.plist and extract JVM options
############################################

# read the program name from CFBundleName
CFBundleName=`/usr/libexec/PlistBuddy -c "print :CFBundleName" "${InfoPlistFile}"`

# read the icon file name
CFBundleIconFile=`/usr/libexec/PlistBuddy -c "print :CFBundleIconFile" "${InfoPlistFile}"`


# check Info.plist for Apple style Java keys -> if key :Java is present, parse in apple mode
/usr/libexec/PlistBuddy -c "print :Java" "${InfoPlistFile}" > /dev/null 2>&1
exitcode=$?
JavaKey=":Java"

# if no :Java key is present, check Info.plist for universalJavaApplication style JavaX keys -> if key :JavaX is present, parse in apple mode
if [ $exitcode -ne 0 ]; then
	/usr/libexec/PlistBuddy -c "print :JavaX" "${InfoPlistFile}" > /dev/null 2>&1
	exitcode=$?
	JavaKey=":JavaX"
fi


# read Info.plist in Apple style if exit code returns 0 (true, :Java key is present)
if [ $exitcode -eq 0 ]; then

	# set Java and Resources folder
	JavaFolder="${AppleJavaFolder}"
	ResourcesFolder="${AppleResourcesFolder}"

	APP_PACKAGE="${AppPackageFolder}"
	JAVAROOT="${AppleJavaFolder}"
	USER_HOME="$HOME"


	# read the Java WorkingDirectory
	JVMWorkDir=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:WorkingDirectory" "${InfoPlistFile}" 2> /dev/null | xargs`

	# set Working Directory based upon Plist info
	if [[ ! -z ${JVMWorkDir} ]]; then
		WorkingDirectory="${JVMWorkDir}"
	else
		# AppPackageRoot is the standard WorkingDirectory when the script is started
		WorkingDirectory="${AppPackageRoot}"
	fi

	# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
	WorkingDirectory=`eval "echo ${WorkingDirectory}"`


	# read the MainClass name
	JVMMainClass=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:MainClass" "${InfoPlistFile}" 2> /dev/null`

	# read the SplashFile name
	JVMSplashFile=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:SplashFile" "${InfoPlistFile}" 2> /dev/null`

	# read the JVM Options
	JVMOptions=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:Properties" "${InfoPlistFile}" 2> /dev/null | grep " =" | sed 's/^ */-D/g' | tr '\n' ' ' | sed 's/  */ /g' | sed 's/ = /=/g' | xargs`
	# replace occurences of $APP_ROOT with its content
	JVMOptions=`eval "echo ${JVMOptions}"`

	# read StartOnMainThread
	JVMStartOnMainThread=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:StartOnMainThread" "${InfoPlistFile}" 2> /dev/null`
	if [ "${JVMStartOnMainThread}" == "true" ]; then
		JVMOptions+=" -XstartOnFirstThread"
	fi

	# read the ClassPath in either Array or String style
	JVMClassPath_RAW=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:ClassPath" "${InfoPlistFile}" 2> /dev/null`
	if [[ $JVMClassPath_RAW == *Array* ]] ; then
		JVMClassPath=.`/usr/libexec/PlistBuddy -c "print ${JavaKey}:ClassPath" "${InfoPlistFile}" 2> /dev/null | grep "    " | sed 's/^ */:/g' | tr -d '\n' | xargs`
	else
		JVMClassPath=${JVMClassPath_RAW}
	fi
	# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
	JVMClassPath=`eval "echo ${JVMClassPath}"`

	# read the JVM Default Options in either Array or String style
	JVMDefaultOptions_RAW=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:VMOptions" "${InfoPlistFile}" 2> /dev/null | xargs`
	if [[ $JVMDefaultOptions_RAW == *Array* ]] ; then
		JVMDefaultOptions=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:VMOptions" "${InfoPlistFile}" 2> /dev/null | grep "    " | sed 's/^ */ /g' | tr -d '\n' | xargs`
	else
		JVMDefaultOptions=${JVMDefaultOptions_RAW}
	fi

	# read the JVM Arguments
	JVMArguments=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:Arguments" "${InfoPlistFile}" 2> /dev/null | xargs`
	# replace occurences of $APP_ROOT with its content
	JVMArguments=`eval "echo ${JVMArguments}"`

	# read the Java version we want to find
	JVMVersion=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:JVMVersion" "${InfoPlistFile}" 2> /dev/null | xargs`

# read Info.plist in Oracle style
else

	# set Working Directory and Java and Resources folder
	JavaFolder="${OracleJavaFolder}"
	ResourcesFolder="${OracleResourcesFolder}"
	WorkingDirectory="${OracleJavaFolder}"

	APP_ROOT="${AppPackageFolder}"

	# read the MainClass name
	JVMMainClass=`/usr/libexec/PlistBuddy -c "print :JVMMainClassName" "${InfoPlistFile}" 2> /dev/null`

	# read the SplashFile name
	JVMSplashFile=`/usr/libexec/PlistBuddy -c "print :JVMSplashFile" "${InfoPlistFile}" 2> /dev/null`

	# read the JVM Options
	JVMOptions=`/usr/libexec/PlistBuddy -c "print :JVMOptions" "${InfoPlistFile}" 2> /dev/null | grep " -" | tr -d '\n' | sed 's/  */ /g' | xargs`
	# replace occurences of $APP_ROOT with its content
	JVMOptions=`eval "echo ${JVMOptions}"`

	# read the ClassPath in either Array or String style
	JVMClassPath_RAW=`/usr/libexec/PlistBuddy -c "print JVMClassPath" "${InfoPlistFile}" 2> /dev/null`
	if [[ $JVMClassPath_RAW == *Array* ]] ; then
		JVMClassPath=.`/usr/libexec/PlistBuddy -c "print JVMClassPath" "${InfoPlistFile}" 2> /dev/null | grep "    " | sed 's/^ */:/g' | tr -d '\n' | xargs`
		# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
		JVMClassPath=`eval "echo ${JVMClassPath}"`

	elif [[ ! -z ${JVMClassPath_RAW} ]] ; then
		JVMClassPath=${JVMClassPath_RAW}
		# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
		JVMClassPath=`eval "echo ${JVMClassPath}"`

	else
		#default: fallback to OracleJavaFolder
		JVMClassPath="${JavaFolder}/*"
		# Do NOT expand the default App.app/Contents/Java/* classpath (#42)
	fi

	# read the JVM Default Options
	JVMDefaultOptions=`/usr/libexec/PlistBuddy -c "print :JVMDefaultOptions" "${InfoPlistFile}" 2> /dev/null | grep -o " \-.*" | tr -d '\n' | xargs`

	# read the JVM Arguments
	JVMArguments=`/usr/libexec/PlistBuddy -c "print :JVMArguments" "${InfoPlistFile}" 2> /dev/null | tr -d '\n' | sed -E 's/Array \{ *(.*) *\}/\1/g' | sed 's/  */ /g' | xargs`
	# replace occurences of $APP_ROOT with its content
	JVMArguments=`eval "echo ${JVMArguments}"`
fi



#
# internationalized messages
#
############################################

LANG=`defaults read -g AppleLocale`

# French localization
if [[ $LANG == fr* ]] ; then
	MSG_ERROR_LAUNCHING="Erreur au lancement de '${CFBundleName}'."
	MSG_MISSING_MAINCLASS="'MainClass' n'est pas spécifié.\nL'application Java ne peut pas être lancée."
	MSG_NO_SUITABLE_JAVA="La version de Java installée sur votre système ne convient pas.\nCe programme nécessite Java"
	MSG_JAVA_VERSION_OR_LATER="ou ultérieur"
	MSG_JAVA_VERSION_LATEST="(dernière mise à jour)"
	MSG_NO_SUITABLE_JAVA_CHECK="Merci de bien vouloir installer la version de Java requise."
	MSG_INSTALL_JAVA="Java doit être installé sur votre système.\nRendez-vous sur java.com et suivez les instructions d'installation..."
	MSG_LATER="Plus tard"
	MSG_VISIT_JAVA_DOT_COM="Visiter java.com"

# German localization
elif [[ $LANG == de* ]] ; then
	MSG_ERROR_LAUNCHING="FEHLER beim Starten von '${CFBundleName}'."
	MSG_MISSING_MAINCLASS="Die 'MainClass' ist nicht spezifiziert!\nDie Java-Anwendung kann nicht gestartet werden!"
	MSG_NO_SUITABLE_JAVA="Es wurde keine passende Java-Version auf Ihrem System gefunden!\nDieses Programm benötigt Java"
	MSG_JAVA_VERSION_OR_LATER="oder neuer"
	MSG_JAVA_VERSION_LATEST="(neuste Unterversion)"
	MSG_NO_SUITABLE_JAVA_CHECK="Stellen Sie sicher, dass die angeforderte Java-Version installiert ist."
	MSG_INSTALL_JAVA="Auf Ihrem System muss die 'Java'-Software installiert sein.\nBesuchen Sie java.com für weitere Installationshinweise."
	MSG_LATER="Später"
	MSG_VISIT_JAVA_DOT_COM="java.com öffnen"

# English default localization
else
	MSG_ERROR_LAUNCHING="ERROR launching '${CFBundleName}'."
	MSG_MISSING_MAINCLASS="'MainClass' isn't specified!\nJava application cannot be started!"
	MSG_NO_SUITABLE_JAVA="No suitable Java version found on your system!\nThis program requires Java"
	MSG_JAVA_VERSION_OR_LATER="or later"
	MSG_JAVA_VERSION_LATEST="(latest update)"
	MSG_NO_SUITABLE_JAVA_CHECK="Make sure you install the required Java version."
	MSG_INSTALL_JAVA="You need to have JAVA installed on your Mac!\nVisit java.com for installation instructions..."
	MSG_LATER="Later"
	MSG_VISIT_JAVA_DOT_COM="Visit java.com"
fi



# helper
# function: extract Java version string
#           from java -version command
############################################

function extractJavaVersionString() {
  echo `"$1" -version 2>&1 | awk '/version/{print $NF}' | sed -E 's/"//g'`
}

# helper
# function: extract Java major version
#           from java version string
############################################

function extractJavaMajorVersion() {
  version_str=$(extractJavaVersionString "$1")
  echo ${version_str} | sed -E 's/([0-9.]{3})[0-9_.]{5,6}/\1/g'
}

# helper
# function: generate comparable Java version
#           number from java version string
############################################

function comparableJavaVersionNumber() {
  echo $1 | sed -E 's/[[:punct:]]//g'
}



#
# function: Java version tester
#           check whether a given java version
#           satisfies the given requirement
############################################

function JavaVersionSatisfiesRequirement() {
  java_ver=$1
  java_req=$2

  # e.g. 1.8*
  if [[ ${java_req} =~ ^[0-9]\.[0-9]\*$ ]] ; then
    java_req_num=${java_req:0:3}
    java_ver_num=${java_ver:0:3}
    if [ ${java_ver_num} == ${java_req_num} ] ; then
      return 0
    else
      return 1
    fi

  # e.g. 1.8+
  elif [[ ${java_req} =~ ^[0-9]\.[0-9]\+$ ]] ; then
    java_req_num=$(comparableJavaVersionNumber ${java_req})
    java_ver_num=$(comparableJavaVersionNumber ${java_ver})
    if [ ${java_ver_num} -ge ${java_req_num} ] ; then
      return 0
    else
      return 1
    fi

  # e.g. 1.8
  elif [[ ${java_req} =~ ^[0-9]\.[0-9]$ ]] ; then
    if [ ${java_ver} == ${java_req} ] ; then
      return 0
    else
      return 1
    fi

  # not matching any of the above patterns
  else
    return 2
  fi
}



#
# find installed Java versions
############################################

apple_jre_plugin="/Library/Java/Home/bin/java"
apple_jre_version=`extractJavaMajorVersion "${apple_jre_plugin}"`
oracle_jre_plugin="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
oracle_jre_version=`extractJavaMajorVersion "${oracle_jre_plugin}"`

# first check system variable "$JAVA_HOME"
if [ -n "$JAVA_HOME" ] ; then

    # PR 26: Allow specifying "$JAVA_HOME" relative to "$AppPackageFolder"
    # which allows for bundling a custom version of Java inside your app!
    if [[ $JAVA_HOME == /* ]] ; then
      # if "$JAVA_HOME" starts with a Slash it's an absolute path
	    JAVACMD="$JAVA_HOME/bin/java"
    else
      # otherwise it's a relative path to "$AppPackageFolder"
      JAVACMD="$AppPackageFolder/$JAVA_HOME/bin/java"
    fi

# check for a specific Java version, specified in JVMversion Plist key
elif [ ! -z ${JVMVersion} ] ; then

	# first check "/usr/libexec/java_home" symlinks
	if [ -x /usr/libexec/java_home ] && /usr/libexec/java_home -F -v ${JVMVersion} > /dev/null 2>&1 ; then
		JAVACMD="`/usr/libexec/java_home -F -v ${JVMVersion} 2> /dev/null`/bin/java"
		JAVACMD_version=$(comparableJavaVersionNumber $(extractJavaVersionString "${JAVACMD}"))
	fi

	# then additionally check the Oracle JRE plugin whether it's a higher/newer compatible version
	if [ -x "${oracle_jre_plugin}" ] && JavaVersionSatisfiesRequirement ${oracle_jre_version} ${JVMVersion} ; then
		this_java_ver=$(comparableJavaVersionNumber $(extractJavaVersionString "${oracle_jre_plugin}"))
		# use this compatible version only if the above returned empty or if the version number is higher
		if [ -z ${JAVACMD} ] || [ ${this_java_ver} -ge ${JAVACMD_version} ] ; then
			JAVACMD="${oracle_jre_plugin}"
			JAVACMD_version=${this_java_ver}
		fi
	fi

	# then additionally check the Apple JRE plugin whether it's a higher/newer compatible version
	if [ -x "${apple_jre_plugin}" ] && JavaVersionSatisfiesRequirement ${apple_jre_version} ${JVMVersion} ; then
		this_java_ver=$(comparableJavaVersionNumber $(extractJavaVersionString "${apple_jre_plugin}"))
		# use this compatible version only if the above returned empty or if the version number is higher
		if [ -z ${JAVACMD} ] || [ ${this_java_ver} -ge ${JAVACMD_version} ] ; then
			JAVACMD="${apple_jre_plugin}"
			JAVACMD_version=${this_java_ver}
		fi
	fi

	if [ -z "$JAVACMD" ] ; then
		# display human readable java version (#28)
		java_version_hr=`echo ${JVMVersion} | sed -E 's/[0-9]\.([0-9+*]+)/ \1/g' | sed "s/+/ ${MSG_JAVA_VERSION_OR_LATER}/" | sed "s/*/ ${MSG_JAVA_VERSION_LATEST}/"`
		# display error message with applescript
		osascript -e "tell application \"System Events\" to display dialog \"${MSG_ERROR_LAUNCHING}\n\n${MSG_NO_SUITABLE_JAVA}${java_version_hr}.\n${MSG_NO_SUITABLE_JAVA_CHECK}\" with title \"${CFBundleName}\"  buttons {\" OK \", \"${MSG_VISIT_JAVA_DOT_COM}\"} default button \"${MSG_VISIT_JAVA_DOT_COM}\" with icon path to resource \"${CFBundleIconFile}\" in bundle (path to me)" \
				-e "set response to button returned of the result" \
				-e "if response is \"${MSG_VISIT_JAVA_DOT_COM}\" then open location \"http://java.com\""
    # exit with error
		exit 3
	fi

# otherwise check "/usr/libexec/java_home" and Oracle and Apple JRE paths and use highest version available
else

	# first check "/usr/libexec/java_home" symlinks
	if [ -x /usr/libexec/java_home ] && /usr/libexec/java_home -F > /dev/null 2>&1 ; then
		JAVACMD="`/usr/libexec/java_home 2> /dev/null`/bin/java"
		JAVACMD_version=$(comparableJavaVersionNumber $(extractJavaVersionString "${JAVACMD}"))
	fi

	# then additionally check the Oracle JRE plugin whether it's a higher/newer compatible version
	if [ -x "${oracle_jre_plugin}" ] ; then
		this_java_ver=$(comparableJavaVersionNumber $(extractJavaVersionString "${oracle_jre_plugin}"))
		# use this compatible version only if the above returned empty or if the version number is higher
		if [ -z ${JAVACMD} ] || [ ${this_java_ver} -ge ${JAVACMD_version} ] ; then
			JAVACMD="${oracle_jre_plugin}"
			JAVACMD_version=${this_java_ver}
		fi
	fi

	# then additionally check the Apple JRE plugin whether it's a higher/newer compatible version
	if [ -x "${apple_jre_plugin}" ] ; then
		this_java_ver=$(comparableJavaVersionNumber $(extractJavaVersionString "${apple_jre_plugin}"))
		# use this compatible version only if the above returned empty or if the version number is higher
		if [ -z ${JAVACMD} ] || [ ${this_java_ver} -ge ${JAVACMD_version} ] ; then
			JAVACMD="${apple_jre_plugin}"
			JAVACMD_version=${this_java_ver}
		fi
	fi
fi

# fallback fallback: /usr/bin/java
# but this would prompt to install deprecated Apple Java 6



#
# execute JAVA commandline and do some pre-checks
####################################################

# display error message if MainClassName is empty
if [ -z ${JVMMainClass} ]; then
	# display error message with applescript
	osascript -e "tell application \"System Events\" to display dialog \"${MSG_ERROR_LAUNCHING}\n\n${MSG_MISSING_MAINCLASS}\" with title \"${CFBundleName}\" buttons {\" OK \"} default button 1 with icon path to resource \"${CFBundleIconFile}\" in bundle (path to me)"
	# exit with error
	exit 2


# check whether $JAVACMD is a file and executable
elif [ -f "$JAVACMD" ] && [ -x "$JAVACMD" ] ; then

	# enable drag&drop to the dock icon
	export CFProcessPath="$0"

	# remove Apples ProcessSerialNumber from passthru arguments (#39)
	if [[ $@ == -psn* ]] ; then
		ArgsPassthru=""
	else
		ArgsPassthru=$@
	fi

	# change to Working Directory based upon Apple/Oracle Plist info
	cd "${WorkingDirectory}"

	# execute Java and set
	#	- classpath
	#	- dock icon
	#	- application name
	#	- JVM options
	#	- JVM default options
	#	- main class
	#	- JVM arguments
	exec "$JAVACMD" \
			-cp "${JVMClassPath}" \
			-splash:"${ResourcesFolder}/${JVMSplashFile}" \
			-Xdock:icon="${ResourcesFolder}/${CFBundleIconFile}" \
			-Xdock:name="${CFBundleName}" \
			${JVMOptions:+$JVMOptions }\
			${JVMDefaultOptions:+$JVMDefaultOptions }\
			${JVMMainClass}\
			${JVMArguments:+ $JVMArguments}\
			${ArgsPassthru:+ $ArgsPassthru}


else

	# display error message with applescript
	osascript -e "tell application \"System Events\" to display dialog \"${MSG_ERROR_LAUNCHING}\n\n${MSG_INSTALL_JAVA}\" with title \"${CFBundleName}\" buttons {\"${MSG_LATER}\", \"${MSG_VISIT_JAVA_DOT_COM}\"} default button \"${MSG_VISIT_JAVA_DOT_COM}\" with icon path to resource \"${CFBundleIconFile}\" in bundle (path to me)" \
				-e "set response to button returned of the result" \
				-e "if response is \"${MSG_VISIT_JAVA_DOT_COM}\" then open location \"http://java.com\""

	# exit with error
	exit 1
fi
