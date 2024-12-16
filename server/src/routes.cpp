  #include "../include/routes.hpp"
#include "../include/database.hpp"

#include <crow/common.h>
#include <crow/http_response.h>
#include <crow/multipart.h>
#include <crow/utility.h>
#include <string>
using namespace std;

extern crow::SimpleApp server;
extern Database db;

void helloServer() {
  CROW_ROUTE(server, "/")([]() { return "Hello from server!"; });
}
void authClient() {
  CROW_ROUTE(server, "/login")
      .methods(crow::HTTPMethod::POST)([](const crow::request &req) {
        auto body = crow::json::load(req.body);
        if (!body || !body.has("email") || !body.has("password")) {
          return crow::response(400, "Invalid request payload");
        }
        string email = body["email"].s();
        string password = body["password"].s();
        const string sql =
            "SELECT password FROM users WHERE email = '" + email + "';";
        vector<vector<string>> ans = db.query(sql);
        if (ans.empty() || password != ans[0][0]) {
          return crow::response(401, "Invalid username or password");
        }
        vector<vector<string>> getId =
            db.query("SELECT id FROM users WHERE email = '" + email + "';");
        int userId = stoi(getId[0][0]);
        const string sql2 =
            "SELECT accountType FROM users WHERE id = " + to_string(userId) +
            ";";
        vector<vector<string>> ans2 = db.query(sql2);
        if (ans2.empty()) {
          return crow::response(500, "Error get account type");
        }
        crow::json::wvalue responseBody;
        responseBody["userId"] = userId;
        responseBody["accountType"] = stoi(ans2[0][0]);
        cout << "Account Type : " << ans2[0][0] << endl;
        return crow::response(200, responseBody);
      });
}
void merchantRegistration() {
  CROW_ROUTE(server, "/registration/merchant")
      .methods("POST"_method)([](const crow::request &req) {
        auto body = crow::json::load(req.body);
        if (!body) {
          return crow::response(400, "Invalid JSON payload");
        }
        // Validate all required keys before accessing them
        if (!body.has("phone") || !body.has("email") || !body.has("name") ||
            !body.has("password") || !body.has("country") ||
            !body.has("city") || !body.has("accountType") ||
            !body.has("businessName") || !body.has("businessType") ||
            !body.has("keywords") || !body.has("pickupAddress") ||
            !body.has("nationalID") || !body.has("cardNumber") ||
            !body.has("expiryDate") || !body.has("CVV")) {
          return crow::response(400,
                                "Missing required fields in the JSON payload");
        }
        // get data from json
        string phoneNumber = body["phone"].s();
        string email = body["email"].s();
        // Check account already exists
        const string sql = "SELECT * FROM users WHERE email = '" + email + "' OR phoneNumber = '" + phoneNumber + "' ;";
        vector<vector<string>> ans = db.query(sql);
        if (!ans.empty()) {
          return crow::response(409, "User is already registered");
        }
        // get other user data from json
        string name = body["name"].s();
        string password = body["password"].s();
        string country = body["country"].s();
        string city = body["city"].s();
        int accountType = body["accountType"].i();
        // get the merchant data from json
        string businessName = body["businessName"].s();
        int businessType = body["businessType"].i();
        string keywords = body["keywords"].s();
        string pickupAddress = body["pickupAddress"].s();
        string nationalId = body["nationalID"].s();
        // get the card data from json
        string cardNumber = body["cardNumber"].s();
        string expiryDate = body["expiryDate"].s();
        int CVV = body["CVV"].i();

        // insert user to database
        if (!db.insertData("users",
                           {"email", "password", "name", "phoneNumber",
                            "country", "city", "accountType"},
                           {email, password, name, phoneNumber, country, city,
                            to_string(accountType)})) {
          return crow::response(500, "Error inserting user to database");
        }
        // get the ID of the just added user
        const string sql2 =
            "SELECT id FROM users WHERE email = '" + email + "';";
        vector<vector<string>> ans2 = db.query(sql2);
        if (ans2.empty()) {
          return crow::response(
              500, "Error retrieving userId after added to database");
        }
        int userId = stoi(ans2[0][0]);
        // insert card to the database
        if (!db.insertData(
                "card", {"userId", "cardNumber", "CVV", "expiryDate"},
                {to_string(userId), cardNumber, to_string(CVV), expiryDate})) {
          return crow::response(500, "Error inserting card to database");
        }

        // get the ID of the just added card
        const string sql3 =
            "SELECT cardId FROM card WHERE cardNumber = '" + cardNumber + "';";
        vector<vector<string>> ans3 = db.query(sql);
        if (ans3.empty()) {
          return crow::response(500, "Error retrieving cardId from database");
        }
        string cardId = ans3[0][0];
        // insert merchant to database
        if (!db.insertData("merchant",
                           {"userId", "cardId", "businessName", "businessType",
                            "keywords", "pickupAddress", "nationalID",
                            "rating"},
                           {to_string(userId), cardId, businessName,
                            to_string(businessType), keywords, pickupAddress,
                            nationalId, "0"})) {
          return crow::response(500, "Error inserting merchant to database");
        }
        return crow::response(200, "Merchant registered successfully");
      });
}

void customerRegistration() {
  CROW_ROUTE(server, "/registration/customer")
      .methods("POST"_method)([](const crow::request &req) {
        auto body = crow::json::load(req.body);
        if (!body) {
          return crow::response(400, "Invalid JSON payload");
        }
        // Validate all required keys before accessing them
        if (!body.has("phone") || !body.has("email") || !body.has("name") ||
            !body.has("password") || !body.has("country") ||
            !body.has("city") || !body.has("accountType") ||
            !body.has("deliveryAddress")) {
          return crow::response(400,
                                "Missing required fields in the JSON payload");
        }
        // get data from json
        string phoneNumber = body["phone"].s();
        string email = body["email"].s();
        // Check account already exists
        const string sql = "SELECT * FROM users WHERE email = '" + email + "' OR phoneNumber = '" + phoneNumber + "' ;";
        vector<vector<string>> ans = db.query(sql);
        if (!ans.empty()) {
          return crow::response(409, "User is already registered");
        }
        // get other user data from json
        string name = body["name"].s();
        string password = body["password"].s();
        string country = body["country"].s();
        string city = body["city"].s();
        int accountType = body["accountType"].i();
        // get the customer data from json
        string deliveryAddress = body["deliveryAddress"].s();
        // insert user to database
        if (!db.insertData("users",
                           {"email", "password", "name", "phoneNumber",
                            "country", "city", "accountType"},
                           {email, password, name, phoneNumber, country, city,
                            to_string(accountType)})) {
          return crow::response(500, "Error inserting user to database");
        }
        // get the ID of the just added user
        const string sql2 =
            "SELECT id FROM users WHERE email = '" + email + "';";
        vector<vector<string>> ans2 = db.query(sql2);
        if (ans2.empty()) {
          return crow::response(
              500, "Error retrieving userId after added to database");
        }
        int userId = stoi(ans2[0][0]);
        // insert customer to database
        if (!db.insertData("customer", {"deliveryAddress", "userId"},
                           {deliveryAddress, to_string(userId)})) {
          return crow::response(500, "Error inserting customer to database");
        }
        return crow::response(200, "Customer registered successfully");
      });
}
void courierRegistration() {
  CROW_ROUTE(server, "/registration/courier")
      .methods("POST"_method)([](const crow::request &req) {
        auto body = crow::json::load(req.body);
        if (!body) {
          return crow::response(400, "Invalid JSON payload");
        }
        // Validate all required keys before accessing them
        if (!body.has("phone") || !body.has("email") || !body.has("name") ||
            !body.has("password") || !body.has("country") ||
            !body.has("city") || !body.has("accountType") ||
            !body.has("vehicleType") || !body.has("nationalID") ||
            !body.has("cardNumber") || !body.has("expiryDate") ||
            !body.has("CVV")) {
          return crow::response(400,
                                "Missing required fields in the JSON payload");
        }
        // get data from json
        string phoneNumber = body["phone"].s();
        string email = body["email"].s();
        // Check account already exists
        const string sql = "SELECT * FROM users WHERE email = '" + email + "' OR phoneNumber = '" + phoneNumber + "' ;";
        vector<vector<string>> ans = db.query(sql);
        if (!ans.empty()) {
          return crow::response(409, "User is already registered");
        }
        // get other user data from json
        string name = body["name"].s();
        string password = body["password"].s();
        string country = body["country"].s();
        string city = body["city"].s();
        int accountType = body["accountType"].i();
        // get the courier data from json
        int vehicleType = body["vehicleType"].i();
        string nationalID = body["nationalID"].s();
        string cardNumber = body["cardNumber"].s();
        string expiryDate = body["expiryDate"].s();
        int CVV = body["CVV"].i();
        // insert user to database
        if (!db.insertData("users",
                           {"email", "password", "name", "phoneNumber",
                            "country", "city", "accountType"},
                           {email, password, name, phoneNumber, country, city,
                            to_string(accountType)})) {
          return crow::response(500, "Error inserting user to database");
        }
        // get the ID of the just added user
        const string sql2 =
            "SELECT id FROM users WHERE email = '" + email + "';";
        vector<vector<string>> ans2 = db.query(sql2);
        if (ans2.empty()) {
          return crow::response(
              500, "Error retrieving userId after added to database");
        }
        int userId = stoi(ans2[0][0]);
        // insert card to the database
        if (!db.insertData(
                "card", {"userId", "cardNumber", "CVV", "expiryDate"},
                {to_string(userId), cardNumber, to_string(CVV), expiryDate})) {
          return crow::response(500, "Error inserting card to database");
        }
        // get the ID of the just added card
        const string sql3 =
            "SELECT cardId FROM card WHERE cardNumber = '" + cardNumber + "';";
        vector<vector<string>> ans3 = db.query(sql);
        if (ans3.empty()) {
          return crow::response(500, "Error retrieving cardId from database");
        }
        string cardId = ans3[0][0];

        // insert courier to database
        if (!db.insertData("courier",
                           {"userId", "cardId", "vehicleType", "nationalID"},
                           {to_string(userId), cardId, to_string(vehicleType),
                            nationalID})) {
          return crow::response(500, "Error inserting customer to database");
        }
        return crow::response(200, "Courier registered successfully");
      });
}

void uploadImage() {
  CROW_ROUTE(server, "/uploadImage/<int>")
      .methods(crow::HTTPMethod::POST)([](const crow::request &req,
                                          const int &userId) {
        try {
          vector<vector<string>> getId =
              db.query("SELECT merchantId FROM merchant WHERE userId = '" +
                       to_string(userId) + "';");
          if (getId.empty()) {
            return crow::response(500, "Error getting merchantId");
          }
          int merchantId = stoi(getId[0][0]);
          sqlite3_stmt *stmt;
          // SQL query to insert the image
          const char *sql =
              "INSERT INTO itemImages (merchantId, itemImg) VALUES (?1, ?2);";
          if (sqlite3_prepare_v2(db.getDB(), sql, -1, &stmt, nullptr) !=
              SQLITE_OK) {
            std::cerr << "Failed to prepare statement: "
                      << sqlite3_errmsg(db.getDB()) << std::endl;
            sqlite3_close(db.getDB());
            return crow::response(500, "Error uploading image");
          }
          try {
            // Bind the merchantId parameter
            if (sqlite3_bind_int(stmt, 1, merchantId) != SQLITE_OK) {
              std::cerr << "Failed to bind merchantId: "
                        << sqlite3_errmsg(db.getDB()) << std::endl;
              sqlite3_finalize(stmt);
              sqlite3_close(db.getDB());
              return crow::response(500, "Error uploading image");
            }

            // Bind the image data (BLOB)
            if (sqlite3_bind_blob(stmt, 2, req.body.data(), req.body.size(),
                                  SQLITE_STATIC) != SQLITE_OK) {
              std::cerr << "Failed to bind image data: "
                        << sqlite3_errmsg(db.getDB()) << std::endl;
              sqlite3_finalize(stmt);
              sqlite3_close(db.getDB());
              return crow::response(500, "Error uploading image");
            }
            // Execute the statement
            if (sqlite3_step(stmt) != SQLITE_DONE) {
              std::cerr << "Failed to execute statement: "
                        << sqlite3_errmsg(db.getDB()) << std::endl;
              sqlite3_finalize(stmt);
              sqlite3_close(db.getDB());
              return crow::response(500, "Error uploading image");
            }

          } catch (const std::exception &ex) {
            std::cerr << "Error: " << ex.what() << std::endl;
          }
          // Finalize and close
          sqlite3_finalize(stmt);

          // Save received binary data as an image file (optional for testing)
          std::ofstream outFile("received_image.jpg", std::ios::binary);
          outFile.write(req.body.c_str(), req.body.size());
          outFile.close();

          std::cout << "Image received and saved successfully." << std::endl;

          return crow::response(200, "Image received successfully.");

        } catch (const std::exception &e) {
          return crow::response(500, std::string("Error: ") + e.what());
        }
      });
}

void addItem() {
  CROW_ROUTE(server, "/add_item/<int>")
      .methods("POST"_method)([](const crow::request &req, const int &userId) {
        auto body = crow::json::load(req.body);
        if (!body) {
          return crow::response(400, "Invalid JSON payload");
        }
        // Validate all required keys before accessing them
        if (!body.has("itemName") || !body.has("itemDescription") ||
            !body.has("itemPrice")) {
          return crow::response(400,
                                "Missing required fields in the JSON payload");
        }
        // get item data from json
        string itemName = body["itemName"].s();
        string itemDescription = body["itemDescription"].s();
        string itemPrice = body["itemPrice"].s();

        // get merchantId
        vector<vector<string>> merchantId_query =
            db.query("SELECT merchantId FROM merchant WHERE userId = " +
                     to_string(userId) + ";");
        if (merchantId_query.empty()) {
          cerr << "Merchant Id query is empty" << endl;
          return crow::response(500, "Error getting merchantId from userId");
        }
        int merchantId = stoi(merchantId_query[0][0]);
        vector<vector<string>> getItemImageId;

        // get Item Image Id
        getItemImageId =
            db.query("SELECT imageId FROM itemImages WHERE merchantId = " +
                     to_string(merchantId) + " ORDER BY imageId DESC LIMIT 1;");
        if (getItemImageId.empty()) {
          std::cerr << "Error getting imageId" << std::endl;
          return crow::response(500, "Error getting imageId");
        }
        int imageId = stoi(getItemImageId[0][0]);

        // insert item data to database
        if (!db.execute("INSERT INTO item (merchantId, itemName, itemPrice, "
                        "itemDescription, imageId) VALUES ('" +
                        to_string(merchantId) + "', '" + itemName + "', '" +
                        itemPrice + "', '" + itemDescription + "', '" +
                        to_string(imageId) + "' );"))
          return crow::response(500, "Error inserting item data to database");

        return crow::response(200, "Item added successfully");
      });
}

void retrieveItem() {
  CROW_ROUTE(server, "/get_item/<int>")
      .methods("GET"_method)([](const crow::request &req, int itemId) {
        // Validate the item ID
        if (itemId <= 0) {
          return crow::response(400, "Invalid item ID");
        }

        // Query the database for the item
        vector<vector<string>> itemData =
            db.query("SELECT itemName, itemDescription, itemPrice, imageId "
                     "FROM item WHERE itemId = " +
                     to_string(itemId) + ";");

        // Check if the item exists
        if (itemData.empty()) {
          return crow::response(404, "Item not found");
        }

        // Retrieve item details
        const string &itemName = itemData[0][0];
        const string &itemDescription = itemData[0][1];
        const float &itemPrice = stof(itemData[0][2]);
        const string &itemImg = itemData[0][3];

        // Log the size of the image (base64)
        std::cout << "Image size before encoding: " << itemImg.size()
                  << " bytes" << std::endl;

        // Base64 encode the image string
        string encodedImg =
            crow::utility::base64encode(itemImg.c_str(), itemImg.size());

        std::cout << "Image size after encoding: " << encodedImg.size()
                  << " bytes" << std::endl;
        // Construct JSON response
        crow::json::wvalue responseBody;
        responseBody["itemName"] = itemName;
        responseBody["itemDescription"] = itemDescription;
        responseBody["itemPrice"] = itemPrice;
        responseBody["itemImg"] = encodedImg.c_str();

        // Send response
        return crow::response(200, responseBody);
      });
}
void retrieveItemImage() {
  CROW_ROUTE(server, "/get_item_image/<int>")
      .methods("GET"_method)([](const crow::request &req, int itemId) {
        // Validate the item ID
        if (itemId <= 0) {
          return crow::response(400, "Invalid item ID");
        }
        // Query the database for the imageId
        vector<vector<string>> queryImageId =
            db.query("SELECT imageId "
                     "FROM item WHERE itemId = " +
                     to_string(itemId) + ";");
        // Check if the item exists
        if (queryImageId.empty()) {
          return crow::response(404, "Item not found");
        }
        // Retrieve item details
        int imageId = stoi(queryImageId[0][0]);
        vector<vector<string>> itemImage =
            db.query("SELECT itemImg FROM itemImages WHERE imageId = " +
                     to_string(imageId) + " ;");
        if (itemImage.empty()) {
          return crow::response(404, "Image not found");
        }
        crow::response res;
        res.set_header("Content-Type", "image/jpeg");
        res.write(itemImage[0][0]);
        return res;
      });
}

void deleteItem() {
  CROW_ROUTE(server, "/delete_item/<int>")
      .methods("GET"_method)([](const crow::request &req, int itemId) {
        // Validate the item ID
        if (itemId <= 0) {
          return crow::response(400, "Invalid item ID");
        }
        // Query the database for the imageId
        vector<vector<string>> queryImageId = db.query(
            "SELECT imageId FROM item WHERE itemId = " + to_string(itemId) +
            ";");
        // Check if the item exists
        if (queryImageId.empty()) {
          return crow::response(404, "Item not found");
        }
        int imageId = stoi(queryImageId[0][0]);
        // Delete the item first (to avoid foreign key violation)
        string sql =
            "DELETE FROM item WHERE itemId = " + to_string(itemId) + ";";
        if (!db.execute(sql)) {
          return crow::response(500, "Error deleting item");
        }
        // Delete the associated image from itemImages
        sql = "DELETE FROM itemImages WHERE imageId = " + to_string(imageId) +
              ";";
        if (!db.execute(sql)) {
          return crow::response(500, "Error deleting item image");
        }
        return crow::response(200, "success");
      });
}
void getMerchantInfoHome() {
  CROW_ROUTE(server, "/getMerchantInfoHome/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        const string sql =
            "SELECT businessName, keywords, rating FROM merchant "
            "WHERE userId = " +
            to_string(userId) + ";";
        vector<vector<string>> ans = db.query(sql);
        if (ans.empty() || ans[0].size() < 2) {
          return crow::response(500, "Error deleting item");
        }

        const string &name = ans[0][0];
        const string &keywords = ans[0][1];
        float rating = stof(ans[0][2]);
        rating = round(rating * 10) / 10; // 0.1 percision

        crow::json::wvalue responseBody;
        responseBody["businessName"] = name;
        responseBody["keywords"] = keywords;
        responseBody["rating"] = rating;

        return crow::response(200, responseBody);
      });
}

void getItems() {
  CROW_ROUTE(server, "/get_items/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        vector<vector<string>> merchantId_query =
            db.query("SELECT merchantId FROM merchant WHERE " + to_string(userId) +
                     " = merchant.userId;");
        if (merchantId_query.empty())
          return crow::response(500, "Getting merchantId at getItems");
        string merchantId = merchantId_query[0][0];
        const string sql1 =
            "SELECT COUNT (*) FROM item WHERE merchantId = " + merchantId + ";";
        vector<vector<string>> ans = db.query(sql1);

        crow::json::wvalue responseBody;
        crow::json::wvalue::list items;

        responseBody["itemsCount"] = ans[0][0];
        const string sql2 = "SELECT itemId, itemName , itemPrice, "
                            "itemDescription ,imageId FROM item "
                            "WHERE merchantId = " +
                            merchantId + " ;";
        vector<vector<string>> res = db.query(sql2);

        for (auto &row : res) {
          crow::json::wvalue item;
          item["itemId"] = stoi(row[0]);
          item["itemName"] = row[1];
          item["itemPrice"] = stof(row[2]);
          item["itemDescription"] = row[3];
          item["imageId"] = row[4];
          items.push_back(std::move(item));
        }
        responseBody["items"] = std::move(items);
        return crow::response(200, responseBody);
      });
}

void getMerchantData() {
  CROW_ROUTE(server, "/getMerchantData/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        string sql =
            "SELECT businessName, businessType, keywords, pickupAddress, "
            "rating FROM merchant WHERE userId = " +
            to_string(userId) + " ;";
        vector<vector<string>> ans = db.query(sql);
        if (ans.empty() || ans[0].size() < 4) {
          return crow::response(500, "Error getMerchantData");
        }

        string businessName = ans[0][0];
        string type = ans[0][1];
        string keywords = ans[0][2];
        string pickupAddress = ans[0][3];
        float rating = stof(ans[0][4]);
        rating = round(rating * 10) / 10;

        crow::json::wvalue responseBody;
        responseBody["businessName"] = businessName;
        responseBody["keywords"] = keywords;
        responseBody["rating"] = rating;
        responseBody["type"] = type;
        responseBody["pickupAddress"] = pickupAddress;

        return crow::response(200, responseBody);
      });
}
void changePickupAddress() {
  CROW_ROUTE(server, "/changePickupAddress/<int>")
      .methods("POST"_method)([](const crow::request &req, int userId) {
        auto body = crow::json::load(req.body);
        if (!body) {
          return crow::response(400, "Invalid JSON payload");
        }
        string pickupAddress = body["pickupAddress"].s();
        const string condition = "userId = " + to_string(userId);
        int ok = db.updateData("merchant", {"pickupAddress"}, {pickupAddress},
                               condition);
        if (ok)
          return crow::response(200, "Item added successfully");
        return crow::response(500, "Error getMerchantData");
      });
}

void getMerchantsSearchResults()
{
  CROW_ROUTE(server, "/getMerchantsSearchResults/<string>")
     .methods("GET"_method)([](const crow::request &req, const string& searchWord) {
       string sql;
       if (searchWord == "empty"){
         sql = "SELECT businessName, keywords, rating FROM merchant";
       }else{
         sql = "SELECT businessName, keywords, rating FROM merchant "
         "WHERE businessName LIKE '%" + searchWord + "%' OR keywords LIKE '%" + searchWord + "%' "
         "LIMIT 10;";
       }
       vector<vector<string>> ans = db.query(sql);
       crow::json::wvalue responseBody;
       responseBody["count"] = ans.size();
       for(int i=0;i< ans.size();i++){
            string businessName = ans[i][0];
            string keywords = ans[i][1];
            float rating = stof(ans[i][2]);
            rating = round(rating * 10) / 10;
            string rate = to_string(rating);
            responseBody["businessName" + to_string(i)] = businessName;
            responseBody["keywords" + to_string(i)] = keywords;
            responseBody["rating" + to_string(i)] = rate;
       }
       return crow::response(200, responseBody);
     });
}
void getItemsSearchResults(){
  CROW_ROUTE(server, "/getItemsSearchResults/<int>")
    .methods("GET"_method)([](const crow::request &req, int userId) {
      // Validate the item ID
      if (userId <= 0) {
        return crow::response(400, "Invalid user ID");
      }
      vector<vector<string>> merchantId_query =
          db.query("SELECT merchantId FROM merchant WHERE " + to_string(userId) +
                   " = merchant.userId;");
      if (merchantId_query.empty())
        return crow::response(500, "Getting merchantId at getItemsSearchResults");
      string merchantId = merchantId_query[0][0];
      const string sql1 =
          "SELECT COUNT (*) FROM item WHERE merchantId = " + merchantId + ";";
      vector<vector<string>> ans = db.query(sql1);

      crow::json::wvalue responseBody;
      crow::json::wvalue::list items;

      responseBody["itemsCount"] = ans[0][0];
      const string sql2 = "SELECT itemId, itemName , itemPrice, "
                          "itemDescription FROM item "
                          "WHERE merchantId = " +
                          merchantId + " ;";
      vector<vector<string>> res = db.query(sql2);
      for (auto &row : res) {
        crow::json::wvalue item;
        item["itemId"] = stoi(row[0]);
        item["itemName"] = row[1];
        item["itemPrice"] = stof(row[2]);
        item["itemDescription"] = row[3];
        items.push_back(std::move(item));
      }
      responseBody["items"] = std::move(items);
      return crow::response(200, responseBody);
    });
}

void uploadProfileImage() {
  CROW_ROUTE(server, "/uploadProfileImage/<int>")
      .methods(crow::HTTPMethod::POST)([](const crow::request &req,
                                          const int &userId) {
        try {
          vector<vector<string>> getId =
              db.query("SELECT merchantId FROM merchant WHERE userId = '" +
                       to_string(userId) + "';");
          if (getId.empty()) {
            return crow::response(500, "Error getting merchantId");
          }
          int merchantId = stoi(getId[0][0]);
         db.execute("DELETE FROM merchantImages WHERE merchantId = " + to_string(merchantId) + ";");
          sqlite3_stmt *stmt;
          // SQL query to insert the image
          const char *sql =
              "INSERT OR REPLACE INTO merchantImages (merchantId, profileImg) VALUES (?1, ?2);";
          if (sqlite3_prepare_v2(db.getDB(), sql, -1, &stmt, nullptr) !=
              SQLITE_OK) {
            std::cerr << "Failed to prepare statement: "
                      << sqlite3_errmsg(db.getDB()) << std::endl;
            sqlite3_close(db.getDB());
            return crow::response(500, "Error uploading image");
          }
          try {
            // Bind the merchantId parameter
            if (sqlite3_bind_int(stmt, 1, merchantId) != SQLITE_OK) {
              std::cerr << "Failed to bind merchantId: "
                        << sqlite3_errmsg(db.getDB()) << std::endl;
              sqlite3_finalize(stmt);
              sqlite3_close(db.getDB());
              return crow::response(500, "Error uploading image");
            }

            // Bind the image data (BLOB)
            if (sqlite3_bind_blob(stmt, 2, req.body.data(), req.body.size(),
                                  SQLITE_STATIC) != SQLITE_OK) {
              std::cerr << "Failed to bind image data: "
                        << sqlite3_errmsg(db.getDB()) << std::endl;
              sqlite3_finalize(stmt);
              sqlite3_close(db.getDB());
              return crow::response(500, "Error uploading image");
            }
            // Execute the statement
            if (sqlite3_step(stmt) != SQLITE_DONE) {
              std::cerr << "Failed to execute statement: "
                        << sqlite3_errmsg(db.getDB()) << std::endl;
              sqlite3_finalize(stmt);
              sqlite3_close(db.getDB());
              return crow::response(500, "Error uploading image");
            }

          } catch (const std::exception &ex) {
            std::cerr << "Error: " << ex.what() << std::endl;
          }
          vector <vector<string>> getImgId = db.query("SELECT imageId FROM merchantImages WHERE merchantId = " + to_string(merchantId) + ";");
          int imgId = stoi(getImgId[0][0]);
          const string condition = "merchantId = " + to_string(merchantId);
          int ok = db.updateData("merchant",{"profileImgId"},{to_string(imgId)},condition);

          // Finalize and close
          sqlite3_finalize(stmt);

          // Save received binary data as an image file (optional for testing)
          std::ofstream outFile("received_image.jpg", std::ios::binary);
          outFile.write(req.body.c_str(), req.body.size());
          outFile.close();

          std::cout << "Image received and saved successfully." << std::endl;

          return crow::response(200, "Image received successfully.");

        } catch (const std::exception &e) {
          return crow::response(500, std::string("Error: ") + e.what());
        }
      });
}
void getProfileImage() {
  CROW_ROUTE(server, "/get_profile_image/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid item ID");
        }
        // Query the database for the imageId
        vector<vector<string>> queryImageId =
            db.query("SELECT profileImgId "
                     "FROM merchant WHERE userId = " +
                     to_string(userId) + ";");
        // Check if the item exists
        if (queryImageId.empty()) {
          return crow::response(404, "Item not found");
        }
        // Retrieve item details
        int imageId = stoi(queryImageId[0][0]);
        vector<vector<string>> itemImage =
            db.query("SELECT profileImg FROM merchantImages WHERE imageId = " +
                     to_string(imageId) + " ;");
        if (itemImage.empty()) {
          return crow::response(404, "Image not found");
        }
        crow::response res;
        res.set_header("Content-Type", "image/jpeg");
        res.write(itemImage[0][0]);
        return res;
      });
}

