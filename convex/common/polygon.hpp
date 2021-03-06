#pragma once
#include <vector>
#include "point.hpp"
#include "segment.hpp"

/**
 *  @defgroup polygon Polygon
 *  @ingroup  geometry_primitives
 *  @{
 */

/**
 *  @brief 多角形
 */
class Polygon {

private:
	std::vector<Point> m_points;

public:
	/**
	 *  @brief コンストラクタ
	 */
	Polygon() : m_points() { }
	/**
	 *  @brief コンストラクタ
	 *
	 *  頂点数sの多角形を生成する。
	 *
	 *  @param[in] s  生成する多角形の頂点数
	 *  @param[in] p  生成する多角形の各頂点の座標
	 */
	explicit Polygon(size_t s, const Point &p = Point())
		: m_points(s, p)
	{ }
	/**
	 *  @brief コンストラクタ
	 *
	 *  頂点列から多角形を生成する
	 *
	 *  @tparam    Iterator  イテレータの型
	 *  @param[in] begin     頂点列の先頭を指すイテレータ
	 *  @param[in] end       頂点列の終端を指すイテレータ
	 */
	template <typename Iterator>
	explicit Polygon(Iterator begin, Iterator end)
		: m_points(begin, end)
	{
		m_points.erase(
			std::unique(m_points.begin(), m_points.end()), m_points.end());
		while(!m_points.empty() && m_points.front() == m_points.back()){
			m_points.pop_back();
		}
	}
	/**
	 *  @brief コンストラクタ
	 *
	 *  頂点列から多角形を生成する
	 *
	 *  @param[in] list  頂点列の初期化リスト
	 */
	explicit Polygon(std::initializer_list<Point> init)
		: m_points(init)
	{
		m_points.erase(
			std::unique(m_points.begin(), m_points.end()), m_points.end());
		while(!m_points.empty() && m_points.front() == m_points.back()){
			m_points.pop_back();
		}
	}

	/**
	 *  @brief 頂点情報の取得
	 *  @param[in] i  取得したい頂点のインデックス
	 *  @return    i番目の頂点への参照
	 */
	const Point &operator[](int i) const { return m_points[i]; }
	/**
	 *  @brief 頂点情報の取得
	 *  @param[in] i  取得したい頂点のインデックス
	 *  @return    i番目の頂点への参照
	 */
	Point &operator[](int i){ return m_points[i]; }

	/**
	 *  @brief 辺の取得
	 *  @param[in] i  取得したい辺へのインデックス
	 *  @return    i番目の点とi+1番目の点からなる辺
	 */
	Segment side(int i) const {
		return Segment(m_points[i], m_points[(i + 1) % m_points.size()]);
	}

	/**
	 *  @brief 多角形の頂点数の取得
	 *  @return 多角形に含まれる頂点の数
	 */
	int size() const { return static_cast<int>(m_points.size()); }

	/**
	 *  @brief 多角形の比較 (<, 厳密評価)
	 *
	 *  コンテナで使用するためのもので数学的な意味はないことに注意。
	 *  (頂点の順番がローテートしている場合など)
	 *
	 *  @param[in] p      比較する値
	 *  @retval    true   *thisがpより辞書順で小さい場合
	 *  @retval    false  *thisがpより辞書順で大きい場合
	 */
	bool operator<(const Polygon &p) const {
		return m_points < p.m_points;
	}

	/**
	 *  @brief 多角形の面積
	 *    - 時間計算量: \f$ O(n) \f$
	 *
	 *  多角形の面積を求める。
	 *
	 *  @return 多角形の面積
	 */
	Rational area() const {
		Rational s;
		for(int i = 0; i < size(); ++i){
			s += cross(m_points[i], m_points[(i + 1) % size()]);
		}
		return s / Rational(2);
	}

	/**
	 *  @brief 点の内外判定
	 *    - 時間計算量: \f$ O(n) \f$
	 *
	 *  点pが多角形の内部にあるかを調べる。
	 *
	 *  @param[in] p   判定する点
	 *  @retval    1   点pが多角形の内部にある
	 *  @retval    0   点pが多角形の辺上にある
	 *  @retval    -1  点pが多角形の外部にある
	 */
	int contains(const Point &p) const {
		int result = -1;
		for(int i = 0; i < size(); ++i){
			Point a = m_points[i] - p;
			Point b = m_points[(i + 1) % size()] - p;
			if(a.y > b.y){ std::swap(a, b); }
			if(a.y <= Rational() && b.y > Rational() && cross(a, b) < Rational()){
				result = -result;
			}
			if(cross(a, b) == Rational() && dot(a, b) <= Rational()){ return 0; }
		}
		return result;
	}

	/**
	 *  @brief 単純多角形の三角形分割
	 *    - 時間計算量: \f$ O(n^2) \f$
	 *
	 *  @return 三角形分割の結果として得られた三角形の集合
	 */
	/*
	std::vector<Polygon> triangulate() const {
		const int n = size();
		const Polygon &self = *this;
		std::vector<Polygon> triangles;
		DoubleLinkedList dll(n, true);
		for(int cur = 0, count = 0; count + 2 < n; cur = dll.next(cur)){
			const int prev = dll.prev(cur), next = dll.next(cur);
			if(ccw(self[prev], self[cur], self[next]) <= 0){ continue; }
			bool accept = true;
			for(int i = 0; accept && i < n; ++i){
				if(i == prev || i == cur || i == next){ continue; }
				const Point &p = self[i];
				if(ccw(self[prev], self[cur],  p) < 0){ continue; }
				if(ccw(self[cur],  self[next], p) < 0){ continue; }
				if(ccw(self[next], self[prev], p) < 0){ continue; }
				accept = false;
			}
			if(accept){
				triangles.push_back(Polygon({
					self[prev], self[cur], self[next]
				}));
				dll.erase(cur);
				++count;
			}
		}
		return triangles;
	}
	*/
};

