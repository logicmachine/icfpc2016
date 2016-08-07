import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ProblemCreater {

	static final int[] DX = { -1, 1, 1, -1, -1 };
	static final int[] DY = { 1, 1, -1, -1, 1 };
	static final int[] NX = { 0, 1, 0, -1 };
	static final int[] NY = { 1, 0, -1, 0 };
	int foldCount = 1;
	Random rnd = new Random();
	long seed;
	int S;
	ArrayList<ArrayList<ArrayList<ArrayList<Panel>>>> panels = new ArrayList<>();
	int xmin, xmax, ymin, ymax;
	HashMap<Pos, Integer> p2i = new HashMap<>(); // src -> idx
	HashMap<Pos, Pos> posMap = new HashMap<>(); // src -> dst
	ArrayList<ArrayList<Integer>> srcShape = new ArrayList<>();

	ProblemCreater(int size, int foldCount) {
		this(size, foldCount, new Random().nextLong());
	}

	ProblemCreater(int size, int foldCount, long seed) {
		//		System.err.println("seed:" + seed);
		this.seed = seed;
		this.S = size;
		xmin = ymin = S;
		xmax = ymax = S * 2;
		this.foldCount = foldCount;
		rnd.setSeed(seed);
		for (int i = 0; i < size * 3; ++i) {
			ArrayList<ArrayList<ArrayList<Panel>>> row = new ArrayList<>();
			for (int j = 0; j < size * 3; ++j) {
				ArrayList<ArrayList<Panel>> cell = new ArrayList<>();
				for (int k = 0; k < 4; ++k) {
					ArrayList<Panel> subcell = new ArrayList<>();
					if (S <= i && i < S * 2 && S <= j && j < S * 2) subcell.add(new Panel(j, i, k, 1, false));
					cell.add(subcell);
				}
				row.add(cell);
			}
			panels.add(row);
		}
	}

	void output(PrintWriter writer) {
		//			System.err.println("p2i:" + p2i);
		//			System.err.println("posMap:" + posMap);
		Pos[] dstPs = new Pos[p2i.size()];
		Pos[] srcPs = new Pos[p2i.size()];
		for (Pos p : p2i.keySet()) {
			int idx = p2i.get(p);
			srcPs[idx] = p;
			dstPs[idx] = posMap.get(p);
		}
		writer.println(srcPs.length);
		for (int i = 0; i < srcPs.length; ++i) {
			outputPos(writer, new Pos(srcPs[i].x - 2 * S, srcPs[i].y - 2 * S));
		}
		writer.println(srcShape.size());
		for (int i = 0; i < srcShape.size(); ++i) {
			ArrayList<Integer> shape = srcShape.get(i);
			writer.print(shape.size());
			for (int j = 0; j < shape.size(); ++j) {
				writer.print(" " + shape.get(j));
			}
			writer.println();
		}
		for (int i = 0; i < srcPs.length; ++i) {
			outputPos(writer, new Pos(dstPs[i].x - 2 * S, dstPs[i].y - 2 * S));
		}
	}

	void outputPos(PrintWriter writer, Pos pos) {
		Rational x = new Rational(BigInteger.valueOf(pos.x), BigInteger.valueOf(2 * S));
		Rational y = new Rational(BigInteger.valueOf(pos.y), BigInteger.valueOf(2 * S));
		Point p = new Point(x, y);
		writer.println(p.toICFPStr());
	}

	boolean validate() {
		int sum = 0;
		Pos[] srcPos = new Pos[p2i.size()];
		for (Pos p : p2i.keySet()) {
			srcPos[p2i.get(p)] = p;
		}
		for (ArrayList<Integer> shape : srcShape) {
			int csum = 0;
			for (int i = 0; i < shape.size(); ++i) {
				Pos p1 = srcPos[shape.get(i)];
				Pos p2 = srcPos[shape.get((i + 1) % shape.size())];
				csum += p1.y * p2.x - p1.x * p2.y;
			}
			sum += Math.abs(csum);
		}
		System.err.println("sum:" + sum);
		if (sum != 2 * S * S * 4) {
			debug();
			return false;
		}
		return true;
	}

	void debug() {
		for (int y = ymin; y < ymax; ++y) {
			for (int x = xmin; x < xmax; ++x) {
				for (int d = 0; d < 4; ++d) {
					ArrayList<Panel> subcell = panels.get(y).get(x).get(d);
					if (!subcell.isEmpty()) {
						System.err.println(y + " " + x + " " + d);
						System.err.println(subcell);
					}
				}
			}
		}
	}

	void create() {
		for (int i = 0; i < foldCount; ++i) {
			if (xmax - xmin == 1 && ymax - ymin == 1) {
				System.err.println("too small");
				break;
			}
			int type = rnd.nextInt(4);
			if (type == 0) {
				if (xmax - xmin == 1) {
					--i;
					continue;
				}
				int flipX = rnd.nextInt(xmax - xmin - 1) + xmin + 1;
				//				System.err.println("flipX:" + flipX);
				flipHorizontal(flipX, i + 1);
			} else if (type == 1) {
				if (ymax - ymin == 1) {
					--i;
					continue;
				}
				int flipY = rnd.nextInt(ymax - ymin - 1) + ymin + 1;
				//				System.err.println("flipY:" + flipY);
				flipVertical(flipY, i + 1);
			} else if (type <= 2) {
				int maxS = xmax - ymin;
				int minS = xmin - ymax;
				if (maxS - minS <= 2) {
					--i;
					continue;
				}
				int flipS = rnd.nextInt(maxS - minS - 2) + minS + 1;
				//				System.err.println("flipS:" + flipS);
				flipSlash(flipS, i + 1);
			} else {
				int maxB = xmax + ymax;
				int minB = xmin + ymin;
				if (maxB - minB <= 2) {
					--i;
					continue;
				}
				int flipB = rnd.nextInt(maxB - minB - 2) + minB + 1;
				//				System.err.println("flipB:" + flipB);
				flipBackslash(flipB, i + 1);
			}
			updateBBox();
			//			debug();
		}
	}

	double evaluate() {
		double len = 0;
		double area = 0;
		ArrayList<Segment> segs = new ArrayList<>();
		for (int i = ymin; i < ymax; ++i) {
			for (int j = xmin; j < xmax; ++j) {
				for (int k = 0; k < 4; ++k) {
					ArrayList<Panel> subcell = panels.get(i).get(j).get(k);
					if (!subcell.isEmpty()) {
						area += 0.25;
						if (panels.get(i).get(j).get((k + 1) % 4).isEmpty()) {
							segs.add(getLeftSeg(j, i, k, false));
							len += Math.sqrt(2) / 2;
						}
						if (panels.get(i).get(j).get((k + 3) % 4).isEmpty()) {
							segs.add(getRightSeg(j, i, k, false));
							len += Math.sqrt(2) / 2;
						}
						if (panels.get(i + NY[k]).get(j + NX[k]).get((k + 2) % 4).isEmpty()) {
							segs.add(getBottomSeg(j, i, k, false));
							len += 1;
						}
					}
				}
			}
		}
		ArrayList<Pos> ps = new ArrayList<>();
		int cx = segs.get(0).x2;
		int cy = segs.get(0).y2;
		ps.add(new Pos(cx, cy));
		boolean[] used = new boolean[segs.size()];
		used[0] = true;
		for (int i = 1; i < segs.size(); ++i) {
			int nx = -1;
			int ny = -1;
			for (int j = 1; j < segs.size(); ++j) {
				if (used[j]) continue;
				if (segs.get(j).x1 == cx && segs.get(j).y1 == cy) {
					nx = segs.get(j).x2;
					ny = segs.get(j).y2;
					used[j] = true;
					break;
				} else if (segs.get(j).x2 == cx && segs.get(j).y2 == cy) {
					nx = segs.get(j).x1;
					ny = segs.get(j).y1;
					used[j] = true;
					break;
				}
			}
			ps.add(new Pos(nx, ny));
			if (ps.size() > 2) {
				int dx1 = ps.get(ps.size() - 2).x - ps.get(ps.size() - 3).x;
				int dy1 = ps.get(ps.size() - 2).y - ps.get(ps.size() - 3).y;
				int dx2 = nx - ps.get(ps.size() - 2).x;
				int dy2 = ny - ps.get(ps.size() - 2).y;
				if (dy1 * dx2 - dx1 * dy2 == 0) {
					ps.remove(ps.size() - 2);
				}
			}
			cx = nx;
			cy = ny;
		}
		{
			int dx1 = ps.get(0).x - ps.get(ps.size() - 1).x;
			int dy1 = ps.get(0).y - ps.get(ps.size() - 1).y;
			int dx2 = ps.get(1).x - ps.get(0).x;
			int dy2 = ps.get(1).y - ps.get(0).y;
			if (dy1 * dx2 - dx1 * dy2 == 0) {
				ps.remove(0);
			}
		}
		return ps.size() * ps.size() * S + len - Math.sqrt(area);
	}

	void compose() {
		for (int i = ymin; i < ymax; ++i) {
			for (int j = xmin; j < xmax; ++j) {
				for (int k = 0; k < 4; ++k) {
					ArrayList<Panel> subcell = panels.get(i).get(j).get(k);
					for (int l = 0; l < subcell.size(); ++l) {
						if (subcell.get(l).l <= 0) continue;
						collect(j, i, k, l);
					}
				}
			}
		}
	}

	void collect(int x, int y, int d, int l) {
		final int level = panels.get(y).get(x).get(d).get(l).l;
		panels.get(y).get(x).get(d).get(l).l *= -1;
		ArrayList<Segment> cs = new ArrayList<>();
		ArrayList<Segment> os = new ArrayList<>();
		ArrayList<Integer> q = new ArrayList<>();
		q.add((y << 24) | (x << 16) | (d << 14) | l);
		for (int i = 0; i < q.size(); ++i) {
			int cy = q.get(i) >> 24 & 0xFF;
			int cx = (q.get(i) >> 16) & 0xFF;
			int cd = (q.get(i) >> 14) & 0x3;
			int cl = (q.get(i) >> 0) & 0x3FFF;
			Panel cp = panels.get(cy).get(cx).get(cd).get(cl);
			{
				// right seg
				int nd = (cd + 3) % 4;
				ArrayList<Panel> nps = panels.get(cy).get(cx).get(nd);
				boolean found = false;
				for (int j = 0; j < nps.size(); ++j) {
					if (nps.get(j).l == level) {
						q.add((cy << 24) | (cx << 16) | (nd << 14) | j);
						nps.get(j).l *= -1;
						found = true;
						break;
					} else if (nps.get(j).l == -level) {
						found = true;
						break;
					}
				}
				if (!found) {
					cs.add(getRightSeg(cx, cy, cd, false));
					os.add(getRightSeg(cp.x, cp.y, cp.d, cp.flip));
				}
			}
			{
				// left seg
				int nd = (cd + 1) % 4;
				ArrayList<Panel> nps = panels.get(cy).get(cx).get(nd);
				boolean found = false;
				for (int j = 0; j < nps.size(); ++j) {
					if (nps.get(j).l == level) {
						q.add((cy << 24) | (cx << 16) | (nd << 14) | j);
						nps.get(j).l *= -1;
						found = true;
						break;
					} else if (nps.get(j).l == -level) {
						found = true;
						break;
					}
				}
				if (!found) {
					cs.add(getLeftSeg(cx, cy, cd, false));
					os.add(getLeftSeg(cp.x, cp.y, cp.d, cp.flip));
				}
			}
			{
				// bottm seg
				int nx = cx + NX[cd];
				int ny = cy + NY[cd];
				int nd = (cd + 2) % 4;
				ArrayList<Panel> nps = panels.get(ny).get(nx).get(nd);
				boolean found = false;
				for (int j = 0; j < nps.size(); ++j) {
					if (nps.get(j).l == level) {
						q.add((ny << 24) | (nx << 16) | (nd << 14) | j);
						nps.get(j).l *= -1;
						found = true;
						break;
					} else if (nps.get(j).l == -level) {
						found = true;
						break;
					}
				}
				if (!found) {
					cs.add(getBottomSeg(cx, cy, cd, false));
					os.add(getBottomSeg(cp.x, cp.y, cp.d, cp.flip));
				}
			}
		}
		registerResultShape(cs, os);
	}

	void registerResultShape(ArrayList<Segment> dstS, ArrayList<Segment> srcS) {
		//		System.err.println(dstS);
		//		System.err.println(srcS);

		ArrayList<Pos> srcPos = new ArrayList<>();
		ArrayList<Pos> dstPos = new ArrayList<>();
		boolean[] used = new boolean[dstS.size()];
		used[0] = true;
		int cx = dstS.get(0).x2;
		int cy = dstS.get(0).y2;
		dstPos.add(new Pos(cx, cy));
		srcPos.add(new Pos(srcS.get(0).x2, srcS.get(0).y2));
		for (int i = 1; i < used.length; ++i) {
			for (int j = 1; j < used.length; ++j) {
				if (used[j]) continue;
				if (dstS.get(j).x1 == cx && dstS.get(j).y1 == cy) {
					cx = dstS.get(j).x2;
					cy = dstS.get(j).y2;
					dstPos.add(new Pos(cx, cy));
					srcPos.add(new Pos(srcS.get(j).x2, srcS.get(j).y2));
					used[j] = true;
					break;
				} else if (dstS.get(j).x2 == cx && dstS.get(j).y2 == cy) {
					cx = dstS.get(j).x1;
					cy = dstS.get(j).y1;
					dstPos.add(new Pos(cx, cy));
					srcPos.add(new Pos(srcS.get(j).x1, srcS.get(j).y1));
					used[j] = true;
					break;
				}
			}
			if (dstPos.size() > 2) {
				// remove redundant points
				int dx1 = dstPos.get(dstPos.size() - 2).x - dstPos.get(dstPos.size() - 3).x;
				int dy1 = dstPos.get(dstPos.size() - 2).y - dstPos.get(dstPos.size() - 3).y;
				int dx2 = dstPos.get(dstPos.size() - 1).x - dstPos.get(dstPos.size() - 2).x;
				int dy2 = dstPos.get(dstPos.size() - 1).y - dstPos.get(dstPos.size() - 2).y;
				if (dy1 * dx2 - dx1 * dy2 == 0) {
					dstPos.remove(dstPos.size() - 2);
					srcPos.remove(srcPos.size() - 2);
				}
			}
		}
		{
			int dx1 = dstPos.get(dstPos.size() - 1).x - dstPos.get(dstPos.size() - 2).x;
			int dy1 = dstPos.get(dstPos.size() - 1).y - dstPos.get(dstPos.size() - 2).y;
			int dx2 = dstPos.get(0).x - dstPos.get(dstPos.size() - 1).x;
			int dy2 = dstPos.get(0).y - dstPos.get(dstPos.size() - 1).y;
			if (dy1 * dx2 - dx1 * dy2 == 0) {
				dstPos.remove(dstPos.size() - 1);
				srcPos.remove(srcPos.size() - 1);
			}
		}
		{
			int dx1 = dstPos.get(0).x - dstPos.get(dstPos.size() - 1).x;
			int dy1 = dstPos.get(0).y - dstPos.get(dstPos.size() - 1).y;
			int dx2 = dstPos.get(1).x - dstPos.get(0).x;
			int dy2 = dstPos.get(1).y - dstPos.get(0).y;
			if (dy1 * dx2 - dx1 * dy2 == 0) {
				dstPos.remove(0);
				srcPos.remove(0);
			}
		}
		ArrayList<Integer> shape = new ArrayList<>();
		for (int i = 0; i < dstPos.size(); ++i) {
			if (!p2i.containsKey(srcPos.get(i))) {
				p2i.put(srcPos.get(i), p2i.size());
				posMap.put(srcPos.get(i), dstPos.get(i));
			}
			shape.add(p2i.get(srcPos.get(i)));
		}
		srcShape.add(shape);
	}

	Segment getRightSeg(int x, int y, int d, boolean flip) {
		if (flip) {
			return getLeftSeg(x, y, d, false);
		}
		int bx = 2 * x + 1 + DX[d];
		int by = 2 * y + 1 + DY[d];
		return new Segment(bx, by, 2 * x + 1, 2 * y + 1);
	}

	Segment getLeftSeg(int x, int y, int d, boolean flip) {
		if (flip) {
			return getRightSeg(x, y, d, false);
		}
		return getRightSeg(x, y, (d + 1) % 4, false);
	}

	Segment getBottomSeg(int x, int y, int d, boolean flip) {
		int bx = 2 * x + 1 + DX[d];
		int by = 2 * y + 1 + DY[d];
		int ex = 2 * x + 1 + DX[d + 1];
		int ey = 2 * y + 1 + DY[d + 1];
		return flip ? new Segment(ex, ey, bx, by) : new Segment(bx, by, ex, ey);
	}

	void updateBBox() {
		OUT: for (int i = 0;; ++i) {
			for (int j = 0; j < 3 * S; ++j) {
				for (int k = 0; k < 4; ++k) {
					if (!panels.get(j).get(i).get(k).isEmpty()) {
						xmin = i;
						break OUT;
					}
				}
			}
		}
		OUT: for (int i = 3 * S - 1;; --i) {
			for (int j = 0; j < 3 * S; ++j) {
				for (int k = 0; k < 4; ++k) {
					if (!panels.get(j).get(i).get(k).isEmpty()) {
						xmax = i + 1;
						break OUT;
					}
				}
			}
		}
		OUT: for (int i = 0;; ++i) {
			for (int j = 0; j < 3 * S; ++j) {
				for (int k = 0; k < 4; ++k) {
					if (!panels.get(i).get(j).get(k).isEmpty()) {
						ymin = i;
						break OUT;
					}
				}
			}
		}
		OUT: for (int i = 3 * S - 1;; --i) {
			for (int j = 0; j < 3 * S; ++j) {
				for (int k = 0; k < 4; ++k) {
					if (!panels.get(i).get(j).get(k).isEmpty()) {
						ymax = i + 1;
						break OUT;
					}
				}
			}
		}
	}

	void flipHorizontal(int x, int level) {
		if (x * 2 < xmin + xmax) { // left -> right
			for (int i = xmin; i < x; ++i) {
				for (int j = ymin; j < ymax; ++j) {
					for (int k = 0; k < 4; ++k) {
						ArrayList<Panel> subcell = panels.get(j).get(i).get(k);
						for (Panel p : subcell) {
							ArrayList<Panel> to = panels.get(j).get(x + x - i - 1).get(k % 2 == 0 ? k : (k ^ 2));
							to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
						}
						subcell.clear();
					}
				}
			}
		} else { // right -> left
			for (int i = x; i < xmax; ++i) {
				for (int j = ymin; j < ymax; ++j) {
					for (int k = 0; k < 4; ++k) {
						ArrayList<Panel> subcell = panels.get(j).get(i).get(k);
						for (Panel p : subcell) {
							ArrayList<Panel> to = panels.get(j).get(x - (i - x) - 1).get(k % 2 == 0 ? k : (k ^ 2));
							to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
						}
						subcell.clear();
					}
				}
			}
		}
	}

	void flipVertical(int y, int level) {
		if (y * 2 < ymin + ymax) { // bottom -> top
			for (int i = ymin; i < y; ++i) {
				for (int j = xmin; j < xmax; ++j) {
					for (int k = 0; k < 4; ++k) {
						ArrayList<Panel> subcell = panels.get(i).get(j).get(k);
						for (Panel p : subcell) {
							ArrayList<Panel> to = panels.get(y + y - i - 1).get(j).get(k % 2 != 0 ? k : (k ^ 2));
							to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
						}
						subcell.clear();
					}
				}
			}
		} else { // top -> bottom
			for (int i = y; i < ymax; ++i) {
				for (int j = xmin; j < xmax; ++j) {
					for (int k = 0; k < 4; ++k) {
						ArrayList<Panel> subcell = panels.get(i).get(j).get(k);
						for (Panel p : subcell) {
							ArrayList<Panel> to = panels.get(y - (i - y) - 1).get(j).get(k % 2 != 0 ? k : (k ^ 2));
							to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
						}
						subcell.clear();
					}
				}
			}
		}
	}

	void flipSlash(int s, int level) {
		if (s * 2 < (xmax - ymin) + (xmin - ymax)) { // topleft -> bottomright
			for (int x = xmin; x < xmax; ++x) {
				for (int y = x - s + 1; y < ymax; ++y) {
					for (int k = 0; k < 4; ++k) {
						ArrayList<Panel> subcell = panels.get(y).get(x).get(k);
						for (Panel p : subcell) {
							ArrayList<Panel> to = panels.get(x - s).get(y + s).get(k ^ 1);
							to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
						}
						subcell.clear();
					}
				}
				for (int k : new int[] { 0, 3 }) {
					ArrayList<Panel> subcell = panels.get(x - s).get(x).get(k);
					for (Panel p : subcell) {
						ArrayList<Panel> to = panels.get(x - s).get((x - s) + s).get(k ^ 1);
						to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
					}
					subcell.clear();
				}
			}
		} else { // bottomright -> topleft
			for (int x = xmin; x < xmax; ++x) {
				for (int y = ymin; y < x - s; ++y) {
					for (int k = 0; k < 4; ++k) {
						ArrayList<Panel> subcell = panels.get(y).get(x).get(k);
						for (Panel p : subcell) {
							ArrayList<Panel> to = panels.get(x - s).get(y + s).get(k ^ 1);
							to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
						}
						subcell.clear();
					}
				}
				for (int k : new int[] { 1, 2 }) {
					ArrayList<Panel> subcell = panels.get(x - s).get(x).get(k);
					for (Panel p : subcell) {
						ArrayList<Panel> to = panels.get(x - s).get((x - s) + s).get(k ^ 1);
						to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
					}
					subcell.clear();
				}
			}
		}
	}

	void flipBackslash(int s, int level) {
		if (s * 2 < (xmax + ymax) + (xmin + ymin)) { // bottomleft -> topright
			for (int x = xmin; x < xmax; ++x) {
				for (int y = ymin; y < s - x; ++y) {
					for (int k = 0; k < 4; ++k) {
						ArrayList<Panel> subcell = panels.get(y).get(x).get(k);
						for (Panel p : subcell) {
							ArrayList<Panel> to = panels.get(s - x).get(s - y).get(k ^ 3);
							to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
						}
						subcell.clear();
					}
				}
				for (int k : new int[] { 2, 3 }) {
					ArrayList<Panel> subcell = panels.get(s - x).get(x).get(k);
					for (Panel p : subcell) {
						ArrayList<Panel> to = panels.get(s - x).get(s - (s - x)).get(k ^ 3);
						to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
					}
					subcell.clear();
				}
			}
		} else { // topright -> bottomleft
			for (int x = xmin; x < xmax; ++x) {
				for (int y = s - x + 1; y < ymax; ++y) {
					for (int k = 0; k < 4; ++k) {
						ArrayList<Panel> subcell = panels.get(y).get(x).get(k);
						for (Panel p : subcell) {
							ArrayList<Panel> to = panels.get(s - x).get(s - y).get(k ^ 3);
							to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
						}
						subcell.clear();
					}
				}
				for (int k : new int[] { 0, 1 }) {
					ArrayList<Panel> subcell = panels.get(s - x).get(x).get(k);
					for (Panel p : subcell) {
						ArrayList<Panel> to = panels.get(s - x).get(s - (s - x)).get(k ^ 3);
						to.add(new Panel(p.x, p.y, p.d, p.l + (1 << level), !p.flip));
					}
					subcell.clear();
				}
			}
		}
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			int size = 15;
			int foldCount = 9;
			long seed = 1;
			for (int i = 0; i < args.length; ++i) {
				if (args[i].equals("-seed")) {
					seed = Long.parseLong(args[i + 1]);
					++i;
				} else if (args[i].equals("-size")) {
					size = Integer.parseInt(args[i + 1]);
					++i;
				} else if (args[i].equals("-count")) {
					foldCount = Integer.parseInt(args[i + 1]);
					++i;
				}
			}
			ProblemCreater creater = new ProblemCreater(size, foldCount, seed);
			creater.create();
			creater.compose();
			try (PrintWriter writer = new PrintWriter(System.out)) {
				creater.output(writer);
			}
			return;
		}

		long startTime = System.currentTimeMillis();
		String bestResult = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		while (true) {
			if (System.currentTimeMillis() - startTime > 10000) {
				break;
			}
			ProblemCreater creater = new ProblemCreater(15, 9);
			creater.create();
			double value = creater.evaluate();
			creater.compose();
			StringWriter writer = new StringWriter();
			creater.output(new PrintWriter(writer));
			String result = writer.toString();
			int solSize = result.length();
			for (int i = 0; i < result.length(); ++i) {
				if (Character.isWhitespace(result.charAt(i))) {
					solSize--;
				}
			}
			value -= solSize * creater.S / 16.0;
			if (value > bestValue) {
				bestResult = result;
				bestValue = value;
				System.err.println("bestValue:" + bestValue + " seed:" + creater.seed);
			}
		}
		System.out.println(bestResult);

		//		while (true) {
		//			long seed = new Random().nextLong();
		//			if (args.length > 0) {
		//				seed = Long.parseLong(args[0]);
		//			}
		//			ProblemCreater creater = new ProblemCreater(12, 12, seed);
		//			creater.create();
		//			if (!creater.validate()) {
		//				creater.output(new PrintWriter(System.out));
		//				break;
		//			}
		//		}
	}

}

class Pos {
	int x, y;

	public Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		return (x << 16) ^ y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		Pos other = (Pos) obj;
		return x == other.x && y == other.y;
	}

	@Override
	public String toString() {
		return "Pos(" + x + ", " + y + ")";
	}
}

class Segment {
	int x1, y1, x2, y2;

	public Segment(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	@Override
	public String toString() {
		return "Seg[(" + x1 + ", " + y1 + ") - (" + x2 + ", " + y2 + ")]";
	}
}

class Panel {
	int x, y, d, l;
	boolean flip;

	Panel(int x, int y, int d, int l, boolean f) {
		this.x = x;
		this.y = y;
		this.d = d;
		this.l = l;
		this.flip = f;
	}

	@Override
	public String toString() {
		return "Panel [x=" + x + ", y=" + y + ", d=" + d + ", l=" + l + "]";
	}
}
