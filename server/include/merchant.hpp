#include <sqlite3.h>
#include <vector>

using namespace std;

#define MAX_SIZE 30 // Maximum size for merhcant name, password and card number
#define MAX_ADDRESS 100 // Maximum size for merhcant address and email
                     
sruct merch
{
   int id;
   char name[MAX_SIZE];
   char address[MAX_ADDRESS];
   char card_num[MAX_SIZE];
   char email[MAX_ADDRESS];
   char password[MAX_SIZE];

};

class Merchant
{
    private:
        /* 
            CREATE TABLE merchant (id INTEGER PRIMARY KEY , name TEXT NOT NULL , address TEXT NOT NULL , 
            card_number TEXT NOT NULL , email TEXT NOT NULL UNIQUE , password TEXT NOT NULL);
         */ 


    public:
        
        static int storeData(); // store merchant data in the database

        static int getData(); //retrieve merchant data from the database
};
