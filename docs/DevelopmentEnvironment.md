## Setting up an IDE

If you are not an IDE person you can skip this step but we strongly encourage you to use an IDE because it provides functionalities that enable you to easy refactor and debug SimSql code.

Since version 0.5 **Maven** is being used to compile SimSql and hence you can use your favorite IDE (IntelliJ, Eclipse, NetBeans...). The only thing you need to do is to import it as a Maven project. SimSql is currently only tested to run on Linux, but development can be done on any major OS that runs **docker**.

## Setting up a docker image

The first step is to actually install docker. A detailed guide can be found [here](https://docs.docker.com/engine/installation/)

The next step is to pull the docker image for simsql. We provide two images. One is for debugging (_dimitrijejankov/simsql-debug_) and the other is for testing (_dimitrijejankov/simsql_). The fist one is configured to be used with a debugger and to do line by line execution, the other isn't. Depending on your needs you can run any of the following commands.

**docker pull dimitrijejankov/simsql-debug**

**docker pull dimitrijejankov/simsql**

After the images are pulled you can run the shell script found in **run_scripts/run_server.sh** directory to run the image as a container for the SimSql server

## Compiling

To to compile SimSql inside the docker container, first you need to copy the SimSql files to the container. In order to to that you can run the shell script **copy-to-server.sh** which is located inside the **run_scripts/** directory. Make sure to run it inside the **run_scripts/** directory because it uses a relative path. The files will be copied to the **/simsql** directory in the container

Next you need to compile SimSql on the server. There is also a shell script called **compile-on-server.sh**, that is located in the **run_scripts/** directory. After the script finishes it will create a .jar file inside the **/simsql/target** directory.

## Running SimSql