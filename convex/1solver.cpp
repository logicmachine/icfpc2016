// g++ -std=c++11 -O2 main.cpp
#include "pch.hpp"

using namespace std;
using TransposedPolygon = pair<Polygon, Matrix3x3>;

Polygon destination_convex(const Problem &problem){
	vector<Point> all_points;
	for(const auto &s : problem.skelton){
		all_points.push_back(s.a);
		all_points.push_back(s.b);
	}
	return convex_hull(all_points);
}

bool fold_polygons(vector<TransposedPolygon> &polygons, const Line &line){
	const auto inv_refmat =
		Matrix3x3::transpose(line.a.x, line.a.y) *
		Matrix3x3::reflection(line.b.x - line.a.x, line.b.y - line.a.y) *
		Matrix3x3::transpose(-line.a.x, -line.a.y);
	bool modified = false;
	vector<TransposedPolygon> next;
	for(const auto &tp : polygons){
		const auto l = convex_cut(tp.first, line);
		if(l.area() == Rational()){
			modified = true;
		}else{
			next.emplace_back(l, tp.second);
		}
		const auto r = convex_cut(tp.first, Line(line.b, line.a));
		if(r.area() == Rational()){ continue; }
		modified = true;
		vector<Point> reflected(r.size());
		for(int j = 0; j < r.size(); ++j){
			reflected[r.size() - 1 - j] = line.reflection(r[j]);
		}
		next.emplace_back(Polygon(
			reflected.begin(), reflected.end()), tp.second * inv_refmat);
	}
	polygons.swap(next);
	return modified;
}

static std::string
output(const vector<TransposedPolygon> &current,
       const Point &shift)
{
	std::ostringstream oss;
	map<Point, int> p2i_table;
	map<Point, Point> p2p_table;
	vector<Point> i2p_table;
	for(const auto &tp : current){
		const int m = tp.first.size();
		for(int i = 0; i < m; ++i){
			const auto &p = tp.first[i];
			const auto q = tp.second.transform(p);
			const auto it = p2i_table.find(q);
			if(it == p2i_table.end()){
				p2i_table.emplace(q, i2p_table.size());
				p2p_table.emplace(q, p);
				i2p_table.push_back(q);
			}
		}
	}

	oss << i2p_table.size() << endl;
	for(const auto &p : i2p_table){ oss << p.x << "," << p.y << endl; }

	oss << current.size() << endl;
	for(const auto &tp : current){
		const int m = tp.first.size();
		oss << m;
		for(int i = 0; i < m; ++i){
			const auto q = tp.second.transform(tp.first[i]);
			oss << " " << p2i_table[q];
		}
		oss << endl;
	}

	for(const auto &p : i2p_table){
		const auto v = p2p_table[p] + shift;
		oss << v.x << "," << v.y << endl;
	}

	return oss.str();
}

struct Edge {
	int v[2];
	int other_vertex(int i) {
		if (v[0] == i) {
			return v[1];
		} else {
			return v[0];
		}
	}
};

struct Vertex {
	std::vector< int > edge_list;
	Point pos;
};

struct Graph {
	vector<Edge> edge_list;
	vector<Vertex> vertex_list;

	int find_point(Point const &p) {
		int n = vertex_list.size();
		for (int i=0; i<n; i++) {
			if (vertex_list[i].pos == p) {
				return i;
			}
		}

		return -1;
	}

	void split_edge(int edge_i, int vert_i) {
		Edge &e0 = this->edge_list[edge_i];
		Vertex &ins_v = this->vertex_list[vert_i];

		/* v0 -----e0----*------------- v1
		 *              ins_v
		 *
		 * v0 -----e0----|-----new_e--- v1
		 *              ins_v
		 *
		 */

		//Vertex &v0 = this->vertex_list[e0.v[0]];
		Vertex &v1 = this->vertex_list[e0.v[1]];

		int new_edge_idx = this->edge_list.size();

		ins_v.edge_list.push_back(edge_i);
		ins_v.edge_list.push_back(new_edge_idx);
		Edge new_edge;

		new_edge.v[0] = vert_i;
		new_edge.v[1] = e0.v[1];

		e0.v[1] = vert_i;

		for (int &ep : v1.edge_list) {
			if (ep == edge_i) {
				ep = new_edge_idx;
			}
		}

		this->edge_list.push_back(new_edge);
	}


	void dump() {
		for (auto && e : edge_list) {
			std::cerr << "edge:" << e.v[0] << "-" << e.v[1] << '\n';
		}

		for (auto && v : vertex_list) {
			std::cerr << "vert:" << v.pos.x << ',' << v.pos.y << ":" ;

			for (auto && e : v.edge_list) {
				std::cerr << e << ',';
			}

			std::cerr << '\n';
		}
	}
};

static void
split_intersect(Graph &g)
{

	for (int i=0; i<(int)g.edge_list.size(); i++) {
		int nl = g.edge_list.size();
		for (int j=i+1; j<nl; j++) {
			Edge &e0 = g.edge_list[i];
			Edge &e1 = g.edge_list[j];

			Segment s0 = Segment(g.vertex_list[e0.v[0]].pos,
					     g.vertex_list[e0.v[1]].pos);
			Segment s1 = Segment(g.vertex_list[e1.v[0]].pos,
					     g.vertex_list[e1.v[1]].pos);

			if (! intersect(s0, s1)) {
				continue;
			}

			Line l0 = s0.to_line();
			Line l1 = s1.to_line();

			std::vector<Point> cp = crossing_points(l0, s1);

			if (cp.size() != 1) {
				/* aa */
				continue;
			}


			Point &p0 = cp[0];;

			int cross_e0 = -1;
			int cross_e1 = -1;

			if (p0 == g.vertex_list[e0.v[0]].pos) {
				cross_e0 = e0.v[0];
			} else if (p0 == g.vertex_list[e0.v[1]].pos) {
				cross_e0 = e0.v[1];
			}


			if (p0 == g.vertex_list[e1.v[0]].pos) {
				cross_e1 = e1.v[0];
			} else if (p0 == g.vertex_list[e1.v[1]].pos) {
				cross_e1 = e1.v[1];
			}

			if (cross_e0 != -1) {
				if (cross_e1 != -1) {
					/* 両方端 (nop) */
				} else {
					/* e0 の端と e1 の中間 */
					g.split_edge(j, cross_e0);
				}
			} else {
				if (cross_e1 != -1) {
					/* e1 の端と e0 の中間 */
					g.split_edge(i, cross_e1);
				} else {
					/* 両方中間 */
					int new_vertex_idx = g.vertex_list.size();
					Vertex new_v;
					new_v.pos = p0;

					g.vertex_list.push_back(new_v);

					g.split_edge(i, new_vertex_idx);
					g.split_edge(j, new_vertex_idx);
				}
			}
		}
	}
}

static void
build_graph(Graph &g,
	    Problem const &p)
{
	int ns = p.skelton.size();
	for (int si=0; si<ns; si++) {
		const Point &p0 = p.skelton[si].a;
		const Point &p1 = p.skelton[si].b;

		int vi0 = g.find_point(p0);
		if (vi0 == -1) {
			Vertex v;
			vi0 = g.vertex_list.size();
			v.pos = p0;
			g.vertex_list.push_back(v);
		}

		int vi1 = g.find_point(p1);
		if (vi1 == -1) {
			Vertex v;
			vi1 = g.vertex_list.size();
			v.pos = p1;
			g.vertex_list.push_back(v);
		}

		Edge e;
		int ei = g.edge_list.size();
		e.v[0] = vi0;
		e.v[1] = vi1;

		g.edge_list.push_back(e);
		g.vertex_list[vi0].edge_list.push_back(ei);
		g.vertex_list[vi1].edge_list.push_back(ei);
	}
}

typedef std::vector<std::vector<int> > route_set_t;

static bool
on_line_3p(const Point &p0,
	   const Point &p1,
	   const Point &p2)
{
	Rational v0 = p0.y * (p1.x-p2.x);
	Rational v1 = p1.y * (p2.x-p0.x);
	Rational v2 = p2.y * (p0.x-p1.x);

	return (v0 + v1 + v2) == Rational(0);
}

static void
find_len1_visit(Graph &g,
		route_set_t &rs,
		std::vector<int> route,
		int vi,
		int from_vi,
		const Point &start_pos,
		const Matrix3x3 &pm,
		Rational const &prev_len)
{
	Vertex &v0 = g.vertex_list[vi];

	Point p_new = pm.transform(v0.pos);
	Rational len = (start_pos - p_new).norm();
	Point &from_pos = g.vertex_list[from_vi].pos;

	//std::cerr << "cur=" << vi << '\n';
	//for (auto v : route) {
	//	std::cerr << v << ',';
	//}
	//std::cerr << '\n';

	route.push_back(vi);

	Point dp = (start_pos - p_new);
	//std::cerr << "cur:" << v0.pos.x << ',' << v0.pos.y << '=' << len.to_double() << '\n';
	//std::cerr << "new:" << p_new.x << ',' << p_new.y << '=' << len.to_double() << '\n';
	//std::cerr << "sta:" << start_pos.x << ',' << start_pos.y << '=' << len.to_double() << '\n';
	//std::cerr << "dp :" << dp.x << ',' << dp.y << '=' << len.to_double() << '\n';

	if (route.size() > 16) {
		return;
	}

	if (len.nume() > Rational::IntType(10000000)) {
		/* too slow for large number */
		return;
	}

	if (len <= prev_len) {
		exit(1);
		return;
	}

	std::vector<char> visited;
	visited.resize(g.vertex_list.size(), 0);

	if (len == Rational(1)) {
		/* find ! */
		//std::cerr << "found\n";
		rs.push_back(route);
	} else if (len < Rational(1)) {

		int ne = v0.edge_list.size();
		for (int ei_to=0; ei_to<ne; ei_to++) {
			Edge &e_to = g.edge_list[v0.edge_list[ei_to]];
			int to_vi = e_to.other_vertex(vi);

			for (int ei_ref=0; ei_ref<ne; ei_ref++) {
				if (ei_ref == ei_to) {
					continue;
				}

				Edge &e_ref = g.edge_list[v0.edge_list[ei_ref]];
				int ref_vi = e_ref.other_vertex(vi);

				//std::cerr << "vi:" << ref_vi << "," << from_vi << '\n';

				if (ref_vi == from_vi) {
					continue;
				}

				//std::cerr << "ei:" << ei_to << "," << v0.edge_list[ei_to] << "," << ei_ref << "," << v0.edge_list[ei_ref] << '\n';

				Vertex &to = g.vertex_list[to_vi];
				Vertex &ref = g.vertex_list[ref_vi];
				Vertex &cur = v0;

				if (visited[to_vi]) {
					continue;
				}

				visited[to_vi] = 1;

				//std::cerr << "cur " << vi << ":" << cur.pos.x << "," << cur.pos.y << "\n";
				//std::cerr << "ref " << ref_vi << ":" << ref.pos.x << "," << ref.pos.y << "\n";
				//std::cerr << "from " << from_vi << ":" << from_pos.x << "," << from_pos.y << "\n";
				//std::cerr << "to " << to_vi << ":" << to.pos.x << "," << to.pos.y << "\n";

				if (ccw(to.pos,cur.pos,from_pos)==-2) {
					find_len1_visit(g, rs, route, to_vi, vi, start_pos, pm, len);
				} else {
					if (on_line_3p(ref.pos,cur.pos,to.pos))
					{
						continue;
					}

					Point ref_ortho;

					Rational dx = ref.pos.x - cur.pos.x;
					Rational dy = ref.pos.y - cur.pos.y;

					ref_ortho.x = cur.pos.x + dy;
					ref_ortho.y = cur.pos.y - dx;

					Line ortho_line;
					ortho_line.a = ref_ortho;
					ortho_line.b = cur.pos;

					Point reflect_to = ortho_line.reflection(to.pos);

					bool on_line = on_line_3p(reflect_to,
								  cur.pos,
								  from_pos);
					if (on_line) {
						Point cur_new = pm.transform(cur.pos);
						Point ref_new = pm.transform(ref.pos);

						const auto inv_refmat =
							(Matrix3x3::transpose(cur_new.x, cur_new.y) *
							 Matrix3x3::reflection(ref_new.x - cur_new.x,
									       ref_new.y - cur_new.y) *
							 Matrix3x3::transpose(-cur_new.x, -cur_new.y)) * pm;

						//std::cerr << inv_refmat(0,0) << ',';
						//std::cerr << inv_refmat(0,1) << ',';
						//std::cerr << inv_refmat(0,2) << '\n';
						//std::cerr << inv_refmat(1,0) << ',';
						//std::cerr << inv_refmat(1,1) << ',';
						//std::cerr << inv_refmat(1,2) << '\n';
						//std::cerr << inv_refmat(2,0) << ',';
						//std::cerr << inv_refmat(2,1) << ',';
						//std::cerr << inv_refmat(2,2) << "\n\n";

						find_len1_visit(g, rs, route, to_vi, vi, start_pos, inv_refmat, len);
					}
				}
			}
		}
	}

	//std::cerr << "ret\n";
	//for (auto v : route) {
	//	std::cerr << v << ',';
	//}
	//std::cerr << "\n\n\n";

}

static route_set_t // list of vertex index
find_len1_edge(Graph &g)
{
	route_set_t rs;
	int nv = g.vertex_list.size();
	Matrix3x3 pm = Matrix3x3::identity();

	for (int vi=0; vi<nv; vi++) {
		Vertex &v = g.vertex_list[vi];
		int ne = v.edge_list.size();
		std::vector<int> route;
		route.push_back(vi);

		for (int eidx=0; eidx<ne; eidx++) {
			int ei = v.edge_list[eidx];
			Edge &e = g.edge_list[ei];
			int oi = e.other_vertex(vi);

			find_len1_visit(g, rs, route, oi, vi,
					v.pos, Matrix3x3::identity(), Rational(0));
		}
	}

	return rs;
}

int main(){
	const auto problem = Problem::load(std::cin);

	/* find 1 */
	struct Graph g;

	build_graph(g, problem);
	//g.dump();

	split_intersect(g);
	//g.dump();

	route_set_t rs = find_len1_edge(g);

	for (auto &&route : rs) {
		for (auto &&p : route) {
			std::cout << p << ',';
		}
		std::cout << '\n';
	}
	exit(1);
#if 0

	auto convex = destination_convex(problem);
	const int n = convex.size();
	Point shift = convex[0], bound = convex[0];
	std::string prev;
	for(int i = 0; i < n; ++i){
		shift.x = min(shift.x, convex[i].x);
		shift.y = min(shift.y, convex[i].y);
		bound.x = max(bound.x, convex[i].x);
		bound.y = max(bound.y, convex[i].y);
	}
	for(int i = 0; i < n; ++i){ convex[i] -= shift; }
	bound -= shift;


	vector<TransposedPolygon> current;
	current.emplace_back(
		Polygon({
			Point(Rational(0), Rational(0)),
			Point(Rational(1), Rational(0)),
			Point(Rational(1), Rational(1)),
			Point(Rational(0), Rational(1))
		}),
		Matrix3x3::identity());

#define CHECK_LENGTH() {					\
		std::string result = output(current, shift);	\
		if (result.size() > 5000) {			\
			goto quit;				\
		}						\
		prev = result;					\
	}

	Rational cur_width = Rational(1), cur_height = Rational(1);
	while(cur_width > bound.x){
		cur_width = max(cur_width / Rational(2), bound.x);
		fold_polygons(current, Line(
			Point(cur_width, Rational(0)), Point(cur_width, Rational(1))));
		CHECK_LENGTH();
	}
	while(cur_height > bound.y){
		cur_height = max(cur_height / Rational(2), bound.y);
		fold_polygons(current, Line(
			Point(Rational(1), cur_height), Point(Rational(0), cur_height)));
		CHECK_LENGTH();
	}

	while(true){
		bool modified = false;
		for(int i = 0; i < n; ++i){
			const auto line = convex.side(i).to_line();
			if(fold_polygons(current, line)){ modified = true; }

			CHECK_LENGTH();
		}

		if(!modified){ break; }
	}

quit:
	std::cout << prev ;


	return 0;
#endif
}

