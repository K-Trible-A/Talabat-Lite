#include <sqlite3.h>
#include <vector>

using namespace std;

const int MAX_SIZE = 50; // maximum size for merchant name and prodcut name
const int MAX_ADDRESS = 100; // maximum size for merchant's address
const int MAX_PRODUCTS = 100; // maximum number of produtcts for each merchant

struct product
{
    char name[MAX];
	int id;
	int merchant_id; // the merchant(s) that has this products
	float price;
};

class Merchant {

private:
/* 
  Schema of Merchant
  CREATE TABLE Merch (id INTEGER PRIMARY KEY ,name TEXT NOT NULL , address TEXT NOT NULL, card_number TEXT NOT NULL UNIQUE);

  Schema for Product
  CREATE TABLE Prod (id INTEGER PRIMARY KEY , merchant_id INTEGER NOT NULL UNIQUE
  ,name TEXT NOT NULL , price REAL NOT NULL , FOREIGN KEY (merchant_id) REFERENCES Merch(id));

*/
   product [MAX_PRODUCTS];

   struct merch 
   {
       char name[MAX];
	   int id;
	   char address[MAX_ADDRESS];
       char card_num[MAX];
   };

public:
   
   Merchant(); // a constructor which will generate an id for the merchant
   
   int setData (); // an included function in the constructor to get the merchant's data
   
   int addItem (); // add a product  
   
   int setAddress ();

   int setCardNum ();

	   
   int remProduct (int ID) // remove a product
   
};
