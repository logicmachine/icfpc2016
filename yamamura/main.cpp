#include <iostream>
#include <vector>
#include "polygon.h"


using namespace std;


void input(){
  Polygon polygon;

  int polygons_num;
  cin >> polygons_num;

  int num_vertices;
  cin >> num_vertices;

  polygon.set_vertecies(num_vertices);

  int num_edges;
  cin >> num_edges;

  polygon.set_edges(num_edges);

}


int main(){
  input();

}
