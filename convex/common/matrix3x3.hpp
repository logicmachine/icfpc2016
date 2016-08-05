#pragma once
#include "rational.hpp"
#include "point.hpp"

class Matrix3x3 {

private:
	Rational a[3][3];

public:
	Matrix3x3(){
		for(int i = 0; i < 3; ++i){
			for(int j = 0; j < 3; ++j){ a[i][j] = Rational(); }
		}
	}

	static Matrix3x3 identity(){
		Matrix3x3 m;
		m.a[0][0] = m.a[1][1] = m.a[2][2] = Rational(1);
		return m;
	}

	Matrix3x3 &operator+=(const Matrix3x3 &m){
		for(int i = 0; i < 3; ++i){
			for(int j = 0; j < 3; ++j){ a[i][j] += m.a[i][j]; }
		}
		return *this;
	}
	Matrix3x3 &operator-=(const Matrix3x3 &m){
		for(int i = 0; i < 3; ++i){
			for(int j = 0; j < 3; ++j){ a[i][j] -= m.a[i][j]; }
		}
		return *this;
	}
	Matrix3x3 &operator*=(const Matrix3x3 &m){
		Matrix3x3 t;
		for(int i = 0; i < 3; ++i){
			for(int j = 0; j < 3; ++j){
				for(int k = 0; k < 3; ++k){ t.a[i][j] += a[i][k] * m.a[k][j]; }
			}
		}
		return (*this = t);
	}

	Matrix3x3 operator+(const Matrix3x3 &m) const { auto c = *this; return c += m; }
	Matrix3x3 operator-(const Matrix3x3 &m) const { auto c = *this; return c -= m; }
	Matrix3x3 operator*(const Matrix3x3 &m) const { auto c = *this; return c *= m; }

	const Rational &operator()(int r, int c) const { return a[r][c]; }

	Point transform(const Point &v) const {
		const auto x = v.x * a[0][0] + v.y * a[0][1] + a[0][2];
		const auto y = v.x * a[1][0] + v.y * a[1][1] + a[1][2];
		return Point(x, y);
	}

	static Matrix3x3 transpose(Rational x, Rational y){
		Matrix3x3 m;
		m.a[0][0] = m.a[1][1] = m.a[2][2] = Rational(1);
		m.a[0][2] = x;
		m.a[1][2] = y;
		return m;
	}
	static Matrix3x3 reflection(Rational x, Rational y){
		const auto norm = x * x + y * y;
		Matrix3x3 m;
		m.a[0][0] = (x * x - y * y) / norm;
		m.a[0][1] = m.a[1][0] = x * y * Rational(2) / norm;
		m.a[1][1] = (y * y - x * x) / norm;
		m.a[2][2] = Rational(1);
		return m;
	}

};

