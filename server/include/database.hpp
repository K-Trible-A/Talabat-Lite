#pragma once
#include <sqlite3.h>
#include <string>
#include <vector>

using namespace std;

class Database {
private:
  sqlite3 *db;
  // Adding little bit safe to strings dealing with the database
  string escapeSpecialChars(const string &);

public:
  Database(const string &); // open database or create if not found
  ~Database();              // close database if opened

  // Specify database tables and relationships
  sqlite3 *getDB();
  bool createSchema();
  bool execute(const string &); // executes sql
  // insert data into a table
  bool insertData(const string &table, const vector<string> &columns,
                  const vector<string> &values);
  // update data in a table
  bool updateData(const string &table, const vector<string> &columns,
                  const vector<string> &values, const string &condition);
  vector<vector<string>> query(const string &); // get query output
};
