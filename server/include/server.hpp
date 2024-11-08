#include <arpa/inet.h>
#include <string>
#include <sys/socket.h>
#include <unistd.h>

class server {
private:
  int serverFD;
  sockaddr_in serverAddr;
  // Specifying server address
  void setAddr(const std::string &IP, int portNum, int qSize);
  // Binding server socket to server address
  void bindSkt();
  // Listening to binded socket
  void listenSkt(int qSize);
  // returns clientFD
  int acceptClient();

public:
  server(const std::string &IP, int portNum, int qSize);
  // Listen for comming connections and handle each in a thread.
  void listenLoop();
  // Hanle each client in a thread then close his socket.
  void handleClient(int clientFD);

  static void send(int clientFD, const void *data, size_t sz);
  static void recv(int clientFD, void *data, size_t sz);
};
