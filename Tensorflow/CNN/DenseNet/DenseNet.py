#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Mar 22 19:42:39 2017

@author: ckboss
"""

import numpy as np
import tensorflow as tf
import tensorlayer as tl
import tensorflow.contrib.slim as slim
import matplotlib.pyplot as plt

import Config
import unit

class DenseNet(object):

    def __init__(self):
        
        self.nb_classes = Config.NB_CLASSES
        self.img_row = Config.IMG_ROW
        self.img_col = Config.IMG_COL
        self.batch_num = Config.BATCH_SIZE
        self.color_channel = 3

        self.keep_prob = 0.8
        self.is_training = True
        self.deepth = 40
        self.config = tf.ConfigProto()
        self.config.gpu_options.allow_growth = True
        self.sess = tf.Session(config=self.config)
        self.saver = None


    def initSess(self,recoverpath=None):
        
        if recoverpath == None:
            print('init a new sess ... ')
            init = tf.global_variables_initializer()
            self.saver = tf.train.Saver()
            self.sess.run(init)
        else :            
            print(' reload a sess from ',recoverpath,'...')
            self.saver = tf.train.Saver()
            self.saver.restore(self.sess,recoverpath)
            print('model restored')
            
        print('DenseNet init done!')

    def batch_activ_conv(self,current,out_feature,kernel_size,is_training,keep_prob):
        
        current = slim.layers.batch_norm(current,scale=True,is_training=is_training,updates_collections=None)
        current = tf.nn.relu(current)
        current = slim.layers.conv2d(current,out_feature,kernel_size)
        current = tf.nn.dropout(current,keep_prob=keep_prob)
        return current

    def block(self,current,layers,in_feature,growth,is_training,keep_prob):
        
        features = in_feature
        for i in range(layers):
            tmp = self.batch_activ_conv(current,growth,[3,3],is_training,keep_prob)
            current = tf.concat((current,tmp),axis=3)
            features += growth
        return current,features

    def genDenseNet(self):
        
        print('gen DenseNet ...')
        layers = (self.deepth - 4) // 3
        weight_decay = 1e-4

        self.images = tf.placeholder(shape=[None,self.img_row,self.img_col,self.color_channel],dtype=tf.float32)
        self.labels = tf.placeholder(shape=[None],dtype=tf.int32)
        self.keep_prob = tf.placeholder(np.float32)
        self.learning_rate = tf.placeholder(np.float32)

        current = self.images
        current = slim.layers.conv2d(current,16,[3,3],scope='conv_1')

        current,features = self.block(current,layers,16,12,self.is_training,self.keep_prob)
        
        current = self.batch_activ_conv(current,features,[1,1],self.is_training,self.keep_prob)
        current = slim.nn.avg_pool(current,[1,2,2,1],[1,2,2,1],padding='SAME')
        current,features = self.block(current,layers,features,12,self.is_training,self.keep_prob)
        
        current = self.batch_activ_conv(current,features,[1,1],self.is_training,self.keep_prob)
        current = slim.nn.avg_pool(current,[1,2,2,1],[1,2,2,1],padding='SAME')
        current,features = self.block(current,layers,features,12,self.is_training,self.keep_prob)

        current = slim.layers.batch_norm(current,scale=True,is_training=self.is_training,updates_collections=None)
        current = tf.nn.relu(current)
        current = slim.nn.avg_pool(current,[1,8,8,1],[1,8,8,1],padding='VALID')

        final_dim = features
        current = tf.reshape(current,[-1,final_dim])
        current = slim.layers.fully_connected(current,self.nb_classes)

        self.output= current
        ##### acc loss and train
        with tf.variable_scope('acc') as scope:
            self.correct_prediction = tf.equal(tf.cast(tf.argmax(self.output, 1), tf.int32), self.labels)
            self.acc = tf.reduce_mean(tf.cast(self.correct_prediction, tf.float32))
            tf.summary.scalar('acc',self.acc)

        with tf.variable_scope('loss') as scope:
            ce = tf.reduce_mean(tf.nn.sparse_softmax_cross_entropy_with_logits(logits=self.output, labels=self.labels))
            self.loss = ce
            tf.summary.scalar('loss',self.loss)

        with tf.variable_scope('trains') as scope:
            self.train_op = tf.train.MomentumOptimizer(self.learning_rate,0.9,use_nesterov=True).minimize(self.loss+12*weight_decay)
            #self.train_op = tf.train.AdamOptimizer(0.0001).minimize(self.loss+12*weight_decay)
            
        self.merged_op = tf.summary.merge_all()
        print('Gen DenseNet Done!')

    def JustTrain(self,base = 0):

        print('JustTrain...')

        self.summary_writer = tf.summary.FileWriter(Config.EXPERIENT_DIR+'/log',graph=tf.get_default_graph())

        loop = 2000
        epoches = 300
        lr = 0.01
        cnt = 0
        
        for epo in range(epoches):
            
            if epo == 150:
                lr = lr / 10
            if epo == 225:
                lr = lr / 10
                
            print('in epoch: ',epo)
            
            for i in range(loop):
                
                cnt = cnt+1
                imgs,labels = unit.getTrainData()
                feed_dict = {self.images:imgs,self.labels:labels, self.learning_rate : lr, self.keep_prob : 0.8}
                _ = self.sess.run([self.train_op],feed_dict=feed_dict)
    
                summ = self.sess.run(self.merged_op,feed_dict=feed_dict)
                self.summary_writer.add_summary(summ,cnt+base)
    
                if (i+1) % 100 == 0:
                    savepath = Config.EXPERIENT_DIR+'/log/'+'Cifar10_'+str(cnt+base)+'.ckpt'
                    print('save: ',savepath)
                    self.saver.save(self.sess,savepath)
                    
                    test_imgs, test_lagels = unit.getTestData()
                    feed_dict = {self.images:test_imgs,self.labels:test_lagels,self.keep_prob : 1}
                    
                    test_acc = self.sess.run([self.acc],feed_dict=feed_dict)
                    
                    print('test_acc: ',test_acc)
                #print(i,' -> ',loss)

    def JustPredict(self):
        pass

    def JustTest(self):
        
        al = 0
        for i in range(100):
            test_imgs, test_lagels = unit.getTestData()
            feed_dict = {self.images:test_imgs,self.labels:test_lagels,self.keep_prob : 1}
            test_acc = self.sess.run([self.acc],feed_dict=feed_dict)
            al = al + test_acc[0]
            print('test acc: ',test_acc)
        print('last: ',al/100)
         

if __name__=='__main__':

    DS = DenseNet()
    DS.genDenseNet()
    DS.initSess('/tmp/log/Cifar10_100400.ckpt')
    #DS.JustTrain()
    DS.JustTest()




'''
imgs,labels = unit.getTrainData()
feed_dict = {DS.images:imgs,DS.labels:labels,DS.keep_prob : 0.8}
correct_p = DS.sess.run([DS.acc],feed_dict=feed_dict)
print(correct_p)
'''



















#