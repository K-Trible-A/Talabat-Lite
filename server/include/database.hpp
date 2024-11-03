#pragma once
#include <sqlite3.h>
#include <string>
#include <vector>

using namespace std;

class Database {
private:
  sqlite3 *db;
public:
  Database(const string &); //open database or create if not found 
  ~Database(); //close database if opened

  bool execute(const string &); //executes sql
  vector<vector<string>> query(const string &); //get query output
};
