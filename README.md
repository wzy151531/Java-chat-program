# Java Project

This java project is about a client-server mode program based on socket.

## Alias

The whole project is divided into two parts: server and client. Both projects are built with `gradle6` and `jdk11`.

## Quick Start

### Build and run

For server project, first you need to add a `jdbc-properties` file which includes the ssh connection information and database information in `src/main/resources`.

The two projects both use gradle-wrapper, you can just open either of them in intellij or eclipse simply, and use the gradle tool inside the IDE to build and run the project.

You don't need to install the gradle, just run the following command in the project root folder like `socotra-server` or `socotra-client`:

```bash
./gradlew build
```

and to run the application:

```bash
./gradlew run
```

Or simply use the gradle gui tool inside the IDE(for intellij it's on the right side bar) to run these tasks.

### Archive and release

For server project, you can release the .jar file, you can archive the server project as:

```bash
./gradlew uploadArchives
```

then the repos foler will be generated with serveral .jar files.

For client project, it's a modular project because of the requires for javafx13 modules, you can use the command to archive client project .jar file and its related module .jar files:

```bash
./gradlew jlink
```

Or just use the gradle gui tool inside the IDE(for intellij it's on the right side bar) to run this task.

And all related .jar files will be generated in `/build/jlinkbase/jlinkjars`, to run this .jar file, use the following command under that folder:

```bash
java --module-path "." --module "socotra.client.main/socotra.Client"
```

## Contributing
Pull requests are welcome. At this moment, maybe it should be more considered about the branch, like everyone should only work on his branch, once the whole function is completed and tested by him, his branch can be merged to the master branch. So, just pull the codes and read them now, push your own code is not recommended.

## License
[MIT](https://choosealicense.com/licenses/mit/)
