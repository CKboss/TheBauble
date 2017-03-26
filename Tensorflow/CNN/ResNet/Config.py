import os

ROOT_PATH = "/media/lab712/datadisk/dataset/Intel&MobileODTCervicalCancerScreening/"

train_data_dir = os.path.join(ROOT_PATH, "train")
test_data_dir = os.path.join(ROOT_PATH, "test")
additional_data_dir = os.path.join(ROOT_PATH,'additional')


exper_dir = '/media/lab712/datadisk/Experience/CervicalCancer/EX4'

BATCH_SIZE = 32
batch_size = BATCH_SIZE

NUM_OF_CLASS = 3

IMG_ROW = 64
IMG_COL = 64


SAVER_PATH = '/media/lab712/datadisk/Experience/CervicalCancer/EX4/save/cervicalcancer_19900.ckpt-19900'