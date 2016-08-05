CC = g++
CFLAGS = -Wall

ALL: main.o polygon.o
	$(CC) $(CFLAGS) -lgmp -o main main.o polygon.o

main.o: main.cpp
	$(CC) $(CFLAGS) -o main.o -c main.cpp

polygon.o: polygon.cpp polygon.h
	$(CC) $(CFLAGS) -o polygon.o -c polygon.cpp
