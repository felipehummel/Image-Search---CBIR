cython calculateHistogram.py
gcc -O2 -lm -c -fPIC -I/usr/include/python2.6/ calculateHistogram.c
gcc -shared calculateHistogram.o -o calculateHistogram.so
rm *.c
rm *.o
