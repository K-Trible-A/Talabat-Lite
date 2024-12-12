#include "../include/constants.hpp"
#include "../include/database.hpp"
#include "../include/server.hpp"
#include <iostream>

using namespace std;

Database db("Database.db");
funcPtr actions[MAX_ACTIONS];

void server::handleClient(int clientFD) {
  int code = server::recvInt(clientFD);
  if(code >= 1000)
    actions[code](clientFD);
  close(clientFD);
}

int main() {
  db.createSchema();
  string IP;
  int portNum, queueSz;
  std::cout << "Enter IP address of the server :" << std::endl;
  std::cin >> IP;
  std::cout << "Enter the port number :" << std::endl;
  std::cin >> portNum;
  std::cout << "Enter the queue size :" << std::endl;
  std::cin >> queueSz;
  server srv(IP, portNum, queueSz);

  srv.listenLoop(); // Start accepting clients connections.

  return 0;
}
