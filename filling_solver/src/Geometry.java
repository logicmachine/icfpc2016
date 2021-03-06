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

	static Rational cosSq(Point p1, Point p2, Point p3) { // p1 -> p2 -> p3
		Rational dx1 = p2.x.sub(p1.x);
		Rational dy1 = p2.y.sub(p1.y);
		Rational dx2 = p3.x.sub(p2.x);
		Rational dy2 = p3.y.sub(p2.y);
		Rational dot = dx1.mul(dy1).add(dx2.mul(dy2));
		Rational norm1 = dx1.mul(dx1).add(dy1.mul(dy1));
		Rational norm2 = dx2.mul(dx2).add(dy2.mul(dy2));
		return dot.mul(dot).div(norm1).div(norm2);
	}

	static boolean isColinear(Point p1, Point p2, Point p3) {
		Rational dx1 = p2.x.sub(p1.x);
		Rational dy1 = p2.y.sub(p1.y);
		Rational dx2 = p3.x.sub(p2.x);
		Rational dy2 = p3.y.sub(p2.y);
		return dy1.mul(dx2).add(dx1.mul(dy2)).equals(Rational.ZERO);
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
		Rational sr = s1.div(ss);
		Rational cx1 = x1.add(x2.sub(x1).mul(sr));
		Rational cy1 = y1.add(y2.sub(y1).mul(sr));
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

	Point transform(Rational dx, Rational dy) {
		return new Point(x.add(dx), y.add(dy));
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

class AffineTransform {
	Rational[][] mat = new Rational[3][3];

	AffineTransform() {}

	AffineTransform(Rational m00, Rational m01, Rational m10, Rational m11) {
		mat[0][2] = mat[1][2] = mat[2][0] = mat[2][1] = Rational.ZERO;
		mat[2][2] = Rational.ONE;
		mat[0][0] = m00;
		mat[0][1] = m01;
		mat[1][0] = m10;
		mat[1][1] = m11;
	}

	static AffineTransform transform(Rational dx, Rational dy) {
		AffineTransform ret = new AffineTransform();
		ret.mat[0][0] = ret.mat[1][1] = ret.mat[2][2] = Rational.ONE;
		ret.mat[0][1] = ret.mat[1][0] = ret.mat[2][0] = ret.mat[2][1] = Rational.ZERO;
		ret.mat[0][2] = dx;
		ret.mat[1][2] = dy;
		return ret;
	}

	static AffineTransform rot(Rational cos, Rational sin) {
		AffineTransform ret = new AffineTransform();
		ret.mat[2][2] = Rational.ONE;
		ret.mat[0][1] = ret.mat[0][2] = ret.mat[1][0] = ret.mat[1][2] = ret.mat[2][0] = ret.mat[2][1] = Rational.ZERO;
		ret.mat[0][0] = ret.mat[1][1] = cos;
		ret.mat[0][1] = sin;
		ret.mat[1][0] = sin.negate();
		return ret;
	}

	AffineTransform apply(AffineTransform t) {
		AffineTransform ret = new AffineTransform();
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				ret.mat[i][j] = t.mat[i][0].mul(this.mat[0][j]).add(t.mat[i][1].mul(this.mat[1][j])).add(t.mat[i][2].mul(this.mat[2][j]));
			}
		}
		return ret;
	}

	Point apply(Point p) {
		Rational nx = mat[0][0].mul(p.x).add(mat[0][1].mul(p.y)).add(mat[0][2]);
		Rational ny = mat[1][0].mul(p.x).add(mat[1][1].mul(p.y)).add(mat[1][2]);
		return new Point(nx, ny);
	}

	@Override
	public String toString() {
		return "AffineTransform: " + Arrays.deepToString(mat);
	}

}
