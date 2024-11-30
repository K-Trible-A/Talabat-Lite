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

using namespace std;

extern funcPtr actions[MAX_ACTIONS];

void server::setAddr(const std::string &IP, int portNum, int queueSize) {
  serverFD = socket(AF_INET, SOCK_STREAM, 0);
  if (serverFD == 0) {
    cerr << "Socket failed" << endl;
    exit(EXIT_FAILURE);
  }
  serverAddr.sin_family = AF_INET;
  serverAddr.sin_port = htons(portNum);
  serverAddr.sin_addr.s_addr = inet_addr(IP.c_str());
}

void server::bindSkt() {
  int ok_bind = bind(serverFD, (sockaddr *)&serverAddr, sizeof(serverAddr));
  if (ok_bind == -1) {
    cerr << "Error binding a socket!";
    close(serverFD);
    exit(EXIT_FAILURE);
  }
}

void server::listenSkt(int qSize) {
  int ok_listen = listen(serverFD, qSize);
  if (ok_listen == -1) {
    cerr << "Error listening to the socket!";
    close(serverFD);
    exit(EXIT_FAILURE);
  }
}

server::server(const std::string &IP, int portNum, int qSize) {
  setAddr(IP, portNum, qSize);
  bindSkt();
  listenSkt(qSize);
  appendFuncs();
  cout << "Server is up and running..." << endl;
}

int server::acceptClient() {
  sockaddr_in clientAddr;
  socklen_t clientAddrSz = sizeof(clientAddr);
  int clientFD = accept(serverFD, (sockaddr *)&clientAddr, &clientAddrSz);
  if (clientFD < 0) {
    cerr << "Accept client failed" << endl;
    exit(EXIT_FAILURE);
  }
  char clientIP[INET_ADDRSTRLEN];
  inet_ntop(AF_INET, &clientAddr.sin_addr, clientIP, sizeof(clientIP));
  cout << "New connection from " << clientIP << ":"
       << ntohs(clientAddr.sin_port) << endl;
  return clientFD;
}

void server::listenLoop() {
  while (true) {
    int clientFD = acceptClient();
    thread clientThread(&server::handleClient, this, clientFD);
    clientThread.detach();
  }
}

void server::send(int clientFD, const int &num) {
  ::send(clientFD, &num, sizeof(num), 0);
}
void server::send(int clientFD, const float &num) {
  ::send(clientFD, &num, sizeof(num), 0);
}

void server::send(int clientFD, const std::string &s) {
  int sz = s.size() + 1; // Including null-terminator
  // First, send the size of the string
  server::send(clientFD, sz);
  // Allocate a buffer large enough for a chunk
  size_t chunkSize = 1024; // You can adjust this chunk size as needed
  size_t bytesSent = 0;
  while (bytesSent < sz) {
    // Calculate the remaining data to be sent
    size_t remaining = sz - bytesSent;
    size_t currentChunkSize = std::min(chunkSize, remaining);
    // Create a buffer for the current chunk
    std::vector<char> buff(currentChunkSize);
    // Copy the appropriate portion of the string into the buffer
    std::memcpy(buff.data(), s.c_str() + bytesSent, currentChunkSize);
    // Send the current chunk
    ssize_t result = ::send(clientFD, buff.data(), currentChunkSize, 0);
    if (result == -1) {
      // Handle send error (e.g., log it, throw exception, etc.)
      perror("send failed");
      return;
    }
    // Update the number of bytes sent
    bytesSent += result;
    // Optional: Handle partial send (if needed)
    if (result < currentChunkSize) {
      std::cerr << "Warning: Partial send of chunk." << std::endl;
    }
  }
}

void server::sendImg(int clientFD, const std::string &image_data) {
  int image_size = image_data.size();
  server::send(clientFD, image_size);
  // Now send the image in chunks
  size_t bytes_sent = 0;
  size_t CHUNK_SIZE = 1024;
  while (bytes_sent < image_size) {
    // Calculate the remaining bytes to send
    size_t remaining_bytes = image_size - bytes_sent;
    size_t chunk_size = std::min(remaining_bytes, CHUNK_SIZE);
    // Send the chunk
    ssize_t sent_chunk =
        ::send(clientFD, image_data.data() + bytes_sent, chunk_size, 0);
    if (sent_chunk == -1) {
      std::cerr << "Send failed." << std::endl;
      return;
    }
    bytes_sent += sent_chunk;
  }
}

int server::recvInt(int clientFD) {
  int revd;
  ::recv(clientFD, &revd, sizeof(revd), 0);
  return revd = ntohl(revd);
}
float server::recvFloat(int clientFD) {
  uint32_t revd;
  ::recv(clientFD, &revd, sizeof(revd), 0);
  revd = ntohl(revd);
  float ret;
  memcpy(&ret, &revd, sizeof(revd));
  return ret;
}
std::string server::recvString(int clientFD) {
  // Step 1: Receive the size of the string first
  uint32_t string_size;
  ssize_t bytes_received = recv(clientFD, &string_size, sizeof(string_size), 0);
  if (bytes_received == -1) {
    perror("Error receiving string size");
    return "";
  }
  if (bytes_received == 0) {
    std::cerr << "Connection closed unexpectedly" << std::endl;
    return "";
  }
  // Convert received size to host byte order (in case of network byte order)
  string_size = ntohl(string_size); // Network-to-host byte order conversion
  // Step 2: Allocate a buffer for the actual data based on the string size
  std::string received_data;
  received_data.resize(string_size); // Resize the string to hold the data
  // Step 3: Receive the actual string data in chunks
  size_t total_received = 0;
  size_t CHUNK_SIZE = 1024;
  while (total_received < string_size) {
    size_t chunk_size = std::min(CHUNK_SIZE, string_size - total_received);
    bytes_received =
        recv(clientFD, &received_data[total_received], chunk_size, 0);
    if (bytes_received == -1) {
      perror("Error receiving data");
      return "";
    }
    if (bytes_received == 0) {
      std::cerr << "Connection closed unexpectedly while receiving data"
                << std::endl;
      return "";
    }
    total_received += bytes_received;
  }
  return received_data;
}

std::pair<unsigned char *, uint32_t> server::recvImg(int clientFD) {
  // First, receive the size of the image
  uint32_t imageSize = 0;
  ssize_t bytesReceived = recv(clientFD, &imageSize, sizeof(imageSize), 0);
  if (bytesReceived <= 0) {
    std::cerr << "Failed to receive image size or connection lost!"
              << std::endl;
    return {NULL, 0};
  }

  // Convert the received size from network byte order to host byte order
  imageSize = ntohl(imageSize);

  std::cout << "Image size: " << imageSize << " bytes" << std::endl;

  // Allocate memory for the image data (raw pointer)
  unsigned char *imageData = new unsigned char[imageSize];

  // Receive the image data in chunks and store it in the buffer
  size_t totalBytesReceived = 0;
  while (totalBytesReceived < imageSize) {
    bytesReceived = recv(clientFD, imageData + totalBytesReceived,
                         imageSize - totalBytesReceived, 0);
    if (bytesReceived == -1) {
      std::cerr << "Error receiving image data!" << std::endl;
      delete[] imageData; // Free the memory before returning
      return {NULL, 0};
    } else if (bytesReceived == 0) {
      std::cerr << "Connection closed before receiving complete image!"
                << std::endl;
      delete[] imageData; // Free the memory before returning
      return {NULL, 0};
    }
    totalBytesReceived += bytesReceived;
  }

  return {imageData, imageSize};

  // Optionally, write the received image data to a file
  std::ofstream outFile("received_image.jpg", std::ios::binary);
  outFile.write(reinterpret_cast<char *>(imageData), imageSize);
  outFile.close();

  std::cout << "Image received and saved as 'received_image.jpg'" << std::endl;

  // Free the allocated memory
  delete[] imageData;
}

///////////
void server::appendFuncs() {
  actions[FIRST_CONNECTION] = firstConnection;
  actions[AUTHENTICATE_CLIENT] = authClient;
  actions[ADD_MERCHANT] = addMerchant;
  actions[ADD_ITEM] = addItem;
  actions[RETRIEVE_ITEM] = retrieveItem;
}
