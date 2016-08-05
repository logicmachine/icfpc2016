import java.math.BigInteger;
import java.util.Arrays;

public class Geometry {
	static boolean isOnLine(Point pos1, Point pos2, Point pos3, Point pos4) {
		if (!area2(pos1, pos2, pos3).equals(Rational.ZERO)) return false;
		if (!area2(pos1, pos2, pos4).equals(Rational.ZERO)) return false;
		Point min1 = pos1.compareTo(pos2) < 0 ? pos1 : pos2;
		Point max1 = pos1.compareTo(pos2) < 0 ? pos2 : pos1;
		Point min2 = pos3.compareTo(pos4) < 0 ? pos3 : pos4;
		Point max2 = pos3.compareTo(pos4) < 0 ? pos4 : pos3;
		if (max1.compareTo(min2) <= 0) return false;
		if (min1.compareTo(max2) >= 0) return false;
		return true;
	}

	static Rational area2(Point p1, Point p2, Point p3) {
		Rational dx1 = p1.x.sub(p3.x);
		Rational dy1 = p1.y.sub(p3.y);
		Rational dx2 = p2.x.sub(p3.x);
		Rational dy2 = p2.y.sub(p3.y);
		return dy1.mul(dx2).sub(dx1.mul(dy2));
	}

	static Point getIntersectPoint(Point p1, Point p2, Point p3, Point p4) {
		Rational x1 = p1.x;
		Rational y1 = p1.y;
		Rational x2 = p2.x;
		Rational y2 = p2.y;
		Rational x3 = p3.x;
		Rational y3 = p3.y;
		Rational x4 = p4.x;
		Rational y4 = p4.y;
		Rational dx2 = x4.sub(x3);
		Rational dy2 = y4.sub(y3);
		Rational s1 = dx2.mul(y1.sub(y3)).sub(dy2.mul(x1.sub(x3)));
		Rational s2 = dx2.mul(y3.sub(y2)).sub(dy2.mul(x3.sub(x2)));
		Rational ss = s1.add(s2);
		if (ss.equals(Rational.ZERO)) return null;
		Rational cx1 = x1.add(x2.sub(x1).mul(s1).div(ss));
		Rational cy1 = y1.add(y2.sub(y1).mul(s1).div(ss));
		if (cx1.compareTo(x1) * cx1.compareTo(x2) > 0) {
			return null;
		}
		if (cy1.compareTo(y1) * cy1.compareTo(y2) > 0) {
			return null;
		}
		if (cx1.compareTo(x3) * cx1.compareTo(x4) > 0) {
			return null;
		}
		if (cy1.compareTo(y3) * cy1.compareTo(y4) > 0) {
			return null;
		}
		return new Point(cx1, cy1);
	}

}

class Polygon {
	int[] vs;
	boolean ccw;

	@Override
	public String toString() {
		return "ccw:" + ccw + " " + Arrays.toString(vs);
	}
}

class Point implements Comparable<Point> {
	Rational x, y;

	Point(Rational x, Rational y) {
		this.x = x;
		this.y = y;
	}

	String toICFPStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(x.num);
		if (!x.den.equals(BigInteger.ONE)) {
			sb.append("/" + x.den);
		}
		sb.append(",");
		sb.append(y.num);
		if (!y.den.equals(BigInteger.ONE)) {
			sb.append("/" + y.den);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return this.x.hashCode() * 31 ^ this.y.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		Point other = (Point) obj;
		return this.x.equals(other.x) && this.y.equals(other.y);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	@Override
	public int compareTo(Point o) {
		int ret = this.x.compareTo(o.x);
		if (ret != 0) return ret;
		return this.y.compareTo(o.y);
	}

}
