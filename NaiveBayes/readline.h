/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   readline.h
 * Author: ckboss
 *
 * Created on October 15, 2016, 4:45 PM
 */

#ifndef READLINE_H
#define READLINE_H


#include <string>
#include <vector>
#include <fstream>

using namespace std;

vector<string> readlines(string filepath) {
    vector<string> ret;
    ifstream fin(filepath.c_str());
    char buff[200000];
    while(fin.getline(buff,200000)) {
        ret.push_back(buff);
    }
    fin.close();
}

vector<string> split(string line,char sp=' ') {
    
    vector<string> ret;
    string s;
    for(int i=0,len=line.length();i<len;i++) {
        if(line[i]==sp) {
            ret.push_back(s);
            s = "";
        } else {
            s += line[i];
        }
    }
    if(s.length()!=0) ret.push_back(s);
    return ret;
}

#endif /* READLINE_H */

