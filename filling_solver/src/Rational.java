import java.math.BigInteger;

public class Rational implements Comparable<Rational> {
	final BigInteger num;
	final BigInteger den;

	Rational(BigInteger n, BigInteger d) {
		if (n.equals(BigInteger.ZERO)) {
			num = BigInteger.ZERO;
			den = BigInteger.ONE;
		} else {
			BigInteger gcd = n.gcd(d);
			num = n.divide(gcd);
			den = d.divide(gcd);
		}
	}

	Rational(String str) {
		String[] numden = str.split("/");
		this.num = new BigInteger(numden[0]);
		if (numden.length == 1) {
			this.den = BigInteger.ONE;
		} else {
			this.den = new BigInteger(numden[1]);
		}
	}

	Rational mul(Rational v) {
		return new Rational(this.num.multiply(v.num), this.den.multiply(v.den));
	}

	Rational div(Rational v) {
		return new Rational(this.num.multiply(v.den), this.den.multiply(v.num));
	}

	Rational add(Rational v) {
		return new Rational(this.num.multiply(v.den).add(v.num.multiply(this.den)), this.den.multiply(v.den));
	}

	public int compareTo(Rational r) {
		BigInteger n1 = this.num.multiply(r.den);
		BigInteger n2 = this.den.multiply(r.num);
		return n1.compareTo(n2);
	}

	public String toString() {
		return this.num + "/" + this.den;
	}

	public int hashCode() {
		return this.num.hashCode() ^ this.den.hashCode();
	}

	public boolean equals(Object obj) {
		Rational other = (Rational) obj;
		if (!den.equals(other.den)) return false;
		if (!num.equals(other.num)) return false;
		return true;
	}

}
