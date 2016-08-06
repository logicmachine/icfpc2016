import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;

public class FillingSolver {

	private static final long NO_TIME_LIMIT = -1;
	static final Rational MAX_SIZE = new Rational(BigInteger.valueOf(1000), BigInteger.valueOf(999));
	//	static final Rational MAX_SIZE = new Rational(BigInteger.valueOf(14), BigInteger.valueOf(10));
	PartsDecomposer decomposer;
	int[] partsUsed;
	int maxBitLength;
	ArrayList<HashSet<Integer>> usedHash = new ArrayList<>();
	long limitTime;
	AffineTransform resultMapping = null;

	FillingSolver(PartsDecomposer decomposer) {
		this.decomposer = decomposer;
		for (Point p : decomposer.points) {
			maxBitLength = Math.max(maxBitLength, p.x.den.bitLength());
			maxBitLength = Math.max(maxBitLength, p.y.den.bitLength());
		}
		maxBitLength += 2;
	}

	/**
	 * @param timelimitMs time limit in microseconds, or 0 (no limit)
	 */
	void solve(int timelimitMs) {
		limitTime = timelimitMs == 0 ? NO_TIME_LIMIT : System.currentTimeMillis() + timelimitMs;

		ArrayList<Part> parts = decomposer.parts;
		Collections.sort(parts, (Part l, Part r) -> {
			return r.area.compareTo(l.area);
		});
		partsUsed = new int[parts.size()];
		Part largest = parts.get(0);
		System.err.println("largest part:" + largest);
		partsUsed[0] = 1;

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
			result.outputImages();
		} else {
			System.out.println("failed");
		}
	}

	State rec(State cur) {
		//				output(cur);
		//				System.out.println(cur.envelop);
		//				System.out.println(cur.xmin + " " + cur.xmax + " " + cur.ymin + " " + cur.ymax);
		//				System.out.println();
		if (limitTime != NO_TIME_LIMIT && System.currentTimeMillis() > limitTime) return null;
		while (usedHash.size() <= cur.envelop.size()) {
			usedHash.add(new HashSet<Integer>());
		}
		int hash = cur.envelop.hashCode();
		if (usedHash.get(cur.envelop.size()).contains(hash)) {
			return null;
		}
		usedHash.get(cur.envelop.size()).add(hash);
		ArrayList<Part> parts = decomposer.parts;
		for (int loop = 0; loop < 2; ++loop) {
			for (int i = 0; i < parts.size(); ++i) {
				if (partsUsed[i] != 0 && loop == 0) continue;
				Part part = parts.get(i);
				final int PS = part.vs.size();
				for (int j = 0; j < cur.envelop.size(); ++j) {
					int i1 = cur.envelop.get(j).pIdx;
					int i2 = cur.envelop.get((j + 1) % cur.envelop.size()).pIdx;
					for (int k = 0; k < PS; ++k) {
						if (part.vs.get(k) != i1) continue;
						if (part.vs.get((k + 1) % PS) == i2 || part.vs.get((k - 1 + PS) % PS) == i2) {
							//							System.out.println("adding " + i1 + " " + i2);
							State ns = cur.add(j, part, k);
							if (ns == null) continue;
							//							System.out.println("added " + i1 + " " + i2);
							if (ns.area.equals(Rational.ONE)) {
								if (finish(cur)) {
									return ns;
								} else {
									continue;
								}
							}
							partsUsed[i]++;
							State ans = rec(ns);
							if (ans != null) return ans;
							partsUsed[i]--;
						}
					}
				}
			}
		}
		return null;
	}

	boolean finish(State st) {
		for (int i = 0; i < partsUsed.length; ++i) {
			if (partsUsed[i] == 0) return false;
		}
		// TODO
		resultMapping = AffineTransform.transform(st.xmin.negate(), st.ymin.negate());
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
			System.out.println(resultMapping.apply(ps[i]).toICFPStr());
		}
		System.out.println(st.filledParts.size());
		Point[] dest = new Point[map.size()];
		for (ArrayList<Vertex> vs : st.filledParts) {
			System.out.print(vs.size());
			for (Vertex v : vs) {
				int idx = map.get(v.p);
				System.out.print(" " + idx);
				dest[idx] = decomposer.points.get(v.pIdx);
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
					Point addP = t.apply(points.get(pi));
					if (addP.x.den.bitLength() > maxBitLength || addP.y.den.bitLength() > maxBitLength) return null;
					addVertex.add(new Vertex(pi, addP));
				}
			}
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
			int addCount = PS - 2;
			int addPos = envIdx + 1;
			{
				// remove matching edges
				while (addCount > 0) { // backward
					if (ret.envelop.get(addPos).equals(ret.envelop.get((addPos - 2 + ret.envelop.size()) % ret.envelop.size()))) {
						ret.envelop.remove(addPos);
						ret.envelop.remove(addPos == 0 ? ret.envelop.size() - 1 : addPos - 1);
						if (addPos > 0) addPos--;
						--addCount;
					} else {
						break;
					}
				}
				while (addCount > 0) { // forward
					if (ret.envelop.get((addPos + addCount - 1) % ret.envelop.size())
							.equals(ret.envelop.get((addPos + addCount + 1) % ret.envelop.size()))) {
						ret.envelop.remove((addPos + addCount) % ret.envelop.size());
						ret.envelop.remove((addPos + addCount) % ret.envelop.size());
						--addCount;
					} else {
						break;
					}
				}
			}
			if (ret.conflict(addPos, addCount)) {
				return null;
			}

			for (ArrayList<Vertex> vs : this.filledParts) {
				ret.filledParts.add(new ArrayList<>(vs));
			}
			ret.filledParts.add(addVertex);
			return ret;
		}

		boolean conflict(int addPos, int addCount) {
			for (int i = 0; i <= addCount; ++i) {
				Point f1 = envelop.get((addPos + i + envelop.size() - 1) % envelop.size()).p;
				Point t1 = envelop.get((addPos + i) % envelop.size()).p;
				for (int j = 1; j < envelop.size() - 2; ++j) {
					Point f2 = envelop.get((addPos + i + j) % envelop.size()).p;
					Point t2 = envelop.get((addPos + i + j + 1) % envelop.size()).p;
					Point cross = Geometry.getIntersectPoint(f1, t1, f2, t2);
					if (cross != null) {
						return true;
					}
					// TODO parallel line
					//					if (!f1.equals(cross) && !t1.equals(cross)) return true;
					//					if (!f2.equals(cross) && !t2.equals(cross)) return true;
				}
			}
			return false;
		}

		void updateBBox(Point p) {
			Rational x = p.x;
			Rational y = p.y;
			if (x.compareTo(xmin) < 0) xmin = x;
			if (x.compareTo(xmax) > 0) xmax = x;
			if (y.compareTo(ymin) < 0) ymin = y;
			if (y.compareTo(ymax) > 0) ymax = y;
		}

		void outputImages() {
			final int SIZE = 800;
			final int MARGIN = 20;
			for (int i = 1; i <= filledParts.size(); ++i) {
				BufferedImage image = new BufferedImage(SIZE + MARGIN * 2, SIZE + MARGIN * 2, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = (Graphics2D) image.getGraphics();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				for (int j = 0; j < i; ++j) {
					ArrayList<Vertex> part = filledParts.get(j);
					int[] xs = new int[part.size()];
					int[] ys = new int[part.size()];
					for (int k = 0; k < part.size(); ++k) {
						Point p = part.get(k).p;
						xs[k] = fitToScale(p.x.sub(xmin), SIZE) + MARGIN;
						ys[k] = SIZE - fitToScale(p.y.sub(ymin), SIZE) + MARGIN;
					}
					g.setColor(new Color(165, 214, 167, 128));
					g.fillPolygon(xs, ys, xs.length);
					g.setColor(new Color(27, 94, 32, 192));
					g.drawPolygon(xs, ys, xs.length);
				}
				try {
					ImageIO.write(image, "png", new File(String.format("img/%04d.png", i)));
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		}

		int fitToScale(Rational r, int size) {
			return r.num.multiply(BigInteger.valueOf(size)).divide(r.den).intValue();
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
		public int hashCode() {
			return p.hashCode() ^ pIdx;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			Vertex other = (Vertex) obj;
			return pIdx == other.pIdx && p.equals(other.p);
		}

		@Override
		public String toString() {
			return "pi=" + pIdx + "(" + p.x + "," + p.y + ")";
		}
	}
}
