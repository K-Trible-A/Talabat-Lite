#include "../include/actions.hpp"
#include "../include/database.hpp"
#include "../include/server.hpp"

#include <string>
using namespace std;

extern Database db;

void firstConnection(int clientFD) {
  // Do nothing
  return;
}

void authClient(int clientFD) {
  string email = server::recvString(clientFD);
  string pass = server::recvString(clientFD);
  const string sql =
      "SELECT password FROM users WHERE email = '" + email + "';";
  vector<vector<string>> ans = db.query(sql);
  int ok = 1;
  if (ans.empty())
    ok = 0;
  else
    ok = (pass == ans[0][0]);
  server::send(clientFD, ok);
}
