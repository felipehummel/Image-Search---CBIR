cython hist.py
gcc -O2 -lm -c -fPIC -I/usr/include/python2.6/ hist.c
gcc -shared hist.o -o hist.so
rm *.c
rm *.o
