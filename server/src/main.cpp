#include "../include/database.hpp"
#include "../include/server.hpp"

using namespace std;

void server::handleClient(int clientFD){
  int code;
  server::recv(clientFD, &code, sizeof(code));
  //Action
}

int main() {
  Database db("Database.db");
  server srv("127.0.0.1", 57000, 20);

  return 0;
}
