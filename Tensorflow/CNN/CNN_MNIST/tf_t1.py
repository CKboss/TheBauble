#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Oct 20 20:31:50 2016

@author: ckboss
"""

import matplotlib.pyplot as plt
import tensorflow as tf
import numpy as np

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

Y3 = np.zeros((N*K,3))
Y3[range(num_examples),Y]=1

plt.scatter(X[:, 0], X[:, 1], c=Y, s=40, cmap=plt.cm.Spectral)

h=100
'''
W = np.random.randn(D,h)*0.01
b = np.zeros((1,h))
W2 = np.random.randn(h,K)*0.01
b2 = np.zeros((1,K))
'''

reg = 1e-3
step_size = 0.1

sess = tf.InteractiveSession()

x = tf.placeholder(tf.float32,shape=[None,2])
y_ = tf.placeholder(tf.float32,shape=[None,3])

W = tf.Variable(tf.random_normal([D,h]))
b = tf.Variable(tf.zeros([h]))
W2 = tf.Variable(tf.random_normal([h,K]))
b2 = tf.Variable(tf.zeros([K]))

y = tf.matmul(tf.nn.relu(tf.matmul(x,W)+b),W2)+b2

cross_entropy = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(y, y_))
train_it = tf.train.GradientDescentOptimizer(0.5).minimize(cross_entropy)


correct_predict = tf.equal(tf.arg_max(y,1),tf.arg_max(y_,1))
accture = tf.reduce_mean(tf.cast(correct_predict,tf.float32))

sess.run(tf.initialize_all_variables())

for loop in range(1000):
    train_it.run(feed_dict={y_:Y3,x:X})
    if loop%100 == 0:
        print('accture: %.3lf\n'%accture.eval(feed_dict={y_:Y3,x:X}))


print(b.eval())


def plotResult():
    # plot the resulting classifier
    h = 0.02
    x_min, x_max = X[:, 0].min() - 0.1, X[:, 0].max() + 0.1
    y_min, y_max = X[:, 1].min() - 0.1, X[:, 1].max() + 0.1
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h),
                         np.arange(y_min, y_max, h))
    Z = np.dot(np.maximum(0, np.dot(np.c_[xx.ravel(), yy.ravel()], W.eval()) + b.eval()), W2.eval()) + b2.eval()
    Z = np.argmax(Z, axis=1)
    Z = Z.reshape(xx.shape)
    fig = plt.figure()
    plt.contourf(xx, yy, Z, cmap=plt.cm.Spectral, alpha=0.8)
    plt.scatter(X[:, 0], X[:, 1], c=Y, s=40, cmap=plt.cm.Spectral)
    plt.xlim(xx.min(), xx.max())
    plt.ylim(yy.min(), yy.max())

    
plotResult()

    
    
    
    
    






    
    
    
    
    
    