all: resemblance
CXXFLAGS=-O2 -Wall -g -std=c++11 # -I$(HOME)/Downloads/boost_1_61_0


pch.hpp.gch: pch.hpp
	g++ $(CXXFLAGS) $<


approximate_solver.o: approximate_solver.cpp pch.hpp.gch
approximate_solver: approximate_solver.o
	g++ -o $@ $^ `pkg-config --libs cairo` -lgmp

resemblance.o: resemblance.cpp pch.hpp.gch
resemblance: resemblance.o
	g++ -o $@ $^ `pkg-config --libs cairo` -lgmp


clean:
	rm -f *.o