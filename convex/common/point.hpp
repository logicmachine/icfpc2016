#pragma once
#include <limits>
#include "rational.hpp"

struct Point {
	/// X座標
	Rational x;
	/// Y座標
	Rational y;

	/**
	 *  @brief コンストラクタ
	 */
	Point() : x(), y() { }

	/**
	 *  @brief コンストラクタ
	 *  @param[in] x  X座標
	 *  @param[in] y  Y座標
	 */
	Point(const Rational &x, const Rational &y) :
		x(x), y(y)
	{ }


	/**
	 *  @brief 座標同士の和の計算
	 *  @param[in] p  加算する座標
	 *  @return    (this->x + p.x, this->y + p.y)
	 */
	Point operator+(const Point &p) const { return Point(x + p.x, y + p.y); }
	/**
	 *  @brief 座標同士の和の計算 (代入)
	 *  @param[in] p  加算する座標
	 *  @return    自身への参照
	 */
	Point &operator+=(const Point &p){ return *this = *this + p; }
	/**
	 *  @brief 座標同士の差の計算
	 *  @param[in] p  減算する座標
	 *  @return    (this->x - p.x, this->y - p.y)
	 */
	Point operator-(const Point &p) const { return Point(x - p.x, y - p.y); }
	/**
	 *  @brief 座標同士の差の計算 (代入)
	 *  @param[in] p  減算する座標
	 *  @return    自身への参照
	 */
	Point &operator-=(const Point &p){ return *this = *this - p; }
	/**
	 *  @brief 座標値のスカラ倍
	 *  @param[in] s  乗算する値
	 *  @return    (this->x * s, this->y * s)
	 */
	Point operator*(Rational s) const { return Point(x * s, y * s); }
	/**
	 *  @brief 座標値のスカラ倍 (代入)
	 *  @param[in] s  乗算する値
	 *  @return    自身への参照
	 */
	Point &operator*=(Rational s){ return *this = *this * s; }
	/**
	 *  @brief 座標値の複素数倍
	 *  @param[in] p  乗算する値
	 *  @return    *this * p;
	 */
	Point operator*(const Point &p) const {
		return Point(x * p.x - y * p.y, x * p.y + y * p.x);
	}
	/**
	 *  @brief 座標値の複素数倍 (代入)
	 *  @param[in] p  乗算する値
	 *  @return    自身への参照
	 */
	Point &operator*=(const Point &p){ return *this = *this * p; }
	/**
	 *  @brief 座標値のスカラ倍 (除算)
	 *  @param[in] s  除算する値
	 *  @return    (this->x / s, this->y / s)
	 */
	Point operator/(Rational s) const { return Point(x / s, y / s); }
	/**
	 *  @brief 座標値のスカラ倍 (除算・代入)
	 *  @param[in] s  除算する値
	 *  @return    自身への参照
	 */
	Point &operator/=(Rational s){ return *this = *this / s; }

	/**
	 *  @brief 点同士の比較 (==, 厳密評価)
	 *  @param[in] p  比較する点
	 *  @retval    true   *thisとpが同じ座標を表している
	 *  @retval    false  *thisとpが同じ座標を表していない
	 */
	bool operator==(const Point &p) const { return x == p.x && y == p.y; }
	/**
	 *  @brief 点同士の比較 (!=, 厳密評価)
	 *  @param[in] p  比較する点
	 *  @retval    true   *thisとpが同じ座標を表していない
	 *  @retval    false  *thisとpが同じ座標を表している
	 */
	bool operator!=(const Point &p) const { return x != p.x || y != p.y; }
	/**
	 *  @brief 点同士の比較 (<, 厳密評価)
	 *  @param[in] p  比較する点
	 *  @retval    true   *thisがpより辞書順で小さい
	 *  @retval    false  *thisがpより辞書順で小さくない
	 */
	bool operator<(const Point &p) const {
		return (x == p.x) ? (y < p.y) : (x < p.x);
	}

	/**
	 *  @brief 原点との間の2乗ノルムを求める
	 *  @return (0, 0) と *this の2乗ノルム
	 */
	Rational norm() const { return x * x + y * y; }
	/**
	 *  @brief 直交するベクトルの計算
	 *  @return *this と直交するベクトル
	 */
	Point ortho() const { return Point(-y, x); }
};

/**
 *  @brief 座標値のスカラ倍
 *  @param[in] s  乗算する値
 *  @param[in] p  乗算する座標
 *  @return    (this->x * s, this->y * s)
 */
inline Point operator*(Rational s, const Point &p){ return p * s; }

/**
 *  @brief クロス積の計算
 *  @param[in] a  計算に用いる項
 *  @param[in] b  計算に用いる項
 *  @return       aとbのクロス積
 */
inline Rational cross(const Point &a, const Point &b){
	return a.x * b.y - a.y * b.x;
}
/**
 *  @brief ドット積の計算
 *  @param[in] a  計算に用いる項
 *  @param[in] b  計算に用いる項
 *  @return       aとbのドット積
 */
inline Rational dot(const Point &a, const Point &b){
	return a.x * b.x + a.y * b.y;
}
/**
 *  @brief 点の進行方向の計算
 *  @param[in] a   始点の座標
 *  @param[in] b   中点の座標
 *  @param[in] c   終点の座標
 *  @retval    0   曲線(a, b, c)が点bで180度曲がり点cが点a,bの間にある場合
 *  @retval    1   曲線(a, b, c)が点bで反時計回りに曲がっている場合
 *  @retval    -1  曲線(a, b, c)が点bで時計回りに曲がっている場合
 *  @retval    2   曲線(a, b, c)が点bで180度曲がり点cが点aを通り過ぎる場合
 *  @retval    -2  曲線(a, b, c)が一直線である場合
 */ 
inline int ccw(const Point &a, const Point &b, const Point &c){
	const Point d = b - a, e = c - a;
	if(cross(d, e) > Rational()){ return 1; }
	if(cross(d, e) < Rational()){ return -1; }
	if(dot(d, e) < Rational()){ return 2; }
	if(d.norm() < e.norm()){ return -2; }
	return 0;
}

/**
 *  @}
 */

