
```
export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home
export JAVA=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Commands/java
java -Xms512m -Xmx1024m -cp diylc.jar:lib org.diylc.DIYLCStarter
```
Note that path to your java 1.6 may be different.

  * **Saving to a file doesn't do anything - busy cursor appears but nothing happens. What can I do?**<br><br>This is a Java bug that occurred in version 1.6.17 and hasn't been fixed yet. The best thing to do is to downgrade to any version between 1.6.10 and 1.6.16 and set the older version as default. It's not a problem to have multiple versions installed, but the old one needs to be default when running DIYLC.</li></ul>

  * **What do those check boxes on the right side of the component editor do?**<br><br>They are used to set default values for the selected component(s). When checked, value on the left of the box will be used as a default for all components of that type.