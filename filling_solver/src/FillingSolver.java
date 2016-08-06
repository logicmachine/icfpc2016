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
	private static final Rational MAX_BBOX_SIZE_PERPENDICULAR = Rational.ONE;
	private static final Rational MAX_BBOX_SIZE_GENERAL = new Rational(BigInteger.valueOf(14143), BigInteger.valueOf(10000));
	Rational maxBboxSize = MAX_BBOX_SIZE_GENERAL;
	PartsDecomposer decomposer;
	int[] partsUsed;
	int maxBitLength;
	ArrayList<HashSet<Integer>> usedHash;
	long limitTime;
	AffineTransform resultMapping = new AffineTransform(Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ONE);

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
		long realLimit = timelimitMs == 0 ? NO_TIME_LIMIT : System.currentTimeMillis() + timelimitMs;
		ArrayList<Part> parts = decomposer.parts;
		Collections.sort(parts, (Part l, Part r) -> {
			return r.area.compareTo(l.area);
		});
		System.err.println("largest part:" + parts.get(0));

		limitTime = System.currentTimeMillis() + (timelimitMs == 0 ? 5000 : Math.min(5000, timelimitMs / 3));
		maxBboxSize = MAX_BBOX_SIZE_PERPENDICULAR;
		boolean result = solveInner();
		if (result) return;
		System.err.println("failed perpendicular");
		limitTime = realLimit;
		maxBboxSize = MAX_BBOX_SIZE_GENERAL;
		result = solveInner();
		if (!result) {
			System.out.println("failed");
		}
	}

	private boolean solveInner() {
		partsUsed = new int[decomposer.parts.size()];
		usedHash = new ArrayList<>();
		Part largest = decomposer.parts.get(0);
		partsUsed[0] = 1;

		ArrayList<Vertex> initialEnvelop = new ArrayList<>();
		for (int i = 0; i < largest.vs.size(); ++i) {
			Point p = decomposer.points.get(largest.vs.get(i));
			initialEnvelop.add(new Vertex(largest.vs.get(i), p.x, p.y));
		}
		State st = new State(initialEnvelop);
		st.area = largest.area;
		State result;
		if (st.area.equals(Rational.ONE)) {
			result = finish(st) ? st : null;
		} else {
			result = rec(st);
		}
		if (result != null) {
			result.shrink();
			output(result);
			//			result.outputImages();
			return true;
		} else {
			return false;
		}
	}

	State rec(State cur) {
		//		output(cur);
		//		System.out.println(cur.envelop);
		//		System.out.println("area:" + cur.area);
		//		System.out.println(cur.xmin + " " + cur.xmax + " " + cur.ymin + " " + cur.ymax);
		//		System.out.println();
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
				if (cur.area.add(part.area).compareTo(Rational.ONE) > 0) continue;
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
							partsUsed[i]++;
							if (ns.area.equals(Rational.ONE)) {
								if (finish(ns)) {
									return ns;
								} else {
									partsUsed[i]--;
									continue;
								}
							}
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
		int start = -1;
		final int NP = st.envelop.size();
		for (int i = 0; i < NP; ++i) {
			Point p1 = st.envelop.get(i).p;
			Point p2 = st.envelop.get((i + 1) % NP).p;
			Point p3 = st.envelop.get((i + 2) % NP).p;
			Rational dx1 = p2.x.sub(p1.x);
			Rational dy1 = p2.y.sub(p1.y);
			Rational dx2 = p3.x.sub(p2.x);
			Rational dy2 = p3.y.sub(p2.y);
			if (!dy1.mul(dx2).sub(dx1.mul(dy2)).equals(Rational.ZERO)) {
				if (!dx1.mul(dx2).add(dy1.mul(dy2)).equals(Rational.ZERO)) {
					return false;
				}
				start = (i + 1) % NP;
				break;
			}
		}
		Point sp = st.envelop.get(start).p;
		Point tp1 = sp;
		Point tp2 = null;
		for (int i = 1; i < NP; ++i) {
			Point cp = st.envelop.get((start + i) % NP).p;
			Point np = st.envelop.get((start + i + 1) % NP).p;
			Rational dx1 = cp.x.sub(sp.x);
			Rational dy1 = cp.y.sub(sp.y);
			Rational dx2 = np.x.sub(cp.x);
			Rational dy2 = np.y.sub(cp.y);
			if (!dy1.mul(dx2).sub(dx1.mul(dy2)).equals(Rational.ZERO)) {
				if (!dx1.mul(dx2).add(dy1.mul(dy2)).equals(Rational.ZERO)) {
					return false;
				}
				if (!dx1.mul(dx1).add(dy1.mul(dy1)).equals(Rational.ONE)) {
					return false;
				}
				if (tp2 == null) tp2 = cp;
				sp = cp;
			}
		}
		if (tp1.x.equals(tp2.x) || tp1.y.equals(tp2.y)) {
			resultMapping = AffineTransform.transform(st.xmin.negate(), st.ymin.negate());
		} else {
			resultMapping = getTransform(tp1, tp2, new Point(Rational.ZERO, Rational.ZERO), new Point(Rational.ONE, Rational.ZERO));
		}
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
			if (ret.xmax.sub(ret.xmin).compareTo(maxBboxSize) > 0) {
				return null;
			}
			if (ret.ymax.sub(ret.ymin).compareTo(maxBboxSize) > 0) {
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

		void shrink() {
			// merge adjacent same rotational order parts
			for (int i = 0; i < filledParts.size(); ++i) {
				ArrayList<Vertex> basePart = filledParts.get(i);
				boolean baseFlip = isFlipped(basePart);
				for (int j = i + 1; j < filledParts.size(); ++j) {
					ArrayList<Vertex> sidePart = filledParts.get(j);
					boolean sideFlip = isFlipped(sidePart);
					if (baseFlip != sideFlip) continue;
					int baseIdx = -1;
					int sideIdx = -1;
					OUT: for (int k = 0; k < basePart.size(); ++k) {
						Vertex v1 = basePart.get(k);
						Vertex v2 = basePart.get((k + 1) % basePart.size());
						for (int l = 0; l < sidePart.size(); ++l) {
							if (v2.equals(sidePart.get(l)) && v1.equals(sidePart.get((l + 1) % sidePart.size()))) {
								baseIdx = k;
								sideIdx = l;
								break OUT;
							}
						}
					}
					if (baseIdx == -1) continue;
					// merge sidePart to basePart
					for (int k = 2; k < sidePart.size(); ++k) {
						basePart.add(baseIdx + k - 1, sidePart.get((sideIdx + k) % sidePart.size()));
					}
					for (int k = 0; k < basePart.size() && basePart.size() > 3; ++k) {
						if (basePart.get(k).equals(basePart.get((k + 2) % basePart.size()))) {
							basePart.remove(k);
							basePart.remove(k == basePart.size() ? 0 : k);
							--k;
						}
					}
					filledParts.remove(j);
					--j;
				}
			}

			// merge collinear adjacent edges
			for (int i = 0; i < filledParts.size(); ++i) {
				ArrayList<Vertex> part = filledParts.get(i);
				for (int j = 0; j < part.size(); ++j) {
					Point p1 = part.get(j).p;
					Point p2 = part.get((j + 1) % part.size()).p;
					Point p3 = part.get((j + 2) % part.size()).p;
					Rational dx1 = p2.x.sub(p1.x);
					Rational dy1 = p2.y.sub(p1.y);
					Rational dx2 = p3.x.sub(p2.x);
					Rational dy2 = p3.y.sub(p2.y);
					if (dy1.mul(dx2).sub(dx1.mul(dy2)).equals(Rational.ZERO)) {
						// remove the mid point
						part.remove(j == part.size() - 1 ? 0 : j + 1);
						--j;
					}
				}
			}
		}

		boolean isFlipped(ArrayList<Vertex> part) {
			ArrayList<Part> origParts = decomposer.parts;
			for (int i = 0; i < origParts.size(); ++i) {
				Part origPart = origParts.get(i);
				if (part.size() != origPart.vs.size()) continue;
				int idx = 0;
				for (int j = 0; j < part.size(); ++j) {
					if (origPart.vs.get(j) == part.get(0).pIdx) {
						idx = j;
						break;
					}
				}
				boolean match = true;
				for (int j = 1; j < part.size(); ++j) {
					if (part.get(j).pIdx != origPart.vs.get((idx + j) % origPart.vs.size())) {
						match = false;
						break;
					}
				}
				if (match) return false;
			}
			return true;
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
