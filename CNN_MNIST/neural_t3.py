#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Oct 20 17:11:10 2016

@author: ckboss
"""

import matplotlib.pyplot as plt
import autograd.numpy as np
from autograd import grad

N = 100
D = 2
K = 3
num_examples = 300

def genData():
    X = np.zeros((N*K,D))
    Y = np.zeros((N*K),dtype='uint8')
    
    for j in range(K):
        ix = range(N*j,N*(j+1))
        r = np.linspace(0,1,N)
        t = np.linspace(j*4,(j+1)*4,N) + np.random.randn(N)*0.2
        X[ix] = np.c_[r*np.sin(t),r*np.cos(t)]
        Y[ix] = j
    return X,Y
    
X,Y = genData()

plt.scatter(X[:, 0], X[:, 1], c=Y, s=40, cmap=plt.cm.Spectral)

h = 100
W = np.random.randn(D,h)*0.01
b = np.zeros((1,h))
W2 = np.random.randn(h,K)*0.01
b2 = np.zeros((1,K))

    
reg = 1e-3
step_size = 0.1

hidden_layer = np.maximum(0,np.dot(X,W))+b
sorces = np.dot(hidden_layer,W2)+b2

def getHiddenLayer(W,b):
    return np.maximum(0,np.dot(X,W))+b

def getSorces(hidden_layer,W2,b2):
    return np.dot(hidden_layer,W2)+b2

def getLoss(W,b,W2,b2):

    hidden_layer = getHiddenLayer(W,b)

    sorces = getSorces(hidden_layer,W2,b2)
    
    exp_sorces = np.exp(sorces)
    
    probs = exp_sorces/np.sum(exp_sorces,axis=1,keepdims=True)
    correct_probs = -np.log(probs[range(num_examples),Y])
    
    data_loss = np.sum(correct_probs)/num_examples
    ret_loss = reg*np.sum(W*W)
    
    loss = data_loss+ret_loss
    
    return loss
    
grad_W = grad(getLoss,0)
grad_b = grad(getLoss,1)
grad_W2 = grad(getLoss,2)
grad_b2 = grad(getLoss,3)
    
for loop in range(20000):
    
    loss = getLoss(W,b,W2,b2)
    if loop%1000 == 0:
        print('ith',loop,'loss:',loss)
    if loop%5000 == 0:
        pass
        #plotResult()
    
   # dhiden_layer[hidden_layer <= 0] = 0

    dW = grad_W(W,b,W2,b2)
    db = grad_b(W,b,W2,b2)
    dW2 = grad_W2(W,b,W2,b2)
    db2 = grad_b2(W,b,W2,b2)

    W += -step_size*dW
    b += -step_size*db
    W2 += -step_size*dW2
    b2 += -step_size*db2



plotResult()

def plotResult():
    # plot the resulting classifier
    h = 0.02
    x_min, x_max = X[:, 0].min() - 0.1, X[:, 0].max() + 0.1
    y_min, y_max = X[:, 1].min() - 0.1, X[:, 1].max() + 0.1
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h),
                         np.arange(y_min, y_max, h))
    Z = np.dot(np.maximum(0, np.dot(np.c_[xx.ravel(), yy.ravel()], W) + b), W2) + b2
    Z = np.argmax(Z, axis=1)
    Z = Z.reshape(xx.shape)
    fig = plt.figure()
    plt.contourf(xx, yy, Z, cmap=plt.cm.Spectral, alpha=0.8)
    plt.scatter(X[:, 0], X[:, 1], c=Y, s=40, cmap=plt.cm.Spectral)
    plt.xlim(xx.min(), xx.max())
    plt.ylim(yy.min(), yy.max())

