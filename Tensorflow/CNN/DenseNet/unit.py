import Config
import re
import os
import pickle
import numpy as np

#regular expression that matches a datafile
r_data_file = re.compile('^data_batch_\d+')

#training and validate datasets as numpy n-d arrays,
#apropriate portions of which are ready to be fed to the placeholder variables
train_all={'data':[], 'labels':[]}
validate_all={'data':[], 'labels':[]}
test_all={'data':{}, 'labels':[]}
label_names_for_validation_and_test=None

#hyper parameters
n_classes=10
batch_size=256
image_width=32
image_height=32
image_depth=3
learning_rate=0.01
n_epochs=20
#only the first 2000 samples is used for validating
n_validate_samples=2000
n_test_samples=5
n_checkpoint_steps=5

def unpickle(relpath):
    with open(relpath, 'rb') as fp:
        d = pickle.load(fp,encoding='latin1')
    return d


def prepare_input(data=None, labels=None):
    global image_height, image_width, image_depth
    assert(data.shape[1] == image_height * image_width * image_depth)
    assert(data.shape[0] == labels.shape[0])
    #do mean normaization across all samples
    mu = np.mean(data, axis=0)
    mu = mu.reshape(1,-1)
    sigma = np.std(data, axis=0)
    sigma = sigma.reshape(1, -1)
    data = data - mu
    data = data / sigma
    is_nan = np.isnan(data)
    is_inf = np.isinf(data)
    if np.any(is_nan) or np.any(is_inf):
        print('data is not well-formed : is_nan {n}, is_inf: {i}'.format(n= np.any(is_nan), i=np.any(is_inf)))
    #data is transformed from (no_of_samples, 3072) to (no_of_samples , image_height, image_width, image_depth)
    #make sure the type of the data is no.float32
    data = data.reshape([-1,image_depth, image_height, image_width])
    data = data.transpose([0, 2, 3, 1])
    data = data.astype(np.float32)
    return data, labels

    #return transform_input(data=data, labels=labels, h=image_height, w=image_width, d=image_depth)

def convert_to_rgb_img_data(index=-1, data=None):
    assert(index < data.shape[0])
    image_holder = np.zeros(shape=[data.shape[1],data.shape[2], data.shape[3]], dtype=np.float32)
    image_holder[:, :, :] = data[index, :, :, :]
    plt.imshow(image_holder)



def load_and_preprocess_input(dataset_dir=None):
    assert(os.path.isdir(dataset_dir))
    global train_all, validate_all, label_names_for_validation_and_test
    trn_all_data=[]
    trn_all_labels=[]
    vldte_all_data=[]
    vldte_all_labels=[]
    tst_all_data=[]
    tst_all_labels=[]


    n_validate_samples = 10000
    n_test_samples = 10000
    #for loading train dataset, iterate through the directory to get matchig data file
    for root, dirs, files in os.walk(dataset_dir):
        for f in files:
            m=r_data_file.match(f)
            if m:
                relpath = os.path.join(root, f)
                d=unpickle(os.path.join(root, f))
                trn_all_data.append(d['data'])
                trn_all_labels.append(d['labels'])
    #concatenate all the  data in various files into one ndarray of shape
    #data.shape == (no_of_samples, 3072), where 3072=image_depth x image_height x image_width
    #labels.shape== (no_of_samples)
    trn_all_data, trn_all_labels = (np.concatenate(trn_all_data).astype(np.float32),
                                          np.concatenate(trn_all_labels).astype(np.int32)
                                        )

    #load the only test data set for validation and testing
    #use only the first n_validate_samples samples for validating
    test_temp=unpickle(os.path.join(dataset_dir, 'test_batch'))
    vldte_all_data=test_temp['data'][0:(n_validate_samples+n_test_samples), :]
    vldte_all_labels=test_temp['labels'][0:(n_validate_samples+n_test_samples)]
    vldte_all_data, vldte_all_labels =  (np.concatenate([vldte_all_data]).astype(np.float32),
                                             np.concatenate([vldte_all_labels]).astype(np.int32))
     #transform the test images in the same manner as the train images
    train_all['data'], train_all['labels'] = prepare_input(data=trn_all_data, labels=trn_all_labels)
    validate_and_test_data, validate_and_test_labels = prepare_input(data=vldte_all_data, labels=vldte_all_labels)

    validate_all['data'] = validate_and_test_data[0:n_validate_samples, :, :, :]
    validate_all['labels'] = validate_and_test_labels[0:n_validate_samples]
    test_all['data'] = validate_and_test_data[n_validate_samples:(n_validate_samples+n_test_samples), :, :, :]
    test_all['labels'] = validate_and_test_labels[n_validate_samples:(n_validate_samples+n_test_samples)]

    #load all label-names
    label_names_for_validation_and_test=unpickle(os.path.join(dataset_dir, 'batches.meta'))['label_names']


load_and_preprocess_input(dataset_dir=Config.DATA_PATH)

def getTrainData(batch_size = Config.BATCH_SIZE):
    rid = np.random.randint(0,50000,(batch_size))
    return train_all['data'][rid],train_all['labels'][rid]

def getTestData(batch_size = Config.BATCH_SIZE):
    rid = np.random.randint(0,10000,(batch_size))
    return validate_all['data'][rid],validate_all['labels'][rid]

if __name__=='__main__':
    images,labels = getTrainData()