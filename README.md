# Talabat-Lite
Talabat-Lite is a server-client application for android. It facilitates food, goods or medicines delivery and other services, connecting customers with local vendors and managing delivery personnel.<br><br>
At client/app/src/main/java/com/kaaa/talabat_lite/globals.java You need to set the serverIp :
```java
public static String serverURL = "http://192.168.1.11:8080";
```
## Server side
The server is written in C++ communicating with client over HTTP using crow (microframework for the web, uses routing similar to Python's Flask).
<br>For database it uses SQLite C/C++ interface.
### Dependencies
```
make or cmake
gcc
sqlite
crow
```
### Build and run
```shell
# To build and run the server on a Linux system :

# Build using Make
make
./server
# Build and delete the database
make clean

# Build using CMake
cmake -B build
cd build
make
./server
```
