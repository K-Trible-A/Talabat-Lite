CC = g++
SRC_SERVER = ../server/src/server.cpp 
SRC_CLIENT = ../client/Networking/client.cpp

INC_SERVER = ../server/include/server.hpp
INC_CLIENT = ../client/Networking/client.hpp

all: server client

server: $(SRC_SERVER) $(INC_SERVER) Makefile server.cpp
	@echo "Compiling server source files.."
	$(CC) $(SRC_SERVER) server.cpp -o server

client: $(SRC_CLIENT) $(INC_CLIENT) Makefile client.cpp
	@echo "Compiling client source files.."
	$(CC) $(SRC_CLIENT) client.cpp -o client
