### Downloading the Source ###

This tutorial assumes that you are using newer version of Eclipse that comes with a Git client. To learn how to build DIYLC, go to this page.

##### Step 1 #####

In "Package Explorer" right click and select "Import" from the context menu. That will bring up the "Import" dialog with many possible sources for projects. 

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_1.png' /></p>

##### Step 2 #####

Select "Git" folder and then "Projects from Git" and click "Next >"

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_2.png' /></p>

##### Step 3 #####

Then select "Clone URI" and click "Next >"

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_3.png' /></p>

##### Step 4 #####

Paste the address (excluding the quotes) into the "URI" field "https://github.com/bancika/diy-layout-creator.git" and type in your GitHub username and password. Click "Next >"

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_4.png' /></p>

##### Step 5 #####

The next screen will show all available branches. The latest version of the code is always on "master" branch, so make sure that it's selected. The others are optional. Click "Next >"

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_5.png' /></p>

##### Step 6 #####

Chose local desctination directory where Git will copy all the source code. In most cases, the default directory is fine. Click "Next >"

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_6.png' /></p>

##### Step 7 #####

It will take a few moments for Git to pull all the source from the remote repository and create a local repository and on the next screen just leave "Import existing projects" selected and click "Next >"

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_7.png' /></p>

##### Step 8 #####

On "Import Project" screen you will see all projects available in the repository. **diylc-server-api** is the PHP/MySql back end project and is not needed in most cases, so we can leave it out. Click "Finish" to add the remaining three projects to the Eclipse workspace.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_8.png' /></p>

##### Step 9 #####

Voila, you're done! Your "Project Explorer" should show the three projects we just downloaded from the repository.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/eclipse_import_9.png' /></p>

##### Step 10: run diylc-swing #####

Now we can run the application. Find "DIYLCStarter.launch" file in diylc-swing project. Right click and select "Run As" -> "DIYLCStarter".

If everything went well, the application should start.
