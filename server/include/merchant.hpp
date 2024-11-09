#include <sqlite3.h>
#include <vector>

using namespace std;

const int MAX_SIZE = 30; // maximum size for merchant name and prodcut name
const int MAX_ADDRESS = 100; // maximum size for merchant's address
const int MAX_PRODUCTS = 100; // maximum number of produtcts for each merchant

class Merchant {

private:
/* 
  Schema of Merchant
  CREATE TABLE Merch (id INTEGER PRIMARY KEY ,name TEXT NOT NULL , address TEXT NOT NULL, card_number TEXT NOT NULL UNIQUE);

  Schema for Product
  CREATE TABLE Prod (id INTEGER PRIMARY KEY , merchant_id INTEGER NOT NULL UNIQUE
  ,name TEXT NOT NULL , price REAL NOT NULL , FOREIGN KEY (merchant_id) REFERENCES Merch(id));

*/
   struct merch 
   {
       char name[MAX];
	   int id;
	   char address[MAX_ADDRESS];
       char card_num[MAX];
   };

public:
   
   
   static int storeData (); // storing merchant data in the database 
   
   static int addItem (); // add a product  
   
   static int updateData (); // update data in the database   

};
