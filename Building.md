# Prerequisites

DIY Layout Creator 5 has updated the build system, so to build and run it you will need Maven and a JDK.

Your IDE may already bundle the tools required to work on DIYLC, so try opening via the pom.xml file in the *diylc* directory.
A popular Java IDE you may wish to use is IntelliJ IDEA by JetBrains.

Otherwise, you may be able to get everything you need to build and run from your package manager, e.g. on Debian systems you can:

`sudo apt install default-jre default-jdk maven`

## Windows 

Windows setup can be a bit more complicated. If you use a package manager like Scoop, Chocolatey or Winget you may be able to get the tooling from there. Otherwise you can install a JDK and Maven manually.

A popular JDK you can use is Adoptium's OpenJDK: <https://adoptium.net/>

[Maven](https://maven.apache.org/download.cgi) does not provide an installer and has to be added to the path, which can be quite involved. 
There is a guide here: <https://www.geeksforgeeks.org/installation-guide/how-to-install-apache-maven-on-windows/> 

# Building

Once everything is installed, you can build DIYLC. All of the main application source code is inside the *diylc* directory, so move inside it and tell Maven to build the project. Unlike before, all of the sub-projects can be built from the one command:

`mvn package`

If the build went fine, you should see something like this in the console. There may be a few warnings, but it is okay to ignore them.

![](images/build_success.png)

This will create the **diylc.jar** file under the *diylc-swing/target*. You can run this directly from that directory using `java -jar diylc.jar`, although you may get complaints that you did not use the proper startup exectuable/script, which can generally be ignored for development purposes. 
During development you can save build time by moving into the subproject you are editing (e.g. diylc-library) and issuing `mvn package` there, which will only rebuild that subproject.

There is a script that will automatically build the packaged versions for all platforms into a *deploy* directory, as it is a Bash script it will only work in *nix environments (e.g. WSL), and the success of each profile depends on whether you have the required build tooling for it (e.g. AppImageTool) already installed.

`./build-all-profiles.sh`
