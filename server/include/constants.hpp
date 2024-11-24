#pragma once

typedef void (*funcPtr)(int);

const int MAX_ACTIONS = 1e6;

// Codes (Like a simple protocol to make server understand receiver) :
// 100XXX

// 101XXX
const int FIRST_CONNECTION = 1010;
const int AUTHENTICATE_CLIENT = 1011;

// 102XXX

//103XXX

const int ADD_MERCHANT = 1030;
