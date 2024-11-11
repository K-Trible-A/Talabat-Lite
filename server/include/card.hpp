#include "constants.hpp"
class card
{
private:

    /* CREATE TABLE card (clientId INTEGER PRIMARY KEY , cardNumber TEXT NOT NULL ,
            CVV TEXT NOT NULL , expiryDate TEXT NOT NULL); */
public:
    int clientId;
    char cardNumber[CARD_NUMBER_LENGTH];
    char expiryDate[EXPIRE_DATE_LENGTH];
    char CVV[CVV_LENGTH];
};