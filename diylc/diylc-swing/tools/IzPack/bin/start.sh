#!/usr/bin/env bash

# ShellScript 
# - to detect a desktop and their native
#   File/WebBrowser such as Konqueror (KDE) or Nautilus/Epiphany (Gnome)
# Or 
# - to detect a browser from the mozilla family
# The first prefered one will open the given $1 document
# which should be a web url (http://host.domain.com/path/index.html) or a 
# local file like file:///local/folder/document.html
# Note: Tested on SuSE, Fedora an Mandriva Linux and Solaris 9 with kde, gnome or a mozilla installed
#
# This is licensed as part of
# IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
# 
# http://izpack.org/
# http://izpack.codehaus.org/
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
#     
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Usage:
#
#  ./start.sh http://izpack.org/
#
# author marc.eppelmann&#064;gmx.de
# $Id$
#

function usage() {
  echo "Launch an url with the browser."
  echo ""
  echo "Usage : "
  echo "  ./start.sh http://izpack.org/"
}

function detectDesktop() {
  if [[ "$DISPLAY" = "" ]]; then
    return 1
  fi

  local LC_ALL=C
  local clients
  if ! clients=`xlsclients`; then
    # TODO: should we fall back to using ps?
    return 1
  fi

  if echo "$clients" | grep -qE '(gnome-panel|nautilus|metacity)'; then
    echo gnome
  elif echo "$clients" | grep -qE '(kicker|slicker|karamba|kwin)'; then
    echo kde
  else
    echo other
  fi
  return 1
}

function lookForRunningGecko(){
  if command -v firefox &>/dev/null; then
    tempfile=`mktemp`
  	
    firefox -remote "ping()" 2> $tempfile
    
  	if [ -s $tempfile ]; then
      rm $tempfile
      echo "none" # is not running :-)
      return 1
  	else
  	  rm $tempfile      
  	  echo firefox # is running :-)
      return 0
  	fi    
  fi    # firefox "$1"
  if command -v mozilla &>/dev/null; then
    #if mozilla -remote "ping()" 
    if command mozilla -remote "ping()" &>/dev/null; then
      echo mozilla # is running :-)
      return 0
    fi
  fi
  echo "none"
  
  return 1
}

if [[ $# = 0 ]]
then
  usage
  exit 1
fi

desktop=`detectDesktop` 
gecko=`lookForRunningGecko`

echo "found Desktop: $desktop"
echo "found Gecko:   $gecko"

if [[ "$gecko" = "none" ]]; then # try open a new instance if found:
  if command -v firefox &>/dev/null; then
    firefox "$1" &
  elif command -v mozilla &>/dev/null; then   
    mozilla "$1" &
  elif [[ "$desktop" = "gnome" ]] && command -v gnome-open &>/dev/null; then
    gnome-open "$1" &
  elif [[ "$desktop" = "kde" ]]; then
    kfmclient exec "$1" &
  else
    exit 1
  fi
else 
  echo "Launching: $gecko"
  $gecko -remote "openurl($1)" &
fi


