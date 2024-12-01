#include "../include/database.hpp"
#include <iostream>
#include <sstream>

Database::Database(const string &dbName) {
  if (sqlite3_open(dbName.c_str(), &db) != SQLITE_OK) {
    cerr << "Cannot open database: " << sqlite3_errmsg(db) << endl;
    db = nullptr;
  }
}
sqlite3 *Database::getDB() { return db; }
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
  sqlite3_stmt *stmt;

  // Prepare SQL statement
  if (sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
    cerr << "Failed to prepare statement: " << sqlite3_errmsg(db) << endl;
    return result;
  }

  // Process each row in the result set
  while (sqlite3_step(stmt) == SQLITE_ROW) {
    vector<string> row;
    for (int i = 0; i < sqlite3_column_count(stmt); ++i) {
      switch (sqlite3_column_type(stmt, i)) {
      case SQLITE_INTEGER: {
        // Handle INTEGER (int) data
        int intValue = sqlite3_column_int(stmt, i);
        row.push_back(to_string(intValue));
        break;
      }
      case SQLITE_FLOAT: {
        // Handle REAL (float/double) data
        double realValue = sqlite3_column_double(stmt, i);
        row.push_back(to_string(realValue));
        break;
      }
      case SQLITE_TEXT: {
        // Handle TEXT (string) data
        const char *text = (const char *)sqlite3_column_text(stmt, i);
        row.push_back(text ? text : "");
        break;
      }
      case SQLITE_BLOB: {
        // Handle BLOB (binary) data
        const void *blobData = sqlite3_column_blob(stmt, i);
        int blobSize = sqlite3_column_bytes(stmt, i);
        stringstream ss;
        ss.write(static_cast<const char *>(blobData), blobSize);
        row.push_back(ss.str()); // Storing BLOB as a string (could also store
                                 // as binary data)
        break;
      }
      case SQLITE_NULL: {
        // Handle NULL values
        row.push_back("NULL");
        break;
      }
      default:
        row.push_back("Unknown type");
        break;
      }
    }
    result.push_back(row);
  }

  sqlite3_finalize(stmt);
  return result;
}

void Database::createSchema() {
  //Enable foreign keys support
  this->execute("PRAGMA foreign_keys = ON;");
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
  this->execute( // Merchant information
      "CREATE TABLE IF NOT EXISTS merchant ("
      "merchantId INTEGER PRIMARY KEY AUTOINCREMENT,"
      "userId INTEGER NOT NULL UNIQUE,"
      "cardId INTEGER NOT NULL,"
      "businessName TEXT NOT NULL,"
      "businessType INTEGER NOT NULL,"
      "keywords TEXT NOT NULL,"
      "pickupAddress TEXT NOT NULL,"
      "nationalID TEXT NOT NULL,"
      "FOREIGN KEY(cardId) REFERENCES card(cardId),"
      "FOREIGN KEY(userId) REFERENCES users(id));");
  this->execute(  // Merchant information
      "CREATE TABLE IF NOT EXISTS merchant (merchantId INTEGER PRIMARY KEY AUTOINCREMENT ,cardId INTEGER NULL , businessName TEXT NOT NULL,"
      "businessType TEXT NOT NULL , keywords TEXT NOT NULL,"
      "pickupAddress TEXT NOT NULL , nationalID TEXT NOT NULL,"
      "FOREIGN KEY(cardId) REFERENCES card(cardId) , FOREIGN KEY(merchantId) REFERENCES users(id));");
  this->execute(  // Courier information
      "CREATE TABLE IF NOT EXISTS courier (courierId INTEGER PRIMARY KEY AUTOINCREMENT ,cardId INTEGER NULL ,"
      "vehicleType TEXT NOT NULL ,"
      "nationalID TEXT NOT NULL,"
      "FOREIGN KEY(cardId) REFERENCES card(cardId) , FOREIGN KEY(courierId) REFERENCES users(id));");
  this->execute( // Card information
      "CREATE TABLE IF NOT EXISTS card ("
      "cardId INTEGER PRIMARY KEY AUTOINCREMENT,"
      "userId INTEGER,"
      "cardNumber TEXT NULL,"
      "CVV INTEGER NULL,"
      "expiryDate TEXT NULL,"
      "FOREIGN KEY(userId) REFERENCES users(id));");
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
  this->execute( // Item information
      "CREATE TABLE IF NOT EXISTS item ("
      "itemId INTEGER PRIMARY KEY AUTOINCREMENT,"
      "merchantId INTEGER NOT NULL,"
      "itemName TEXT NOT NULL,"
      "itemPrice REAL NOT NULL,"
      "itemDescription TEXT NULL,"
      "itemImg BLOB NOT NULL,"
      "FOREIGN KEY(merchantId) REFERENCES merchant(merchantId)"
      ");");

  // Other tables
}
