import os
import matplotlib.pyplot as plt
import Config
import numpy as np
import cv2


def pretreatment_picture(imagefilepath,enable_enhance = True):
    '''
    resize img to 256x256 (np.float32)
    '''
    img = cv2.imread(imagefilepath)
    img = cv2.resize(img,(Config.IMG_ROW,Config.IMG_COL))
    
    ''' data enhance'''
    if enable_enhance :
        from imgaug import augmenters as iaa
        st = lambda aug: iaa.Sometimes(0.3, aug)
        seq = iaa.Sequential([
            iaa.Fliplr(0.5), # horizontally flip 50% of all images
            iaa.Flipud(0.5), # vertically flip 50% of all images
            iaa.Crop(px=(0,10)),
            iaa.Superpixels(p_replace=(0, 0.1)),
            st(iaa.ContrastNormalization((0.8, 1.5), per_channel=0.5)), # improve or worsen the contrast
            #st(iaa.Invert(0.25, per_channel=True)),
            st(iaa.Affine(
                scale={"x": (0.8, 1.2), "y": (0.8, 1.2)}, # scale images to 80-120% of their size, individually per axis
                translate_px={"x": (-6, 6), "y": (-6, 6)}, # translate by -16 to +16 pixels (per axis)
                rotate=(-5, 5), # rotate by -45 to +45 degrees
                shear=(-6, 6), # shear by -16 to +16 degrees
                cval=(0, 0.2), # if mode is constant, use a cval between 0 and 1.0
                )),
            st(iaa.ElasticTransformation(alpha=(0.0, 0.2), sigma=0.15)),
            ])
        img_aug = seq.augment_image(img)
    else :
        img_aug = img

    img = cv2.resize(img_aug,(Config.IMG_ROW,Config.IMG_COL))
    img = img.astype(np.float32)
    
    return img


def load_data(data_dir):
    """Loads a data set and returns two lists:

    images_paths: a list of Numpy arrays, each representing an image.
    labels: a list of numbers that represent the images labels.
    """
    directories = [d for d in os.listdir(data_dir) 
                   if os.path.isdir(os.path.join(data_dir, d))]
    labels = []
    image_paths = []
    for d in directories:
        label_dir = os.path.join(data_dir, d)
        file_names = [os.path.join(label_dir, f) 
                      for f in os.listdir(label_dir) if f.endswith(".jpg")]
        for f in file_names:
            image_paths.append(f)
            labels.append(int(d[-1])-1)
    return image_paths, labels

def load_test_data(data_dir):
    '''
    image_paths: test image's path from data dir
    '''
    image_paths = list()
    files = os.listdir(data_dir)
    for i in range(512):
        file = '%d.jpg'%i
        image_paths.append(os.path.join(data_dir,file))
    return image_paths

image_paths, labels = load_data(Config.train_data_dir)

addition_image_paths, addition_image_labels = load_data(Config.additional_data_dir)

def getTrainData(batch_size = Config.batch_size):
    idx = np.random.randint(0,len(image_paths),size=(batch_size,1))
    train_images = list()
    train_labels = list()
    for i in idx:
        train_images.append(pretreatment_picture(image_paths[i]))
        train_labels.append(labels[i])
    return np.array(train_images),np.array(train_labels)


def getAditionData(batch_size = Config.batch_size):
    idx = np.random.randint(0,len(addition_image_paths),size=(batch_size,1))
    train_images = list()
    train_labels = list()
    for i in idx:
        train_images.append(pretreatment_picture(addition_image_paths[i],False))
        train_labels.append(addition_image_labels[i])
    return np.array(train_images),np.array(train_labels)

test_images_path = load_test_data(Config.test_data_dir)

def getTestData(fr=0,to=10):
    tis = list()
    for i in range(fr,to+1):
        tis.append(pretreatment_picture(test_images_path[i],False))
    return np.array(tis)

if __name__=='__main__':
    I = getTestData()
    I,L = getAditionData()



































#