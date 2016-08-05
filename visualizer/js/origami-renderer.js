var drawProblem = function(svg_selector, problem){
	var x_offset = problem.skelton[0].from.x;
	var y_offset = problem.skelton[0].from.y;
	var xmin = Infinity, ymin = Infinity;
	problem.silhouette.forEach(function(polygon){
		polygon.vertices.forEach(function(v){
			x_offset = x_offset.min(v.x);
			y_offset = y_offset.min(v.y);
		});
	});
	problem.skelton.forEach(function(s){
		x_offset = x_offset.min(s.from.x);
		y_offset = y_offset.min(s.from.y);
		x_offset = x_offset.min(s.to.x);
		y_offset = y_offset.min(s.to.y);
	});

	var padding = 10;
	var svg_width  = $(svg_selector).width();
	var svg_height = $(svg_selector).height();
	var svg_size   = Math.min(svg_width, svg_height);
	var x_scale = d3.scaleLinear()
		.domain([ 0, 1.5 ])
		.range ([ padding, svg_size - padding ]);
	var y_scale = d3.scaleLinear()
		.domain([ 0, 1.5 ])
		.range ([ svg_size - padding, padding ]);
	var colors = d3.scaleOrdinal(d3.schemeCategory10);

	d3.select(svg_selector).selectAll("g").remove();

	// Draw silhouette
	d3.select(svg_selector).append("g")
		.selectAll("polygon")
		.data(problem.silhouette)
		.enter()
			.append("polygon")
			.attr("points", function(polygon, index){
				return polygon.vertices.map(function(v){
					return [
						x_scale(v.x.sub(x_offset).toNumber()),
						y_scale(v.y.sub(y_offset).toNumber())
					].join(",");
				}).join(" ");
			})
			.attr("stroke", function(d, i){ return colors(i - 1); })
			.attr("stroke-width", "1")
			.attr("fill", function(p, i){
				if(!p.isClockWise()){ return "white"; }
				return colors(i - 1);
			})
			.attr("fill-opacity", function(p, i){
				if(!p.isClockWise()){ return "1"; }
				return "0.3";
			});

	// Draw skelton
	d3.select(svg_selector).append("g")
		.selectAll("path")
		.data(problem.skelton)
		.enter()
			.append("path")
			.attr("d", function(s){
				var sx = x_scale(s.from.x.sub(x_offset).toNumber());
				var sy = y_scale(s.from.y.sub(y_offset).toNumber());
				var tx = x_scale(s.to.x.sub(x_offset).toNumber());
				var ty = y_scale(s.to.y.sub(y_offset).toNumber());
				return "M " + sx + " " + sy + " L " + tx + " " + ty;
			})
			.attr("stroke", "black")
			.attr("stroke-width", "2");
};

var drawSolution = function(svg_selector, solution){
	var padding = 10;
	var svg_width  = $(svg_selector).width();
	var svg_height = $(svg_selector).height();
	var svg_size   = Math.min(svg_width, svg_height);
	var x_scale = d3.scaleLinear()
		.domain([ 0, 1.5 ])
		.range ([ padding, svg_size - padding ]);
	var y_scale = d3.scaleLinear()
		.domain([ 0, 1.5 ])
		.range ([ svg_size - padding, padding ]);
	var colors = d3.scaleOrdinal(d3.schemeCategory10);

	var svg = d3.select(svg_selector);
	svg.selectAll("polygon")
		.data(solution.polygons)
		.enter()
			.append("polygon")
			.attr("points", function(polygon, index){
				return polygon.vertices.map(function(v){
					return [
						x_scale(v.x.toNumber()),
						y_scale(v.y.toNumber())
					].join(",");
				}).join(" ");
			})
			.attr("stroke", function(d, i){ return colors(i - 1); })
			.attr("stroke-width", "1")
			.attr("fill", function(p, i){
				if(!p.isClockWise()){ return "white"; }
				return colors(i - 1);
			})
			.attr("fill-opacity", "0.3");
};

