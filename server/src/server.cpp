#include "../include/server.hpp"
#include "../include/actions.hpp"
#include "../include/constants.hpp"
#include <cstring>
#include <iostream>
#include <netinet/in.h>
#include <sys/socket.h>
#include <thread>

using namespace std;

extern funcPtr actions[MAX_ACTIONS];

void server::setAddr(const std::string &IP, int portNum, int queueSize) {
  serverFD = socket(AF_INET, SOCK_STREAM, 0);
  if (serverFD == 0) {
    cerr << "Socket failed" << endl;
    exit(EXIT_FAILURE);
  }
  serverAddr.sin_family = AF_INET;
  serverAddr.sin_port = htons(portNum);
  serverAddr.sin_addr.s_addr = inet_addr(IP.c_str());
}

void server::bindSkt() {
  int ok_bind = bind(serverFD, (sockaddr *)&serverAddr, sizeof(serverAddr));
  if (ok_bind == -1) {
    cerr << "Error binding a socket!";
    close(serverFD);
    exit(EXIT_FAILURE);
  }
}

void server::listenSkt(int qSize) {
  int ok_listen = listen(serverFD, qSize);
  if (ok_listen == -1) {
    cerr << "Error listening to the socket!";
    close(serverFD);
    exit(EXIT_FAILURE);
  }
}

server::server(const std::string &IP, int portNum, int qSize) {
  setAddr(IP, portNum, qSize);
  bindSkt();
  listenSkt(qSize);
  appendFuncs();
  cout << "Server is up and running..." << endl;
}

int server::acceptClient() {
  sockaddr_in clientAddr;
  socklen_t clientAddrSz = sizeof(clientAddr);
  int clientFD = accept(serverFD, (sockaddr *)&clientAddr, &clientAddrSz);
  if (clientFD < 0) {
    cerr << "Accept client failed" << endl;
    exit(EXIT_FAILURE);
  }
  char clientIP[INET_ADDRSTRLEN];
  inet_ntop(AF_INET, &clientAddr.sin_addr, clientIP, sizeof(clientIP));
  cout << "New connection from " << clientIP << ":"
       << ntohs(clientAddr.sin_port) << endl;
  return clientFD;
}

void server::listenLoop() {
  while (true) {
    int clientFD = acceptClient();
    thread clientThread(&server::handleClient, this, clientFD);
    clientThread.detach();
  }
}

void server::send(int clientFD, const int& num) {
  ::send(clientFD, &num, sizeof(num), 0);
}
void server::send(int clientFD, const float& num) {
  ::send(clientFD, &num, sizeof(num), 0);
}
void server::send(int clientFD, const std::string& s) {
  int sz = s.size() + 1;
  server::send(clientFD, sz);
  char buff[sz];
  memset(buff, 0, sizeof(buff));
  strcpy(buff, s.c_str());
  ::send(clientFD, buff, sizeof(buff), 0);
}

int server::recvInt(int clientFD) {
  int revd;
  ::recv(clientFD, &revd, sizeof(revd), 0);
  return revd = ntohl(revd);
}
float server::recvFloat(int clientFD) {
  uint32_t revd;
  ::recv(clientFD, &revd, sizeof(revd), 0);
  revd = ntohl(revd);
  float ret;
  memcpy(&ret, &revd, sizeof(revd));
  return ret;
}
std::string server::recvString(int clientFD) {
  int sz = server::recvInt(clientFD);
  char buff[sz + 1];
  ::recv(clientFD, buff, sz * sizeof(char), 0);
  buff[sz] = '\0';
  return string(buff);
}

///////////
void server::appendFuncs() {
  actions[FIRST_CONNECTION] = firstConnection;
  actions[AUTHENTICATE_CLIENT] = authClient;
  actions[ADD_MERCHANT] = addMerchant;
  actions[ADD_USER]=addUser;
  actions[ADD_CUSTOMER]=addCustomer;
}
