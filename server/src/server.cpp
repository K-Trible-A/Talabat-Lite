#include "../include/server.hpp"
#include <cstdlib>
#include <iostream>
#include <netinet/in.h>
#include <sys/socket.h>
#include <thread>

using namespace std;

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

void server::send(int clientFD, const void* data, size_t sz) {
  ::send(clientFD, data, sz, 0);
}
void server::recv(int clientFD, void* data, size_t sz) {
  ::recv(clientFD, data, sz, 0);
}
