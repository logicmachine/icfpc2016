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
    //分数
    struct Fraction{
      mpz_t ordinal;
      mpz_t denominator;
    };

    //座標
    struct Cordinate{
      //分数
      Fraction x_ini;
      Fraction y_ini;
      //正規化した後
      // mpz_t x;
      // mpz_t y;
    };

    struct Edge{
      Cordinate c1, c2;
    };

    //頂点
    struct Vertex{
      Cordinate c;
    };
    vector<Vertex> vertices;
    vector<Edge> edges; 
    // mpz_t normalization_max;

  public:
    Polygon(){}

    void set_vertecies(int num_vertices){
      mpz_init(normalization_max);
      mpz_set_str(normalization_max, 0, 10);

      for (int i = 0; i < num_vertices; ++i)
      {
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
        cout << tmp2[0] << " " << tmp2[1] << endl;
        cout << tmp3[0] << " " << tmp3[1] << endl;

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
    // void normalize(){
    //   for (int i = 0; i < vertices.size(); ++i)
    //   {
    //     vertices[i].c.x = vertices[i].c.x_ini.ordinal * normalization_max / vertices[i].c.x_ini.denominator ; 
    //     vertices[i].c.y = vertices[i].c.y_ini.ordinal * normalization_max / vertices[i].c.y_ini.denominator ;
    //   }
    // }

    // void set_edges(int num_edges){
    //   for (int i = 0; i < ; ++i)
    //   {
    //     Edge edge;
    //   }

    // }

};


void input(){
  Polygon polygon;

  int polygons_num;
  cin >> polygons_num;

  int num_vertices;
  cin >> num_vertices;

  polygon.set_vertecies(num_vertices);

  int num_edges;
  cin >> num_edges;

  // polygon.set_edges(num_edges);
  polygon.normalize();

}


int main(){
  input();
  // long long int a, b, c, d;
  // string input;
  // cin >> input;
  // string tmp;
  // vector<string> v;
  // boost::algorithm::split(v, input, boost::is_any_of(","));
  // cout << v[0] << " " << v[1] << endl;


 
}
