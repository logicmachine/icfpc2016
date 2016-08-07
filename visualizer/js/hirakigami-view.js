var HirakigamiView = function(){
	this.stateManager = null;

	var svg_padding = 20;
	var segmentColor = "#1f77b4";
	var hilightColor = "#d62728";

	this.current_polygon = [];
	this.current_segment = null;
	this.hilighted_polygon = null;

	var self = this;
	var drawState = function(state){
		var svg = d3.select("#paper-svg");
		var zoom_wrapper = d3.select("#svg-zoom");
		var container = d3.select("#svg-container");
		container.selectAll("*").remove();

		// zoomable
		var zoom = d3.zoom()
			.scaleExtent([ 1, 10 ])
			.on("zoom", function(){
				var transform = d3.event.transform;
				transform.x =
					Math.min(0, Math.max(transform.x, vp_size - vp_size * transform.k)),
				transform.y =
					Math.min(0, Math.max(transform.y, vp_size - vp_size * transform.k));
				container.attr("transform", transform.toString());
			})
		zoom_wrapper.call(zoom)
			.on("mousedown.zoom", null)
			.on("dblclick.zoom", null);

		// compute offsets
		var updateOffset = function(a, b){
			if(a === null){ return b; }
			return a.min(b);
		};
		var x_offset = null, y_offset = null;
		state.segments.forEach(function(s){
			x_offset = updateOffset(x_offset, s.from.x.min(s.to.x));
			y_offset = updateOffset(y_offset, s.from.y.min(s.to.y));
		});
		// compute scales
		var vp_size = Math.min($(svg.node()).width(), $(svg.node()).height());
		var x_scale = d3.scaleLinear()
			.domain([ 0, 1.5 ])
			.range ([ svg_padding, vp_size - svg_padding ]);
		var y_scale = d3.scaleLinear()
			.domain([ 0, 1.5 ])
			.range ([ vp_size - svg_padding, svg_padding ]);

		// draw segments
		var svg_segments = container.append("g");
		svg_segments.selectAll("path").data(state.segments).enter()
			.append("path")
			.attr("d", function(s){
				var sx = x_scale(s.from.x.sub(x_offset).toNumber());
				var sy = y_scale(s.from.y.sub(y_offset).toNumber());
				var tx = x_scale(s.to.x.sub(x_offset).toNumber());
				var ty = y_scale(s.to.y.sub(y_offset).toNumber());
				return "M " + sx + " " + sy + " L " + tx + " " + ty;
			})
			.attr("stroke", segmentColor)
			.attr("stroke-width", "1");

		// enumerate points
		var pointSet = new Map();
		state.segments.forEach(function(s){
			pointSet.set(s.from.toString(), s.from);
			pointSet.set(s.to.toString(), s.to);
		});
		var points = [];
		pointSet.forEach(function(p){ points.push(p); });

		// hilighted point/segments
		var svg_hilight = container.append("g");
		var hilighted_point = svg_hilight.append("circle")
			.attr("r", 4).attr("cx", -10).attr("cy", -10).attr("fill", hilightColor);
		self.hilighted_polygon = svg_hilight.append("path")
			.attr("stroke", hilightColor)
			.attr("stroke-width", 2)
			.attr("fill", hilightColor)
			.attr("fill-opacity", 0.2);

		// polygon builder
		var appendPoint = function(p){
			if(self.current_polygon.length > 1 && self.current_polygon[0].equals(p)){
				// commit the polygon
				self.stateManager.selectPolygon(self.current_polygon);
				drawState(self.stateManager.last());
				self.current_polygon.length = 0;
			}else if(self.current_polygon.length == 0){
				self.current_polygon.push(p);
			}else if(!self.current_polygon[self.current_polygon.length - 1].equals(p)){
				self.current_polygon.push(p);
			}
		};

		// draw voronoi regions (transparent)
		var voronoi = d3.voronoi()
			.x(function(p){ return x_scale(p.x.sub(x_offset).toNumber()); })
			.y(function(p){ return y_scale(p.y.sub(y_offset).toNumber()); })
			.extent([[0, 0], [vp_size, vp_size]]);
		var digram = voronoi(points);
		var svg_voronoi = container.append("g");
		svg_voronoi.selectAll("path").data(digram.polygons()).enter()
			.append("path")
			.attr("d", function(d){
				if(d == null){ return null; }
				return "M" + d.join("L") + "Z";
			})
			.style("fill", "none")
			.style("pointer-events", "all")
			.attr("stroke", "none")
			.attr("stroke-width", "1")
			.on("mouseover", function(d, i){
				var p = points[i];
				hilighted_point
					.attr("cx", x_scale(p.x.sub(x_offset).toNumber()))
					.attr("cy", y_scale(p.y.sub(y_offset).toNumber()))
			})
			.on("click", function(d, i){
				if(d3.event.ctrlKey && self.current_polygon.length == 0){
					if(self.current_segment !== null){
						self.stateManager.selectSegment(
							new Segment(self.current_segment, points[i]));
						drawState(self.stateManager.last());
					}
					self.current_segment = points[i];
				}else{
					self.current_segment = null;
					var p = points[i];
					appendPoint(p);
					var path =
						"M " + self.current_polygon.map(function(p){
							var x = x_scale(p.x.sub(x_offset).toNumber());
							var y = y_scale(p.y.sub(y_offset).toNumber());
							return x + " " + y;
						}).join(" L ") + " Z";
					if(self.current_polygon.length <= 1){ path = ""; };
					self.hilighted_polygon.attr("d", path);
				}
			})
			.on("dblclick", function(d, i){
				var p = points[i];
				appendPoint(p);
				appendPoint(self.current_polygon[0]);
				self.hilighted_polygon.attr("d", "");
			});
	};

	this.setStateManager = function(stateManager){
		this.stateManager = stateManager;
		drawState(stateManager.last());
	};

	this.cancel = function(){
		this.current_polygon = [];
		this.current_segment = null;
		if(this.hilighted_polygon){
			this.hilighted_polygon.attr("d", "");
		}
	};
	this.repaint = function(){
		drawState(this.stateManager.last());
	};
};

