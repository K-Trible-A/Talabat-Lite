#include <arpa/inet.h>
#include <string>
#include <sys/socket.h>
#include <unistd.h>

class client {
private:
  std::string IP;
  int portNum;
  sockaddr_in serverAddr;
public:
  int serverFD;
  client(const std::string &ip, int p);
  // Connecting to specified serverFD and serverAddr (Must connect each request)
  bool connect();

  static void send(int serverFD, const void *data, size_t sz);
  static void recv(int serverFD, void *data, size_t sz);
};
