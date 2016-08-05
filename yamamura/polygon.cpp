#include "polygon.h"
#include <iostream>
#include <vector>
#include <algorithm>
#include <gmpxx.h>
#include <stdio.h>
#include <string>
#include <sstream>
#include <boost/algorithm/string.hpp>


Polygon::Polygon(){}

void Polygon::set_vertex_from_input(std::string input, struct Vertex *vertex){

  std::vector<std::string> tmp1, tmp2, tmp3;
  boost::algorithm::split(tmp1, input, boost::is_any_of(","));
  boost::algorithm::split(tmp2, tmp1[0], boost::is_any_of("/"));
  boost::algorithm::split(tmp3, tmp1[1], boost::is_any_of("/"));
  // std::cout << tmp2[0] << " " << tmp2[1] << std::endl;
  // std::cout << tmp3[0] << " " << tmp3[1] << std::endl;

  mpz_init(vertex->c.x.ordinal);
  mpz_init(vertex->c.y.ordinal);
  mpz_init(vertex->c.x.denominator);
  mpz_init(vertex->c.y.denominator);

  mpz_set_str(vertex->c.x.ordinal, tmp2[0].c_str(), 10);
  mpz_set_str(vertex->c.y.ordinal, tmp3[0].c_str(), 10);

  if(tmp2.size() == 2){
    mpz_set_str(vertex->c.x.denominator, tmp2[1].c_str(), 10);
  }else{
    mpz_set_str(vertex->c.x.denominator, "1", 10);
  }
  if(tmp3.size() == 2){
    mpz_set_str(vertex->c.y.denominator, tmp3[1].c_str(), 10);
  }else{
    mpz_set_str(vertex->c.y.denominator, "1", 10);
  }

}


void Polygon::set_vertecies(int num_vertices){
  for (int i = 0; i < num_vertices; ++i){
    Vertex vertex;
    std::string input;
    std::cin >> input;
    set_vertex_from_input(input, &vertex);
    vertices.push_back(vertex);
  }
}

void Polygon::set_edges(int num_edges) {
  for (int i = 0; i < num_edges; ++i){
    Edge edge;
    Vertex vertex1, vertex2;
    std::string input1, input2;
    std::cin >> input1 >> input2;
    set_vertex_from_input(input1, &vertex1);
    set_vertex_from_input(input2, &vertex2);
    edge.v1 = vertex1;
    edge.v2 = vertex2;
    edges.push_back(edge);
  }
}
