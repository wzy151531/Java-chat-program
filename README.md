# Java Project

This java project is about a client-server mode program based on socket.

## Alias

The whole project is divided into two parts: server and client. Both projects are built with `gradle6` and `jdk13`.

## Quick Start

### Build and run

For server project, first you need to add a `jdbc.properties` file which includes the ssh connection information and database information in `src/main/resources` like:

```bash
sshUser=aaa111
sshPassword=password
dbUser=socotra
dbPassword=password
```

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

then the `repos` foler will be generated with serveral .jar files.

For client project, it use `javafx13` to build the GUI, so once you use the gradle to generate a .jar file, you need to download javafx13 sdk first, and run it as following:

```bash
java --module-path $PATHTOJAVAFXSDK13 --add-modules javafx.controls,javafx.fxml,javafx.base -jar $YOURCLIENT.jar
```

## Contributing
First clone the project, and in your local git repository, create a new branch named as your name, for example:

```bash
git branch yourname
```

and then checkout to that branch:

```bash
git checkout yourname
```

Now, to check if you are in the right branch, use the command:

```bash
git branch -a
```

and the '*' will be infront of the current branch.

Now you can do some change to your branch, once you have done your task, after testing the new function and commiting the change, first use command:

```bash
git checkout master
```

to checkout to the master branch, and then use the command to merge your branch to the master branch:

```bash
git merge yourname
```
Make sure before each commit, checkout your current branch first.


## License
[MIT](https://choosealicense.com/licenses/mit/)
