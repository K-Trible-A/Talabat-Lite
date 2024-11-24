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

void addMerchant(int clientFD)
{
  enum {grocery = 1 , restaurant = 2 , pharmacy = 3};
  string type;
  string businessName = server::recvString(clientFD);
  int businessType = server::recvInt(clientFD);
  string keywords = server::recvString(clientFD);
  string pickupAddress = server::recvString(clientFD);
  string nationaID = server::recvString(clientFD);
  string cardNumber = server::recvString(clientFD);
  string expiryDate = server::recvString(clientFD);
  string CVV = server::recvString(clientFD);
  switch (businessType)
  {
      case 1:
          type = "grocery";
          break;
      case 2:
          type = "restaurant";
          break;
      case 3:
          type = "pharmacy";
          break; 
  }

  string cardExec;
  string cardId;

  if (cardNumber == "null")
      cardExec = "INSERT INTO card (cardNumber,CVV,expiryDate) VALUES (NULL,NULL,NULL)";
  else
  cardExec = "INSERT INTO card (cardNumber,CVV,expiryDate) VALUES ('" + cardNumber + "','" + CVV + "','" + expiryDate + "');";
      const string cardExecConst = cardExec;    
      db.execute(cardExecConst);
      const string sql = "SELECT cardId FROM card WHERE cardNumber = '" + cardNumber + "'";
      vector <vector<string>> ans = db.query(sql);
      if (ans.empty())
      cardId = "NULL";
      else 
      cardId = ans[0][0];
      const string merchExec = "INSERT INTO merchant (cardId,businessName,businessType,keywords,pickupAddress,nationalID) VALUES ('" + cardId + "','" + businessName + "','" + type + "','" + keywords + "','" + pickupAddress + "','" + nationaID + "')";
      db.execute(merchExec);
      const int ok = 1;
      server::send(clientFD,ok);

}
