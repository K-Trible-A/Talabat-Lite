#include "../client/Networking/client.hpp"
#include <iostream>

using namespace std;

struct MyStruct {
  char str[10];
  int arr[5];
};

int main() {
  client clnt("127.0.0.1", 57000);
  bool ok = clnt.connect();
  if (!ok) {
    cout << "Connection error!" << endl;
    exit(1);
  }

  int code = 1;
  client::send(clnt.serverFD, &code, sizeof(code));
  // receive a string size
  int sz;
  client::recv(clnt.serverFD, &sz, sizeof(sz));
  cout << "size received : " << sz << endl;
  char *temp = new char[sz];
  client::recv(clnt.serverFD, temp, sz * sizeof(char));
  string s(temp);
  delete[] temp;
  cout << "string received : " << s << endl << endl;


  ok = clnt.connect();
  if (!ok) {
    cout << "Connection error!" << endl;
    exit(1);
  }
  code = 2;
  client::send(clnt.serverFD, &code, sizeof(code));
  MyStruct strk;
  // receive a struct
  client::recv(clnt.serverFD, &strk, sizeof(strk));
  cout << "struct received : " << endl;
  cout << strk.str << endl;
  for (int i = 0; i < 5; i++)
    cout << strk.arr[i] << ' ';
  cout << endl;
}
