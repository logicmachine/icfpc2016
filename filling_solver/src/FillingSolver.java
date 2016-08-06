import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class FillingSolver {

	static final Rational MAX_SIZE = new Rational(BigInteger.valueOf(9), BigInteger.valueOf(8));
	PartsDecomposer decomposer;
	int[] partsUsed;

	FillingSolver(PartsDecomposer decomposer) {
		this.decomposer = decomposer;
	}

	void solve() {
		ArrayList<Part> parts = decomposer.parts;
		partsUsed = new int[parts.size()];
		int largestI = 0;
		Part largest = parts.get(0);
		for (int i = 1; i < parts.size(); ++i) {
			if (parts.get(i).area.compareTo(largest.area) > 0) {
				largest = parts.get(i);
				largestI = i;
			}
		}
		System.err.println("largest part:" + largest);
		partsUsed[largestI] = 1;

		ArrayList<Vertex> initialEnvelop = new ArrayList<>();
		for (int i = 0; i < largest.vs.size(); ++i) {
			Point p = decomposer.points.get(largest.vs.get(i));
			initialEnvelop.add(new Vertex(largest.vs.get(i), p.x, p.y));
		}
		State st = new State(initialEnvelop);
		st.area = largest.area;
		State result = rec(st);
		if (result != null) {
			output(result);
		} else {
			System.out.println("failed");
		}
	}

	State rec(State cur) {
		//		output(cur);
		//		System.out.println(cur.envelop);
		//		System.out.println(cur.xmin + " " + cur.xmax + " " + cur.ymin + " " + cur.ymax);
		//		System.out.println();
		ArrayList<Part> parts = decomposer.parts;
		for (int loop = 0; loop < 2; ++loop) {
			for (int i = 0; i < cur.envelop.size(); ++i) {
				int i1 = cur.envelop.get(i).pIdx;
				int i2 = cur.envelop.get((i + 1) % cur.envelop.size()).pIdx;
				for (int j = 0; j < parts.size(); ++j) {
					if (partsUsed[j] != 0 && loop == 0) continue;
					Part part = parts.get(j);
					final int PS = part.vs.size();
					for (int k = 0; k < PS; ++k) {
						if (part.vs.get(k) != i1) continue;
						if (part.vs.get((k + 1) % PS) == i2 || part.vs.get((k - 1 + PS) % PS) == i2) {
							State ns = cur.add(i, part, k);
							if (ns == null) continue;
							if (ns.area.equals(Rational.ONE)) {
								if (finish(cur)) {
									return ns;
								} else {
									continue;
								}
							}
							partsUsed[j]++;
							State ans = rec(ns);
							if (ans != null) return ans;
							partsUsed[j]--;
						}
					}
				}
			}
		}
		return null;
	}

	boolean finish(State st) {
		// TODO
		return true;
	}

	void output(State st) {
		HashMap<Point, Integer> map = new HashMap<>();
		for (ArrayList<Vertex> vs : st.filledParts) {
			for (Vertex v : vs) {
				if (!map.containsKey(v.p)) {
					map.put(v.p, map.size());
				}
			}
		}
		Point[] ps = new Point[map.size()];
		for (Point p : map.keySet()) {
			ps[map.get(p)] = p;
		}
		System.out.println(ps.length);
		for (int i = 0; i < ps.length; ++i) {
			System.out.println(ps[i].transform(st.xmin.negate(), st.ymin.negate()).toICFPStr());
		}
		System.out.println(st.filledParts.size());
		Point[] dest = new Point[map.size()];
		for (ArrayList<Vertex> vs : st.filledParts) {
			System.out.print(vs.size());
			for (Vertex v : vs) {
				int idx = map.get(v.p);
				System.out.print(" " + idx);
				dest[idx] = v.p;
			}
			System.out.println();
		}
		for (int i = 0; i < dest.length; ++i) {
			System.out.println(dest[i].transform(st.xmin.negate(), st.ymin.negate()).toICFPStr());
		}
	}

	AffineTransform getTransform(Point p1, Point p2, Point p3, Point p4) { // (p1-p2) -> (p3-p4)
		AffineTransform t = AffineTransform.transform(p1.x.negate(), p1.y.negate());
		Rational dx1 = p2.x.sub(p1.x);
		Rational dy1 = p2.y.sub(p1.y);
		Rational dx2 = p4.x.sub(p3.x);
		Rational dy2 = p4.y.sub(p3.y);
		Rational norm = dx1.mul(dx1).add(dy1.mul(dy1));
		Rational cos = dx1.mul(dx2).add(dy1.mul(dy2)).div(norm);
		Rational sin = dx2.mul(dy1).sub(dy2.mul(dx1)).div(norm);
		t = t.apply(AffineTransform.rot(cos, sin));
		t = t.apply(AffineTransform.transform(p3.x, p3.y));
		return t;
	}

	Point reflect(Point p1, Point p2, Point p3) {
		Rational dx = p2.x.sub(p1.x);
		Rational dy = p2.y.sub(p1.y);
		Rational dx2 = dx.mul(dx);
		Rational dy2 = dy.mul(dy);
		Rational dxdy = dx.mul(dy);
		Rational norm = dx2.add(dy2);
		Rational cx = p3.x.mul(dx2).add(p3.y.mul(dxdy)).add(p1.x.mul(dy2)).sub(p1.y.mul(dxdy)).div(norm);
		Rational cy = p3.x.mul(dxdy).add(p3.y.mul(dy2)).sub(p1.x.mul(dxdy)).add(p1.y.mul(dx2)).div(norm);
		return new Point(cx.add(cx).sub(p3.x), cy.add(cy).sub(p3.y));
	}

	class State {
		ArrayList<ArrayList<Vertex>> filledParts = new ArrayList<>();
		ArrayList<Vertex> envelop = new ArrayList<>();
		Rational area;
		Rational xmin, xmax, ymin, ymax;

		State() {}

		State(ArrayList<Vertex> initialEnvelop) {
			this.envelop = new ArrayList<>(initialEnvelop);
			this.filledParts.add(new ArrayList<>(this.envelop));
			xmin = xmax = this.envelop.get(0).p.x;
			ymin = ymax = this.envelop.get(0).p.y;
			for (int i = 1; i < this.envelop.size(); ++i) {
				updateBBox(this.envelop.get(i).p);
			}
		}

		State add(int envIdx, Part part, int partIdx) {
			final int PS = part.vs.size();
			State ret = new State();
			ret.area = this.area.add(part.area);
			if (ret.area.compareTo(Rational.ONE) > 0) return null;
			ArrayList<Point> points = decomposer.points;
			Vertex ev1 = envelop.get(envIdx);
			Vertex ev2 = envelop.get((envIdx + 1) % envelop.size());
			int pp2 = part.vs.get((partIdx + 1) % PS);
			boolean reverse = ev2.pIdx == pp2;
			ArrayList<Vertex> addVertex = new ArrayList<>();
			Point p1 = points.get(ev1.pIdx);
			Point p2 = points.get(ev2.pIdx);
			AffineTransform t = getTransform(p1, p2, ev1.p, ev2.p);
			if (reverse) {
				for (int i = 0; i < part.vs.size(); ++i) {
					int pi = part.vs.get((partIdx - i + PS) % PS);
					addVertex.add(new Vertex(pi, reflect(ev1.p, ev2.p, t.apply(points.get(pi)))));
				}
			} else {
				for (int i = 0; i < part.vs.size(); ++i) {
					int pi = part.vs.get((i + partIdx) % PS);
					addVertex.add(new Vertex(pi, t.apply(points.get(pi))));
				}
			}
			// TODO conflict check
			ret.xmin = this.xmin;
			ret.xmax = this.xmax;
			ret.ymin = this.ymin;
			ret.ymax = this.ymax;
			for (int i = 0; i <= envIdx; ++i) {
				ret.envelop.add(this.envelop.get(i));
			}
			for (int i = 1; i < PS - 1; ++i) {
				ret.envelop.add(addVertex.get(i));
				ret.updateBBox(addVertex.get(i).p);
			}
			if (ret.xmax.sub(ret.xmin).compareTo(MAX_SIZE) >= 0) {
				return null;
			}
			if (ret.ymax.sub(ret.ymin).compareTo(MAX_SIZE) >= 0) {
				return null;
			}
			for (int i = envIdx + 1; i < this.envelop.size(); ++i) {
				ret.envelop.add(this.envelop.get(i));
			}
			Point addNext = ret.envelop.get(envIdx + 1).p;
			Point addPrev = ret.envelop.get((envIdx - 1 + ret.envelop.size()) % ret.envelop.size()).p;
			if (addNext.equals(addPrev)) {
				ret.envelop.remove(envIdx);
				ret.envelop.remove(envIdx);
			}
			for (ArrayList<Vertex> vs : this.filledParts) {
				ret.filledParts.add(new ArrayList<>(vs));
			}
			ret.filledParts.add(addVertex);
			return ret;
		}

		void updateBBox(Point p) {
			Rational x = p.x;
			Rational y = p.y;
			if (x.compareTo(xmin) < 0) xmin = x;
			if (x.compareTo(xmax) > 0) xmax = x;
			if (y.compareTo(ymin) < 0) ymin = y;
			if (y.compareTo(ymax) > 0) ymax = y;
		}
	}

	static class Vertex {
		final int pIdx;
		final Point p;

		Vertex(int i, Rational x, Rational y) {
			this.pIdx = i;
			this.p = new Point(x, y);
		}

		Vertex(int i, Point p) {
			this.pIdx = i;
			this.p = p;
		}

		@Override
		public String toString() {
			return "pi=" + pIdx + "(" + p.x + "," + p.y + ")";
		}
	}
}
