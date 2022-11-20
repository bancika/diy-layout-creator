#!/usr/bin/env python
# ........................................................................... #
#
# IzPack - Copyright 2007, 2010 Julien Ponge, All Rights Reserved.
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
import subprocess
import shutil
import optparse

def parse_options():
    parser = optparse.OptionParser()
    parser.add_option("--file", action="append", dest="file",
                      help="The installer JAR file / files")
    parser.add_option("--output", action="store", dest="output",
                      default="setup.exe",
                      help="The executable file")
    parser.add_option("--with-jdk", action="store", dest="with_jre", default="",
      help="The bundled JRE to run the exe independently of the system resources. ")  # choosen JDK that may came with the package
    parser.add_option("--with-7z", action="store", dest="p7z",
                      default="7za",
                      help="Path to the 7-Zip executable")
    parser.add_option("--with-upx", action="store", dest="upx",
                      default="upx",
                      help="Path to the UPX executable")
    parser.add_option("--no-upx", action="store_true", dest="no_upx",
                      default=False,
                      help="Do not use UPX to further compress the output")
    parser.add_option("--launch-file", action="store", dest="launch",
                      default="",
                      help="File to launch after extract")
    parser.add_option("--launch-args", action="store", dest="launchargs",
                      default="",
                      help="Arguments for file to launch after extract")
    parser.add_option("--name", action="store", dest="name",
                      default="IzPack",
                      help="Name of package for title bar and prompts")
    parser.add_option("--prompt", action="store_true", dest="prompt",
                      default=False,
                      help="Prompt the user before extraction?")
    (options, args) = parser.parse_args()
    if (options.file is None):
        parser.error("no installer file has been given")
    return options

def create_exe(settings):
    if len(settings.file) > 0:
        filename = os.path.basename(settings.file[0])
    else:
        filename = ''
    
    if len(settings.with_jre) > 0:
        jdk = os.path.basename(settings.with_jre) #inside the jdk/jre that was given, there must be a bin/java.exe file
        jdk = jdk + "\\bin\\javaw.exe"
        print(jdk)
        settings.file.append(settings.with_jre) #jdk/jre is going in the package
    else:
        jdk = 'javaw' #java is added somehow to the PATH
    
    if settings.p7z == '7za':
        p7z = os.path.join(os.path.dirname(sys.argv[0]), '7za')
    else:
        p7z = settings.p7z
    
    use_shell = sys.platform != 'win32'
    
    if (os.access('installer.7z', os.F_OK)):
        os.remove('installer.7z')
    files = '" "'.join(settings.file)
    p7zcmd = '"%s" a -mmt -t7z -mx=9 installer.7z "%s"' % (p7z, files)
    subprocess.call(p7zcmd, shell=use_shell)
    
    config = open('config.txt', 'w')
    config.write(';!@Install@!UTF-8!\n')
    config.write('Title="%s"\n' % settings.name)
    if settings.prompt:
        config.write('BeginPrompt="Install %s?"\n' % settings.name)
    config.write('Progress="yes"\n')
    
    if settings.launch == '':
        config.write('ExecuteFile="' + jdk +'"\n') # who is going to run my installer.jar?
        config.write('ExecuteParameters="-jar \\\"%s\\\"' % filename)
        if settings.launchargs != '':
            config.write(' %s"\n' % settings.launchargs)
        else:
            config.write('"\n') 
    else:
        config.write('ExecuteFile="%s"\n' % settings.launch)
        if settings.launchargs != '':
            config.write('ExecuteParameters="%s"\n' % settings.launchargs)

    config.write(';!@InstallEnd@!\n')
    config.close()
    
    sfx = os.path.join(os.path.dirname(p7z), '7zS.sfx')
    files = [sfx, 'config.txt', 'installer.7z']
    
    output = open(settings.output, 'wb')
    for f in files:
        in_file = open(f, 'rb')
        shutil.copyfileobj(in_file, output, 2048)
        in_file.close()
    output.close()
    
    if (not settings.no_upx):
        if settings.upx == 'upx':
            upx = os.path.join(os.path.dirname(sys.argv[0]), 'upx')
        else:
            upx = settings.upx
        upx = '"%s" --ultra-brute "%s"' % (upx, settings.output)
        subprocess.call(upx, shell=use_shell)
    
    os.remove('config.txt')
    os.remove('installer.7z')

def main():
    create_exe(parse_options())

if __name__ == "__main__":
    main()
