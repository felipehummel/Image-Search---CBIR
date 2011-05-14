import cv
import sys
import math
from hist import creatHistogram

def partitionImage(image_path, NUM_PARTITIONS=5):
    img = cv.LoadImageM(image_path)
    height = img.rows
    width = img.cols
    width_partition = width/NUM_PARTITIONS
    height_partition = height/NUM_PARTITIONS
    y_cur = 0
    x_cur = 0
    partitions = []
    for i in range(0, NUM_PARTITIONS): #itera linhas
        for j in range(0, NUM_PARTITIONS): #itera colunas
            if j == NUM_PARTITIONS-1:        #se ultima coluna
                how_much_left_width = width - x_cur
                if i == NUM_PARTITIONS-1:      #se ultima coluna e ultima linha
                    how_much_left_height = height - y_cur
                    partitions.append(cv.GetSubRect(img, (x_cur, y_cur, how_much_left_width , how_much_left_height)))
                else:                    #so ultima coluna
                    partitions.append(cv.GetSubRect(img, (x_cur, y_cur, how_much_left_width , height_partition)))
            else:
                partitions.append(cv.GetSubRect(img, (x_cur, y_cur, width_partition , height_partition)))
            x_cur = x_cur + width_partition      #atualiza posicao da coluna (x)
        y_cur = y_cur + height_partition      #atualiza posicao da linha (y)
        x_cur = 0             #reseta a posicao da coluna (x = 0)
    return partitions


def createFilesForPartitionedImageArray(partitions):
    cont = 0
    for img in partitions:
        cv.SaveImage("./segmentos/"+str(cont)+".png", img)
        cont += 1

#def creatHistogram(partition):
#    width     = partition.width
#    height    = partition.height
##    nchannels = 3                    #COLOCANDO NA MARRA COMO CONSTANTE 3 = RGB
##    step      = partition.step
#    fatorG = 4
#    fatorB = 16
#    for i in range(0, height):
#        for j in range(0, width):
#            r = partition[i, j][0]*4/256
#            g = partition[i, j][1]*4/256
#            b = partition[i, j][2]*4/256
#            r = math.floor(r)
#            g = math.floor(g)
#            b = math.floor(b)
#            cor = int(r + fatorG*g + fatorB*b)

#createFilesForPartitionedImageArray(partitions)

import os
from array import array
dirList=os.listdir('/home/felipe/ufam/doutorado/ri/trab1_busca/jpg/')
output_file = open('output_binary_histograms', 'wb')
file_lookup_file = open('file_lookup_file', 'w')
i = 0
print 'Vai processar ', len(dirList), ' imagens'
for fname in dirList:
    partitions = partitionImage('/home/felipe/ufam/doutorado/ri/trab1_busca/jpg/'+fname, 5)
    hist = []
    file_lookup_file.write(fname.replace('.jpg', '')+'\n')
    for part in partitions:
        hist += creatHistogram(part)
    int_array = array('i', hist)
    int_array.byteswap()
    int_array.tofile(output_file)
    if i%10 == 0:
        print i
    i += 1

output_file.close()
file_lookup_file.close()

