struct ExecState {
	int next;
	// 0=edge=0, reflect=0
	// 1=edge=0, reflect=1
	// 2=edge=1, reflect=0
	// 3=edge=1, reflect=1

	int depth;

	SqBuilder builder;
	FrontierEdge *next_enhance;
};



static bool
add_frontier_initial(SqBuilder &b,
                     Graph &g,
                     int poly_id,
                     Matrix3x3 const &transform_matrix)
{
	GraphPolygon &poly = g.polygon_list[poly_id];
	FrontierEdge *fe = b.frontier_edge_list;
	int nv = poly.size();
	std::vector<int> result_polygon;

	FrontierEdge *first = NULL, *prev = NULL;

	for (int vi=0; vi<nv; vi++) {
		VertexRef &vr = poly[vi];
		VertexRef &vr_next = poly[(vi+1)%nv];
		Vertex &v = g.vertex_list[vr.vertex_id];
		Edge &e = g.edge_list[v.edge_list[vr.edge_idx_in_vertex]];

		FrontierEdge *fe = b.pool->alloc<FrontierEdge>();
		if (first == NULL) {
			first = fe;
		}
		if (prev) {
			prev->next = fe;
		}
		fe->prev = prev;
		fe->next = NULL;

		prev = fe;

		fe->dst_p0 = g.vertex_list[vr.vertex_id].pos;
		fe->dst_p1 = g.vertex_list[vr_next.vertex_id].pos;

		Point src_p0 = transform_matrix.transform(fe->dst_p0);
		Point src_p1 = transform_matrix.transform(fe->dst_p1);

		fe->src_p0 = src_p0;
		fe->src_p1 = src_p1;

		bool added = false;
		int new_vi = b.add_result_point(src_p0, fe->dst_p0, &added);
		if (new_vi == -1) {
			return false;
		}

		fe->src_p0_vert_idx = new_vi;

		result_polygon.push_back(new_vi);

		fe->putter_polygon = poly_id;
		fe->dst_edge = &e;
	}

	prev->next = NULL;
	b.frontier_edge_list = first;

	b.result_polygon_list.push_back(result_polygon);

	return true;

}



static bool
add_frontier_polygon1(SqBuilder &b,
		      Graph &g,
		      int poly_id,
		      Matrix3x3 const &transform_matrix)
{
	GraphPolygon &poly = g.polygon_list[poly_id];
	FrontierEdge *fe = b.frontier_edge_list;
	int nv = poly.size();
	std::vector<int> result_polygon;

	std::vector<char> merged;

	merged.resize(nv,0);

	{
		fprintf(stderr, "add polygon pid=%d\n", poly_id);
		for (int vi=0; vi<nv; vi++) {
			VertexRef &vr = poly[vi];
			Vertex &v = g.vertex_list[vr.vertex_id];
			Point p0 = transform_matrix.transform(v.pos);

			fprintf(stderr,
				"   (%f,%f)\n",
				p0.x.to_double(),
				p0.y.to_double());
		}

	}



	for (int vi=0; vi<nv; vi++) {
		VertexRef &vr = poly[vi];
		VertexRef &vr_next = poly[(vi+1)%nv];

		Vertex &v = g.vertex_list[vr.vertex_id];
		Vertex &vn = g.vertex_list[vr_next.vertex_id];

		Point p0 = transform_matrix.transform(v.pos);
		Point p1 = transform_matrix.transform(vn.pos);

		if (b.find_merge_edge(p0,p1)) {
			merged[vi] = 1;
		}

		bool added = false;
		int new_vi = b.add_result_point(p0, v.pos, &added);

		//fprintf(stderr, "add %d %d (%f,%f)\n",
		//	new_vi,
		//	(int)added,
		//	v.pos.x.to_double(),
		//	v.pos.y.to_double());

		if (new_vi == -1) {
			/* オーバーラップしてる || src が 四角に入ってない */
			fputs("add fail\n", stderr);
			return false;
		}

		if (!added) {
			bool merge_to_frontier = false;
			FrontierEdge *e = b.frontier_edge_list;
			while (e) {
				if (e->src_p0_vert_idx == new_vi) {
					merge_to_frontier = true;
				}

				e = e->next;
			}

			if (!merge_to_frontier) {
				fputs("fail merge to frontier\n", stderr);

				return false;
			}
		}

		result_polygon.push_back(new_vi);
	}

	int down_count = 0; // 1->0
	int cur_in_1=0;
	int start_pos = -1;
	int end_pos = -1;

	if (merged[0]) {
		cur_in_1 = 1;
	} else {
		if (merged[nv-1]) { // 0 . . . . 1
			down_count++;
		}
	}

	for (int vi=1; vi<nv; vi++) {
		if (cur_in_1) {
			if (merged[vi] == 0) {
				down_count++;
				cur_in_1 = 0;
				end_pos = vi;
			}
		} else {
			if (merged[vi] == 1) {
				cur_in_1 = 1;
				if (start_pos == -1) {
					start_pos = vi;
				}
			}
		}
	}

	if (down_count > 1) {
		/* hole ができる場合はあとまわし is 何 ?? */
		//puts("hole");
		//abort();
		fputs("fail hall\n", stderr);
		return false;
		b.retry_count++;
		if (b.retry_count > 10000) {
			return false;
		}
		return true;
	}

	bool remove_all = true;
	for (int vi=0; vi<nv; vi++) {
		VertexRef &vr = poly[vi];
		VertexRef &vr_next = poly[(vi+1)%nv];

		Vertex &v = g.vertex_list[vr.vertex_id];
		Vertex &vn = g.vertex_list[vr_next.vertex_id];

		Point p0 = transform_matrix.transform(v.pos);
		Point p1 = transform_matrix.transform(vn.pos);

		Edge &dst_e = g.edge_list[v.edge_list[vr.edge_idx_in_vertex]];

		FrontierEdge *e = b.find_merge_edge(p0,p1);
		if (e) {
			b.dump_frontier();

			fprintf(stderr,
				"remove (%f,%f) - (%f,%f)\n",
				e->src_p0.x.to_double(),
				e->src_p0.y.to_double(),
				e->src_p1.x.to_double(),
				e->src_p1.y.to_double());

			if (e->prev) {
				e->prev->next = e->next;
			} else {
				/* 先頭 */
				b.frontier_edge_list = e->next;
			}

			if (e->next) {
				e->next->prev = e->prev;
			}

			b.dump_frontier();

			fputs("\n\n", stderr);
		} else {
			remove_all = false;
			fprintf(stderr,
				"add (%f,%f) - (%f,%f)\n",
				p0.x.to_double(),
				p0.y.to_double(),
				p1.x.to_double(),
				p1.y.to_double());

			/* つなげる */
			FrontierEdge *new_edge = b.pool->alloc<FrontierEdge>();

			if (b.frontier_edge_list == NULL) {
				fputs("fail cannot add edge", stderr);
				return false;
			}

			assert(b.frontier_edge_list != NULL);

			b.frontier_edge_list->prev = new_edge;
			new_edge->next = b.frontier_edge_list;
			new_edge->prev = NULL;
			b.frontier_edge_list = new_edge;

			new_edge->dst_p0 = v.pos;
			new_edge->dst_p1 = vn.pos;
			bool added = false;
			new_edge->src_p0_vert_idx = b.add_result_point(p0, v.pos, &added);

			new_edge->src_p0 = p0;
			new_edge->src_p1 = p1;

			new_edge->dst_edge = &dst_e;
			new_edge->putter_polygon = poly_id;
		}
	}

	if (remove_all) {
		/* 同じサイズのがふたつある */
		fputs("NULL list\n", stderr);
		return false;
	}

	//fprintf(stderr, "result length = %d\n", (int)result_polygon.size());
	b.result_polygon_list.push_back(result_polygon);

	return true;
}


static FrontierEdge *
find_next_enhance(SqBuilder &b)
{
	FrontierEdge *e = b.frontier_edge_list;
	assert(e != NULL);

	while (e) {
		if (! e->side_edge()) {
			return e;
		}
		e = e->next;
	}

	b.output();
	exit(1);
}

