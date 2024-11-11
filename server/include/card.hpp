#define CNUM_SIZE 17
#define EXP_SIZE 6  // maximum size for expiry date length
class card 
{
private:

    /* CREATE TABLE card (client_id INTEGER PRIMARY KEY , card_number TEXT NOT NULL , 
            CVV INTEGER NOT NULL , expiry_date TEXT NOT NULL); */
    public:
        int clientId;
        char cardNumber[CNUM_SIZE];
        char expiryDate[EXP_SIZE];
        int CVV;

};
