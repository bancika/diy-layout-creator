#!/bin/bash

export JAVA_HOME=$SNAP/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/jre/bin:$PATH

#Start in user configuration directory 
cd $SNAP_USER_DATA

#Create any missing user configuration directories

#themes
if [ ! -d $SNAP_USER_DATA/themes ]
then
	mkdir $SNAP_USER_DATA/themes
fi

#copy a mising update.xml file
if [ ! -f $SNAP_USER_DATA/update.xml ]
then
	cp $SNAP/update.xml $SNAP_USER_DATA/update.xml
fi

#copy themes
if [ ! -f $SNAP_USER_DATA/themes/'Blueprint.xml' ]
then
	cp $SNAP/themes/'Blueprint.xml' $SNAP_USER_DATA/themes/'Blueprint.xml'
fi

if [ ! -f $SNAP_USER_DATA/themes/'Dark.xml' ]
then
	cp $SNAP/themes/'Dark.xml' $SNAP_USER_DATA/themes/'Dark.xml'
fi

if [ ! -f $SNAP_USER_DATA/themes/'Light.xml' ]
then
	cp $SNAP/themes/'Light.xml' $SNAP_USER_DATA/themes/'Light.xml'
fi

cd $SNAP

echo =============================================
echo SNAP
echo $SNAP
echo =============================================
echo SNAP_USER_DATA
echo $SNAP_USER_DATA
echo =============================================

java -Xms512m -Xmx2048m -Dorg.diylc.scriptRun=true -Duser.dir=$SNAP_USER_DATA -Dfile.encoding=UTF-8 -cp $SNAP/diylc.jar:lib --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens java.desktop/java.awt.geom=ALL-UNNAMED org.diylc.DIYLCStarter
