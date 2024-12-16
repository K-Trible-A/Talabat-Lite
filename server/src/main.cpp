#include "../include/database.hpp"
#include "../include/routes.hpp"
#include <crow.h>
#include <cstdlib>

using namespace std;

crow::SimpleApp server;
Database db("Database.db");

// Define a type for route handlers
using RouteDefinition = std::function<void()>;

// List of route definitions
void registerRoutes() {
  helloServer();
  authClient();
  merchantRegistration();
  customerRegistration();
  courierRegistration();
  addItem();
  uploadImage();
  retrieveItem();
  retrieveItemImage();
  deleteItem();
  getMerchantInfoHome();
  getItems();
  getMerchantData();
  changePickupAddress();
  getMerchantsSearchResults();
  getItemsSearchResults();
  uploadProfileImage();
  getProfileImage();
}

int main() {
  if (!db.createSchema())
    exit(EXIT_FAILURE);
  registerRoutes();
  server.port(8080).multithreaded().run();
  return 0;
}
