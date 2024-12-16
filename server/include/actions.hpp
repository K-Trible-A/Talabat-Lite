#pragma once
// Declaration of functions called as response depending on client requests

// First time connection from MainActivity.
// No actions needed just ensure the client knows
// server's IP and port number.
void firstConnection(int);
// Login authentication
void authClient(int);
// Insert merchant data into database 
void addMerchant(int);
// Add item data into database
void addItem(int);
// Send Item data to the client
void retrieveItem(int);
//Insert Courier data into database
void addCourier(int);
// Insert user data into database
void addUser(int);
// Insert Customer data into database
void addCustomer(int);
// get three top rated merchants form customer city
void getTopRatedMerchants(int);
// get customer data that he registered with 
void getCustomerData(int);
// modify customer data in Customer and users table in Database
void editCustomerData(int);
// add customer card to set payment method of orders he will order
void addCustomerCard(int);
// get categorie of merchants to be viewed for customer
void getCategorie(int);
// add Customer image to CustomerImage Table in Database
void addCustomerImage(int);
// get Customer image to be viewed in Customer_profile
void getCustomerImage(int);
// add item to CustomerCart
void addCartItem(int);
// get cart items to be viewed for customer before make order
void getCartItems(int);
// Get merchant data from database
void getMerchantData(int);
// Change merchant's pickup address
void changePickupAddress(int);
// Check account type after login
void checkAccountType(int);
// Get items data from database 
void getItems(int);
// Get an image with a specific ID
void getImage(int);
// Delete an item from database
void deleteItem(int);
// Get the merchant info to the home UI
void getMerchantInfoHome(int);


