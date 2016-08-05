#pragma once
#include "point.hpp"
#include "line.hpp"

/**
 *  @defgroup segment Segment
 *  @ingroup  geometry_primitives
 *  @{
 */

/**
 *  @brief 線分
 */
struct Segment {
	/// 線分の始点
	Point a;
	/// 線分の終点
	Point b;

	/**
	 *  @brief コンストラクタ
	 */
	Segment() : a(), b() { }

	/**
	 *  @brief コンストラクタ
	 *  @param[in] a  線分の始点
	 *  @param[in] b  線分の終点
	 */
	Segment(const Point &a, const Point &b) :
		a(a), b(b)
	{ }

	/**
	 *  @brief 線分の比較 (<, 厳密評価)
	 *
	 *  コンテナで使用するためのもので数学的な意味はないことに注意。
	 *
	 *  @param[in] s      比較する値
	 *  @retval    true   *thisがsより辞書順で小さい場合
	 *  @retval    false  *thisがsより辞書順で大きい場合
	 */
	bool operator<(const Segment &s) const {
		return (a == s.a) ? (b < s.b) : (a < s.a);
	}

	/**
	 *  @brief 線分を含む直線の生成
	 *  @return 線分 *this を含む直線
	 */
	Line to_line() const { return Line(a, b); }

	/**
	 *  @brief 線分の長さの2乗の計算
	 *  @return 線分の長さの2乗
	 */
	Rational length2() const { return (b - a).norm(); }
};

/**
 *  @}
 */

