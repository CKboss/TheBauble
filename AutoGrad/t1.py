#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Oct 18 21:14:27 2016

@author: ckboss
"""

import autograd.numpy as np
from autograd import grad

def func(x):
    return 3*x*x
    
grad_func = grad(func)

print('gd: ',grad_func(0.001))