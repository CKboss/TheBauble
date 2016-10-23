/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: ckboss
 *
 * Created on October 14, 2016, 8:26 PM
 */

#include <cstdlib>
#include <iostream>
#include <string>
#include <cstdio>
#include <cstring>
#include <unordered_map>
#include <unordered_set>

#include "PorterStemmer.h"
#include "readline.h"
#include "stopwords.h"

using namespace std;


void DoIt() {
    
    /***********************train**************************/
    
    string filepath = "/home/ckboss/Desktop/homework/NaiveBayes/spam_train.txt";

    ifstream *fin = new ifstream(filepath.c_str());
    char buff[2*0x100000];
    int cnt=0;
    
    unordered_set<string> stops;
    
    for(auto str : stopwords) {
        string wd = stem((char *)str.c_str());
        stops.insert(wd);
    }
    
    unordered_map<string,int> wd_count[2];
    int sp_num[2];
    sp_num[0]=sp_num[1]=0;

    while(fin->getline(buff,2*0x100000,'\n')) {
        cnt++;
        vector<string> words = split(buff);
        
        int spam = -1;
        
        for(auto word : words) {
            
            if(spam==-1) {
                if(word[0]=='0') spam = 0;
                else spam = 1;
                sp_num[spam]++;
                continue;
            }
            
            string wd = stem((char *)word.c_str());
            if(stops.count(wd)==1) continue;
            
            wd_count[spam][wd]++;
        }
    }
    
    fin->close();
    
    
    /***********************test**************************/
    
    
    filepath = "/home/ckboss/Desktop/homework/NaiveBayes/spam_test.txt";
    fin = new ifstream(filepath.c_str());
    
    long double PS[2];
    PS[0] = (sp_num[0]+1.)/(cnt+2);
    PS[1] = (sp_num[1]+1.)/(cnt+2);
    int correct=0;

    while(fin->getline(buff,2*0x100000,'\n')) {
        
        vector<string> words = split(buff);
        
        int spam = -1;
        
        long double p0=PS[0],p1=PS[1];
        
        for(auto word : words) {
            
            if(spam==-1) {
                if(word[0]=='0') spam = 0;
                else spam = 1;
                continue;
            }
            
            string wd = stem((char *)word.c_str());
            if(stops.count(wd)==1) continue;
            
            p0 *= (wd_count[0][wd]+(long double)1)/(cnt+2)/PS[0];
            p1 *= (wd_count[1][wd]+(long double)1)/(cnt+2)/PS[1];
            
        }
        
        if(((p0>p1)&&(spam==0))||((p1>p0)&&(spam==1)))
            correct++;
    }
    
    cout<<"correct num: "<<correct<<endl;
    cout<<"%: "<<correct/10.<<"%"<<endl;

    delete fin;
}

void Test() {
}

int main(int argc, char** argv) {
    DoIt();
    return 0;
}

