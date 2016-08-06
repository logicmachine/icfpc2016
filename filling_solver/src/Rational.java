import java.math.BigInteger;

public class Rational implements Comparable<Rational> {
	final BigInteger num;
	final BigInteger den;
	static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);
	static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);

	Rational(BigInteger n, BigInteger d) {
		if (n.equals(BigInteger.ZERO)) {
			num = BigInteger.ZERO;
			den = BigInteger.ONE;
		} else {
			BigInteger gcd = n.gcd(d);
			if (d.signum() < 0 && gcd.signum() > 0) { // invariant: den > 0
				gcd = gcd.negate();
			}
			num = n.divide(gcd);
			den = d.divide(gcd);
		}
	}

	Rational(String str) {
		String[] numden = str.split("/");
		BigInteger n = new BigInteger(numden[0]);
		if (n.equals(BigInteger.ZERO)) {
			num = BigInteger.ZERO;
			den = BigInteger.ONE;
		} else {
			BigInteger d = numden.length == 1 ? BigInteger.ONE : new BigInteger(numden[1]);
			BigInteger gcd = n.gcd(d);
			if (d.signum() < 0 && gcd.signum() > 0) { // invariant: den > 0
				gcd = gcd.negate();
			}
			num = n.divide(gcd);
			den = d.divide(gcd);
		}
	}

	Rational mul(Rational v) {
		if (v.num.equals(BigInteger.ZERO)) return Rational.ZERO;
		if (v.equals(Rational.ONE)) return this;
		return new Rational(this.num.multiply(v.num), this.den.multiply(v.den));
	}

	Rational div(Rational v) {
		return new Rational(this.num.multiply(v.den), this.den.multiply(v.num));
	}

	Rational add(Rational v) {
		if (v.num.equals(BigInteger.ZERO)) return this;
		return new Rational(this.num.multiply(v.den).add(v.num.multiply(this.den)), this.den.multiply(v.den));
	}

	Rational sub(Rational v) {
		if (v.num.equals(BigInteger.ZERO)) return this;
		return new Rational(this.num.multiply(v.den).subtract(v.num.multiply(this.den)), this.den.multiply(v.den));
	}

	Rational negate() {
		return new Rational(this.num.negate(), this.den);
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
