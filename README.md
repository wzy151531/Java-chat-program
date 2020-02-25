# Java Project

This java project is about a client-server mode program based on socket.

## Alias

The whole project is divided into two parts: server and client. Both projects are built with gradle6 and jdk8.

## Installation

### Server

For server project, first you need to add a 'jdbc-properties' file which includes the ssh connection information and database information in 'src/main/resources'.

The two projects both use gradle-wrapper, so if you don't install the gradle and want to use gradle to build or archive, just run the following command in the project root folder like 'socotra-server' or 'socotra-client'

```bash
./gradlew build
```

```bash
./gradlew uploadArchives
```

If you have the gradle, just use the command to build the server project:

```bash
gradle build
```
and then run the main class file Server.class.

Or simply run the Server.java in IDEA.

If you want to release the .jar file, you can archive the server project as:

```bash
gradle uploadArchives
```

then the repos foler will be generated with serveral .jar files.

## Contributing
Pull requests are welcome. At this moment, maybe it should be more considered about the branch, like everyone should only work on his branch, once the whole function is completed and tested, his branch can be merged to the master branch. So, just pull the codes and read them now, push your own code is not recommended.

## License
[MIT](https://choosealicense.com/licenses/mit/)
