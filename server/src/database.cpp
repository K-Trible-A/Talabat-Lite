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
std::string Database::escapeSpecialChars(const std::string &input) {
  std::string sanitized = "";
  for (size_t i = 0; i < input.size(); ++i) {
    char c = input[i];
    // Escape single quotes by replacing them with two single quotes
    if (c == '\'') {
      sanitized += "''";
    }
    // Escape backslashes by replacing them with two backslashes
    else if (c == '\\') {
      sanitized += "\\\\";
    }
    // Skip null characters (not allowed in SQL)
    else if (c == '\0') {
      continue;
    }
    // Escape double quotes by adding a backslash
    else if (c == '\"') {
      sanitized += "\\\"";
    }
    // Escape newlines and carriage returns
    else if (c == '\n') {
      sanitized += "\\n";
    } else if (c == '\r') {
      sanitized += "\\r";
    }
    // Escape semicolons
    else if (c == ';') {
      sanitized += "\\;";
    }
    // Escape comment starters '--' and '/*'
    else if (c == '-') {
      if (i + 1 < input.size() && input[i + 1] == '-') {
        sanitized += "\\-\\-"; // Escape comment start "--"
        i++;                   // Skip the next character
      } else {
        sanitized += c;
      }
    } else if (c == '/') {
      if (i + 1 < input.size() && input[i + 1] == '*') {
        sanitized += "\\/\\*"; // Escape comment start "/*"
        i++;                   // Skip the next character
      } else {
        sanitized += c;
      }
    } else {
      sanitized += c; // Append all other characters unchanged
    }
  }
  return sanitized;
}
bool Database::insertData(const std::string &table,
                          const std::vector<std::string> &columns,
                          const std::vector<std::string> &values) {
  if (columns.size() != values.size()) {
    std::cerr << "Columns and values size mismatch!" << std::endl;
    return false;
  }

  // Construct the SQL INSERT statement
  std::string sql = "INSERT INTO " + table + " (";
  for (size_t i = 0; i < columns.size(); ++i) {
    sql += columns[i];
    if (i < columns.size() - 1)
      sql += ", ";
  }
  sql += ") VALUES (";

  for (size_t i = 0; i < values.size(); ++i) {
    std::string value =
        escapeSpecialChars(values[i]); // Escape any special characters
    sql += "'" + value + "'";
    if (i < values.size() - 1)
      sql += ", ";
  }
  sql += ");";

  // Execute the SQL statement
  char *errMsg = nullptr;
  int rc = sqlite3_exec(db, sql.c_str(), nullptr, nullptr, &errMsg);
  if (rc != SQLITE_OK) {
    std::cerr << "SQL error: " << errMsg << std::endl;
    sqlite3_free(errMsg);
    return false;
  }
  return true;
}

bool Database::updateData(const string &table, const vector<string> &columns,
                          const vector<string> &values,
                          const string &condition) {
  if (columns.size() != values.size()) {
    std::cerr << "Error: Columns and values size mismatch." << std::endl;
    return false;
  }
  // Construct the SQL UPDATE query dynamically
  std::string sql = "UPDATE " + table + " SET ";
  // Add the column-value assignments to the SET clause
  for (size_t i = 0; i < columns.size(); ++i) {
    sql += columns[i] + " = ?";
    if (i < columns.size() - 1) {
      sql += ", "; // Add a comma for all but the last column
    }
  }
  // Add the condition (WHERE clause) if provided
  if (!condition.empty()) {
    sql += " WHERE " + condition;
  }
  sqlite3_stmt *stmt;
  if (sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
    std::cerr << "Failed to prepare statement: " << sqlite3_errmsg(db)
              << std::endl;
    return false;
  }
  // Bind the values to the prepared statement
  for (size_t i = 0; i < values.size(); ++i) {
    sqlite3_bind_text(stmt, static_cast<int>(i + 1), values[i].c_str(), -1,
                      SQLITE_STATIC);
  }
  // Execute the statement
  int rc = sqlite3_step(stmt);
  if (rc != SQLITE_DONE) {
    std::cerr << "Failed to execute statement: " << sqlite3_errmsg(db)
              << std::endl;
    sqlite3_finalize(stmt);
    return false;
  }
  // Finalize the statement
  sqlite3_finalize(stmt);
  return true;
}

bool Database::createSchema() {
  // Enable foreign keys support
  if (!this->execute("PRAGMA foreign_keys = ON;"))
    return false;
  if (!this->execute( // user information
          "CREATE TABLE IF NOT EXISTS users ("
          "id INTEGER  PRIMARY KEY AUTOINCREMENT,"
          "email TEXT NOT NULL UNIQUE,"
          "password TEXT NOT NULL,"
          "name TEXT NOT NULL,"
          "phoneNumber TEXT NOT NULL UNIQUE,"
          "country TEXT NOT NULL,"
          "city TEXT NOT NULL,"
          "accountType INTEGER NOT NULL"
          ");"))
    return false;
  if (!this->execute( // Merchant information
          "CREATE TABLE IF NOT EXISTS merchant ("
          "merchantId INTEGER PRIMARY KEY AUTOINCREMENT,"
          "userId INTEGER NOT NULL UNIQUE,"
          "cardId INTEGER NOT NULL,"
          "businessName TEXT NOT NULL,"
          "businessType INTEGER NOT NULL,"
          "keywords TEXT NOT NULL,"
          "pickupAddress TEXT NOT NULL,"
          "nationalID TEXT NOT NULL,"
          "rating REAL NOT NULL,"
          "profileImgId INTEGER,"
          "FOREIGN KEY(cardId) REFERENCES card(cardId),"
          "FOREIGN KEY(userId) REFERENCES users(id),"
          "FOREIGN KEY(profileImgId) REFERENCES merchantImages(imageId) ON DELETE SET NULL);"))
    return false;
	  
  if (!this->execute( // Courier information
          "CREATE TABLE IF NOT EXISTS courier (courierId INTEGER PRIMARY KEY "
          "AUTOINCREMENT ,"
          "userId INTEGER NOT NULL,"
          "vehicleType TEXT NOT NULL ,"
          "nationalID TEXT NOT NULL,"
          "cardId INTEGER NOT NULL,"
          "FOREIGN KEY(cardId) REFERENCES card(cardId) , FOREIGN "
          "KEY(courierId) "
          "REFERENCES users(id));"))
    return false;
  if (!this->execute( // Card information
          "CREATE TABLE IF NOT EXISTS card ("
          "cardId INTEGER PRIMARY KEY AUTOINCREMENT,"
          "userId INTEGER,"
          "cardNumber TEXT NULL,"
          "CVV INTEGER NULL,"
          "expiryDate TEXT NULL,"
          "FOREIGN KEY(userId) REFERENCES users(id));"))
    return false;
  if (!this->execute( // customer infromation
          "CREATE TABLE IF NOT EXISTS customer ("
          "customerId  INTEGER PRIMARY KEY AUTOINCREMENT,"
          "deliveryAddress  TEXT NOT NULL,"
          "userId INTEGER NOT NULL UNIQUE,"
          "cardId INTEGER,"
          "FOREIGN KEY (userId) REFERENCES users (id)"
          "ON UPDATE CASCADE,"
          "FOREIGN KEY (cardId) REFERENCES card (cardId)"
          "ON UPDATE CASCADE"
          ");"))
    return false;
  if (!this->execute( // Item information
          "CREATE TABLE IF NOT EXISTS item ("
          "itemId INTEGER PRIMARY KEY AUTOINCREMENT,"
          "merchantId INTEGER NOT NULL,"
          "itemName TEXT NOT NULL,"
          "itemPrice REAL NOT NULL,"
          "itemDescription TEXT,"
          "imageId INTEGER NOT NULL,"
          "FOREIGN KEY (merchantId) REFERENCES merchant(merchantId),"
          "FOREIGN KEY (imageId) REFERENCES itemImages(imageId)"
          ");"))
    return false;
  if (!this->execute( // itemImage information
          "CREATE TABLE IF NOT EXISTS itemImages ("
          "imageId INTEGER PRIMARY KEY AUTOINCREMENT,"
          "merchantId INTEGER NOT NULL,"
          "itemImg BLOB NOT NULL,"
          "FOREIGN KEY (merchantId) REFERENCES merchant(merchantId)"
          ");"))
    return false;
  if (!this->execute( // customerImage information
    "CREATE TABLE IF NOT EXISTS customerImage ("
    "customerId INTEGER NOT NULL,"
    "customerImage BLOB NOT NULL,"
    "FOREIGN KEY(customerId) REFERENCES customer(customerId)"
    ");"))return false;
  if (!this->execute( // cart information
    " CREATE TABLE IF NOT EXISTS cart ( "
    " cartId INTEGER PRIMARY KEY AUTOINCREMENT, "
    " userId INTEGER NOT NULL " //  each cart belongs to a user
    ");")) return false;
  if (!this->execute( // cartItems information
    " CREATE TABLE IF NOT EXISTS cartItems ( "
    " cartId INTEGER NOT NULL, "      // Foreign key to Cart table
    " itemId INTEGER NOT NULL, "      // Foreign key to Items table
    " quantity INTEGER NOT NULL, "    // Quantity of this item in the cart
    " PRIMARY KEY (cartId, itemId), "   // Composite primary key
    " FOREIGN KEY (cartId) REFERENCES cart(cartId) ON DELETE CASCADE, "
    " FOREIGN KEY (itemId) REFERENCES item(itemId) ON DELETE CASCADE "
    " );"))return false;
  // Other tables
  if (!this->execute(
        "CREATE TABLE IF NOT EXISTS orders ("
        "orderId INTEGER PRIMARY KEY AUTOINCREMENT,"
        "createdAt DATETIME DEFAULT CURRENT_TIMESTAMP," // Timestamp for record creation
        "customerId INTEGER NOT NULL,"
        "merchantId INTEGER NOT NULL,"
        "totalAmount REAL NOT NULL,"
        "orderStatus TEXT DEFAULT 'active',"            // 'active', 'completed', or 'canceled'
        "assignedCourierId INTEGER,"                    //Courier who accepted the order, NULL if not accepted yet
        "FOREIGN KEY (customerId) REFERENCES customer(customerId),"
        "FOREIGN KEY (merchantId) REFERENCES merchant(merchantId)"
        ");"))
    return false;

  if (!this->execute( // order items
      "CREATE TABLE IF NOT EXISTS orderItems ("
      "orderItemId INTEGER PRIMARY KEY AUTOINCREMENT,"
      "orderId INTEGER NOT NULL,"
      "itemId INTEGER NOT NULL,"
      "quantity INTEGER NOT NULL,"
      "FOREIGN KEY (itemId) REFERENCES item(itemId),"
      "FOREIGN KEY (orderId) REFERENCES orders(orderId)"
      ");"))
    return false;
  if (!this->execute( // merchantImage information
          "CREATE TABLE IF NOT EXISTS merchantImages ("
          "imageId INTEGER PRIMARY KEY AUTOINCREMENT,"
          "merchantId INTEGER,"
          "profileImg BLOB NOT NULL,"
          "FOREIGN KEY (merchantId) REFERENCES merchant(merchantId) ON DELETE CASCADE"
          ");"))
    return false;
  
  return true;
}
