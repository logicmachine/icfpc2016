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

void Polygon::set_vertecies(int num_vertices){
  // mpz_init(normalization_max);
  // mpz_set_str(normalization_max, 0, 10);

  for (int i = 0; i < num_vertices; ++i)
  {
    Vertex vertex;
    std::string input;
    std::cin >> input;
    std::string tmp;
    std::vector<std::string> tmp1, tmp2, tmp3;
    boost::algorithm::split(tmp1, input, boost::is_any_of(","));
    boost::algorithm::split(tmp2, tmp1[0], boost::is_any_of("/"));
    boost::algorithm::split(tmp3, tmp1[1], boost::is_any_of("/"));
    std::cout << tmp2[0] << " " << tmp2[1] << std::endl;
    std::cout << tmp3[0] << " " << tmp3[1] << std::endl;

    mpz_init(vertex.c.x_ini.ordinal);
    mpz_init(vertex.c.y_ini.ordinal);
    mpz_init(vertex.c.x_ini.denominator);
    mpz_init(vertex.c.y_ini.denominator);

    mpz_set_str(vertex.c.x_ini.ordinal, tmp2[0].c_str(), 10);
    mpz_set_str(vertex.c.y_ini.ordinal, tmp3[0].c_str(), 10);

    if(tmp2.size() == 2){
      mpz_set_str(vertex.c.x_ini.denominator, tmp2[1].c_str(), 10);
      // if(normalization_max < vertex.c.x_ini.denominator){
      //   mpz_set(normalization_max, vertex.c.x_ini.denominator);
      //   // normalization_max = vertex.c.x_ini.denominator;
      // }

    }else{
      mpz_set_str(vertex.c.x_ini.denominator, "1", 10);
    }
    if(tmp3.size() == 2){
      mpz_set_str(vertex.c.y_ini.denominator, tmp3[1].c_str(), 10);
      // if(normalization_max < vertex.c.y_ini.denominator){
      //   mpz_set(normalization_max, vertex.c.y_ini.denominator);
      //   // normalization_max = vertex.c.y_ini.denominator;
      // }

    }else{
      mpz_set_str(vertex.c.y_ini.denominator, "1", 10);
    }
    vertices.push_back(vertex);
  }
}

//正規化はケタ数大きすぎるのでやらない方向で
// void Polygon::normalize(){
//   for (int i = 0; i < vertices.size(); ++i)
//   {
//     vertices[i].c.x = vertices[i].c.x_ini.ordinal * normalization_max / vertices[i].c.x_ini.denominator ; 
//     vertices[i].c.y = vertices[i].c.y_ini.ordinal * normalization_max / vertices[i].c.y_ini.denominator ;
//   }
// }

void Polygon::set_edges(int num_edges) {

}


