#include "../include/actions.hpp"
#include "../include/database.hpp"
#include "../include/server.hpp"

#include <cmath>
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
  int userId = server::recvInt(clientFD);
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

  int ok = 1;
  string cardId;
  string tableName = "card";
  vector<string> columns = {"userId", "cardNumber", "CVV", "expiryDate"};
  vector<string> values = {to_string(userId), cardNumber, CVV, expiryDate};
  ok &= db.insertData(tableName, columns, values);
  const string sql =
      "SELECT cardId FROM card WHERE cardNumber = '" + cardNumber + "';";
  vector<vector<string>> ans = db.query(sql);
  cardId = ans[0][0];
  tableName = "merchant";
  columns = {"userId",   "cardId",        "businessName", "businessType",
             "keywords", "pickupAddress", "nationalID",   "rating"};
  values = {to_string(userId), cardId,        businessName, type,
            keywords,          pickupAddress, nationaID,    "0"};
  ok &= db.insertData(tableName, columns, values);
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
  cout << desc << ' ' << desc.size() << endl;

  server::send(clientFD, name); // Name
  server::sendImg(clientFD, res[0][3]);
  server::send(clientFD, desc);  // Description
  server::send(clientFD, price); // Price
}
void addCourier(int clientFD) {
  enum { car = 1, motorcycle = 2, bicycle = 3 };
  string type;
  int vehicleType = server::recvInt(clientFD);
  string nationaID = server::recvString(clientFD);
  string cardNumber = server::recvString(clientFD);
  string expiryDate = server::recvString(clientFD);
  string CVV = server::recvString(clientFD);
  switch (vehicleType) {
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
    cardExec =
        "INSERT INTO card (cardNumber,CVV,expiryDate) VALUES (NULL,NULL,NULL);";
  else
    cardExec = "INSERT INTO card (cardNumber,CVV,expiryDate) VALUES ('" +
               cardNumber + "','" + CVV + "','" + expiryDate + "');";
  const string cardExecConst = cardExec;
  db.execute(cardExecConst);
  const string sql =
      "SELECT cardId FROM card WHERE cardNumber = '" + cardNumber + "';";
  vector<vector<string>> ans = db.query(sql);
  if (ans.empty())
    cardId = "NULL";
  else
    cardId = ans[0][0];
  const string courExec =
      "INSERT INTO courier (cardId,vehicleType,nationalID) VALUES ('" + cardId +
      "','" + type + "','" + nationaID + "');";
  db.execute(courExec);
  const int ok = 1;
  server::send(clientFD, ok);
}
void addUser(int clientFD) {
  enum { Customer = 1, Merchant = 2, Courier = 3 };
  string name = server::recvString(clientFD);
  string phoneNumber = server::recvString(clientFD);
  string email = server::recvString(clientFD);
  string password = server::recvString(clientFD);
  string country = server::recvString(clientFD);
  string city = server::recvString(clientFD);
  int accountType = server::recvInt(clientFD);
  const string sql = "SELECT * FROM users WHERE email = '" + email + "';";
  vector<vector<string>> ans = db.query(sql);
  const string sql1 =
      "SELECT * FROM users WHERE phoneNumber = '" + phoneNumber + "';";
  vector<vector<string>> ans1 = db.query(sql1);
  int ok = 0;
  if (ans.empty() && ans1.empty())
    ok = 1;
  if (ok == 1) {
    const string userExec = "INSERT INTO users "
                            "(email,password,name,phoneNumber,country,city,"
                            "accountType) VALUES ( '" +
                            email + "','" + password + "','" + name + "','" +
                            phoneNumber + "','" + country + "','" + city +
                            "','" + std::to_string(accountType) + "');";
    db.execute(userExec);
  }
  server::send(clientFD, ok);
  if (ok == 1) {
    const string sql2 = "SELECT id FROM users WHERE email = '" + email + "';";
    vector<vector<string>> ans2 = db.query(sql2);
    int userId = std::stoi(ans2[0][0]);
    server::send(clientFD, userId);
  }
}
void addCustomer(int clientFD) {
  string deliveryAddress = server::recvString(clientFD);
  int userId = server::recvInt(clientFD);
  const string customerExec =
      "INSERT INTO customer (deliveryAddress,userId) VALUES ('" +
      deliveryAddress + "','" + std::to_string(userId) + "');";
  db.execute(customerExec);
  const int ok = 1;
  server::send(clientFD, ok);
}

void getMerchantData(int clientFD) {

  int userId = server::recvInt(clientFD);

  string sql = "SELECT merchant.* FROM users "
               "JOIN merchant ON users.id = merchant.merchantId "
               "WHERE users.id = '" +
               to_string(userId) + "';";

  vector<vector<string>> ans = db.query(sql);

  string businessName = ans[0][3];
  string type = ans[0][4];
  string keywords = ans[0][5];
  string pickupAddress = ans[0][6];
  float rating = stof(ans[0][8]);
  rating = round(rating * 10) / 10;
  server::send(clientFD, businessName);
  server::send(clientFD, type);
  server::send(clientFD, keywords);
  server::send(clientFD, pickupAddress);
  server::send(clientFD, rating);

  int ok = 1;

  server::send(clientFD, ok);
}

void changePickupAddress(int clientFD) {
  int userId = server::recvInt(clientFD);
  string pickupAddress = server::recvString(clientFD);
  const string condition = "userId = " + to_string(userId);
  int ok =
      db.updateData("merchant", {"pickupAddress"}, {pickupAddress}, condition);
  server::send(clientFD, ok);
}

void checkAccountType(int clientFD) {
  enum { CUSTOMER = 51, MERCHANT = 52, COURIER = 53 };
  int userId = server::recvInt(clientFD);
  const string sql =
      "SELECT accountType FROM users WHERE id = " + to_string(userId) + ";";
  vector<vector<string>> ans = db.query(sql);
  int accountType = stoi(ans[0][0]);
  switch (accountType) {
  case 1:
    accountType = CUSTOMER;
    break;
  case 2:
    accountType = MERCHANT;
    break;
  case 3:
    accountType = COURIER;
    break;
  }
  server::send(clientFD, accountType);
}
void getTopRatedMerchants(int clientFD)
{
    int userId = server::recvInt(clientFD);
    const string sql="SELECT m.businessName,m.rating FROM merchant m  JOIN users u ON m.userId = u.userId WHERE  u.city = ( SELECT city  ROM users WHERE userId              = :"+to_string(userId)+"ORDER BY m.rating DESC  LIMIT 3 ;";
    vector <vector<string>> ans =db.query(sql);
     string merch_1_Name=ans[0][0],merch_1_Rate=ans[0][1];
     string merch_2_Name=ans[1][0],merch_2_Rate=ans[1][1];
     string merch_3_Name=ans[2][0],merch_3_Rate=ans[2][1];
     server::send(clientFD,merch_1_Name);
     server::send(clientFD,merch_1_Rate);
     server::send(clientFD,merch_2_Name);
     server::send(clientFD,merch_2_Rate);
     server::send(clientFD,merch_3_Name);
     server::send(clientFD,merch_3_Rate);
     int ok = 1;
     server::send(clientFD,ok);
}
