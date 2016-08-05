#ifndef _INC_POLYGON   //まだ読み込まれていなければ以下の処理をする
#define _INC_POLYGON

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
    void set_vertecies(int num_vertices);
    // void normalize(); 正規化はケタ数大きすぎるのでやらない方向で
    void set_edges(int num_edges);

};
#endif
