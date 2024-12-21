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
## Features
### For Merchant: 
#### Registration:
- Information useful for his branding like name, keywords, profile image and rating. <br>
- Information useful for managing delivery personnel like pick-up address. <br>
#### Functionality:
- Add new items with information like (name, description, price and image). <br>
- Remove added items. <br>
- Edit his profile information (pick-up address and profile image). <br>
- Search for his items. <br>
- View active orders, accept them to be sent to active orders at every courier <br>
account. 
### For Customer: 
#### Registration:
- Information useful for delivery like delivery address and phone number. <br>
- Information useful for his profile like name and profile image. <br>
#### Functionality:
- View merchant categories at home and the 3 top rated merchants of his city. <br>
- Search for all merchants. <br>
- View cart activity <br>
- Update item quantity or remove them from cart. <br>
- View orders <br>
- View & edit profile information
### For Courier: 
#### Registration:
- Information like vehicle type, phone number and national id. <br>
- Card information to get his earnings. <br>
#### Functionality:
- View all active orders from merchants of same city. <br>
- View order details.<br>
- Accept orders.<br>
## Security Warnings
- There is no Encryption for sensitive data like passwords, card number, ...
- There is no protection from SQL Injection attacks.
- No protection from DDoS attacks.
## Screenshots
![screenshots](https://github.com/user-attachments/assets/6c4ebe77-06ed-43cf-a8bd-d372e623f695)



