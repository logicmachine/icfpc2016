#pragma once
#include <iostream>
#include <sstream>
#include <algorithm>
#include <cctype>
#include <boost/multiprecision/cpp_int.hpp>

class Rational {

public:
	using IntType = boost::multiprecision::cpp_int;

private:
	IntType m_nume, m_deno;

	void normalize(){
		const auto g = std::__gcd(m_nume, m_deno);
		m_nume /= g;
		m_deno /= g;
		if(m_deno < 0){
			m_nume = -m_nume;
			m_deno = -m_deno;
		}
	}

public:
	Rational()
		: m_nume(0)
		, m_deno(1)
	{ }

	Rational(const IntType &nume)
		: m_nume(nume)
		, m_deno(1)
	{ }

	Rational(const IntType &nume, const IntType &deno)
		: m_nume(nume)
		, m_deno(deno)
	{
		normalize();
	}


	const IntType &nume() const { return m_nume; }
	const IntType &deno() const { return m_deno; }


	Rational operator-() const {
		return Rational(-m_nume, m_deno);
	}


	Rational operator+(const Rational &x) const {
		const auto g = std::__gcd(m_deno, x.m_deno);
		const auto deno = m_deno / g * x.m_deno;
		const auto nume = m_nume * (x.m_deno / g) + x.m_nume * (m_deno / g);
		return Rational(nume, deno);
	}
	Rational &operator+=(const Rational &x){
		return (*this = *this + x);
	}

	Rational operator-(const Rational &x) const {
		const auto g = std::__gcd(m_deno, x.m_deno);
		const auto deno = m_deno / g * x.m_deno;
		const auto nume = m_nume * (x.m_deno / g) - x.m_nume * (m_deno / g);
		return Rational(nume, deno);
	}
	Rational &operator-=(const Rational &x){
		return (*this = *this - x);
	}

	Rational operator*(const Rational &x) const {
		return Rational(m_nume * x.m_nume, m_deno * x.m_deno);
	}
	Rational &operator*=(const Rational &x){
		return (*this = *this * x);
	}

	Rational operator/(const Rational &x) const {
		return Rational(m_nume * x.m_deno, m_deno * x.m_nume);
	}
	Rational &operator/=(const Rational &x){
		return (*this = *this / x);
	}


	bool operator==(const Rational &x) const {
		return (m_nume == x.m_nume) && (m_deno == x.m_deno);
	}
	bool operator!=(const Rational &x) const {
		return !(*this == x);
	}

	bool operator<(const Rational &x) const {
		const auto g = std::__gcd(m_deno, x.m_deno);
		return (m_nume * (x.m_deno / g)) < (x.m_nume * (m_deno / g));
	}
	bool operator>(const Rational &x) const {
		return (x < *this);
	}
	bool operator<=(const Rational &x) const {
		return !(*this > x);
	}
	bool operator>=(const Rational &x) const {
		return !(*this < x);
	}


	Rational abs() const {
		if(m_nume < 0){ return -*this; }
		return *this;
	}
};

std::istream &operator>>(std::istream &is, Rational &x){
	while(isspace(is.peek())){ is.ignore(); }
	Rational::IntType nume, deno = 1;
	std::stringstream ss;
	if(is.peek() == '-'){ ss << static_cast<char>(is.get()); }
	while(isdigit(is.peek())){ ss << static_cast<char>(is.get()); }
	ss >> nume;
	if(is.peek() == '/'){
		is.ignore();
		ss.clear();
		while(isdigit(is.peek())){ ss << static_cast<char>(is.get()); }
		ss >> deno;
	}
	x = Rational(nume, deno);
	return is;
}

std::ostream &operator<<(std::ostream &os, const Rational &x){
	os << x.nume();
	if(x.deno() != 1){ os << "/" << x.deno(); }
	return os;
}

