import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class FillingSolver {

	ArrayList<Point<Rational>> points = new ArrayList<>();
	HashMap<Point<Rational>, Integer> pointToIdx = new HashMap<>();
	Polygon[] polygons;
	Edge[] edges;
	ArrayList<Part> parts = new ArrayList<>();

	void readInput() {
		try (Scanner sc = new Scanner(System.in)) {
			int NP = sc.nextInt();
			polygons = new Polygon[NP];
			for (int i = 0; i < NP; ++i) {
				int NV = sc.nextInt();
				polygons[i] = new Polygon();
				polygons[i].vs = new int[NV];
				ArrayList<Point<Rational>> polygon = new ArrayList<>();
				for (int j = 0; j < NV; ++j) {
					String[] coord = sc.next().split(",");
					Rational x = new Rational(coord[0]);
					Rational y = new Rational(coord[1]);
					Point<Rational> p = new Point<Rational>(x, y);
					polygon.add(p);
					if (!pointToIdx.containsKey(p)) {
						pointToIdx.put(p, points.size());
						points.add(p);
					}
					polygons[i].vs[j] = pointToIdx.get(p);
				}
				polygons[i].ccw = isCcw(polygons[i]);
			}
			int NE = sc.nextInt();
			edges = new Edge[NE];
			for (int i = 0; i < NE; ++i) {
				String[] pos1 = sc.next().split(",");
				Rational x1 = new Rational(pos1[0]);
				Rational y1 = new Rational(pos1[1]);
				Point<Rational> p1 = new Point<Rational>(x1, y1);
				if (!pointToIdx.containsKey(p1)) {
					pointToIdx.put(p1, points.size());
					points.add(p1);
				}
				int ep1 = pointToIdx.get(p1);

				String[] pos2 = sc.next().split(",");
				Rational x2 = new Rational(pos2[0]);
				Rational y2 = new Rational(pos2[1]);
				Point<Rational> p2 = new Point<Rational>(x2, y2);
				if (!pointToIdx.containsKey(p2)) {
					pointToIdx.put(p2, points.size());
					points.add(p2);
				}
				int ep2 = pointToIdx.get(p2);
				edges[i] = new Edge(ep1, ep2);
			}
		}
	}

	void edgesArrangement() {
		ArrayList<Edge> newEdges = new ArrayList<>();
		for (int i = 0; i < edges.length; ++i) {
			HashSet<Point<Rational>> pointSet = new HashSet<>();
			for (int j = 0; j < edges.length; ++j) {
				if (j == i) continue;
				Point<Rational> cp = getIntersectPoint(edges[i], edges[j]);
				if (cp != null && !cp.equals(points.get(edges[i].p1)) && !cp.equals(points.get(edges[i].p2))) {
					if (!pointToIdx.containsKey(cp)) {
						pointToIdx.put(cp, points.size());
						points.add(cp);
					}
					pointSet.add(cp);
				}
			}
			ArrayList<Point<Rational>> list = new ArrayList<>(pointSet);
			Collections.sort(list);
			Point<Rational> prev = points.get(edges[i].p1);
			if (points.get(edges[i].p1).compareTo(points.get(edges[i].p2)) < 0) {
				for (int j = 0; j < list.size(); ++j) {
					Point<Rational> cur = list.get(j);
					newEdges.add(new Edge(pointToIdx.get(prev), pointToIdx.get(cur)));
					prev = cur;
				}
			} else {
				for (int j = list.size() - 1; j >= 0; --j) {
					Point<Rational> cur = list.get(j);
					newEdges.add(new Edge(pointToIdx.get(prev), pointToIdx.get(cur)));
					prev = cur;
				}
			}
			newEdges.add(new Edge(pointToIdx.get(prev), edges[i].p2));
		}
		edges = newEdges.toArray(new Edge[0]);
	}

	Point<Rational> getIntersectPoint(Edge e1, Edge e2) {
		Rational x1 = points.get(e1.p1).x;
		Rational y1 = points.get(e1.p1).y;
		Rational x2 = points.get(e1.p2).x;
		Rational y2 = points.get(e1.p2).y;
		Rational x3 = points.get(e2.p1).x;
		Rational y3 = points.get(e2.p1).y;
		Rational x4 = points.get(e2.p2).x;
		Rational y4 = points.get(e2.p2).y;
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
		return new Point<Rational>(cx1, cy1);
	}

	void decompositeParts() {
		for (int i = 0; i < edges.length; ++i) {

		}
	}

	Part extractPart(int start, int next) {
		Part ret = new Part();
		int cur = next;

		return ret;
	}

	boolean isCcw(Polygon polygon) {
		Rational sum = Rational.ZERO;
		Point<Rational> prev = points.get(polygon.vs[polygon.vs.length - 1]);
		for (int i = 0; i < polygon.vs.length; ++i) {
			Point<Rational> cur = points.get(polygon.vs[i]);
			sum = sum.add(cur.y.mul(prev.x).sub(cur.x.mul(prev.y)));
			prev = cur;
		}
		return sum.num.signum() > 0;
	}

	static Point<BigInteger> scaleToInt(Point<Rational> p, BigInteger mul) {
		return new Point<BigInteger>(scaleToInt(p.x, mul), scaleToInt(p.y, mul));
	}

	static BigInteger scaleToInt(Rational r, BigInteger mul) {
		return r.num.multiply(mul.divide(r.den));
	}

	static BigInteger lcm(BigInteger a, BigInteger b) {
		return a.divide(a.gcd(b)).multiply(b);
	}

	public static void main(String[] args) {
		FillingSolver solver = new FillingSolver();
		solver.readInput();
		solver.edgesArrangement();
		System.out.println(solver.points);
		System.out.println(Arrays.toString(solver.polygons));
		System.out.println(Arrays.toString(solver.edges));
		solver.decompositeParts();
	}
}

class Part {
	ArrayList<Integer> vs;
}

class Edge {
	int p1, p2;
	boolean usedForward, usedBackward;

	public Edge(int p1, int p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	@Override
	public String toString() {
		return "[" + p1 + "," + p2 + "]";
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

class Point<T extends Comparable<T>> implements Comparable<Point<T>> {
	T x, y;

	Point(T x, T y) {
		this.x = x;
		this.y = y;
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
	public int compareTo(Point<T> o) {
		int ret = this.x.compareTo(o.x);
		if (ret != 0) return ret;
		return this.y.compareTo(o.y);
	}

}
