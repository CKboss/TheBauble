#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Sep 23 15:49:34 2016

@author: ckboss
"""

import numpy as np
import pandas as pd
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt

datafile = '/home/ckboss/Documents/MachineLearning/DataSet/iris/iris.data'


def loadData():
    f = pd.read_csv(datafile)
    data = f.get_values()
    ret = list()
    for x in data :
        ret.append(list(x[:4]))
    return np.mat(ret)

def pca(data,K):
    
    # n : data number
    # m : dimension
    m,n = data.shape
    
    # make data mean = 0
    m1 = np.mean(data,axis=1)
    data = data -m1
    
    # calulate matrix C
    C = data.dot(data.transpose())/n

    # calulate eigValue and eigVector
    eigValue,eigVector = np.linalg.eig(C)
    
    pkg = zip(eigValue,eigVector)
    firstK = list(sorted(pkg,key = lambda x : -x[0]))[:K]
    firstKM = np.zeros((K,m))
    
    for i in range(K):
        firstKM[i] = firstK[i][1]

    print(firstKM)

    # to K
    ret = firstKM.dot(data)    
    return ret
    
def Disp(data):
    m,n = data.shape
    fig = plt.figure()
    if n == 2:
        
        xs = np.array(data.transpose())[0].flatten().tolist()
        ys = np.array(data.transpose())[1].flatten().tolist()
        
        ax = fig.add_subplot(111)
        ax.scatter(xs[:51],ys[:51],c='r',marker='o')
        ax.scatter(xs[51:101],ys[51:101],c='g',marker='o')
        ax.scatter(xs[101:],ys[101:],c='b',marker='o')
        
    elif n == 3:
        
        xs = np.array(data.transpose())[0].flatten().tolist()
        ys = np.array(data.transpose())[1].flatten().tolist()
        zs = np.array(data.transpose())[2].flatten().tolist()
        
        fig = plt.figure()
        ax = fig.add_subplot(111, projection='3d')
        ax.scatter(xs[:51],ys[:51],zs[:51],c='r',marker='o')
        ax.scatter(xs[51:101],ys[51:101],zs[51:101],c='g',marker='o')
        ax.scatter(xs[101:],ys[101:],zs[101:],c='b',marker='o')

    plt.show()

data = loadData()

ret = pca(data.transpose(),2).transpose()

Disp(ret)

print(ret)
rk = list(x[0] for x in fk)