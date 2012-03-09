Database parser for FR/CN Dict (BSD License)
============================================

- Download CFDICT, and unzip the `cfdict.u8` file on the root of the project
- Run the `ant clean` command to clean data
- Run the `ant` command to create the output directory containing all required data.

This project uses the following library:

- [sqlitejdbc](http://www.zentus.com/sqlitejdbc/)


Note: If you modify the database structure, please update `app.min.version` in the `configuration.properties` file.
