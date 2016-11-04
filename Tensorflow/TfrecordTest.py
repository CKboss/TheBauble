import tensorflow as tf
import numpy as np


IM = np.arange(16)

imgs = [ IM for i in range(100)]
labels = [ i%10 for i in range(100)]

# read and write test

def getFloatList(value,isArray = False):
    if isArray==False : value = [value]
    return tf.train.Feature(float_list=tf.train.FloatList(value=value))

def getInt64List(value,isArray = False) :
    if isArray==False : value = [value]
    return tf.train.Feature(int64_list=tf.train.Int64List(value=value))


filename = '/tmp/t1.TRF'

def WriteToTFR(filename):

    with tf.python_io.TFRecordWriter(filename) as writer :
        for i in range(100):
            img = imgs[i]
            label = labels[i]
            imgr = img.tolist()
            example = tf.train.Example(features = tf.train.Features(feature =
                                                                    {'img':getFloatList(imgr,True),
                                                                     'label':getInt64List(label)}))
            writer.write(example.SerializeToString())

def ExploreTRF(filename):

    for serilize_example in tf.python_io.tf_record_iterator(filename) :

        example = tf.train.Example()
        example.ParseFromString(serilize_example)

        print(example)

def read_and_decode(filename):

    filename_queue = tf.train.string_input_producer([filename])

    reader = tf.TFRecordReader()
    _,serialize_example  = reader.read(filename_queue)
    features = tf.parse_single_example(serialized= serialize_example,
                                       features= {
                                           'img':tf.FixedLenFeature([16],tf.float32),
                                           'label':tf.FixedLenFeature([],tf.int64) })

    return features['img'],features['label']
    #return '_',features['label']


def RandomBatch():

    img,label = read_and_decode(filename)
    img_batch,label_batch = tf.train.shuffle_batch([img,label],batch_size=5,capacity=20,min_after_dequeue=10)

    init = tf.initialize_all_variables()

    sess = tf.Session()
    sess.run(init)

    threads = tf.train.start_queue_runners(sess=sess)

    for i in range(10):
        a,b = sess.run([img_batch,label_batch])
        print(a,'<-->',b)

    sess.close()

if __name__=='__main__':
    #WriteToTFR(filename=filename)
    #ExploreTRF(filename)
    a,b = read_and_decode(filename)
    print(a,b)
    RandomBatch()
