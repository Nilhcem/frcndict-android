Database parser for FR/CN Dict (BSD License)
============================================

- Download CFDICT, and unzip the `cfdict.u8` file on the root of the project
- Run the `ant clean` command to clean data
- Run the `ant` command to generate a database in the "assets" directory
- Copy the content of the "assets" directory in the "assets" directory of the Android application
- Copy the `Tables.java` (`com/nilhcem/cfdictparser/sqlite`) file into the Android application (`com/nilhcem/frcndict/database`).

This project uses the following library:

- [sqlitejdbc](http://www.zentus.com/sqlitejdbc/)
