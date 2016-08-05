#include "pch.hpp"

#define RENDER_WIDTH 1024
#define RENDER_HEIGHT 1024

namespace bg = boost::geometry;
namespace bm = boost::multiprecision;

typedef bm::mpq_rational val_t;
typedef bg::model::d2::point_xy<val_t> point_t;
typedef bg::model::linestring<point_t> line_t;
typedef bg::model::segment<point_t> seg_t;
typedef bg::model::polygon<point_t> poly_t;

static double
get_double(val_t const &v)
{
    return (double) v;
}


struct Input {
    val_t x_min;
    val_t y_min;
    val_t x_max;
    val_t y_max;

    std::vector<poly_t> facet_list;
    std::vector<seg_t> seg_list;
};

struct TransformStat {
    val_t move_x;
    val_t move_y;

    TransformStat()
        :move_x(0),
         move_y(0)
    {}
};

struct History {

};

struct RefPoly {
    std::vector<int> vertex_list;
};

struct Solution {
    std::vector<point_t> src_point;
    std::vector<RefPoly> src_poly;
    std::vector<point_t> dst_point;
};

static void
output_solution(Solution &s, 
                TransformStat &ts)
{
    std::cout << s.src_point.size() << '\n';
    for (auto &&p : s.src_point) {
        std::cout << p.x()
                  << ','
                  << p.y()
                  << '\n';
    }

    std::cout << s.src_poly.size() << '\n';
    for (auto &&poly : s.src_poly) {
        auto & p0_list = poly.vertex_list;
        std::cout << p0_list.size() << ' ';

        for (auto &&pv : p0_list ) {
            std::cout << pv << ' ';
        }

        std::cout << '\n';
    }

    for (auto &&p : s.dst_point) {
        std::cout << (p.x()-ts.move_x)
                  << ','
                  << (p.y()-ts.move_y)
                  << '\n';
    }


    /* debug */
#if 0
    std::cerr << s.src_point.size() << '\n';
    for (auto &&p : s.src_point) {
        std::cerr << get_double(p.x())
                  << ','
                  << get_double(p.y())
                  << '\n';
    }

    std::cerr << s.src_poly.size() << '\n';
    for (auto &&poly : s.src_poly) {
        auto & p0_list = poly.vertex_list;
        std::cerr << p0_list.size() << ' ';

        for (auto &&pv : p0_list ) {
            std::cerr << get_double(pv) << ' ';
        }

        std::cerr << '\n';
    }

    for (auto &&p : s.dst_point) {
        std::cerr << get_double((p.x()-ts.move_x))
                  << ','
                  << get_double((p.y()-ts.move_y))
                  << '\n';
    }
#endif

}

static Solution
simple_solution()
{
    Solution s;
    s.src_point.push_back(point_t(0,0));
    s.src_point.push_back(point_t(1,0));
    s.src_point.push_back(point_t(1,1));
    s.src_point.push_back(point_t(0,1));

    RefPoly rp;
    rp.vertex_list = std::vector<int>({0,1,2,3});
    s.src_poly.push_back(std::move(rp));

    s.dst_point.push_back(point_t(0,0));
    s.dst_point.push_back(point_t(1,0));
    s.dst_point.push_back(point_t(1,1));
    s.dst_point.push_back(point_t(0,1));

    return s;
}

#if 0
static Solution
fold(Solution *prev_sol,
     line_t const &cut_line)
{
    /*
     * 1. 交差してる線は切る
     *   1.1. vertex を 増やす
     *   1.2. 切られるlineをはずす
     *   1.3. 切ったlineふたつを追加
     *
     */
    Solution next;

    auto &llist = prev_sol->line_list;
    auto &vlist = prev_sol->vertex_list;

    int n=llist.size();
    for (int li=0; li<n; li++) {
        std::vector<point_t> ip;
        seg_t s0(vlist[llist[li].first],
                 vlist[llist[li].second]);
        seg_t s1(vlist[llist[li].first],
                 vlist[llist[li].second]);

        bg::intersects(s0, s1);

        if (ip.size() == 1) {

        }
    }

    next.prev = prev_sol;
    return std::move(next);
}
#endif

static Solution
kuso_solver(Input &i)
{
    Solution s;

    val_t xm = i.x_max;
    val_t ym = i.y_max;

    if (xm >= 1||ym>=1) {
        if (ym < 1) {
            ym = std::max(val_t(0.51), ym);

            s.src_point.push_back(point_t(0,0));
            s.src_point.push_back(point_t(1,0));

            s.src_point.push_back(point_t(0,ym));
            s.src_point.push_back(point_t(1,ym));

            s.src_point.push_back(point_t(0,1));
            s.src_point.push_back(point_t(1,1));

            RefPoly rp;
            rp.vertex_list = std::vector<int>({0,1,3,2});
            s.src_poly.push_back(std::move(rp));

            rp.vertex_list = std::vector<int>({2,3,5,4});
            s.src_poly.push_back(std::move(rp));

            val_t ym2 = 1-(1-ym)*2;

            //std::cerr << xm << "," << xm2 << '\n';

            s.dst_point.push_back(point_t(0,0));
            s.dst_point.push_back(point_t(1,0));

            s.dst_point.push_back(point_t(0,ym));
            s.dst_point.push_back(point_t(1,ym));

            s.dst_point.push_back(point_t(0,ym2));
            s.dst_point.push_back(point_t(1,ym2));

            return s;
        }

        if (xm < 1) {
            xm = std::max(val_t(0.51), xm);

            s.src_point.push_back(point_t(0,0));
            s.src_point.push_back(point_t(xm,0));
            s.src_point.push_back(point_t(1,0));

            s.src_point.push_back(point_t(0,1));
            s.src_point.push_back(point_t(xm,1));
            s.src_point.push_back(point_t(1,1));

            RefPoly rp;
            rp.vertex_list = std::vector<int>({0,1,4,3});
            s.src_poly.push_back(std::move(rp));

            rp.vertex_list = std::vector<int>({1,2,5,4});
            s.src_poly.push_back(std::move(rp));

            val_t xm2 = 1-(1-xm)*2;

            s.dst_point.push_back(point_t(0,0));
            s.dst_point.push_back(point_t(xm,0));
            s.dst_point.push_back(point_t(xm2,0));

            s.dst_point.push_back(point_t(0,1));
            s.dst_point.push_back(point_t(xm,1));
            s.dst_point.push_back(point_t(xm2,1));

            return s;
        }

        return simple_solution();
    }

    xm = std::max(val_t(0.51), xm);
    ym = std::max(val_t(0.51), ym);

    s.src_point.push_back(point_t(0,0));
    s.src_point.push_back(point_t(xm,0));
    s.src_point.push_back(point_t(1,0));

    s.src_point.push_back(point_t(0,ym));
    s.src_point.push_back(point_t(xm,ym));
    s.src_point.push_back(point_t(1,ym));

    s.src_point.push_back(point_t(0,1));
    s.src_point.push_back(point_t(xm,1));
    s.src_point.push_back(point_t(1,1));

    RefPoly rp;
    rp.vertex_list = std::vector<int>({0,1,4,3});
    s.src_poly.push_back(std::move(rp));

    rp.vertex_list = std::vector<int>({1,2,5,4});
    s.src_poly.push_back(std::move(rp));

    rp.vertex_list = std::vector<int>({3,4,7,6});
    s.src_poly.push_back(std::move(rp));

    rp.vertex_list = std::vector<int>({4,5,8,7});
    s.src_poly.push_back(std::move(rp));

    val_t xm2 = 1-(1-xm)*2;
    val_t ym2 = 1-(1-ym)*2;

    //std::cerr << xm << "," << xm2 << '\n';

    s.dst_point.push_back(point_t(0,0));
    s.dst_point.push_back(point_t(xm,0));
    s.dst_point.push_back(point_t(xm2,0));

    s.dst_point.push_back(point_t(0,ym));
    s.dst_point.push_back(point_t(xm,ym));
    s.dst_point.push_back(point_t(xm2,ym));

    s.dst_point.push_back(point_t(0,ym2));
    s.dst_point.push_back(point_t(xm,ym2));
    s.dst_point.push_back(point_t(xm2,ym2));

    return s;

}


static void
move_to_zero(TransformStat &ts,
             Input &i)
{
    val_t xm = i.x_min;
    val_t ym = i.y_min;

    ts.move_x = -xm;
    ts.move_y = -ym;

    //std::cerr << "size:"
    //          << get_double(i.x_max) << "-"
    //          << get_double(i.x_min) << 'x'
    //          << get_double(i.y_max) << '-'
    //          << get_double(i.y_min) << '\n';

    i.x_min = 0;
    i.y_min = 0;
    i.x_max -= xm;
    i.y_max -= ym;

    //std::cerr << "a:" << get_double(xm) << "," << get_double(ym) << '\n';
    //std::cerr << "b:" << get_double(i.x_max) << "," << get_double(i.y_max) << '\n';

    for (auto &poly : i.facet_list) {
        auto &p0_list = poly.outer();

        for (auto &v : p0_list) {
            v.x(v.x() - xm);
            v.y(v.y() - ym);
        }
    }

    for (auto &s : i.seg_list) {
        bg::set<0,0>(s, bg::get<0,0>(s) - xm);
        bg::set<0,1>(s, bg::get<0,1>(s) - ym);
        bg::set<1,0>(s, bg::get<1,0>(s) - xm);
        bg::set<1,1>(s, bg::get<1,1>(s) - ym);
    }
}

static Input load(std::istream &ins)
{
    Input i;
    int np;

    ins >> np;

    for (int pi=0; pi<np; pi++) {
        int nv;

        ins >> nv;

        poly_t poly;

        for (int vi=0; vi<nv; vi++) {
            std::string xs, ys;

            getline(ins, xs, ',');
            getline(ins, ys);

            val_t px(xs);
            val_t py(ys);

            poly.outer().push_back(point_t(px,py));
        }

        i.facet_list.push_back(poly);
    }

    int nl;

    ins >> nl;

    for (int li=0; li<nl; li++) {
        std::string x0,y0, x1,y1;

        getline(std::cin, x0, ',');
        getline(std::cin, y0, ' ');
        getline(std::cin, x1, ',');
        getline(std::cin, y1);

        seg_t s;
        bg::set<0,0>(s, val_t(x0));
        bg::set<0,1>(s, val_t(y0));
        bg::set<1,0>(s, val_t(x1));
        bg::set<1,1>(s, val_t(y1));
        i.seg_list.push_back(s);
    }

    return std::move(i);
}

struct Render {
    cairo_t *c;
    val_t scale;
    val_t off_x;
    val_t off_y;

    void move_to(val_t const &x, val_t const &y) {
        cairo_move_to(c,
                      get_double((x+off_x) * scale) + 100,
                      900-get_double((y+off_y) * scale));
    }

    void line_to(val_t const &x, val_t const &y) {
        cairo_line_to(c,
                      get_double((x+off_x) * scale) + 100,
                      900-get_double((y+off_y) * scale));
    }
};

static Render
build_render(cairo_t *c,
             Input &i)
{
    auto p0_list = i.facet_list[0].outer();
    val_t x_max(p0_list[0].x());
    val_t x_min(p0_list[0].x());
    val_t y_max(p0_list[0].y());
    val_t y_min(p0_list[0].y());

    for (auto &&poly : i.facet_list) {
        auto p0_list = poly.outer();

        for (auto &&v : p0_list) {
            x_max = std::max(v.x(), x_max);
            x_min = std::min(v.x(), x_min);

            y_max = std::max(v.y(), y_max);
            y_min = std::min(v.y(), y_min);
        }
    }

    val_t off_x = -x_min;
    val_t off_y = -y_min;
    val_t scale = 800 / (x_max - x_min);
    val_t scale_y = 800 / (y_max - y_min);

    if (scale_y < scale) {
        scale = scale_y;
    }

    Render r;
    r.c = c;
    r.scale = scale;
    //r.off_x = off_x;
    //r.off_y = off_y;

    r.off_x = 0;
    r.off_y = 0;

    //std::cout << (800/get_double(scale)) << '\n';

    i.x_min = x_min;
    i.y_min = y_min;
    i.x_max = x_max;
    i.y_max = y_max;

    //std::cerr << "c:" << x_min << "," << y_min << '\n';
    //std::cerr << "d:" << x_max << "," << y_max << '\n';

    return r;
}

static void
render_result(Render &r,
              Input &i,
              Solution &s)
{
    /* in */
    cairo_set_source_rgb(r.c, 0, 1, 0);
    cairo_set_line_width(r.c, 2);

    for (auto &&s : i.seg_list) {
        r.move_to(bg::get<0,0>(s),
                  bg::get<0,1>(s));
        r.line_to(bg::get<1,0>(s),
                  bg::get<1,1>(s));
        cairo_stroke(r.c);
    }

    /* out */
    cairo_set_source_rgb(r.c, 0.5, 0.5, 1);
    cairo_set_line_width(r.c, 4);
    for (auto &&poly : i.facet_list) {
        auto p0_list = poly.outer();

        r.move_to(p0_list[0].x(),
                  p0_list[0].y());

        int n = p0_list.size();

        for (int pi=1; pi<n; pi++) {
            r.line_to(p0_list[pi].x(),
                      p0_list[pi].y());
        }

        cairo_close_path(r.c);

        cairo_stroke(r.c);
    }


    /* result */
    cairo_set_source_rgb(r.c, 1, 0, 0);
    cairo_set_line_width(r.c, 2);

    //std::cout << s.src_poly.size() << '\n';
    for (auto &&poly : s.src_poly) {
        auto & p0_list = poly.vertex_list;
        int n = p0_list.size();

        r.move_to(s.dst_point[p0_list[0]].x(),
                  s.dst_point[p0_list[0]].y());
        for (int pi=1; pi<n; pi++) {
            r.line_to(s.dst_point[p0_list[pi]].x(),
                      s.dst_point[p0_list[pi]].y());
        }
        cairo_close_path(r.c);
        cairo_stroke(r.c);
    }

}

int
main(int argc, char **argv)
{
    Input input = load(std::cin);
    TransformStat ts;

    cairo_surface_t *dst = cairo_image_surface_create(CAIRO_FORMAT_RGB24,
                                                      RENDER_WIDTH,
                                                      RENDER_HEIGHT);

    cairo_t *c = cairo_create(dst);

    cairo_set_source_rgb(c, 1, 0, 0);

    Render r = build_render(c, input);
    move_to_zero(ts, input);

    Solution s = kuso_solver(input);
    output_solution(s, ts);

    render_result(r, input, s);

    cairo_destroy(c);

    cairo_surface_write_to_png(dst, "out.png");
}