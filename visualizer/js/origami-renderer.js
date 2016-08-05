var SourceRenderer = function(){
	this.solution = null;

	this.onselect = function(){ };
	this.onunselect = function(){ };

	var self = this;
	var normalColor = "#2ca02c";
	var hilightColor = "#d62728";
	var svg_padding = 10;
	var drawSolution = function(){
		var svg = d3.select("#source-solution-group");
		svg.selectAll("*").remove();
		var vp_size = Math.min(
			$(svg.node()).parent().width(),
			$(svg.node()).parent().height());
		var x_scale = d3.scaleLinear()
			.domain([ 0, 1.5 ])
			.range ([ svg_padding, vp_size - svg_padding ]);
		var y_scale = d3.scaleLinear()
			.domain([ 0, 1.5 ])
			.range ([ vp_size - svg_padding, svg_padding ]);
		svg.selectAll("polygon").data(self.solution.sourcePolygons()).enter()
			.append("polygon")
			.attr("points", function(polygon){
				return polygon.map(function(v){
					return [
						x_scale(v.x.toNumber()),
						y_scale(v.y.toNumber())
					].join(",");
				}).join(" ");
			})
			.attr("stroke", normalColor)
			.attr("stroke-width", "1")
			.attr("fill", normalColor)
			.attr("fill-opacity", "0.2")
			.on("mouseover", function(polygon, i){
				d3.select(this)
					.attr("stroke", hilightColor)
					.attr("fill", hilightColor);
				if(self.onselect){ self.onselect(i); }
			})
			.on("mouseout", function(polygon, i){
				d3.select(this)
					.attr("stroke", normalColor)
					.attr("fill", normalColor);
				if(self.onunselect){ self.onunselect(i); }
			});
	};

	this.setSolution = function(solution){
		this.solution = solution.clone();
		drawSolution();
	};
};

var DestinationRenderer = function(){
	this.problem  = null;
	this.solution = null;

	var self = this;
	var problemColor = "#1f77b4";
	var solutionColor = "#2ca02c";
	var hilightColor = "#d62728";
	var svg_padding = 10;
	var computeOffset = function(){
		var updateOffset = function(a, b){
			if(a === null){ return b; }
			return a.min(b);
		};
		var x_offset = null, y_offset = null;
		if(self.problem !== null){
			self.problem.silhouette.forEach(function(polygon){
				polygon.vertices.forEach(function(v){
					x_offset = updateOffset(x_offset, v.x);
					y_offset = updateOffset(y_offset, v.y);
				});
			});
			self.problem.skelton.forEach(function(s){
				x_offset = updateOffset(x_offset, s.from.x);
				y_offset = updateOffset(y_offset, s.from.y);
				x_offset = updateOffset(x_offset, s.to.x);
				y_offset = updateOffset(y_offset, s.to.y);
			});
		}
		if(self.solution !== null){
			self.solution.vertices.forEach(function(v){
				x_offset = updateOffset(x_offset, v[1].x);
				y_offset = updateOffset(y_offset, v[1].y);
			});
		}
		return [x_offset, y_offset];
	};
	var computeScales = function(svg){
		var vp_size = Math.min(
			$(svg.node()).parent().width(),
			$(svg.node()).parent().height());
		var x_scale = d3.scaleLinear()
			.domain([ 0, 1.5 ])
			.range ([ svg_padding, vp_size - svg_padding ]);
		var y_scale = d3.scaleLinear()
			.domain([ 0, 1.5 ])
			.range ([ vp_size - svg_padding, svg_padding ]);
		return [x_scale, y_scale];
	};
	var drawProblem = function(x_offset, y_offset){
		var svg = d3.select("#destination-problem-group");
		svg.selectAll("*").remove();
		var scales = computeScales(svg), x_scale = scales[0], y_scale = scales[1];
		// Draw silhouette
		svg.selectAll("polygon").data(self.problem.silhouette).enter()
			.append("polygon")
			.attr("points", function(polygon){
				return polygon.vertices.map(function(v){
					return [
						x_scale(v.x.sub(x_offset).toNumber()),
						y_scale(v.y.sub(y_offset).toNumber())
					].join(",");
				}).join(" ");
			})
			.attr("stroke", problemColor)
			.attr("stroke-width", "1")
			.attr("fill", function(p){ return p.isClockWise() ? problemColor : "#ffffff"; })
			.attr("fill-opacity", function(p){ return p.isClockWise() ? "0.2" : "1"; });
		// Draw skelton
		svg.selectAll("path").data(self.problem.skelton).enter()
			.append("path")
			.attr("d", function(s){
				var sx = x_scale(s.from.x.sub(x_offset).toNumber());
				var sy = y_scale(s.from.y.sub(y_offset).toNumber());
				var tx = x_scale(s.to.x.sub(x_offset).toNumber());
				var ty = y_scale(s.to.y.sub(y_offset).toNumber());
				return "M " + sx + " " + sy + " L " + tx + " " + ty;
			})
			.attr("stroke", problemColor)
			.attr("stroke-width", "1");
	};
	var drawSolution = function(x_offset, y_offset){
		var svg = d3.select("#destination-solution-group");
		svg.selectAll("*").remove();
		var scales = computeScales(svg), x_scale = scales[0], y_scale = scales[1];
		// Draw polygons 
		svg.selectAll("polygon").data(self.solution.destinationPolygons()).enter()
			.append("polygon")
			.attr("points", function(polygon){
				return polygon.map(function(v){
					return [
						x_scale(v.x.sub(x_offset).toNumber()),
						y_scale(v.y.sub(y_offset).toNumber())
					].join(",");
				}).join(" ");
			})
			.attr("stroke", solutionColor)
			.attr("stroke-width", "1")
			.attr("fill", solutionColor)
			.attr("fill-opacity", "0.2");
	};

	this.setProblem = function(problem){
		this.problem = problem.clone();
		var offset = computeOffset();
		drawProblem(offset[0], offset[1]);
	};
	this.setSolution = function(solution){
		this.solution = solution.clone();
		var offset = computeOffset();
		drawSolution(offset[0], offset[1]);
	};

	this.enableHilight = function(index){
		var svg = d3.select("#destination-hilight-group");
		svg.selectAll("*").remove();
		var offset = computeOffset(), x_offset = offset[0], y_offset = offset[1];
		var scales = computeScales(svg), x_scale = scales[0], y_scale = scales[1];
		var polygons = this.solution.destinationPolygons();
		svg.selectAll("polygon").data([polygons[index]]).enter()
			.append("polygon")
			.attr("points", function(polygon){
				return polygon.map(function(v){
					return [
						x_scale(v.x.sub(x_offset).toNumber()),
						y_scale(v.y.sub(y_offset).toNumber())
					].join(",");
				}).join(" ");
			})
			.attr("stroke", hilightColor)
			.attr("stroke-width", "1")
			.attr("fill", hilightColor)
			.attr("fill-opacity", "0.5");
	};
	this.disableHilight = function(index){
		var svg = d3.select("#destination-hilight-group");
		svg.selectAll("*").remove();
	};
};

