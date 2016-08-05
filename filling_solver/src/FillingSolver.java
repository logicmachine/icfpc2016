import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FillingSolver {

	PartsDecomposer decomposer;

	FillingSolver(PartsDecomposer decomposer) {
		this.decomposer = decomposer;
	}

	void solve() {
		ArrayList<Part> parts = decomposer.parts;
		Part largest = parts.get(0);
		for (int i = 1; i < parts.size(); ++i) {
			if (parts.get(i).area.compareTo(largest.area) > 0) {
				largest = parts.get(i);
			}
		}
		System.err.println("largest part:" + largest);

		State st = new State();
		for (int i = 0; i < largest.vs.size(); ++i) {
			Point p = decomposer.points.get(largest.vs.get(i));
			st.envelop.add(new Vertex(largest.vs.get(i), p.x, p.y));
		}
		st.area = largest.area;
		st.filledParts.add(new ArrayList<>(st.envelop));
		State result = rec(st);
		if (result != null) {
			output(result);
		} else {
			System.out.println("failed");
		}
	}

	State rec(State cur) {
		ArrayList<Part> parts = decomposer.parts;
		for (int i = 0; i < cur.envelop.size(); ++i) {
			int i1 = cur.envelop.get(i).pIdx;
			int i2 = cur.envelop.get((i + 1) % cur.envelop.size()).pIdx;
			for (int j = 0; j < parts.size(); ++j) {
				Part part = parts.get(j);
				final int PS = part.vs.size();
				for (int k = 0; k < PS; ++k) {
					if (part.vs.get(k) != i1) continue;
					if (part.vs.get((k + 1) % PS) == i2 || part.vs.get((k - 1 + PS) % PS) == i2) {
						State ns = cur.add(i, part, k);
						if (ns == null) continue;
						if (ns.area.equals(Rational.ONE)) return ns;
						State ans = rec(ns);
						if (ans != null) return ans;
					}
				}
			}
		}
		return null;
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
			System.out.println(ps[i].toICFPStr());
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
			System.out.println(dest[i].toICFPStr());
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
					addVertex.add(new Vertex(pi, reflect(p1, p2, t.apply(points.get(pi)))));
				}
			} else {
				for (int i = 0; i < part.vs.size(); ++i) {
					int pi = part.vs.get((i + partIdx) % PS);
					addVertex.add(new Vertex(pi, t.apply(points.get(pi))));
				}
			}
			// TODO conflict check
			// TODO edge merging
			for (int i = 0; i <= partIdx; ++i) {
				ret.envelop.add(this.envelop.get(i));
			}
			for (int i = 1; i < PS - 1; ++i) {
				ret.envelop.add(addVertex.get(i));
			}
			for (int i = partIdx + 1; i < this.envelop.size(); ++i) {
				ret.envelop.add(this.envelop.get(i));
			}
			ret.filledParts.addAll(this.filledParts);
			ret.filledParts.add(addVertex);
			return ret;
		}
	}

	static class AffineTransform {
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
					ret.mat[i][j] = Rational.ZERO;
					for (int k = 0; k < 3; ++k) {
						ret.mat[i][j] = ret.mat[i][j].add(t.mat[i][k].mul(this.mat[k][j]));
					}
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

	static class Vertex {
		int pIdx;
		Point p;

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
			return "[pIdx=" + pIdx + ", x=" + p.x + ", y=" + p.y + "]";
		}
	}
}
