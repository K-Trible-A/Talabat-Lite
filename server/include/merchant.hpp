#include <sqlite3.h>
#include "card.hpp"

#define MAX_SIZE 31 // Maximum size for merhcant name, password, card number, phone number and national ID
#define MAX_ADDRESS 101 // Maximum size for merhcant pickup address, email and keyword
                        
enum {GROCERY = 1 , RESTAURANT = 2 , PHARMACY = 3}; 

struct merch
{
   int id;
   int businessType;
   char name[MAX_SIZE];
   char pickupAddress[MAX_ADDRESS];
   char email[MAX_ADDRESS];
   char password[MAX_SIZE];
   char nationalId[MAX_SIZE];
   char keywords[MAX_ADDRESS];
   char phone[MAX_SIZE];
   char country[MAX_SIZE];
   char city[MAX_SIZE];
   card merchCard;

};
 
class Merchant
{
    private:
        /* 
            CREATE TABLE merchant (id INTEGER PRIMARY KEY , name TEXT NOT NULL , pickup_address TEXT NOT NULL 
            , email TEXT NOT NULL UNIQUE , password TEXT NOT NULL ,
            national_id TEXT NOT NULL , keywords TEXT NOT NULL , phone TEXT NOT NULL ,business_type INTEGER NOT NULL ,
            country TEXT NOT NULL , city TEXT NOT NULL);
         */ 


    public:
        
        static void storeData(); // store merchant data in the database

        static void getData(); //retrieve merchant data from the database
};
