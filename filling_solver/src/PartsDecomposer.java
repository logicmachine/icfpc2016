import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class PartsDecomposer {

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
				Point<Rational> cp = Geometry.getIntersectPoint(points.get(edges[i].p1), points.get(edges[i].p2), points.get(edges[j].p1),
						points.get(edges[j].p2));
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
			for (Edge e : newEdges) {
				e.sides = findPositiveSides(e);
			}
		}
		edges = newEdges.toArray(new Edge[0]);
	}

	void decompositeToParts() {
		for (int i = 0; i < edges.length; ++i) {
			Edge e = edges[i];
			if (!e.usedForward && (e.sides == Edge.PositiveSides.BOTH || e.sides == Edge.PositiveSides.LEFT)) {
				parts.add(extractPart(e.p1, e.p2));
				e.usedForward = true;
			}
			if (!e.usedBackward && (e.sides == Edge.PositiveSides.BOTH || e.sides == Edge.PositiveSides.RIGHT)) {
				parts.add(extractPart(e.p2, e.p1));
				e.usedBackward = true;
			}
		}
	}

	Part extractPart(int start, int next) {
		Part ret = new Part();
		int prev = start;
		int cur = next;
		ret.vs.add(cur);
		while (cur != start) {
			Edge cand = null;
			Rational cosSq = new Rational(BigInteger.valueOf(10), BigInteger.ONE); // smaller is better
			for (Edge e : edges) {
				if (e.p1 == cur) {
					if (e.p2 == prev) continue;
					Rational cos = pseudoCosSq(points.get(prev), points.get(cur), points.get(e.p2));
					if (cos.compareTo(cosSq) < 0) {
						cosSq = cos;
						cand = e;
					}
				} else if (e.p2 == cur) {
					if (e.p1 == prev) continue;
					Rational cos = pseudoCosSq(points.get(prev), points.get(cur), points.get(e.p1));
					if (cos.compareTo(cosSq) < 0) {
						cosSq = cos;
						cand = e;
					}
				}
			}
			prev = cur;
			if (cand.p1 == cur) {
				cur = cand.p2;
				cand.usedForward = true;
			} else {
				cur = cand.p1;
				cand.usedBackward = true;
			}
			ret.vs.add(cur);
		}
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

	Rational pseudoCosSq(Point<Rational> p1, Point<Rational> p2, Point<Rational> p3) {
		Rational dx1 = p2.x.sub(p1.x);
		Rational dy1 = p2.y.sub(p1.y);
		Rational dx2 = p3.x.sub(p2.x);
		Rational dy2 = p3.y.sub(p2.y);
		boolean ccw = dy2.mul(dx1).sub(dx2.mul(dy1)).compareTo(Rational.ZERO) > 0;
		Rational dot = dx1.mul(dx2).add(dy1.mul(dy2));
		Rational norm1 = dx1.mul(dx1).add(dy1.mul(dy1));
		Rational norm2 = dx2.mul(dx2).add(dy2.mul(dy2));
		Rational cos2 = dot.mul(dot).div(norm1.mul(norm2));
		if (dot.num.signum() < 0) {
			cos2 = cos2.negate();
		}
		if (!ccw) {
			cos2 = cos2.negate().add(new Rational(BigInteger.valueOf(3), BigInteger.ONE)); // add bonus
		}
		return cos2;
	}

	Edge.PositiveSides findPositiveSides(Edge e) {
		Point<Rational> ep1 = points.get(e.p1);
		Point<Rational> ep2 = points.get(e.p2);
		for (int i = 0; i < polygons.length; ++i) {
			Polygon poly = polygons[i];
			int n = poly.vs.length;
			Point<Rational> prev = points.get(poly.vs[n - 1]);
			for (int j = 0; j < n; ++j) {
				Point<Rational> cur = points.get(poly.vs[j]);
				if (Geometry.isOnLine(ep1, ep2, prev, cur)) {
					return ep1.compareTo(ep2) == prev.compareTo(cur) ? Edge.PositiveSides.LEFT : Edge.PositiveSides.RIGHT;
				}
				prev = cur;
			}
		}
		return Edge.PositiveSides.BOTH;
	}
}

class Part {
	ArrayList<Integer> vs = new ArrayList<>();

	@Override
	public String toString() {
		return vs.toString();
	}
}

class Edge {
	enum PositiveSides {
		LEFT, RIGHT, BOTH;
	};

	int p1, p2;
	PositiveSides sides;
	boolean usedForward, usedBackward;

	public Edge(int p1, int p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	@Override
	public String toString() {
		return "[" + p1 + "," + p2 + "] " + sides + " (" + usedForward + ", " + usedBackward + ")";
	}
}
