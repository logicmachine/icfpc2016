var drawProblem = function(svg_selector, problem){
	var xmin = Infinity, ymin = Infinity;
	problem.silhouette.forEach(function(polygon){
		polygon.vertices.forEach(function(v){
			xmin = Math.min(xmin, v.x.toNumber());
			ymin = Math.min(ymin, v.y.toNumber());
		});
	});
	problem.skelton.forEach(function(s){
		xmin = Math.min(xmin, Math.min(s.from.x.toNumber(), s.to.x.toNumber()));
		ymin = Math.min(ymin, Math.min(s.from.y.toNumber(), s.to.y.toNumber()));
	});

	var padding = 10;
	var svg_width  = $(svg_selector).width();
	var svg_height = $(svg_selector).height();
	var svg_size   = Math.min(svg_width, svg_height);
	var x_scale = d3.scaleLinear()
		.domain([ xmin, xmin + 1.5 ])
		.range ([ padding, svg_size - padding ]);
	var y_scale = d3.scaleLinear()
		.domain([ ymin, ymin + 1.5 ])
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
					return [x_scale(v.x.toNumber()), y_scale(v.y.toNumber())].join(",");
				}).join(" ");
			})
			.attr("stroke", colors)
			.attr("stroke-width", "1")
			.attr("fill", function(p, i){
				if(!p.isClockWise()){ return "white"; }
				return colors(i);
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
				var sx = x_scale(s.from.x.toNumber());
				var sy = y_scale(s.from.y.toNumber());
				var tx = x_scale(s.to.x.toNumber());
				var ty = y_scale(s.to.y.toNumber());
				return "M " + sx + " " + sy + " L " + tx + " " + ty;
			})
			.attr("stroke", "black")
			.attr("stroke-width", "2");
};

