#include "polygon.h"

Polygon::set_vertecies(int num_vertices){
  mpz_init(normalization_max);
  mpz_set_str(normalization_max, 0, 10);

  for (int i = 0; i < num_vertices; ++i)
  {
    Vertex vertex;
    string input;
    cin >> input;
    string tmp;
    vector<string> tmp1, tmp2, tmp3;
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
// Polygon::normalize(){
//   for (int i = 0; i < vertices.size(); ++i)
//   {
//     vertices[i].c.x = vertices[i].c.x_ini.ordinal * normalization_max / vertices[i].c.x_ini.denominator ; 
//     vertices[i].c.y = vertices[i].c.y_ini.ordinal * normalization_max / vertices[i].c.y_ini.denominator ;
//   }
// }
