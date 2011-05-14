import cv
import numpy

cdef extern from "math.h":
    float floor(float)

def creatHistogram(partition):
    cdef int width
    cdef int height
    cdef int i
    cdef int j
    cdef int fatorG
    cdef int fatorB
    cdef float r
    cdef float g
    cdef float b
    cdef float cor
    cdef float x
    cdef tuple rgb_tuple
    cdef list hist
    cdef int index
    height = partition.height
    width = partition.width
    fatorG = 4
    fatorB = 16
    hist = [0]*64
    for i in range(0, height):
        for j in range(0, width):
            rgb_tuple = partition[i, j]
            x = rgb_tuple[0];
            r = x*0.015625
            x = rgb_tuple[1];
            g = x*0.015625
            x = rgb_tuple[2];
            b = x*0.015625
            r = floor(r)
            g = floor(g)
            b = floor(b)
            cor = r + fatorG*g + fatorB*b
            index = int(cor)
            hist[index] = hist[index] + 1
    return hist

