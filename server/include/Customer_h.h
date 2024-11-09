#ifndef CUSTOMER_H_H_INCLUDED
#define CUSTOMER_H_H_INCLUDED


class customerFunctions
{
private:
    /*
    Schema of Customer
    CREATE TABLE Customer (id INTEGER PRIMARY KEY ,name TEXT NOT NULL , email address TEXT NOT NULL, phone_number TEXT NOT NULL UNIQUE,
                           password TEXT NOT NULL ,);

    */

    struct customerData
    {
        char emailAddress[100];// Customer email address used for registration and login

        char Password[100]; // Customer account password used for login and registration

        char Country[30]; // Customer Country used for registration

        char City[30]; // Customer city used for registration

        char Name[30]; // Customer city used for registration

        char phoneNumber[30]; // Customer phone number used for registration

        char deliverAddress[100]; // Customer Delivery address used for Customer registration (for customers only)

        int Id;// Customer Id used in Customer Table (for database)
    };

public:
    static void storeData (); // in implementation : get customer data in our customer data structure to be store in database

    static void updateData(int customerId); // in implementation : update customer data stored in database with these Customer Id

    static void removeCustomer(int customerId); // in implementation : remove customer with these Id from our database

};

#endif // CUSTOMER_H_H_INCLUDED
