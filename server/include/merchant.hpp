#include <sqlite3.h>

using namespace std;

#define MAX_SIZE 30 // Maximum size for merhcant name, password, card number and national ID
#define MAX_ADDRESS 100 // Maximum size for merhcant pickup address, email and keyword
                        
enum {GROCERY = 1 , RESTAURANT = 2}; 

struct merch
{
   int id;
   int business_type;
   char name[MAX_SIZE];
   char pickup_address[MAX_ADDRESS];
   char card_num[MAX_SIZE];
   char email[MAX_ADDRESS];
   char password[MAX_SIZE];
   char national_id[MAX_SIZE];
   char keywords[MAX_ADDRESS];
   

};
 
class Merchant
{
    private:
        /* 
            CREATE TABLE merchant (id INTEGER PRIMARY KEY , name TEXT NOT NULL , pickup_address TEXT NOT NULL , 
            card_number TEXT NOT NULL , email TEXT NOT NULL UNIQUE , password TEXT NOT NULL ,
            national_id TEXT NOT NULL , keywords TEXT NOT NULL , business_type INTEGER NOT NULL);
         */ 


    public:
        
        static void storeData(); // store merchant data in the database

        static void getData(); //retrieve merchant data from the database
};
