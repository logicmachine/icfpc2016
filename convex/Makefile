all: 1solver

CXXFLAGS=-std=c++11 -O0 -fopenmp
pch.hpp.gch: pch.hpp
	g++ $(CXXFLAGS) $<

1solver.o: 1solver.cpp pch.hpp.gch
1solver: 1solver.o
	g++ -o $@ $^ -fopenmp
