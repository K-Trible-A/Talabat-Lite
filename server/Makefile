CC = g++
SRC = src/*.cpp 
INC = include/*.hpp

all: server

clean: server
	@echo "Removing Database.db.."
	rm -f Database.db

server: $(SRC) $(INC) Makefile
	@echo "Compiling source files.."
	$(CC) $(SRC) -o server -lsqlite3 -lpthread
