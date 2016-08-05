#pragma once
#include "point.hpp"
#include "line.hpp"
#include "segment.hpp"

/**
 *  @defgroup intersect Intersect
 *  @ingroup  geometry
 *  @{
 */

/**
 *  @brief 点と直線の交差判定
 *  @param[in] a      判定する点
 *  @param[in] b      判定する直線
 *  @retval    true   aとbが交差している
 *  @retval    false  aとbが交差していない
 */
inline bool intersect(const Point &a, const Line &b){
	return cross(b.b - a, b.a - a) == Rational();
}

/**
 *  @brief 直線と点の交差判定
 *  @param[in] a      判定する直線
 *  @param[in] b      判定する点
 *  @retval    true   aとbが交差している
 *  @retval    false  aとbが交差していない
 */
inline bool intersect(const Line &a, const Point &b){
	return intersect(b, a);
}

/**
 *  @brief 直線と直線の交差判定
 *  @param[in] a      判定する直線
 *  @param[in] b      判定する直線
 *  @retval    true   aとbが交差している
 *  @retval    false  aとbが交差していない
 */
inline bool intersect(const Line &a, const Line &b){
	if(cross(a.b - a.a, b.b - b.a) > Rational()){ return true; }
	return tolerant_eq(a, b);
}

/**
 *  @brief 直線と線分の交差判定
 *  @param[in] a      判定する直線
 *  @param[in] b      判定する線分
 *  @retval    true   aとbが交差している
 *  @retval    false  aとbが交差していない
 */
inline bool intersect(const Line &a, const Segment &b){
	return cross(a.b - a.a, b.a - a.a) * cross(a.b - a.a, b.b - a.a) == Rational();
}

/**
 *  @brief 線分と直線の交差判定
 *  @param[in] a      判定する線分
 *  @param[in] b      判定する直線
 *  @retval    true   aとbが交差している
 *  @retval    false  aとbが交差していない
 */
inline bool intersect(const Segment &a, const Line &b){
	return intersect(b, a);
}

/**
 *  @brief 線分と線分の交差判定
 *  @param[in] a      判定する線分
 *  @param[in] b      判定する線分
 *  @retval    true   aとbが交差している
 *  @retval    false  aとbが交差していない
 */
inline bool intersect(const Segment &a, const Segment &b){
	if(ccw(a.a, a.b, b.a) * ccw(a.a, a.b, b.b) > 0){ return false; }
	if(ccw(b.a, b.b, a.a) * ccw(b.a, b.b, a.b) > 0){ return false; }
	return true;
}

