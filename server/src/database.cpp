#include "../include/database.hpp"
#include <iostream>

Database::Database(const string &dbName) {
  if (sqlite3_open(dbName.c_str(), &db) != SQLITE_OK) {
    cerr << "Cannot open database: " << sqlite3_errmsg(db) << endl;
    db = nullptr;
  }
}
Database::~Database() {
  if (db) {
    sqlite3_close(db);
  }
}

bool Database::execute(const string &sql) {
  char *errMsg = nullptr;
  if (sqlite3_exec(db, sql.c_str(), nullptr, nullptr, &errMsg) != SQLITE_OK) {
    cerr << "SQL error: " << errMsg << endl;
    sqlite3_free(errMsg);
    return false;
  }
  return true;
}

vector<vector<string>> Database::query(const string &sql) {
  vector<vector<string>> result;
  char *errMsg = nullptr;
  sqlite3_stmt *stmt;

  if (sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
    cerr << "Failed to prepare statement: " << sqlite3_errmsg(db) << endl;
    return result;
  }

  while (sqlite3_step(stmt) == SQLITE_ROW) {
    vector<string> row;
    for (int i = 0; i < sqlite3_column_count(stmt); ++i) {
      const char *text = (const char *)sqlite3_column_text(stmt, i);
      row.push_back(text ? text : "");
    }
    result.push_back(row);
  }

  sqlite3_finalize(stmt);
  return result;
}

void Database::createSchema() {
      this->execute( // user information
      "CREATE TABLE IF NOT EXISTS users ("
      "id INTEGER  PRIMARY KEY AUTOINCREMENT,"
      "email TEXT NOT NULL UNIQUE,"
      "password TEXT NOT NULL,"
      "name TEXT NOT NULL,"
      "phoneNumber TEXT NOT NULL UNIQUE,"
      "country TEXT NOT NULL,"
      "city TEXT NOT NULL,"
      "accountType INTEGER NOT NULL"
      ");");
  this->execute(  // Merchant information
      "CREATE TABLE IF NOT EXISTS merchant (merchantId INTEGER PRIMARY KEY AUTOINCREMENT ,cardId INTEGER NULL , businessName TEXT NOT NULL,"
      "businessType TEXT NOT NULL , keywords TEXT NOT NULL,"
      "pickupAddress TEXT NOT NULL , nationalID TEXT NOT NULL,"
      "FOREIGN KEY(cardId) REFERENCES card(cardId) , FOREIGN KEY(merchantId) REFERENCES users(id));");
  this->execute( // Card information
    "CREATE TABLE IF NOT EXISTS card (cardId INTEGER PRIMARY KEY AUTOINCREMENT,"
                "cardNumber TEXT NULL , CVV TEXT NULL,"
                "expiryDate TEXT NULL);");
  this->execute( // customer infromation
                 "CREATE TABLE IF NOT EXISTS customer ("
                 "customerId  INTEGER PRIMARY KEY AUTOINCREMENT,"
                 "deliveryAddress  TEXT NOT NULL,"
                 "userId INTEGER,"
                 "cardId INTEGER,"
                 "FOREIGN KEY (userId) REFERENCES users (id)"
                 "ON UPDATE CASCADE,"
                 "FOREIGN KEY (cardId) REFERENCES card (cardId)"
                 "ON UPDATE CASCADE"
                 ");");

  // Other tables
}
