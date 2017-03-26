#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sun Mar 12 14:13:29 2017

@author: lab712
"""

import Config
import os
import numpy as np
import cv2

default_batch_size = Config.batch_size
files = os.listdir(Config.train_data_set)


def pretreatment_picture(imagefilepath):
    '''
    resize img to 64x64 (np.float32)
    '''
    img = cv2.imread(imagefilepath)
    img = cv2.resize(img,(64,64))
    img = img.astype(np.float32)
    
    return img

def getTrainData(batch_size=default_batch_size):
    '''
    get train data
    image and label
    0 cat 1 dog
    '''
    
    images = list()
    labels = list()
        
    randid = np.random.randint(0,len(files),size=(batch_size))
    for i in randid:
        imagepath = os.path.join(Config.train_data_set,files[i])
        label = 0
        if imagepath.find('dog') > -1:
            label = 1
        img = pretreatment_picture(imagepath)
        images.append(img)
        labels.append(label)
        
    return np.array(images),np.array(labels)


def getTestData(fr,to):
    images = list()
    
    for i in range(fr,to+1):
        imagepath = Config.test_data_set+'/'+str(i)+'.jpg'
        img = pretreatment_picture(imagefilepath=imagepath)
        images.append(img)
        
    return np.array(images)
        
if __name__=='__main__':
    I,L = getTrainData()



























#        