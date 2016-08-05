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

struct Input {
    val_t x_min;
    val_t y_min;

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

struct Solution {
    Solution *prev;             // NULL if root
    std::vector<std::pair<int,int> > line_list;
    std::vector<point_t> vertex_list;
};

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

    next.prev = prev_sol;
    return std::move(next);
}


static void
move_to_zero(TransformStat &ts,
             Input &i)
{
    val_t &xm = i.x_min;
    val_t &ym = i.y_min;

    ts.move_x = -xm;
    ts.move_y = -ym;

    i.x_min = 0;
    i.y_min = 0;

    for (auto &&poly : i.facet_list) {
        auto p0_list = poly.outer();

        for (auto &&v : p0_list) {
            v.x(v.x() - xm);
            v.y(v.y() - ym);
        }
    }

    for (auto &&s : i.seg_list) {
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

static double
get_double(val_t const &v)
{
    return (double) v;
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
    r.off_x = off_x;
    r.off_y = off_y;

    std::cout << (800/get_double(scale)) << '\n';

    i.x_min = x_min;
    i.y_min = y_min;

    return r;
}

static void
render_input(Render &r,
             Input &i)
{
    cairo_set_source_rgb(r.c, 0, 1, 0);

    for (auto &&s : i.seg_list) {
        r.move_to(bg::get<0,0>(s),
                  bg::get<0,1>(s));
        r.line_to(bg::get<1,0>(s),
                  bg::get<1,1>(s));
        cairo_stroke(r.c);
    }

    cairo_set_source_rgb(r.c, 1, 0, 0);
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

    //move_to_zero(ts, input);

    Render r = build_render(c, input);
    render_input(r, input);

    cairo_destroy(c);

    cairo_surface_write_to_png(dst, "out.png");
}