import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class FillingSolver {

	ArrayList<Point<BigInteger>> points = new ArrayList<>();
	int[][] polygons;
	Edge[] edges;
	BigInteger scale;

	void readInput() {
		try (Scanner sc = new Scanner(System.in)) {
			int NP = sc.nextInt();
			ArrayList<ArrayList<Point<Rational>>> rawPolygons = new ArrayList<>();
			scale = BigInteger.ONE;
			for (int i = 0; i < NP; ++i) {
				int NV = sc.nextInt();
				ArrayList<Point<Rational>> polygon = new ArrayList<>();
				for (int j = 0; j < NV; ++j) {
					String[] coord = sc.next().split(",");
					Rational x = new Rational(coord[0]);
					Rational y = new Rational(coord[1]);
					polygon.add(new Point<Rational>(x, y));
					scale = lcm(scale, x.den);
					scale = lcm(scale, y.den);
				}
				rawPolygons.add(polygon);
			}
			int NE = sc.nextInt();
			ArrayList<ArrayList<Point<Rational>>> rawEdges = new ArrayList<>();
			for (int i = 0; i < NE; ++i) {
				ArrayList<Point<Rational>> edge = new ArrayList<>();
				String[] pos1 = sc.next().split(",");
				Rational x1 = new Rational(pos1[0]);
				Rational y1 = new Rational(pos1[1]);
				edge.add(new Point<Rational>(x1, y1));
				String[] pos2 = sc.next().split(",");
				Rational x2 = new Rational(pos2[0]);
				Rational y2 = new Rational(pos2[1]);
				edge.add(new Point<Rational>(x2, y2));
				scale = lcm(scale, x1.den);
				scale = lcm(scale, y1.den);
				scale = lcm(scale, x2.den);
				scale = lcm(scale, y2.den);
				rawEdges.add(edge);
			}

			HashMap<Point<BigInteger>, Integer> pointToIdx = new HashMap<>();
			polygons = new int[NP][];
			for (int i = 0; i < NP; ++i) {
				ArrayList<Point<Rational>> polygon = rawPolygons.get(i);
				polygons[i] = new int[polygon.size()];
				for (int j = 0; j < polygon.size(); ++j) {
					Point<BigInteger> p = scaleToInt(polygon.get(j), scale);
					if (!pointToIdx.containsKey(p)) {
						pointToIdx.put(p, points.size());
						points.add(p);
					}
					polygons[i][j] = pointToIdx.get(p);
				}
			}
			edges = new Edge[NE];
			for (int i = 0; i < NE; ++i) {
				int p1, p2;
				{
					Point<BigInteger> p = scaleToInt(rawEdges.get(i).get(0), scale);
					if (!pointToIdx.containsKey(p)) {
						pointToIdx.put(p, points.size());
						points.add(p);
					}
					p1 = pointToIdx.get(p);
				}
				{
					Point<BigInteger> p = scaleToInt(rawEdges.get(i).get(1), scale);
					if (!pointToIdx.containsKey(p)) {
						pointToIdx.put(p, points.size());
						points.add(p);
					}
					p2 = pointToIdx.get(p);
				}
				edges[i] = new Edge(p1, p2);
			}
		}
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
		System.out.println(solver.points);
		System.out.println(Arrays.deepToString(solver.polygons));
		System.out.println(Arrays.toString(solver.edges));
	}
}

class Edge {
	int p1, p2;

	public Edge(int p1, int p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	@Override
	public String toString() {
		return "Edge [" + p1 + "," + p2 + "]";
	}
}

class Point<T> {
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
		return "Point (" + x + ", " + y + ")";
	}
}
