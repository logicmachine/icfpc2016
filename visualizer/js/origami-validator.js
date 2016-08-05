var SolutionValidator = function(){

	// All the source positions of the vertices are within the initial square
	// spanned by the four vertices (0,0), (1,0), (1,1), (0,1).
	this.validateSourcePositions = function(solution){
		var zero = new Rational(), one = new Rational(1);
		var pass = true;
		solution.vertices.forEach(function(v){
			if(v[0].x.compare(zero) < 0){ pass = false; }
			if(v[0].x.compare(one)  > 0){ pass = false; }
			if(v[0].y.compare(zero) < 0){ pass = false; }
			if(v[0].y.compare(one)  > 0){ pass = false; }
		});
		return pass;
	};

	// No coordinate appears more than once in the source positions part.
	this.validateSourcePositionUniqueness = function(solution){
		var n = solution.vertices.length;
		for(var i = 0; i < n; ++i){
			for(var j = i + 1; j < n; ++j){
				if(solution.vertices[i][0].equals(solution.vertices[j][0])){
					return false;
				}
			}
		}
		return true;
	};

	// Any edge of any facet has length greater than zero.
	this.validateEdgeLength = function(solution){
		var pass = true;
		solution.sourcePolygons().forEach(function(polygon){
			polygon.edges().forEach(function(s){
				if(s.to.sub(s.from).norm().equals(new Rational())){ pass = false; }
			});
		});
		return pass;
	};

	// At source positions, if two different edges share a point, the point
	// should always be one of the endpoints for both the edges. That is, an 
	// edge touching another edge, or edges crossing each other are prohibited.
	this.validateCrossingEdges = function(solution){
		var edges = [];
		solution.sourcePolygons().forEach(function(polygon){
			polygon.edges().forEach(function(s){ edges.push(s.clone()); });
		});
		var pass = true;
		for(var i = 0; pass && i < edges.length; ++i){
			var ei = edges[i];
			for(var j = i + 1; pass && j < edges.length; ++j){
				var ej = edges[j];
				if(ei.from.equals(ej.from)){ continue; }
				if(ei.from.equals(ej.to)){ continue; }
				if(ei.to.equals(ej.from)){ continue; }
				if(ei.to.equals(ej.to)){ continue; }
				if(ei.intersect(ej)){ pass = false; }
			}
		}
		return pass;
	};

	// All facet polygons are simple; a facet polygon’s perimeter must not
	// intersect itself.
	this.validateFacetSimplicity = function(solution){
		var pass = true;
		solution.sourcePolygons().forEach(function(polygon){
			if(!polygon.isSimple()){ pass = false; }
		});
		return pass;
	};

	// Every facet at source position maps to its destination position, by a
	// congruent transformation that maps its source vertices to corresponding
	// destination vertices.
	this.validateConguentness = function(solution){
		var dot   = function(a, b, c){ return b.sub(a).dot(c.sub(a)); }
		var cross = function(a, b, c){ return b.sub(a).cross(c.sub(a)); }
		var srcPolygons = solution.sourcePolygons();
		var dstPolygons = solution.destinationPolygons();
		var n = srcPolygons.length;
		var pass = true;
		for(var p = 0; pass && p < n; ++p){
			var src = srcPolygons[p].vertices;
			var dst = dstPolygons[p].vertices;
			var m = src.length, localPass = true;
			for(var i = 0; localPass && i < m; ++i){
				var j = (i + 1) % m, k = (i + 2) % m;
				var sdot = dot(src[i], src[j], src[k]);
				var ddot = dot(dst[i], dst[j], dst[k]);
				if(!sdot.equals(ddot)){ pass = false; }
				var scross = cross(src[i], src[j], src[k]);
				var dcross = cross(dst[i], dst[j], dst[k]);
				if(!scross.equals(dcross)){ localPass = false; }
			}
			if(localPass){ continue; }
			localPass = true;
			for(var i = 0; localPass && i < m; ++i){
				var j = (i + 1) % m, k = (i + 2) % m;
				var scross = cross(src[i], src[j], src[k]);
				var dcross = cross(dst[i], dst[j], dst[k]);
				if(!scross.equals(dcross.negate())){ localPass = false; }
			}
			if(!localPass){ pass = false; }
		}
		return pass;
	};

	// At source position, the intersection set of any two different facets 
	// has zero area.
	this.validateFacetIntersectionOnSource = function(solution){
		// Each facets will be convex
		var polygons = solution.sourcePolygons(), n = polygons.length;
		for(var i = 0; i < n; ++i){
			for(var ii = 0; ii < polygons[i].length; ++ii){
				var vi = polygons[i][ii];
				for(var j = 0; j < n; ++j){
					if(j == i){ continue; }
					var dir = polygons[j][jj].isClockWise() ? 1 : -1;
					var all = true;
					polygons[j].edges().forEach(function(e){
						if(e.ccw(vi) != dir){ all = false; }
					});
					if(all){ return false; }
				}
			}
		}
		return true;
	};

	// At source position, the union set of all facets exactly matches the
	// initial square.
	this.validateTotalArea = function(solution){
		var sum = new Rational();
		solution.sourcePolygons().forEach(function(polygon){
			sum = sum.add(polygon.area().abs());
		});
		return sum.equals(new Rational(1));
	};

	// Test all conditions
	this.validate = function(solution){
		var messages = [];
		if(!this.validateSourcePositions(solution)){
			messages.push(
				"All the source positions of the vertices are within the " +
				"initial square spanned by the four vertices " +
				"(0,0), (1,0), (1,1), (0,1).");
		}
		if(!this.validateSourcePositionUniqueness(solution)){
			messages.push(
				"No coordinate appears more than once in the source " +
				"positions part.");
		}
		if(!this.validateEdgeLength(solution)){
			messages.push(
				"Any edge of any facet has length greater than zero.");
		}
		if(!this.validateCrossingEdges(solution)){
			messages.push(
				"At source positions, if two different edges share a point, " +
				"the point should always be one of the endpoints for both " +
				"the edges. That is, an edge touching another edge, or " +
				"edges crossing each other are prohibited.");
		}
		if(!this.validateFacetSimplicity(solution)){
			messages.push(
				"All facet polygons are simple; a facet polygon’s perimeter " +
				"must not intersect itself.");
		}
		if(!this.validateConguentness(solution)){
			messages.push(
				"Every facet at source position maps to its destination " +
				"position, by a congruent transformation that maps its " +
				"source vertices to corresponding destination vertices.");
		}
		if(!this.validateFacetIntersectionOnSource(solution)){
			messages.push(
				"At source position, the intersection set of any two " +
				"different facets has zero area.");
		}
		if(!this.validateTotalArea(solution)){
			messages.push(
				"At source position, the union set of all facets exactly " +
				"matches the initial square.");
		}
		return messages;
	};
};

