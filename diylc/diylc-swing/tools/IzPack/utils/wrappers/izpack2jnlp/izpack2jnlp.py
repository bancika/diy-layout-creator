#!/usr/bin/env python
# ........................................................................... #
#
# IzPack - Copyright 2008 Julien Ponge, All Rights Reserved.
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

JNLP_TEMPLATE = """
<?xml version="1.0" encoding="utf-8"?>
<jnlp
  spec="1.6+"
  codebase="$codebase"
  href="$jnlp">
  <information>
    <title>$title</title>
    <vendor>$vendor</vendor>
    <homepage href="$homepage"/>
    <description>$description</description>
    <offline-allowed/>
  </information>
  <security>
      <all-permissions/>
  </security>
  <resources>
    <j2se version="1.6+"/>
    <jar href="$installer"/>
  </resources>
  <application-desc main-class="com.izforge.izpack.installer.bootstrap.Installer"/>
</jnlp>
"""

def parse_options():
    from optparse import OptionParser
    parser = OptionParser()

    parser.add_option("--codebase",
                      action="store",
                      dest="codebase",
                      help="URL to the downloads")
    parser.add_option("--jnlp",
                      action="store",
                      dest="jnlp",
                      help="Name of the JNLP file, relative to --codebase")
    parser.add_option("--title",
                      action="store",
                      dest="title",
                      help="Title of the application to install")
    parser.add_option("--vendor",
                      action="store",
                      dest="vendor",
                      help="Name of the application vendor")
    parser.add_option("--homepage",
                      action="store",
                      dest="homepage",
                      help="URL to the application homepage")
    parser.add_option("--description",
                      action="store",
                      dest="description",
                      help="A short description of the application")
    parser.add_option("--installer",
                      action="store",
                      dest="installer",
                      help="Name of the installer JAR file, relative to --codebase")

    (options, args) = parser.parse_args()

    to_check = (options.codebase,
                options.jnlp,
                options.title,
                options.vendor,
                options.homepage,
                options.description,
                options.installer)
    for item in to_check:
        if item is None:
            parser.error("Some arguments were missing, please run again with --help")

    return options

def main():
    from string import Template
    opts = parse_options()
    jnlp = Template(JNLP_TEMPLATE)
    jnlp_file = open(opts.jnlp, "w")
    jnlp_file.write(jnlp.substitute(codebase=opts.codebase,
                                    jnlp=opts.jnlp,
                                    title=opts.title,
                                    vendor=opts.vendor,
                                    homepage=opts.homepage,
                                    description=opts.description,
                                    installer=opts.installer))
    jnlp_file.close()

if __name__ == "__main__":
    main()