#pragma once
#include <vector>
#include "line.hpp"
#include "segment.hpp"
#include "intersect.hpp"

/**
 *  @brief 直線と直線の交点
 *  @param[in] a  直線
 *  @param[in] b  直線
 *  @return    aとbの交点の座標。
 *             aとbが等しい場合は直線上の2点を返す。
 */
inline std::vector<Point> crossing_points(const Line &a, const Line &b){
	std::vector<Point> ret;
	if(tolerant_eq(a, b)){
		ret.push_back(a.a);
		ret.push_back(a.b);
	}else{
		const auto x = cross(a.b - a.a, b.b - b.a);
		const auto y = cross(a.b - a.a, a.b - b.a);
		if(x.abs() > Rational()){ ret.push_back(b.a + y / x * (b.b - b.a)); }
	}
	return ret;
}

/**
 *  @brief 直線と線分の交点
 *  @param[in] l  直線
 *  @param[in] s  線分
 *  @return    lとsの交点の座標。
 *             sがlに含まれている場合はsの端点2つを返す。
 */
inline std::vector<Point> crossing_points(const Line &l, const Segment &s){
	if(intersect(s.a, l) && intersect(s.b, l)){
		return std::vector<Point>({ s.a, s.b });
	}
	std::vector<Point> ret = crossing_points(l, s.to_line());
	if(ret[0].x < std::min(s.a.x, s.b.x)){ return std::vector<Point>(); }
	if(ret[0].x > std::max(s.a.x, s.b.x)){ return std::vector<Point>(); }
	if(ret[0].y < std::min(s.a.y, s.b.y)){ return std::vector<Point>(); }
	if(ret[0].y > std::max(s.a.y, s.b.y)){ return std::vector<Point>(); }
	return ret;
}
/**
 *  @brief 線分と直線の交点
 *  @param[in] s  線分
 *  @param[in] l  直線
 *  @return    lとsの交点の座標。
 *             sがlに含まれている場合はsの端点2つを返す。
 */
inline std::vector<Point> crossing_points(const Segment &s, const Line &l){
	return crossing_points(l, s);
}

