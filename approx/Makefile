all: approximate_solver

pch.hpp.gch: pch.hpp
	g++ $<

CXXFLAGS=-Wall -g -std=c++11

approximate_solver.o: approximate_solver.cpp pch.hpp.gch

approximate_solver: approximate_solver.o
	g++ -o $@ $^ `pkg-config --libs cairo` -lgmp


clean:
	rm -f *.o