#ifndef _INC_POLYGON   //まだ読み込まれていなければ以下の処理をする
#define _INC_POLYGON
#include <gmpxx.h>
#include <vector>
#include <iostream>


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
      Fraction x;
      Fraction y;
    };

    //頂点
    struct Vertex{
      Cordinate c;
    };

    //頂点を結ぶ
    struct Edge{
      Vertex v1, v2;
    };

    std::vector<Vertex> vertices;
    std::vector<Edge> edges; 

  public:
    Polygon();
    void set_vertex_from_input(std::string input, struct Vertex *vertex);
    void set_vertecies(int num_vertices);
    // void normalize(); 正規化はケタ数大きすぎるのでやらない方向で
    void set_edges(int num_edges);


};
#endif
