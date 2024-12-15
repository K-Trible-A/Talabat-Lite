#include "../include/server.hpp"
#include "../include/actions.hpp"
#include "../include/constants.hpp"
#include <algorithm>
#include <cstring>
#include <fstream>
#include <iostream>
#include <netinet/in.h>
#include <sys/socket.h>
#include <thread>
#include <vector>
#include <stdexcept>

using namespace std;

extern funcPtr actions[MAX_ACTIONS];
const size_t CHUNK_SIZE = 51200; // 50 kb

void server::setAddr(const std::string &IP, int portNum, int queueSize) {
    serverFD = socket(AF_INET, SOCK_STREAM, 0);
    if (serverFD == -1) {
        throw runtime_error("Socket creation failed");
    }
    
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(portNum);
    if (inet_pton(AF_INET, IP.c_str(), &serverAddr.sin_addr) <= 0) {
        close(serverFD);
        throw invalid_argument("Invalid IP address");
    }
}

void server::bindSkt() {
    if (bind(serverFD, (sockaddr *)&serverAddr, sizeof(serverAddr)) == -1) {
        close(serverFD);
        throw runtime_error("Error binding socket");
    }
}

void server::listenSkt(int qSize) {
    if (listen(serverFD, qSize) == -1) {
        close(serverFD);
        throw runtime_error("Error listening on socket");
    }
}

server::server(const std::string &IP, int portNum, int qSize) {
    try {
        setAddr(IP, portNum, qSize);
        bindSkt();
        listenSkt(qSize);
        appendFuncs();
        cout << "Server is up and running..." << endl;
    } catch (const exception &e) {
        cerr << e.what() << endl;
        exit(EXIT_FAILURE);
    }
}

int server::acceptClient() {
    sockaddr_in clientAddr;
    socklen_t clientAddrSz = sizeof(clientAddr);
    int clientFD = accept(serverFD, (sockaddr *)&clientAddr, &clientAddrSz);
    
    if (clientFD < 0) {
        cerr << "Accept client failed" << endl;
        return -1; // Return an error code instead of exiting
    }

    char clientIP[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &clientAddr.sin_addr, clientIP, sizeof(clientIP));
    cout << "New connection from " << clientIP << ":" << ntohs(clientAddr.sin_port) << endl;
    
    return clientFD;
}

void server::listenLoop() {
    while (true) {
        int clientFD = acceptClient();
        if (clientFD >= 0) {
            thread clientThread(&server::handleClient, this, clientFD);
            clientThread.detach();
        } else {
            cerr << "Failed to accept a new client." << endl;
            // Optionally add a sleep or retry mechanism here
        }
    }
}

void server::send(int clientFD, const int &num) {
    if (::send(clientFD, &num, sizeof(num), 0) == -1) {
        perror("send failed");
    }
}

void server::send(int clientFD, const float &num) {
    if (::send(clientFD, &num, sizeof(num), 0) == -1) {
        perror("send failed");
    }
}

void server::send(int clientFD, const std::string &s) {
    int sz = s.size() + 1; // Including null-terminator
    send(clientFD, sz); // Send the size first

    size_t bytesSent = 0;
    
    while (bytesSent < sz) {
        size_t remaining = sz - bytesSent;
        size_t currentChunkSize = std::min(CHUNK_SIZE, remaining);
        
        ssize_t result = ::send(clientFD, s.c_str() + bytesSent, currentChunkSize, 0);
        
        if (result == -1) {
            perror("send failed");
            return; // Handle error appropriately
        }

        bytesSent += result;

        if (result < currentChunkSize) { // Handle partial sends
            cerr << "Warning: Partial send of chunk." << endl;
            break; // Optionally break or continue based on your logic
        }
    }
}


void server::sendImg(int clientFD, const std::string &image_data) {
    int image_size = static_cast<int>(image_data.size());
    
    // Send the length of the image first in network byte order
    ::send(clientFD, &image_size, sizeof(image_size), 0); // Send size
    
    // Now send the image in chunks
    size_t bytes_sent = 0;
    
    while (bytes_sent < image_size) {
        size_t remaining_bytes = image_size - bytes_sent;
        size_t chunk_size = std::min(remaining_bytes, CHUNK_SIZE);
        
        ssize_t sent_chunk = ::send(clientFD, image_data.data() + bytes_sent, chunk_size, 0);
        
        if (sent_chunk == -1) {
            std::cerr << "Send failed." << std::endl;
            return; // Handle error appropriately
        }
        
        bytes_sent += sent_chunk;
    }
}


int server::recvInt(int clientFD) {
    int revd;
    
    if (::recv(clientFD, &revd, sizeof(revd), 0) <= 0) {
        cerr << "Failed to receive integer" << endl;
        return -1; // Return an error code
    }

    return ntohl(revd); // Convert to host byte order
}

float server::recvFloat(int clientFD) {
    uint32_t revd;

    if (::recv(clientFD, &revd, sizeof(revd), 0) <= 0) {
        cerr << "Failed to receive float" << endl;
        return -1.0f; // Return an error code
    }

    revd = ntohl(revd);
    
    float ret;
    memcpy(&ret, &revd, sizeof(revd));
    
    return ret;
}

std::string server::recvString(int clientFD) {
    uint32_t string_size;

    ssize_t bytes_received = recv(clientFD, &string_size, sizeof(string_size), 0);
    
    if (bytes_received <= 0) {
        cerr << "Error receiving string size or connection closed" << endl;
        return ""; // Return empty string on error
    }

    string_size = ntohl(string_size); // Convert to host byte order
    std::string received_data(string_size, '\0'); // Preallocate space for the string

    size_t total_received = 0;

    while (total_received < string_size) {
        ssize_t chunk_size = std::min(CHUNK_SIZE, string_size - total_received);
        
        bytes_received = recv(clientFD, &received_data[total_received], chunk_size, 0);

        if (bytes_received <= 0) { 
            cerr << "Connection closed unexpectedly while receiving data" << endl;
            return ""; // Return empty string on error
        }

        total_received += bytes_received;
    }

    return received_data; 
}


std::pair<unsigned char *, uint32_t> server::recvImg(int clientFD) {
    uint32_t imageSize;

    // Receive the size of the image
    ssize_t bytesReceived = recv(clientFD, &imageSize, sizeof(imageSize), 0);
    if (bytesReceived <= 0) { 
        std::cerr << "Failed to receive image size or connection lost!" << std::endl; 
        return {nullptr, 0}; 
    }

    // Convert from network byte order to host byte order
    imageSize = ntohl(imageSize); 
    std::cout << "Image size: " << imageSize << " bytes" << std::endl;

    // Allocate memory for the image data
    unsigned char *imageData = new unsigned char[imageSize];
    if (!imageData) {
        std::cerr << "Memory allocation failed for image data!" << std::endl;
        return {nullptr, 0};
    }

    size_t totalBytesReceived = 0;

    // Receive the image data in chunks
    while (totalBytesReceived < imageSize) { 
        bytesReceived = recv(clientFD, imageData + totalBytesReceived,
                             imageSize - totalBytesReceived, 0);

        if (bytesReceived == -1) {
            std::cerr << "Error receiving image data!" << std::endl; 
            delete[] imageData; // Free memory before returning
            return {nullptr , 0}; 
        } else if (bytesReceived == 0) {
            std::cerr << "Connection closed before receiving complete image!" << std::endl; 
            delete[] imageData; // Free memory before returning
            return {nullptr , 0}; 
        } 

        totalBytesReceived += bytesReceived; 
    }

    // Optional: Write the received image data to a file
    std::ofstream outFile("received_image.jpg", std::ios::binary); 
    outFile.write(reinterpret_cast<char *>(imageData), imageSize); 
    outFile.close(); 

    std::cout << "Image received and saved as 'received_image.jpg'" << std::endl; 

    return {imageData , imageSize}; 
}

void server::appendFuncs() { 
   actions[FIRST_CONNECTION] = firstConnection; 
   actions[AUTHENTICATE_CLIENT] = authClient; 
   actions[ADD_MERCHANT] = addMerchant; 
   actions[ADD_ITEM] = addItem; 
   actions[RETRIEVE_ITEM] = retrieveItem; 
   actions[ADD_COURIER] = addCourier; 
   actions[ADD_USER] = addUser; 
   actions[ADD_CUSTOMER] = addCustomer; 
   actions[GET_MERCHANT_DATA] = getMerchantData; 
   actions[CHANGE_PICKUP_ADDRESS] = changePickupAddress; 
   actions[CHECK_ACCOUNT_TYPE] = checkAccountType; 
   actions[GET_ITEMS] = getItems; 
   actions[GET_IMAGE] = getImage; 
   actions[DELETE_ITEM] = deleteItem; 
   actions[GET_MERCHANT_INFO_HOME] = getMerchantInfoHome; 
   actions[GET_TOP_RATED_MERCHANTS] = getTopRatedMerchants;
   actions[GET_CUSTOMER_DATA] = getCustomerData;
   actions[EDIT_CUSTOMER_DATA] = editCustomerData;
   actions[ADD_CUSTOMER_CARD] = addCustomerCard;
   actions[GET_CATEGORIE] = getCategorie;
   actions[ADD_CUSTOMER_IMAGE] = addCustomerImage;
   actions[GET_CUSTOMER_IMAGE] = getCustomerImage;
}
