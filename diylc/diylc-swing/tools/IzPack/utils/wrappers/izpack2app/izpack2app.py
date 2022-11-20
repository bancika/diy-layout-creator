#!/usr/bin/env python
# ........................................................................... #
#
# IzPack - 2007, 2008 Julien Ponge, All Rights Reserved.
#
# http://izpack.org/
# http://izpack.codehaus.org/
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ........................................................................... #

import os
import sys
from shutil import *

def main():
	base = os.path.dirname(sys.argv[0])
	jar = sys.argv[1]
	jar_name = os.path.basename(jar)
	app = sys.argv[2]
	
	if os.path.exists(app): rmtree(app)
	copytree(os.path.join(base, 'Mac-App-Template'), app)
	java_folder = os.path.join(app, 'Contents/Resources/Java/')
	if not os.path.exists(java_folder): os.mkdir(java_folder)
	copy(jar, java_folder)

	def reducer(str, line):
		return str + line

	plist_path = os.path.join(app, 'Contents/Info.plist')
	plist = open(plist_path, 'r')
	plist_content = reduce(reducer, plist.readlines(), '').replace('__JAR__', jar_name)
	plist.close()
	plist = open(plist_path, 'w')
	plist.write(plist_content)
	plist.close()
	
if __name__ == '__main__':
	if (len(sys.argv) != 3):
		print "Usage: izpack2app.py installer.jar Installer.app"
	else:
		main()
