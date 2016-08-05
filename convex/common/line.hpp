#pragma once
#include "point.hpp"


/**
 *  @defgroup line Line
 *  @ingroup  geometry_primitives
 *  @{
 */

/**
 *  @brief 直線
 */
struct Line {
	/// 直線状の点
	Point a;
	/// 直線状の点
	Point b;

	/**
	 *  @brief コンストラクタ
	 */
	Line() : a(), b() { }

	/**
	 *  @brief コンストラクタ
	 *  @param[in] a  直線状の点
	 *  @param[in] b  直線状の点 (a != b)
	 */
	Line(const Point &a, const Point &b) :
		a(a), b(b)
	{ }

	/**
	 *  @brief 直線の比較 (<, 厳密評価)
	 *
	 *  コンテナで使用するためのもので数学的な意味はないことに注意。
	 *  (同一直線ではあるものの選んでいる点が異なる場合など)
	 *
	 *  @param[in] l      比較する値
	 *  @retval    true   *thisがlより辞書順で小さい場合
	 *  @retval    false  *thisがlより辞書順で小さくない場合
	 */
	bool operator<(const Line &l) const {
		return (a == l.a) ? (b < l.b) : (a < l.a);
	}

	/**
	 *  @brief 点の直線への射影
	 *  @param[in] p  射影する点
	 *  @return    直線上で最も点pに近くなる座標
	 */
	Point projection(const Point &p) const {
		const auto t = dot(p - a, b - a) / (b - a).norm();
		return a + t * (b - a);
	}
	/**
	 *  @brief 直線を挟んで対称な位置にある点の計算
	 *  @param[in] p  変換する点
	 *  @return    直線を挟んで点pと対称な点
	 */
	Point reflection(const Point &p) const {
		return p + Rational(2) * (projection(p) - p);
	}
};

/**
 *  @brief 直線の比較 (==, 誤差許容, 無向)
 *  @param[in] a      比較する値
 *  @param[in] b      比較する値
 *  @retval    true   aとbが同じ直線を表している場合
 *  @retval    false  aとbが同じ直線を表していない場合
 */
inline bool tolerant_eq(const Line &a, const Line &b){
	const auto x = cross(a.b - a.a, b.b - b.a);
	const auto y = cross(a.b - a.a, a.b - b.a);
	return x == Rational() && y == Rational();
}

/**
 *  @brief 直線と点の進行方向
 *  @param[in] l  直線
 *  @param[in] p  点
 *  @retval    0   曲線(l.a, l.b, p)が点l.bで180度曲がり点pが点l.a, l.bの間にある場合
 *  @retval    1   曲線(l.a, l.b, p)が点l.bで反時計回りに曲がっている場合
 *  @retval    -1  曲線(l.a, l.b, p)が点l.bで時計回りに曲がっている場合
 *  @retval    2   曲線(l.a, l.b, p)が点l.bで180度曲がり点pが点l.aを通り過ぎる場合
 *  @retval    -2  曲線(l.a, l.b, p)が一直線である場合
 */
inline int ccw(const Line &l, const Point &p){
	return ccw(l.a, l.b, p);
}

/**
 *  @}
 */

