#include "pch.hpp"

#define RENDER_WIDTH 1024
#define RENDER_HEIGHT 1024

#define RENDER_OFFSET 1024

namespace bg = boost::geometry;
namespace bm = boost::multiprecision;

//#define USE_DOUBLE

#ifdef USE_DOUBLE
typedef bm::mpq_rational mpval_t;
typedef double val_t;
#define str_to_val(x) (get_double(mpval_t(x)))
#else
typedef bm::mpq_rational val_t;
typedef bm::mpq_rational mpval_t;
#define str_to_val(x) (x)
#endif
typedef bg::model::d2::point_xy<val_t> point_t;
typedef bg::model::linestring<point_t> linestr_t;
typedef bg::model::segment<point_t> seg_t;
typedef bg::model::polygon<point_t> poly_t;

struct Input {
    val_t x_min;
    val_t y_min;
    val_t x_max;
    val_t y_max;

    std::vector<poly_t> facet_list;
    std::vector<seg_t> seg_list;
};

static double
get_double(mpval_t const &v)
{
    return (double) v;
}


struct TransformStat {
    val_t move_x;
    val_t move_y;

    TransformStat()
        :move_x(0),
         move_y(0)
    {}
};


static Input
load_answer(std::istream &ins)
{
    int nv ;
    ins >> nv;
    for (int i=0; i<nv; i++) {
        /* discard source pos */
        std::string xs, ys;
        getline(ins, xs, ',');
        getline(ins, ys);
    }

    std::vector< std::vector<int> > poly_index_list;
    std::vector<point_t> dst_vert_list;

    int np;
    ins >> np;

    for (int pi=0; pi<np; pi++) {
        int nv;
        ins >> nv;
        std::vector<int> index_list;

        for (int vi=0; vi<nv; vi++) {
            int x;
            ins >> x;
            index_list.push_back(x);
        }

        poly_index_list.push_back(index_list);
    }

    for (int vi=0; vi<nv; vi++) {
        /* dst vertex */
        std::string xs, ys;
        getline(ins, xs, ',');
        getline(ins, ys);

        val_t px(xs);
        val_t py(ys);

        dst_vert_list.push_back(point_t(px,py));
    }

    Input ret;
    for (auto && il : poly_index_list) {
        poly_t poly;

        for (auto && idx : il) {
            poly.outer().push_back(dst_vert_list[idx]);
        }

        ret.facet_list.push_back(poly);
    }

    return ret;
}

static Input load_input(std::istream &ins)
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

#ifdef USE_DOUBLE
            val_t px(get_double(mpval_t(xs)));
            val_t py(get_double(mpval_t(ys)));
#else
            val_t px(xs);
            val_t py(ys);
#endif

            poly.outer().push_back(point_t(px,py));
        }

        i.facet_list.push_back(poly);
    }

    int nl;

    ins >> nl;

    for (int li=0; li<nl; li++) {
        std::string x0,y0, x1,y1;

        getline(ins, x0, ',');
        getline(ins, y0, ' ');
        getline(ins, x1, ',');
        getline(ins, y1);

        seg_t s;
        bg::set<0,0>(s, val_t(str_to_val(x0)));
        bg::set<0,1>(s, val_t(str_to_val(y0)));
        bg::set<1,0>(s, val_t(str_to_val(x1)));
        bg::set<1,1>(s, val_t(str_to_val(y1)));
        i.seg_list.push_back(s);
    }

    return std::move(i);
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

static void
calc_minmax(Input &i)
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

    //std::cout << (800/get_double(scale)) << '\n';

    i.x_min = x_min;
    i.y_min = y_min;
    i.x_max = x_max;
    i.y_max = y_max;

    //std::cerr << "c:" << x_min << "," << y_min << '\n';
    //std::cerr << "d:" << x_max << "," << y_max << '\n';
}


static void
render_result(cairo_surface_t *cs,
              Input &i)
{
    cairo_t *c = cairo_create(cs);

    cairo_translate(c, RENDER_OFFSET, RENDER_OFFSET);
    cairo_scale(c, RENDER_WIDTH, RENDER_HEIGHT);
    cairo_set_antialias(c, CAIRO_ANTIALIAS_NONE);

    /* in */
    cairo_set_source_rgb(c, 0, 1, 0);
    cairo_set_line_width(c, 2);

    for (auto &&s : i.seg_list) {
        cairo_move_to(c,
                      get_double(bg::get<0,0>(s)),
                      get_double(bg::get<0,1>(s)));
        cairo_line_to(c,
                      get_double(bg::get<1,0>(s)),
                      get_double(bg::get<1,1>(s)));
        cairo_fill(c);
    }

    /* out */
    cairo_set_source_rgb(c, 1, 1, 1);
    cairo_set_line_width(c, 4);
    for (auto &&poly : i.facet_list) {
        auto p0_list = poly.outer();

        cairo_move_to(c,
                      get_double(p0_list[0].x()),
                      get_double(p0_list[0].y()));

        int n = p0_list.size();

        for (int pi=1; pi<n; pi++) {
            cairo_line_to(c,
                          get_double(p0_list[pi].x()),
                          get_double(p0_list[pi].y()));
        }

        cairo_close_path(c);

        cairo_fill(c);
    }
    cairo_destroy(c);
}

struct CompareResult {
    long long or_count;
    long long and_count;
};

static CompareResult
compare(cairo_surface_t *s0,
        cairo_surface_t *s1)
{
    unsigned char *p0 = cairo_image_surface_get_data(s0);
    unsigned char *p1 = cairo_image_surface_get_data(s1);
    int w = cairo_image_surface_get_width(s0);
    int h = cairo_image_surface_get_height(s0);
    int stride0 = cairo_image_surface_get_stride(s0);
    int stride1 = cairo_image_surface_get_stride(s1);

    CompareResult cr;
    cr.or_count = 0;
    cr.and_count = 0;

    for (int yi=0; yi<h; yi++) {
        uint32_t *l0 = (uint32_t*)(p0 + stride0 * yi);
        uint32_t *l1 = (uint32_t*)(p1 + stride1 * yi);

        for (int xi=0; xi<w; xi++) {
            bool v0 = !!l0[xi];
            bool v1 = !!l1[xi];

            bool or_val = v0 || v1;
            bool and_val = v0 && v1;

            if (or_val) {
                cr.or_count++;
            }
            if (and_val) {
                cr.and_count++;
            }
        }
    }

    return cr;
}

int
main(int argc, char **argv)
{
    cairo_surface_t *ref = cairo_image_surface_create(CAIRO_FORMAT_RGB24,
                                                      RENDER_WIDTH + (RENDER_OFFSET*2),
                                                      RENDER_HEIGHT + (RENDER_OFFSET*2));
    cairo_surface_t *answer = cairo_image_surface_create(CAIRO_FORMAT_RGB24,
                                                         RENDER_WIDTH + (RENDER_OFFSET*2),
                                                         RENDER_HEIGHT + (RENDER_OFFSET*2));

    std::ifstream ifs_in(argv[1]);
    std::ifstream ifs_answer(argv[2]);

    Input input_data = load_input(ifs_in);
    Input answer_data = load_answer(ifs_answer);

    calc_minmax(input_data);

    answer_data.x_min = input_data.x_min;
    answer_data.y_min = input_data.y_min;
    answer_data.x_max = input_data.x_max;
    answer_data.y_max = input_data.y_max;

    TransformStat ts;
    move_to_zero(ts, input_data);
    move_to_zero(ts, answer_data);

    render_result(ref, input_data);
    render_result(answer, answer_data);

    cairo_surface_write_to_png(ref, "ref.png");
    cairo_surface_write_to_png(answer, "answer.png");

    CompareResult cr = compare(ref, answer);

    printf("%f\n",
           (double)cr.and_count/(double)cr.or_count);
}
