<h3><a name="News"></a>Introduction<a href="#Introduction" class="section_anchor"></a></h3>
<p align="justify">
DIY Layout Creator (DIYLC) is a specialized software tool designed for hobbyists and electronics enthusiasts to create circuit layouts on stripboard, perfboard, PCB, and point-to-point wiring. It provides an intuitive, drag-and-drop interface for placing components, arranging connections, and visualizing circuit designs before assembly. With a built-in component library and customization options, it allows users to design layouts with precision and clarity. The software is particularly useful for designing, analyzing, and debugging guitar wiring diagrams, helping musicians and builders plan and troubleshoot custom wiring configurations for pickups, switches, and controls. Its versatility makes it an ideal solution for DIY electronics projects, prototyping, and documentation.</p>
<br>
<a href="https://github.com/bancika/diy-layout-creator/releases/latest">Click here</a> to get the latest version for all platforms.</a><br>
<a href="https://github.com/bancika/diy-layout-creator/tree/wiki">Click here</a> to read the user documentation as well as developer documentation.<br>
<br>
<b>Mac users</b>: in most instances, the Gatekeeper will block DIYLC and mark it as damaged because it's a 3rd party app from an independent vendor (me). <a href="[https://support.apple.com/en-us/HT202491](https://support.apple.com/kb/ph18657?locale=en_US)">This document</a> covers how to allow DIYLC on your Mac.

<h3><a name="Samples"></a>Samples<a href="#Samples" class="section_anchor"></a></h3>

<h4>Point-to-point tube amplifier</h4>
<p align="center"><a href="http://diy-fever.com/wordpress/wp-content/gallery/diylc/slo.png" rel="nofollow"><img src="https://diy-fever.com/wordpress/wp-content/gallery/diylc/cache/slo.png-nggid041072-ngg0dyn-450x350-00f0w010c010r110f110r010t010.png"></a></p>
<h4>Guitar wiring</h4>
<p align="center"><a href="http://diy-fever.com/wordpress/wp-content/gallery/diylc/travelcaster.png" rel="nofollow"><img src="https://diy-fever.com/nextgen-image/1073/450x350/34b70d759e46422208d61e376b4633dc"></a></p>
<h4>PCB tube amplifier</h4>
<p align="center"><a href="http://diy-fever.com/wordpress/wp-content/gallery/diylc/iic.png" rel="nofollow"><img src="https://diy-fever.com/nextgen-image/1070/450x350/ccb592ec5828b6f5bc75e840438918f1"></a></p>
<h4>Perfboard guitar effect</h4>
<p align="center"><a href="http://diy-fever.com/wordpress/wp-content/gallery/diylc/split.png" rel="nofollow"><img src="https://diy-fever.com/nextgen-image/1069/450x350/358e43165de40e9c4043726c9efde4a7"></a></p>

<h3><a name="Key Features"></a>Features<a href="#Features" class="section_anchor"></a></h3>
<ul>
   <li>platform independence, will run on any machine having Java JRE/JDK 8 or newer </li>
   <li>easy to use interface, most of the operations can be done using mouse </li>
   <li>draw PCB, perfboard, stripboard or point-to-point circuit layouts layouts, schematics and guitar wiring diagrams </li>
   <li>high flexibility, has the API to allow plug-ins and new components to be added</li>
   <li>improved performance and reduced memory consumption compared to the previous versions </li>
   <li>save default values for each property of a component individually </li>
   <li>group components together and treat them as one, e.g. move, edit or delete </li>
   <li>export the output to image, PDF or printer </li>
   <li>export PCB trace mask suitable for toner transfer </li>
   <li>export the circuit to a SPICE compatible netlist (beta) </li>
   <li>create a Bill of Materials as a part of the project or export it to few different file formats </li>
   <li>analyze guitar wirings and show detailed pickup configuration in each switch configuration</li>
   <li>zoom in/out feature </li>
   <li>configurable grid spacing on the project level </li>
   <li>auto update checks for new versions </li>
   <li>import files created with older versions of the application </li>   
   <li>cloud feature - share your projects, search through the cloud and download other users' projects </li>
   <li>highlight connected areas to simplify validation and circuit debugging</li>
   <li>analyze guitar wiring diagrams and visualize pickup wirings for each switch configuration</li>
   <li>export circuits to SPICE-compatible netlists</li>
</ul>
<p>To report bugs or request a new feature go to <a href="https://github.com/bancika/diy-layout-creator/issues" rel="nofollow">Issues</a> page and create a new issue describing your problem or request. </p>
<p>To join the discussion or request technical assistance, use the dedicated <a href="http://groups.google.com/group/diy-layout-creator" rel="nofollow">Discussion Group</a>. </p>
<h3><a name="Want_to_contribute?"></a>Want to contribute?<a href="#Want_to_contribute?" class="section_anchor"></a></h3>
<p align="justify"><strong>DIYLC is provided free of charge and can be used and redistributed freely. Please note that it takes a lot of time and effort to build, test and maintain it and to interact with the growing community of users. If you find DIYLC useful and want to support further development, these are some of the ways you can contribute</strong></p>
<ul>
   <li><a href="https://www.patreon.com/c/user?u=5213116">Become a Patreon.</a></li>
   <li><a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=U6GLS8HLTSU88">Make a one-time donation with PayPal.</a></li>
   <li>Help with adding the information to the <a href="https://github.com/bancika/diy-layout-creator/blob/wiki/Manual.md">user manual</a> or other project documentation.</li>
   <li>Extend the component base, read <a href="https://github.com/bancika/diy-layout-creator/blob/wiki/ComponentAPI.md" rel="nofollow">this wiki</a> to learn how to create new components. </li>
   <li>Add a new functionality though plug-ins, read <a href="https://github.com/bancika/diy-layout-creator/blob/wiki/PluginAPI.md" rel="nofollow">this wiki</a> to learn how to create new plug-ins. </li>
</ul>
<!--<p align="center"><a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=U6GLS8HLTSU88" rel="nofollow"><img src="https://www.paypal.com/en_US/i/btn/btn_donateCC_LG.gif"></a> </p>-->
<h3><a name="License_information"></a>License information<a href="#License_information" class="section_anchor"></a></h3>
<p>Source code is released under <a href="https://www.gnu.org/licenses/gpl-3.0.txt">GNU General Public License version 3</a>. Contact the project owner to obtain a license in case you plan to use the source code, or any part of it, in commercial purposes. </p>
<p></p>
<h3><a name="Credits"></a>Credits<a href="#Credits" class="section_anchor"></a></h3>
<ul>
   <li>Big thanks goes to folks from the following online communities for contributing ideas and helped test the app: <a href="http://www.diystompboxes.com/smfforum/" rel="nofollow">DIY Stompboxes</a>, <a href="http://ax84.com/bbs" rel="nofollow">AX84</a> and <a href="http://freestompboxes.org" rel="nofollow">Free Stompboxes</a>. </li>
   <li>I'm happy to report that DIYLC 4 is the fastest version ever. <a href="http://www.yourkit.com/java/profiler/index.jsp" rel="nofollow">YourKit Java Profiler</a> helped tremendously with memory and performance profiling and pointed me to places in the code that took longer to run or consumed more memory than I'd like them to. I strongly recommend this tool to anyone developing Java or .NET applications because it helps finding bottlenecks and allocation hot-spots very fast.<br><br>YourKit is kindly supporting open source projects with its full-featured Java Profiler.<br>YourKit, LLC is the creator of innovative and intelligent tools for profiling Java and .NET applications.<br>Take a look at YourKit's leading software products: <a href="http://www.yourkit.com/java/profiler/index.jsp" rel="nofollow">YourKit Java Profiler</a> and <a href="http://www.yourkit.com/.net/profiler/index.jsp" rel="nofollow">YourKit .NET Profiler</a> </li>
</ul>
