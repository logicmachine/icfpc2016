#include <iostream>
#include <vector>
#include <algorithm>
#include <gmpxx.h>
#include <stdio.h>
#include <string>
#include <sstream>
#include <boost/algorithm/string.hpp>

using namespace std;

class Polygon{
  private:
    struct Fraction{
      mpz_t ordinal;
      mpz_t denominator;
    };

    //座標
    struct Cordinate{
      Fraction x;
      Fraction y;
    };

    struct Edge{
      Cordinate c1, c2;
    };

    //頂点
    struct Vertex{
      Cordinate c;
    };
    vector<Vertex> vetecies;
    vector<Edge> edges; 

  public:
    Polygon(){}

    void set_vertecies(int num_vertices){
      for (int i = 0; i < num_vertices; ++i)
      {
        Edge
        Vertex vertex;
        string input;
        cin >> input;
        string tmp;
        vector<string> tmp1;
        vector<string> tmp2;
        vector<string> tmp3;
        boost::algorithm::split(tmp1, input, boost::is_any_of(","));
        boost::algorithm::split(tmp2, tmp1[0], boost::is_any_of("/"));
        boost::algorithm::split(tmp3, tmp1[1], boost::is_any_of("/"));
        cout << tmp1[0] << " " << tmp1[1] << endl;
        

        // scanf("%ld, %ld" &n);
        // cin >> 
        // vertex.c.x;
        vetecies.push_back(vertex);
      }

    }

};


void input(){
  int polygons_num;
  cin >> polygons_num;

  int num_vertices;
  cin >> num_vertices;

  Polygon polygon;

  polygon.set_vertecies(num_vertices);

}


int main(){
  input();
  long long int a, b, c, d;
  string input;
  cin >> input;
  string tmp;
  vector<string> v;
  boost::algorithm::split(v, input, boost::is_any_of(","));
  cout << v[0] << " " << v[1] << endl;


 
}
