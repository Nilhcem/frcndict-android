Chinese French Dictionary For Android
=====================================

Free Chinese-French Dictionary for Android devices.
Released under the BSD license.

Any feedback is highly appreciated!


Build project
-------------

- Go to the project root directory and type `android update project --path .` to specify the sdk directory (only required once).
- Run the `ant release` command.


Modify the project configuration
--------------------------------

- Open the `ant.properties` file and modify it if needed
- Run the `ant config` command to include the previously modified configuration to the Java project.


Dictionary data
----------------

This project uses a modified version of the [CFDICT](http://www.chine-informations.com/chinois/open/CFDICT/) dictionary.
To generate dictionary data, please see the **dbparser** git branch.


Steps to do before releasing the application
---------------------------------------------

- Open `AndroidManifest.xml` and set `android:debuggable="false"`
- Open `ant.properties` and set `logging.level=0`
- Launch `ant clean release`
- Sign application and run `zipalign` (See Android Developers Documentation)


Screenshots
-----------

<img src="http://nilhcem.github.com/screenshots/cfdict.png" width="640" height="480" />


Contact me
----------

Use my github's nickname (at) gmail (dot) com
