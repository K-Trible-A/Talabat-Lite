#include "../server/include/server.hpp"
#include <iostream>
using namespace std;

struct MyStruct {
  char str[10];
  int arr[5];
};

void server::handleClient(int clientFD) {
  int code;
  server::recv(clientFD, &code, sizeof(code));

  string s = "Hi client!";

  MyStruct strk;
  string temp = "hello";
  for (int i = 0; i < temp.size(); i++) {
    strk.str[i] = temp[i];
  }
  strk.str[temp.size()] = '\0';

  for (int i = 1; i <= 5; i++)
    strk.arr[i - 1] = i;

  if (code == 1) {
    // send string size first
    int sz = s.size();
    server::send(clientFD, &sz, sizeof(sz));
    // send cstring
    server::send(clientFD, s.c_str(), sz * sizeof(char));
  } else if (code == 2) {
    // send stsruct
    server::send(clientFD, &strk, sizeof(strk));
  }
  close(clientFD);
}

int main() {
  string IP;
  int portNum, queueSz;
  std::cout << "Enter IP address of the server :" << std::endl;
  std::cin >> IP;
  std::cout << "Enter the port number :" << std::endl;
  std::cin >> portNum;
  std::cout << "Enter the queue size :" << std::endl;
  std::cin >> queueSz;
  server srv(IP, portNum, queueSz);
  srv.listenLoop();
}
