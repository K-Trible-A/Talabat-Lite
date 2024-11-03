#include "../include/database.hpp"
#include <iostream>
using namespace std;

int main() {
  Database db("example.db");

  // Create a table
  db.execute(
      "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT)");

  // Insert data
  db.execute("INSERT INTO users (name) VALUES ('Alice')");
  db.execute("INSERT INTO users (name) VALUES ('Bob')");

  // Query data
  auto results = db.query("SELECT distinct name FROM users");

  // Print results
  for (const auto &row : results) {
    for (const auto &col : row) {
      cout << col << " ";
    }
    cout << endl;
  }
  cout << endl;

  return 0;
}
