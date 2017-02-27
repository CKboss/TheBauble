def iou(self,boxes1,boxes2):
    """calculate ious
    Args:
      boxes1: 4-D tensor [CELL_SIZE, CELL_SIZE, BOXES_PER_CELL, 4]
                                          ====> (x_center, y_center, w, h)
      boxes2: 1-D tensor [4] ===> (x_center, y_center, w, h)
    Return:
      iou: 3-D tensor [CELL_SIZE, CELL_SIZE, BOXES_PER_CELL]
    """
    boxes1 = tf.stack([boxes1[:,:,:,0]-boxes1[:,:,:,2]/2, boxes1[:,:,:,1]-boxes1[:,:,:,3]/2,
                      boxes1[:,:,:,0]+boxes1[:,:,:,2]/2, boxes1[:,:,:,1]+boxes1[:,:,:,3]/2])
    boxes1 = tf.transpose(boxes1,[1,2,3,0])

    boxes2 = tf.stack([boxes2[0]-boxes2[2]/2,boxes2[1]-boxes2[3]/2,
                      boxes2[0]+boxes2[2]/2,boxes2[1]+boxes2[3]/2])

    #calculate the left up point
    lu = tf.maximum(boxes1[:, :, :, 0:2], boxes2[0:2])
    rd = tf.minimum(boxes1[:, :, :, 2:], boxes2[2:])

    #intersection
    intersection = rd - lu
    inter_square = intersection[:, :, :, 0] * intersection[:, :, :, 1]
    mask = tf.cast(intersection[:, :, :, 0] > 0, tf.float32) * tf.cast(intersection[:, :, :, 1] > 0, tf.float32)
    inter_square = mask * inter_square

    #calculate the boxs1 square and boxs2 square
    square1 = (boxes1[:, :, :, 2] - boxes1[:, :, :, 0]) * (boxes1[:, :, :, 3] - boxes1[:, :, :, 1])
    square2 = (boxes2[2] - boxes2[0]) * (boxes2[3] - boxes2[1])

    return inter_square/(square1 + square2 - inter_square + 1e-6)