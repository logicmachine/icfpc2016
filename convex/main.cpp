// g++ -std=c++11 -O2 main.cpp
#include <iostream>
#include <vector>
#include <map>
#include "common/polygon.hpp"
#include "common/convex.hpp"
#include "common/matrix3x3.hpp"
#include "common/problem.hpp"

using namespace std;

Polygon destination_convex(const Problem &problem){
	vector<Point> all_points;
	for(const auto &s : problem.skelton){
		all_points.push_back(s.a);
		all_points.push_back(s.b);
	}
	return convex_hull(all_points);
}

int main(){
	const auto problem = Problem::load(std::cin);
	auto convex = destination_convex(problem);
	const int n = convex.size();
	Point shift = convex[0];
	for(int i = 0; i < n; ++i){
		shift.x = min(shift.x, convex[i].x);
		shift.y = min(shift.y, convex[i].y);
	}
	for(int i = 0; i < n; ++i){ convex[i] -= shift; }
	using TransposedPolygon = pair<Polygon, Matrix3x3>;
	vector<TransposedPolygon> current;
	current.emplace_back(
		Polygon({
			Point(Rational(0), Rational(0)),
			Point(Rational(1), Rational(0)),
			Point(Rational(1), Rational(1)),
			Point(Rational(0), Rational(1))
		}),
		Matrix3x3::identity());
	while(true){
		bool modified = false;
		for(int i = 0; i < n; ++i){
			const auto line = convex.side(i).to_line();
			const auto inv_refmat =
				Matrix3x3::transpose(line.a.x, line.a.y) *
				Matrix3x3::reflection(line.b.x - line.a.x, line.b.y - line.a.y) *
				Matrix3x3::transpose(-line.a.x, -line.a.y);
			vector<TransposedPolygon> next;
			for(const auto &tp : current){
				const auto l = convex_cut(tp.first, line);
				if(l.area() == Rational()){
					modified = true;
				}else{
					next.emplace_back(l, tp.second);
				}
				const auto r = convex_cut(tp.first, Line(line.b, line.a));
				if(r.area() == Rational()){ continue; }
				modified = true;
				vector<Point> reflected(r.size());
				for(int j = 0; j < r.size(); ++j){
					reflected[r.size() - 1 - j] = line.reflection(r[j]);
				}
				next.emplace_back(Polygon(
					reflected.begin(), reflected.end()), tp.second * inv_refmat);
			}
			next.swap(current);
		}
		if(!modified){ break; }
	}

	map<Point, int> p2i_table;
	map<Point, Point> p2p_table;
	vector<Point> i2p_table;
	for(const auto &tp : current){
		const int m = tp.first.size();
		for(int i = 0; i < m; ++i){
			const auto &p = tp.first[i];
			const auto q = tp.second.transform(p);
			const auto it = p2i_table.find(q);
			if(it == p2i_table.end()){
				p2i_table.emplace(q, i2p_table.size());
				p2p_table.emplace(q, p);
				i2p_table.push_back(q);
			}
		}
	}

	cout << i2p_table.size() << endl;
	for(const auto &p : i2p_table){ cout << p.x << "," << p.y << endl; }

	cout << current.size() << endl;
	for(const auto &tp : current){
		const int m = tp.first.size();
		cout << m;
		for(int i = 0; i < m; ++i){
			const auto q = tp.second.transform(tp.first[i]);
			cout << " " << p2i_table[q];
		}
		cout << endl;
	}

	for(const auto &p : i2p_table){
		const auto v = p2p_table[p] + shift;
		cout << v.x << "," << v.y << endl;
	}

	return 0;
}

