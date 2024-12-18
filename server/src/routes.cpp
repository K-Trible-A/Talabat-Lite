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
        const string sql = "SELECT * FROM users WHERE email = '" + email +
                           "' OR phoneNumber = '" + phoneNumber + "' ;";
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
        vector<vector<string>> ans3 = db.query(sql3);

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
        const string sql = "SELECT * FROM users WHERE email = '" + email +
                           "' OR phoneNumber = '" + phoneNumber + "' ;";
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
        const string sql = "SELECT * FROM users WHERE email = '" + email +
                           "' OR phoneNumber = '" + phoneNumber + "' ;";
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
            db.query("SELECT itemName, itemDescription, itemPrice "
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

        // Construct JSON response
        crow::json::wvalue responseBody;
        responseBody["itemName"] = itemName;
        responseBody["itemDescription"] = itemDescription;
        responseBody["itemPrice"] = itemPrice;

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
        vector<vector<string>> merchantId_query =
            db.query("SELECT merchantId FROM merchant WHERE " +
                     to_string(userId) + " = merchant.userId;");
        if (merchantId_query.empty()) {
          return crow::response(500, "Error getting merchantId");
        }
        int merchantId = stoi(merchantId_query[0][0]);
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
            db.query("SELECT merchantId FROM merchant WHERE userId = " +
                     to_string(userId) + " ;");
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

void getMerchantsSearchResults() {
  CROW_ROUTE(server, "/getMerchantsSearchResults/<string>")
      .methods("GET"_method)(
          [](const crow::request &req, const string &searchWord) {
            string sql;
            if (searchWord == "empty") {

              sql = "SELECT businessName, keywords, rating, merchantId FROM merchant";
            } else {
              sql = "SELECT businessName, keywords, rating FROM merchant "
                    "WHERE businessName LIKE '%" +
                    searchWord + "%' OR keywords LIKE '%" + searchWord +
                    "%' "
                    "LIMIT 10;";
            }
            vector<vector<string>> ans = db.query(sql);
            crow::json::wvalue responseBody;
            responseBody["count"] = ans.size();
            for (int i = 0; i < ans.size(); i++) {
              string businessName = ans[i][0];
              string keywords = ans[i][1];
              float rating = stof(ans[i][2]);
              rating = round(rating * 10) / 10;
              string rate = to_string(rating);
              int merchantId = stoi(ans[i][3]);
              responseBody["businessName" + to_string(i)] = businessName;
              responseBody["keywords" + to_string(i)] = keywords;
              responseBody["rating" + to_string(i)] = rate;
              responseBody["merchantId" + to_string(i)] = merchantId;
            }
            return crow::response(200, responseBody);
          });
}
void getItemsSearchResults() {
  CROW_ROUTE(server, "/getItemsSearchResults/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        vector<vector<string>> merchantId_query =
            db.query("SELECT merchantId FROM merchant WHERE " +
                     to_string(userId) + " = merchant.userId;");
        if (merchantId_query.empty())
          return crow::response(500,
                                "Getting merchantId at getItemsSearchResults");
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
void getCustomerImage() {
  CROW_ROUTE(server, "/customer/getImage/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        vector<vector<string>> temp =
            db.query("SELECT customerId FROM customer WHERE userId = " +
                     to_string(userId) + ";");
        if (temp.empty()) {
          return crow::response(500, "Error getCustomerId");
        }
        int customerId = stoi(temp[0][0]);
        vector<vector<string>> customerImage =
            db.query("SELECT customerImage FROM customerImage "
                     "WHERE customerId = " +
                     to_string(customerId) + " ;");
        if (customerImage.empty()) {
          return crow::response(500, "Error getCustomerImage");
        }

        crow::response res;
        res.set_header("Content-Type", "image/jpeg");
        res.write(customerImage[0][0]);
        return res;
      });
}

void getCustomerData() {
  CROW_ROUTE(server, "/customer/getData/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        const string sql1 =
            "SELECT deliveryAddress FROM customer where userId = " +
            std::to_string(userId) + " ;";
        vector<vector<string>> ans1 = db.query(sql1);
        if (ans1.empty()) {
          return crow::response(500, "Error get customer delivery address");
        }
        const string sql2 =
            "SELECT city FROM users where id = " + std::to_string(userId) +
            " ;";
        vector<vector<string>> ans2 = db.query(sql2);
        if (ans2.empty()) {
          return crow::response(500, "Error get customer city");
        }
        string customerAddress = ans1[0][0];
        string customerCity = ans2[0][0];

        crow::json::wvalue responseBody;
        responseBody["city"] = customerCity;
        responseBody["customerAddress"] = customerAddress;

        return crow::response(200, responseBody);
      });
}

void setCustomerData() {
  CROW_ROUTE(server, "/customer/setData/<int>")
      .methods("POST"_method)([](const crow::request &req, int userId) {
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }

        auto body = crow::json::load(req.body);
        string city = body["city"].s();
        string deliveryAddress = body["address"].s();

        const string condition1 = "userId = " + to_string(userId);
        const string condition2 = "id = " + to_string(userId);

        if (!db.updateData("customer", {"deliveryAddress"}, {deliveryAddress},
                           condition1)) {
          return crow::response(500, "Error setting customer address");
        }
        if (!db.updateData("users", {"city"}, {city}, condition2)) {
          return crow::response(500, "Error setting customer city");
        }

        return crow::response(200, "Customer data updated");
      });
}

void deleteCustomerImageIfExists(int customerId) {
  vector<vector<string>> res =
      db.query("SELECT customerImage FROM customerImage "
               "WHERE customerId = " +
               to_string(customerId) + " ;");
  if (!res.empty()) {
    const string sql = "DELETE FROM customerImage WHERE customerId = " +
                       to_string(customerId) + ";";
    db.execute(sql);
  }
}

void addCustomerImage() {
  CROW_ROUTE(server, "/customer/uploadImage/<int>")
      .methods(crow::HTTPMethod::POST)([](const crow::request &req,
                                          const int &userId) {
        try {
          vector<vector<string>> temp =
              db.query("SELECT customerId FROM customer WHERE userId = " +
                       to_string(userId) + ";");
          int customerId = stoi(temp[0][0]);
          if (temp.empty()) {
            return crow::response(500, "Error getting adding customer image");
          }
          string sql = "INSERT INTO customerImage (customerId, "
                       " customerImage ) "
                       "VALUES (?1, ?2);";
          deleteCustomerImageIfExists(customerId);
          sqlite3_stmt *stmt = nullptr;
          // Prepare the SQL statement
          if (sqlite3_prepare_v2(db.getDB(), sql.c_str(), -1, &stmt, nullptr) !=
              SQLITE_OK) {
            std::cerr << "SQL prepare error: " << sqlite3_errmsg(db.getDB())
                      << std::endl;
            return crow::response(500, "Error getting adding customer image");
          }

          // Bind values to the placeholders
          if (sqlite3_bind_int(stmt, 1, customerId) != SQLITE_OK ||
              sqlite3_bind_blob(stmt, 2, req.body.c_str(), req.body.size(),
                                SQLITE_STATIC) != SQLITE_OK) {
            std::cerr << "SQL bind error: " << sqlite3_errmsg(db.getDB())
                      << std::endl;
            sqlite3_finalize(stmt);
            return crow::response(500, "Error getting adding customer image");
          }

          // Execute the statement
          if (sqlite3_step(stmt) != SQLITE_DONE) {
            std::cerr << "SQL step error: " << sqlite3_errmsg(db.getDB())
                      << std::endl;
            sqlite3_finalize(stmt);
            return crow::response(500, "Error getting adding customer image");
          }
          // Clean up
          sqlite3_finalize(stmt);

          return crow::response(200, "Image received successfully.");

        } catch (const std::exception &e) {
          return crow::response(500, std::string("Error: ") + e.what());
        }
      });
}
void getTopRatedMerchants() {
  CROW_ROUTE(server, "/customer/getTopRatedMerchants/<int>")
      .methods(crow::HTTPMethod::GET)([](const crow::request &req,
                                         const int &userId) {
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        const string sql = "SELECT "
                           "merchant.businessName, "
                           "merchant.rating, "
                           "merchant.merchantId "
                           "FROM "
                           "merchant "
                           "JOIN "
                           "users "
                           "ON merchant.userId = users.id "
                           "WHERE "
                           "users.city = (SELECT city FROM users WHERE id = " +
                           std::to_string(userId) +
                           ") ORDER BY "
                           "merchant.rating DESC LIMIT 3;";
        vector<vector<string>> ans = db.query(sql);
        string merch_1_Name = "there is no other merchants", merch_1_Rate = "0",
               merch_2_Name = "there is no other merchants", merch_2_Rate = "0";
        string merch_3_Name = "there is no other merchants", merch_3_Rate = "0";
        int merch1_id = 0, merch2_id = 0, merch3_id = 0;

        if (ans.size() >= 1) {
          merch_1_Name = ans[0][0];
          merch_1_Rate = ans[0][1];
          merch1_id = stoi(ans[0][2]);
        }
        if (ans.size() >= 2) {
          merch_2_Name = ans[1][0];
          merch_2_Rate = ans[1][1];
          merch2_id = stoi(ans[1][2]);
        }
        if (ans.size() >= 3) {
          merch_3_Name = ans[2][0];
          merch_3_Rate = ans[2][1];
          merch3_id = stoi(ans[2][2]);
        }
        crow::json::wvalue responseBody;
        responseBody["Name1"] = merch_1_Name;
        responseBody["Name2"] = merch_2_Name;
        responseBody["Name3"] = merch_3_Name;

        responseBody["Rate1"] = merch_1_Rate;
        responseBody["Rate2"] = merch_2_Rate;
        responseBody["Rate3"] = merch_3_Rate;

        responseBody["Id1"] = merch1_id;
        responseBody["Id2"] = merch2_id;
        responseBody["Id3"] = merch3_id;

        return crow::response(200, responseBody);
      });
}

void getCartItems() {
  CROW_ROUTE(server, "/customer/getCartItems/<int>")
      .methods(crow::HTTPMethod::GET)([](const crow::request &req,
                                         const int &userId) {
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        vector<vector<string>> cartId_query = db.query(
            "SELECT cartId FROM cart WHERE userId =" + to_string(userId) +
            " ;");
        if (cartId_query.empty()) {
          return crow::response(400, "Error getting cart items");
        }
        string cartId = cartId_query[0][0];

        const string sql1 =
            "SELECT COUNT (*) FROM cartItems WHERE cartId = " + cartId + ";";
        vector<vector<string>> ans = db.query(sql1);

        if (ans.empty()) {
          return crow::response(400, "Error getting cart items");
        }
        int cartitemCount = stoi(ans[0][0]);

        crow::json::wvalue responseBody;
        crow::json::wvalue::list items;
        responseBody["cartItemCount"] = cartitemCount;
        const string sql_all =
            "SELECT i.itemName, ci.quantity, i.itemPrice, i.merchantId "
            ",(ci.Quantity "
            "* i.itemPrice) AS TotalPrice ,i.imageId ,ci.itemId "
            "FROM cartItems ci "
            "JOIN item i ON ci.itemId = i.itemId "
            "WHERE ci.cartId = " +
            cartId + "; ";
        vector<vector<string>> ans_all = db.query(sql_all);
        if (ans_all.empty()) {
          return crow::response(400, "Error getting cart items");
        }
        for (auto &it : ans_all) {
          crow::json::wvalue item;
          item["itemName"] = it[0];
          item["itemCount"] = stoi(it[1]);
          item["itemPrice"] = round(stof(it[2]) * 10) / 10;
          string merchantId = it[3];
          const string sql_merch =
              "SELECT businessName FROM merchant WHERE merchantId =" +
              merchantId + " ;";
          vector<vector<string>> ans_merch = db.query(sql_merch);
          item["merchName"] = ans_merch[0][0];
          item["TotalPrice"] = round(stof(it[4]) * 10) / 10;
          item["imageId"] = it[5];
          item["itemId"] = stoi(it[6]);
          items.push_back(std::move(item));
        }
        responseBody["items"] = std::move(items);
        return crow::response(200, responseBody);
      });
}

void getCategory() {
  CROW_ROUTE(server, "/customer/getCategory/<int>")
      .methods(crow::HTTPMethod::GET)(
          [](const crow::request &req, const int &businessType) {
            const string sql1 =
                "SELECT COUNT (*) FROM merchant WHERE businessType = " +
                std::to_string(businessType) + ";";
            vector<vector<string>> ans = db.query(sql1);
            if (ans.empty()) {
              return crow::response(400, "Error getting merchants");
            }

            crow::json::wvalue responseBody;
            crow::json::wvalue::list merchants;
            responseBody["merchantCount"] = stoi(ans[0][0]);

            const string sql2 =
                "SELECT merchantId, businessName , rating FROM merchant "
                "WHERE businessType = " +
                std::to_string(businessType) + ";";
            vector<vector<string>> res = db.query(sql2);
            if (res.empty())
              return crow::response(400, "Error getting merchants");

            string merchName;

            for (auto &it : res) {
              crow::json::wvalue merchant;
              merchant["merchId"] = stoi(it[0]);
              merchant["merchName"] = it[1];
              merchant["merchRate"] = round(stof(it[2]) * 10) / 10;
              merchants.push_back(std::move(merchant));
            }
            responseBody["merchants"] = std::move(merchants);

            return crow::response(200, responseBody);
          });
}

void addCustomerCard() {
  CROW_ROUTE(server, "/customer/addCard/<int>")
      .methods(crow::HTTPMethod::POST)([](const crow::request &req,
                                          const int &userId) {
        auto body = crow::json::load(req.body);

        string cardNumber = body["cardNumber"].s();
        string expiryDate = body["expiryDate"].s();
        string CVV = body["CVV"].s();
        string tableName = "card";
        vector<string> columns = {"userId", "cardNumber", "CVV", "expiryDate"};
        vector<string> values = {to_string(userId), cardNumber, CVV,
                                 expiryDate};
        db.insertData(tableName, columns, values);

        return crow::response(200, "Card added");
      });
}
void customerGetItems() {
  CROW_ROUTE(server, "/customer/get_items/<int>")
      .methods("GET"_method)([](const crow::request &req, int merchantId) {
        const string sql1 = "SELECT COUNT (*) FROM item WHERE merchantId = " +
                            to_string(merchantId) + ";";
        vector<vector<string>> ans = db.query(sql1);

        crow::json::wvalue responseBody;
        crow::json::wvalue::list items;

        responseBody["itemsCount"] = ans[0][0];
        const string sql2 = "SELECT itemId, itemName , itemPrice, "
                            "itemDescription ,imageId FROM item "
                            "WHERE merchantId = " +
                            to_string(merchantId) + " ;";
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
void addItemToCart() {
  CROW_ROUTE(server, "/cart/add_item/<int>")
      .methods("POST"_method)([](const crow::request &req, const int &userId) {
        auto body = crow::json::load(req.body);
        if (!body || userId <= 0) {
          return crow::response(400, "Invalid JSON payload");
        }

        // get item data from json
        int itemId = body["itemId"].i();
        int itemQuantity = body["itemCount"].i();

        // check if customer already has cart
        vector<vector<string>> temp = db.query(
            "SELECT cartId FROM cart WHERE userId = " + to_string(userId) +
            ";");
        int cartId;
        if (temp.empty()) {
          vector<string> columns = {"userId"};
          string tableName = "cart";
          vector<string> values = {to_string(userId)};
          db.insertData(tableName, columns, values);
          temp = db.query("SELECT cartId FROM cart WHERE userId = " +
                          to_string(userId) + ";");
          cartId = stoi(temp[0][0]);
        } else {
          cartId = stoi(temp[0][0]);
        }
        // get merchant of this item
        const string getMerch = "SELECT "
                                "id "
                                "FROM "
                                "users "
                                "JOIN "
                                "merchant "
                                "ON merchant.userId = users.id "
                                "WHERE "
                                "merchant.merchantId = (SELECT merchantId FROM "
                                "item WHERE itemId = " +
                                std::to_string(itemId) + ") ;";
        vector<vector<string>> answer = db.query(getMerch);
        int merchId = stoi(answer[0][0]);
        crow::json::wvalue responseBody;
        responseBody["merchId"] = merchId;
        // check if merchant is the same of items in cart
        vector<vector<string>> sql2 =
            db.query("SELECT itemId FROM cartItems WHERE "+ to_string(cartId) + " = cartItems.cartId LIMIT 1;");
        if (!sql2.empty())
        {
            string existingItemId = sql2[0][0];
            vector<vector<string>> sql3 =
            db.query("SELECT "
                                "id "
                                "FROM "
                                "users "
                                "JOIN "
                                "merchant "
                                "ON merchant.userId = users.id "
                                "WHERE "
                                "merchant.merchantId = (SELECT merchantId FROM "
                                "item WHERE itemId = " + existingItemId + ") ;");
            int existedMerchantId = stoi(sql3[0][0]);
            if( existedMerchantId != merchId)
            {
                responseBody["ok"] = -1;
                return crow::response(200, responseBody);
            }
        }
        // check if item is already in cart
        vector<vector<string>> temp2 = db.query(
            "SELECT quantity FROM cartItems WHERE cartId = " +
            to_string(cartId) + " AND itemId = " + to_string(itemId) + ";");
        if (temp2.empty()) {
          vector<string> columns = {"cartId", "itemId", "quantity"};
          string tableName = "cartItems";
          vector<string> values = {to_string(cartId), to_string(itemId),
                                   to_string(itemQuantity)};
          db.insertData(tableName, columns, values);
          responseBody["ok"] = 1;
        } else {
          int itemCurrentQuantity = stoi(temp2[0][0]);
          if (itemCurrentQuantity != itemQuantity) {
            const string condition = "cartId = " + to_string(cartId) +
                                     " AND itemId = " + to_string(itemId);
            db.updateData("cartItems", {"quantity"}, {to_string(itemQuantity)},
                          condition);
            responseBody["ok"] = 0;
          } else
            responseBody["ok"] = 0;
        }

        return crow::response(200, responseBody);
      });
}
void getMerchantActiveOrders() {
  CROW_ROUTE(server, "/getMerchantActiveOrders/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        // get merchantId
        vector<vector<string>> merchantId_query =
            db.query("SELECT merchantId FROM merchant WHERE " +
                     to_string(userId) + " = merchant.userId;");
        if (merchantId_query.empty())
          return crow::response(
              500, "Getting merchantId at getMerchantActiveOrders");
        string merchantId = merchantId_query[0][0];
        vector<vector<string>> ans = db.query(
            "SELECT orderId,totalAmount,customerId FROM orders WHERE " +
            merchantId +
            " = orders.merchantId AND orders.orderStatus = 'active' ;");
        // replace customerId with customerName;
        for (int i = 0; i < ans.size(); i++) {
          // get customerUserId from customer table
          string customerUserId =
              db.query("SELECT userId FROM customer WHERE " + ans[i][2] +
                       " = customer.customerId;")[0][0];
          string customerName = db.query("SELECT name FROM users WHERE " +
                                         customerUserId + " = users.id;")[0][0];
          ans[i].pop_back();
          ans[i].push_back(customerName);
        }
        crow::json::wvalue responseBody;
        crow::json::wvalue::list orders;
        // answer have orderId, totalAmount , customerName

        for (auto &row : ans) {
          crow::json::wvalue order;
          order["orderId"] = stoi(row[0]);
          order["totalAmount"] = stof(row[1]);
          order["customerName"] = row[2];
          orders.push_back(std::move(order));
        }
        responseBody["orders"] = std::move(orders);
        return crow::response(200, responseBody);
      });
}
void getOrderDetails() {
  CROW_ROUTE(server, "/getOrderDetails/<int>")
      .methods("GET"_method)([](const crow::request &req, int orderId) {
        // Validate the item ID
        if (orderId <= 0) {
          return crow::response(400, "Invalid orderId ID");
        }
        // get merchantId
        vector<vector<string>> merchantId_query =
            db.query("SELECT merchantId FROM orders WHERE " +
                     to_string(orderId) + " = orders.orderId;");
        if (merchantId_query.empty())
          return crow::response(500, "Getting merchantId at getOrderDetails");
        string merchantId = merchantId_query[0][0];
        // get customerId
        vector<vector<string>> customerId_query =
            db.query("SELECT customerId FROM orders WHERE " +
                     to_string(orderId) + " = orders.orderId;");
        if (customerId_query.empty())
          return crow::response(500, "Getting customerId at getOrderDetails");
        string customerId = customerId_query[0][0];
        vector<vector<string>> sql =
            db.query("SELECT businessName,pickupAddress FROM merchant WHERE " +
                     merchantId + " = merchant.merchantId;");
        if (sql.empty())
          return crow::response(
              500, "Getting businessName or pickupAddress at getOrderDetails");
        string businessName = sql[0][0];
        string pickupAddress = sql[0][1];
        vector<vector<string>> sql2 = db.query(
            "SELECT totalAmount,orderStatus,createdAt FROM orders WHERE " +
            to_string(orderId) + " = orders.orderId ;");
        if (sql2.empty())
          return crow::response(
              500, "Getting totalAmount or orderStatus at getOrderDetails");
        string totalAmount = sql2[0][0];
        string orderStatus = sql2[0][1];
        string createdAt = sql2[0][2];
        string customerAddress =
            db.query("SELECT deliveryAddress FROM customer WHERE " +
                     customerId + " = customer.customerId ;")[0][0];
        vector<vector<string>> orderItem =
            db.query("SELECT quantity,itemId FROM orderItems WHERE " +
                     to_string(orderId) + " = orderItems.orderId ;");
        // replace customerId with customerName;
        for (int i = 0; i < orderItem.size(); i++) {
          // get itemName and itemPrice from item table
          string itemId = orderItem[i][1];
          string itemName = db.query("SELECT itemName FROM item WHERE " +
                                     itemId + " = item.itemId;")[0][0];
          string itemPrice = db.query("SELECT itemPrice FROM item WHERE " +
                                      itemId + " = item.itemId;")[0][0];
          orderItem[i].pop_back();
          orderItem[i].push_back(itemName);
          orderItem[i].push_back(itemPrice);
        }
        crow::json::wvalue responseBody;
        responseBody["businessName"] = businessName;
        responseBody["pickupAddress"] = pickupAddress;
        responseBody["totalAmount"] = stof(totalAmount);
        responseBody["orderStatus"] = orderStatus;
        responseBody["createdAt"] = createdAt;
        responseBody["customerAddress"] = customerAddress;
        crow::json::wvalue::list items;
        // orderItem = {quantitiy,itemName,itemPrice}
        for (auto &row : orderItem) {
          crow::json::wvalue item;
          item["quantity"] = stoi(row[0]);
          item["itemName"] = row[1];
          item["itemPrice"] = stof(row[2]);
          items.push_back(std::move(item));
        }
        responseBody["items"] = std::move(items);
        return crow::response(200, responseBody);
      });
}
void getAccountType() {
  CROW_ROUTE(server, "/getAccountType/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        vector<vector<string>> sql =
            db.query("SELECT accountType FROM users WHERE " +
                     to_string(userId) + " = users.id;");
        if (sql.empty())
          return crow::response(500, "Getting accountType ");
        string accountType = sql[0][0];
        crow::json::wvalue responseBody;
        responseBody["accountType"] = accountType;
        return crow::response(200, responseBody);
      });
}

void removeCartItem() {
  CROW_ROUTE(server, "/cart/removeItem/<int>/<int>")
      .methods("GET"_method)(
          [](const crow::request &req, const int &userId, const int &itemId) {
            vector<vector<string>> ans = db.query(
                "SELECT cartId FROM cart WHERE userId = " + to_string(userId) +
                " ;");
            if (ans.empty()) {
              return crow::response(500, "Error getting cartId");
            }
            int cartId = stoi(ans[0][0]);
            if (!db.execute("DELETE FROM cartItems "
                            "WHERE cartId = " +
                            to_string(cartId) +
                            " AND itemId = " + to_string(itemId) + " ;")) {
              return crow::response(500, "Error getting cartId");
            }
            return crow::response(200, "Successfully removed itemId");
          });
}

void getCourierData() {
  CROW_ROUTE(server, "/getCourierData/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        // from users email,name,phoneNumber, country,city
        // from courier nationalID vehicleType
        vector<vector<string>> ans =
            db.query("SELECT email, name, phoneNumber, country, city "
                     "FROM users WHERE id = " +
                     to_string(userId) + " ;");
        if (ans.empty()) {
          return crow::response(500, "Error getCourierData");
        }
        string email = ans[0][0];
        string name = ans[0][1];
        string phoneNumber = ans[0][2];
        string country = ans[0][3];
        string city = ans[0][4];
        vector<vector<string>> ans2 =
            db.query("SELECT nationalID, vehicleType  "
                     "FROM courier WHERE courier.userId = " +
                     to_string(userId) + " ;");
        if (ans2.empty()) {
          return crow::response(500, "Error getCourierData");
        }
        string nationalId = ans2[0][0];
        string vehicleType = ans2[0][1];
        crow::json::wvalue responseBody;
        responseBody["email"] = email;
        responseBody["name"] = name;
        responseBody["phoneNumber"] = phoneNumber;
        responseBody["country"] = country;
        responseBody["city"] = city;
        responseBody["nationalId"] = nationalId;
        responseBody["vehicleType"] = vehicleType;
        return crow::response(200, responseBody);
      });
}
void getCourierOrdersFromServer() {
  CROW_ROUTE(server, "/getCourierOrdersFromServer/<int>")
      .methods("GET"_method)([](const crow::request &req, int userId) {
        // Validate the item ID
        if (userId <= 0) {
          return crow::response(400, "Invalid user ID");
        }
        vector<vector<string>> ans =
            db.query("SELECT orderId, totalAmount,merchantId "
                     "FROM orders WHERE orderStatus = 'active';");

        for (auto &row : ans) {
          string merchantId = row[2];
          string merchantName = db.query("SELECT businessName "
                                         "FROM merchant WHERE merchantId = " +
                                         merchantId + ";")[0][0];
          row.pop_back();
          row.push_back(merchantName);
        }
        crow::json::wvalue responseBody;
        crow::json::wvalue::list orders;
        crow::json::wvalue order1, order2;
        order1["orderId"] = stoi("5123");
        order1["totalAmount"] = stof("801.5");
        order1["merchantName"] = "pro";
        orders.push_back(std::move(order1));
        order2["orderId"] = stoi("51122323");
        order2["totalAmount"] = stof("8012.5");
        order2["merchantName"] = "HELLO";
        orders.push_back(std::move(order2));
        for (auto &row : ans) {
          crow::json::wvalue order;
          order["orderId"] = stoi(row[0]);
          order["totalAmount"] = stof(row[1]);
          order["merchantName"] = row[2];
          orders.push_back(std::move(order));
        }
        responseBody["orders"] = std::move(orders);
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

void customerGetsMerchantInfoHome() {
  CROW_ROUTE(server, "/customer/getMerchantInfoHome/<int>")
      .methods("GET"_method)([](const crow::request &req, int merchantId) {
        const string sql =
            "SELECT businessName, keywords, rating FROM merchant "
            "WHERE merchantId = " +
            to_string(merchantId) + ";";
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
void getProfileImageMerchId() {
  CROW_ROUTE(server, "/get_profile_image_merchId/<int>")
      .methods("GET"_method)([](const crow::request &req, int merchantId) {
        // Validate the item ID
        if (merchantId <= 0) {
          return crow::response(400, "Invalid merchant ID");
        }
        // Query the database for the imageId
        vector<vector<string>> queryImageId =
            db.query("SELECT profileImgId "
                     "FROM merchant WHERE merchantId = " +
                     to_string(merchantId) + ";");
        // Check if the item exists
        if (queryImageId.empty()) {
          return crow::response(404, "Image not found");
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
