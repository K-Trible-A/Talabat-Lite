#include "client.hpp"

bool client::connect() {
  serverFD = socket(AF_INET, SOCK_STREAM, 0);
  serverAddr.sin_family = AF_INET;
  serverAddr.sin_port = htons(portNum);
  serverAddr.sin_addr.s_addr = inet_addr(IP.c_str());
  int con =
      ::connect(serverFD, (struct sockaddr *)&serverAddr, sizeof(serverAddr));
  return con == 0;
}

client::client(const std::string &ip, int p) : IP(ip), portNum(p) {}

void client::send(int serverFD, const void *data, size_t sz) {
  ::send(serverFD, data, sz, 0);
}
void client::recv(int serverFD, void *data, size_t sz) {
  ::recv(serverFD, data, sz, 0);
}
