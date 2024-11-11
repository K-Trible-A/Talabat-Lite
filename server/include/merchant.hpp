#include <sqlite3.h>
#include "card.hpp"
#include "constants.hpp"

                        
enum {GROCERY = 1 , RESTAURANT = 2 , PHARMACY = 3}; 

struct merch
{
   int id;
   int businessType;
   char name[MAX_NAME_LENGTH];
   char pickupAddress[MAX_ADDRESS_LENGTH];
   char email[MAX_EMAIL_LENGTH];
   char password[MAX_PASSWORD_LENGTH];
   char nationalId[NATIONAL_ID_LENGTH];
   char keywords[MAX_ADDRESS_LENGTH];
   char phone[PHONE_NUMBER_LENGTH];
   char country[MAX_NAME_LENGTH];
   char city[MAX_NAME_LENGTH];
   card merchCard;

};
 
class merchant
{
    private:
        /* 
            CREATE TABLE merchant (id INTEGER PRIMARY KEY , name TEXT NOT NULL , pickupAddress TEXT NOT NULL 
            , email TEXT NOT NULL UNIQUE , password TEXT NOT NULL ,
            nationalId TEXT NOT NULL , keywords TEXT NOT NULL , phone TEXT NOT NULL ,businessType INTEGER NOT NULL ,
            country TEXT NOT NULL , city TEXT NOT NULL);
         */ 


    public:
        
        static void storeData(); // store merchant data in the database

        static void getData(); //retrieve merchant data from the database
};
