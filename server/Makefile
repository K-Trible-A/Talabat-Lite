CC = g++
SRC = src/*.cpp 
INC = include/*.hpp

all: server

server: $(SRC) $(INC) Makefile
	@echo "Compiling source files.."
	$(CC) $(SRC) -o server -lsqlite3
