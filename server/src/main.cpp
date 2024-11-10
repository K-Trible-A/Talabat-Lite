#include "../include/database.hpp"
#include "../include/server.hpp"
#include <iostream>

using namespace std;

void server::handleClient(int clientFD){
  int code;
  server::recv(clientFD, &code, sizeof(code));
  //Action
}

int main() {
  Database db("Database.db");

  string IP;
  int portNum, queueSz;
  std::cout << "Enter IP address of the server :" << std::endl;
  std::cin >> IP;
  std::cout << "Enter the port number :" << std::endl;
  std::cin >> portNum;
  std::cout << "Enter the queue size :" << std::endl;
  std::cin >> queueSz;
  server srv(IP, portNum, queueSz);

  return 0;
}
