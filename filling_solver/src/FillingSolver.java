import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class FillingSolver {

	ArrayList<Point<Rational>> points = new ArrayList<>();
	int[][] polygons;
	Edge[] edges;

	void readInput() {
		try (Scanner sc = new Scanner(System.in)) {
			HashMap<Point<Rational>, Integer> pointToIdx = new HashMap<>();
			int NP = sc.nextInt();
			polygons = new int[NP][];
			for (int i = 0; i < NP; ++i) {
				int NV = sc.nextInt();
				polygons[i] = new int[NV];
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
					polygons[i][j] = pointToIdx.get(p);
				}
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
