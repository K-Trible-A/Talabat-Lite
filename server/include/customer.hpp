#include "constants.hpp" // used for constant sizes of array of data

struct customerData
{
    char emailAddress[MAX_EMAIL_LENGTH];// Customer email address used for registration and login

    char password[MAX_PASSWORD_LENGTH]; // Customer account password used for login and registration

    char country[MAX_ADDRESS_LENGTH]; // Customer Country used for registration

    char city[MAX_ADDRESS_LENGTH]; // Customer city used for registration

    char name[MAX_NAME_LENGTH]; // Customer city used for registration

    char phoneNumber[PHONE_NUMBER_LENGTH]; // Customer phone number used for registration

    char deliveryAddress[MAX_ADDRESS_LENGTH]; // Customer Delivery address used for Customer registration (for customers only)

    int id;// Customer Id used in Customer Table (for database)
};

class customerFunctions
{
private:

    /*

    Schema of Customer

    CREATE TABLE Customer ( id INTEGER PRIMARY KEY , name TEXT NOT NULL , emailAddress TEXT NOT NULL UNIQUE ,
                            phoneNumber TEXT NOT NULL UNIQUE , password TEXT NOT NULL , deliveryAddress  TEXT NOT NULL ,
                            city TEXT NOT NULL , country TEXT NOT NULL );
    */

public:

    static void storeData (); // in implementation : get customer data in our customer data structure to be store in database

    static void getData(); // in implementation : remove customer with these Id from our database

};
