import tensorflow as tf
import tensorlayer as tl
from tensorlayer.layers import set_keep
import matplotlib.pyplot as plt
import numpy as np
import os

import Config
import unit

class ResNet:
    
    def __init__(self):

        # For wide resnets
        self.blocks_per_group = 4
        self.widening_factor = 4

        # Basic info
        self.batch_num = 64
        self.img_row = 64
        self.img_col = 64
        self.img_channels = 3
        self.nb_classes = 2
        
        
    def reset(self, first):
        self.first = first
        if self.first is True:
            self.sess.close()

        config = tf.ConfigProto()
        config.gpu_options.allow_growth = True
        self.sess = tf.Session(config=config)

    def restore(self,ckptpath):
        
        config = tf.ConfigProto()
        config.gpu_options.allow_growth = True
        self.sess = tf.Session(config=config)
        
        self.saver = tf.train.Saver()
        self.saver.restore(self.sess,ckptpath) 
        
        print('model restored!')
        
    def next_batch(self):
        Images,Labels = unit.getTrainData(self.batch_num)
        return [Images,Labels]
    
    
        
    def genResNet(self):
        '''
        gen resnet
        '''

        def zero_pad_channels(x, pad=0):
            """
            Function for Lambda layer
            """
            pattern = [[0, 0], [0, 0], [0, 0], [pad - pad // 2, pad // 2]]
            return tf.pad(x, pattern)

        def residual_block(x, count, nb_filters=16, subsample_factor=1):
            prev_nb_channels = x.outputs.get_shape().as_list()[3]

            if subsample_factor > 1:
                subsample = [1, subsample_factor, subsample_factor, 1]
                # shortcut: subsample + zero-pad channel dim
                name_pool = 'pool_layer' + str(count)
                shortcut = tl.layers.PoolLayer(x,
                                               ksize=subsample,
                                               strides=subsample,
                                               padding='VALID',
                                               pool=tf.nn.avg_pool,
                                               name=name_pool)

            else:
                subsample = [1, 1, 1, 1]
                # shortcut: identity
                shortcut = x

            if nb_filters > prev_nb_channels:
                name_lambda = 'lambda_layer' + str(count)
                shortcut = tl.layers.LambdaLayer(
                    shortcut,
                    zero_pad_channels,
                    fn_args={'pad': nb_filters - prev_nb_channels},
                    name=name_lambda)

            name_norm = 'norm' + str(count)
            y = tl.layers.BatchNormLayer(x,
                                         decay=0.999,
                                         epsilon=1e-05,
                                         is_train=True,
                                         name=name_norm)

            name_conv = 'conv_layer' + str(count)
            y = tl.layers.Conv2dLayer(y,
                                      act=tf.nn.relu,
                                      shape=[3, 3, prev_nb_channels, nb_filters],
                                      strides=subsample,
                                      padding='SAME',
                                      name=name_conv)

            name_norm_2 = 'norm_second' + str(count)
            y = tl.layers.BatchNormLayer(y,
                                         decay=0.999,
                                         epsilon=1e-05,
                                         is_train=True,
                                         name=name_norm_2)

            prev_input_channels = y.outputs.get_shape().as_list()[3]
            name_conv_2 = 'conv_layer_second' + str(count)
            y = tl.layers.Conv2dLayer(y,
                                      act=tf.nn.relu,
                                      shape=[3, 3, prev_input_channels, nb_filters],
                                      strides=[1, 1, 1, 1],
                                      padding='SAME',
                                      name=name_conv_2)

            name_merge = 'merge' + str(count)
            out = tl.layers.ElementwiseLayer([y, shortcut],
                                             combine_fn=tf.add,
                                             name=name_merge)


            return out
        
         # Placeholders
        self.learning_rate = tf.placeholder(tf.float32)
        self.img = tf.placeholder(tf.float32, shape=[None, self.img_col, self.img_row, 3])
        self.labels = tf.placeholder(tf.int32, shape=[None, ])

        x = tl.layers.InputLayer(self.img, name='input_layer')
        x = tl.layers.Conv2dLayer(x,
                                  act=tf.nn.relu,
                                  shape=[3, 3, 3, 16],
                                  strides=[1, 1, 1, 1],
                                  padding='SAME',
                                  name='cnn_layer_first')

        for i in range(0, self.blocks_per_group):
            nb_filters = 16 * self.widening_factor
            count = i
            x = residual_block(x, count, nb_filters=nb_filters, subsample_factor=1)

        for i in range(0, self.blocks_per_group):
            nb_filters = 32 * self.widening_factor
            if i == 0:
                subsample_factor = 2
            else:
                subsample_factor = 1
            count = i + self.blocks_per_group
            x = residual_block(x, count, nb_filters=nb_filters, subsample_factor=subsample_factor)

        for i in range(0, self.blocks_per_group):
            nb_filters = 64 * self.widening_factor
            if i == 0:
                subsample_factor = 2
            else:
                subsample_factor = 1
            count = i + 2*self.blocks_per_group
            x = residual_block(x, count, nb_filters=nb_filters, subsample_factor=subsample_factor)

        x = tl.layers.BatchNormLayer(x,
                                     decay=0.999,
                                     epsilon=1e-05,
                                     is_train=True,
                                     name='norm_last')

        x = tl.layers.PoolLayer(x,
                                ksize=[1, 8, 8, 1],
                                strides=[1, 8, 8, 1],
                                padding='VALID',
                                pool=tf.nn.avg_pool,
                                name='pool_last')

        x = tl.layers.FlattenLayer(x, name='flatten')

        x = tl.layers.DenseLayer(x,
                                 n_units=self.nb_classes,
                                 act=tf.identity,
                                 name='fc')

        self.output = x.outputs
        
        self.tensorLayers = x

        output = self.output
        
        with tf.variable_scope('cost') as vs:
            ce = tf.reduce_mean(tf.nn.sparse_softmax_cross_entropy_with_logits(logits=output, labels=self.labels))
            self.cost = ce
        
        tf.summary.scalar('loss',self.cost)

        with tf.variable_scope('acc') as vs:
            self.correct_prediction = tf.equal(tf.cast(tf.argmax(output, 1), tf.int32), self.labels)
            self.acc = tf.reduce_mean(tf.cast(self.correct_prediction, tf.float32))
        
        tf.summary.scalar('acc',self.acc)

        train_params = x.all_params
        self.train_op = tf.train.GradientDescentOptimizer(
            self.learning_rate, use_locking=False).minimize(self.cost, var_list=train_params)
        
        self.merged_op = tf.summary.merge_all()

    def train(self,loop=1000):

        self.sess.run(tf.global_variables_initializer())
        self.summary_writer = tf.summary.FileWriter(Config.exper_dir+'/log',graph=tf.get_default_graph())
        self.saver = tf.train.Saver()

        for i in range(loop):
            batch = self.next_batch()
            feed_dict = {self.img: batch[0], self.labels: batch[1], self.learning_rate: 0.001}
            #feed_dict.update(self.x.all_drop)
            self.sess.run(self.train_op,feed_dict=feed_dict)
            summ = self.sess.run(self.merged_op,feed_dict=feed_dict)
            self.summary_writer.add_summary(summ,i)
            
            if i%500 == 0:
                spath = Config.exper_dir+'/save/cat_and_dog_'+str(i)+'.ckpt'
                saver_path = self.saver.save(self.sess,spath,i)
                print('%d th sess is save in %s\n'%(i,saver_path))
                
            if i%10 == 0:
                print('in loop:',i)
    
    def predect(self,images):
        feed_dict = {self.img: images, self.labels: np.array([-1]), self.learning_rate: 0.001}
        kind = self.sess.run(tf.nn.softmax(self.output),feed_dict=feed_dict)
        return kind
    
def JustTrain():
    RS = ResNet()
    RS.reset(False)
    RS.genResNet()
    RS.train(10000)
    
def JustTest():
    RS = ResNet()
    RS.genResNet()
    RS.restore('/media/lab712/datadisk/Experience/CatAndDog/save/cat_and_dog_7500.ckpt-7500')
            
    f = open('/tmp/ans2.csv','w')
    
    
    files =  os.listdir(Config.test_data_set)
    nt = 0
    for i in range(1,12501,100):
        images = unit.getTestData(i,i+99)
        outans = RS.predect(images)
        print('batch -->',i)
        for j in range(100):
            dog = outans[j,1]
            f.write('%d,%.3f\n'%(i+j,dog))
            f.flush()
    '''    
    for file in files:
        image = unit.pretreatment_picture(os.path.join(Config.test_data_set,file))
        image = np.reshape(image,(1,64,64,3))
        outans = RS.predect(image)
        cat = outans[0,0]
        dog = outans[0,1]
        #prob = min(dog,0) / (max(cat,0)+max(dog,0)+1e-8)
        f.write('%s,%.3f\n'%(file[:-4],dog))
        print('-->',file)
        nt = nt+1
        if nt%100==0 :
            f.flush()
    '''    
    f.flush()
    f.close()
            
    
if __name__=='__main__':
    #...
    JustTest()
    
    '''
    RS = ResNet()
    RS.genResNet()
    RS.restore('/media/lab712/datadisk/Experience/CatAndDog/save/cat_and_dog_7500.ckpt-7500')
    '''

    
    '''     
    files =  os.listdir(Config.test_data_set)
    for file in files:
        image = unit.pretreatment_picture(os.path.join(Config.test_data_set,file))
        break
    image = np.reshape(image,(1,64,64,3))
    ans = RS.predect(image)
    '''
    #I,L = RS.next_batch()