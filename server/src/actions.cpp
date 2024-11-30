#include "../include/actions.hpp"
#include "../include/database.hpp"
#include "../include/server.hpp"

#include <fstream>
#include <iostream>
#include <sqlite3.h>
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
  int ok;
  if (ans.empty() || pass != ans[0][0]) {
    ok = -1;
  } else {
    vector<vector<string>> getId =
        db.query("SELECT id FROM users WHERE email = '" + email + "';");
    ok = stoi(getId[0][0]);
  }
  server::send(clientFD, ok);
}

void addMerchant(int clientFD) {
  enum { grocery = 1, restaurant = 2, pharmacy = 3 };
  string type;
  string businessName = server::recvString(clientFD);
  int businessType = server::recvInt(clientFD);
  string keywords = server::recvString(clientFD);
  string pickupAddress = server::recvString(clientFD);
  string nationaID = server::recvString(clientFD);
  string cardNumber = server::recvString(clientFD);
  string expiryDate = server::recvString(clientFD);
  string CVV = server::recvString(clientFD);
  switch (businessType) {
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
    cardExec =
        "INSERT INTO card (cardNumber,CVV,expiryDate) VALUES (NULL,NULL,NULL)";
  else
    cardExec = "INSERT INTO card (cardNumber,CVV,expiryDate) VALUES ('" +
               cardNumber + "','" + CVV + "','" + expiryDate + "');";
  const string cardExecConst = cardExec;
  db.execute(cardExecConst);
  const string sql =
      "SELECT cardId FROM card WHERE cardNumber = '" + cardNumber + "'";
  vector<vector<string>> ans = db.query(sql);
  if (ans.empty())
    cardId = "NULL";
  else
    cardId = ans[0][0];
  const string merchExec = "INSERT INTO merchant "
                           "(cardId,businessName,businessType,keywords,"
                           "pickupAddress,nationalID) VALUES ('" +
                           cardId + "','" + businessName + "','" + type +
                           "','" + keywords + "','" + pickupAddress + "','" +
                           nationaID + "')";
  db.execute(merchExec);
  const int ok = 1;
  server::send(clientFD, ok);
}

void addItem(int clientFD) {
  int userId = server::recvInt(clientFD);
  string itemName = server::recvString(clientFD);
  string itemDescription = server::recvString(clientFD);
  float itemPrice = server::recvFloat(clientFD);
  cout << "Item price : " << itemPrice << endl;
  pair<unsigned char *, uint32_t> rvd = server::recvImg(clientFD);
  unsigned char *itemImg = rvd.first;
  uint32_t itemImgSize = rvd.second;
  vector<vector<string>> temp = db.query(
      "SELECT merchantId FROM merchant WHERE userId = " + to_string(userId) +
      ";");
  int merchantId = stoi(temp[0][0]);
  string sql = "INSERT INTO item (merchantId, itemName, itemDescription, "
               "itemPrice, itemImg) "
               "VALUES (?1, ?2, ?3, ?4, ?5);";
  sqlite3_stmt *stmt = nullptr;
  // Prepare the SQL statement
  if (sqlite3_prepare_v2(db.getDB(), sql.c_str(), -1, &stmt, nullptr) !=
      SQLITE_OK) {
    std::cerr << "SQL prepare error: " << sqlite3_errmsg(db.getDB())
              << std::endl;
    return;
  }

  // Bind values to the placeholders
  if (sqlite3_bind_int(stmt, 1, merchantId) != SQLITE_OK ||
      sqlite3_bind_text(stmt, 2, itemName.c_str(), -1, SQLITE_STATIC) !=
          SQLITE_OK ||
      sqlite3_bind_text(stmt, 3, itemDescription.c_str(), -1, SQLITE_STATIC) !=
          SQLITE_OK ||
      sqlite3_bind_double(stmt, 4, itemPrice) != SQLITE_OK ||
      sqlite3_bind_blob(stmt, 5, itemImg, itemImgSize, SQLITE_STATIC) !=
          SQLITE_OK) {
    std::cerr << "SQL bind error: " << sqlite3_errmsg(db.getDB()) << std::endl;
    sqlite3_finalize(stmt);
    return;
  }

  // Execute the statement
  if (sqlite3_step(stmt) != SQLITE_DONE) {
    std::cerr << "SQL step error: " << sqlite3_errmsg(db.getDB()) << std::endl;
    sqlite3_finalize(stmt);
    return;
  }

  // Clean up
  sqlite3_finalize(stmt);

  // Testing save the received image to a file
  /* vector<vector<string>> myblob = db.query(
      "SELECT itemImg FROM item WHERE merchantId = " + to_string(merchantId) +
      " ;");
  ofstream outFile("revdImage.jpg", std::ios::binary);
  outFile.write(myblob[0][0].c_str(), myblob[0][0].size());
  outFile.close();

  //Testing query the inserted image from database and save to a file
  cout << "Testing query for the image -> string : " << endl;
  myblob = db.query("SELECT itemImg FROM item WHERE itemId = 1");
  ofstream outFile2("queriedImage.jpg", std::ios::binary);
  outFile2.write(myblob[0][0].c_str(), myblob[0][0].size());
  outFile2.close(); */
}

void retrieveItem(int clientFD) {
  int itemId = server::recvInt(clientFD);
  vector<vector<string>> res =
      db.query("SELECT itemName, itemPrice, itemDescription, itemImg FROM item "
               "WHERE itemId = " +
               to_string(itemId) + " ;");

  string name = res[0][0];
  float price = stof(res[0][1]);
  string desc = res[0][2];
  cout << name << ' ' << name.size() << endl;
    cout << price << endl;
    cout << desc << ' ' <<  desc.size() << endl;

  server::send(clientFD, name);  // Name
  server::sendImg(clientFD, res[0][3]);
  server::send(clientFD, desc);  // Description
  server::send(clientFD, price); // Price
}
void addCourier(int clientFD)
{
    enum {car = 1 , motorcycle = 2 , bicycle = 3};
    string type;
    int vehicleType = server::recvInt(clientFD);
    string nationaID = server::recvString(clientFD);
    string cardNumber = server::recvString(clientFD);
    string expiryDate = server::recvString(clientFD);
    string CVV = server::recvString(clientFD);
    switch (vehicleType)
    {
        case 1:
            type = "car";
            break;
        case 2:
            type = "motorcycle";
            break;
        case 3:
            type = "bicycle";
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
    const string courExec = "INSERT INTO courier (cardId,vehicleType,nationalID) VALUES ('" + cardId  + "','" + type  + "','" + nationaID + "')";
    db.execute(courExec);
    const int ok = 1;
    server::send(clientFD,ok);
}
