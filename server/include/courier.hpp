#include "constants.hpp"
#include "card.hpp"

enum {BICYCLE = 1 , MOTORCYCLE = 2 , CAR = 3};
struct courier{
    int id;
    int vehicleType;
    char name[MAX_NAME_LENGTH];
    char email[MAX_ADDRESS_LENGTH];
    char password[MAX_PASSWORD_LENGTH];
    char nationalId[NATIONAL_ID_LENGTH];
    char phoneNumber[PHONE_NUMBER_LENGTH];
    char country[MAX_NAME_LENGTH];
    char city[MAX_NAME_LENGTH];
    card courierCard;
};
class Courier{
private:

public:

    static void storeData(); // store Courier data in the database

    static void getData(); //retrieve Courier data from the database
};
