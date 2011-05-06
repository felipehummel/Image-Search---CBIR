import cv
import sys

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


def createFilesForPartitionedImageArray(partitions, directory="."):
    cont = 0
    for img in partitions:
        cv.SaveImage("./testes/"+str(cont)+".png", img)
        cont += 1

partitions = partitionImage(sys.argv[1], 3)
createFilesForPartitionedImageArray(partitions, "./testes")

