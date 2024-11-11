#define CNUM_SIZE 17 /// length of card number
#define EXP_SIZE 6  // length of expiry date string
#define CVV_SIZE 4 // length of CVV number                    
class card 
{
private:

    /* CREATE TABLE card (clientId INTEGER PRIMARY KEY , cardNumber TEXT NOT NULL , 
            CVV TEXT NOT NULL , expiryDate TEXT NOT NULL); */
    public:
        int clientId;
        char cardNumber[CNUM_SIZE];
        char expiryDate[EXP_SIZE];
        char CVV[CVV_SIZE];

};
