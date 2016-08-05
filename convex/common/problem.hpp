#pragma once
#include "polygon.hpp"

struct Problem {
	std::vector<Polygon> silhouette;
	std::vector<Segment> skelton;

	static Problem load(std::istream &is){
		Problem problem;
		int n;
		is >> n;
		for(int i = 0; i < n; ++i){
			int m;
			is >> m;
			std::vector<Point> vs;
			for(int j = 0; j < m; ++j){
				Rational x, y;
				is >> x;
				is.ignore();
				is >> y;
				vs.emplace_back(x, y);
			}
			problem.silhouette.emplace_back(vs.begin(), vs.end());
		}
		int m;
		is >> m;
		for(int i = 0; i < m; ++i){
			Rational sx, sy, tx, ty;
			is >> sx;
			is.ignore();
			is >> sy;
			is >> tx;
			is.ignore();
			is >> ty;
			problem.skelton.emplace_back(Point(sx, sy), Point(tx, ty));
		}
		return problem;
	}
};

