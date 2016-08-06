var UnfoldState = function(){
	this.segments = [];
	this.pointMap = new Map();

	this.clone = function(){
		var state = new UnfoldState();
		this.segments.forEach(function(s){
			var t = s.clone();
			state.segments.push(t);
		});
		this.pointMap.forEach(function(v, k){
			state.pointMap.set(k, v);
		});
		return state;
	};
};

var StateManager = function(){
	this.history = [];

	this.initialize = function(problem){
		var segments = problem.skelton;
		var state = new UnfoldState();
		// segment arragement
		var n = segments.length;
		for(var i = 0; i < n; ++i){
			var si = segments[i];
			var cps = [
				[ new Rational(),            si.from ],
				[ si.to.sub(si.from).norm(), si.to ]
			];
			for(var j = 0; j < n; ++j){
				if(i === j){ continue; }
				var sj = segments[j];
				if(!si.intersect(sj)){ continue; }
				var cp = si.crossing_point(sj);
				cps.push([cp.sub(si.from).norm(), cp]);
			}
			cps.sort(function(a, b){ return a[0].compare(b[0]); });
			var m = cps.length;
			for(var j = 0; j + 1 < m; ++j){
				if(cps[j][0].equals(cps[j + 1][0])){ continue; }
				var s = new Segment(cps[j][1], cps[j + 1][1]);
				state.segments.push(s);
			}
		}
		state.segments.forEach(function(s){
			state.pointMap.set(s.from.toString(), s.from);
			state.pointMap.set(s.to.toString(), s.to);
		});
		this.history = [state];
	};

	this.last = function(){
		return this.history[this.history.length - 1];
	};

	this.selectPolygon = function(vertices){
		var polygon = new Polygon(vertices);
		var mirror = new Segment(vertices[vertices.length - 1], vertices[0]);
		var current = this.history[this.history.length - 1];
		var next = new UnfoldState();
		current.pointMap.forEach(function(v, k){
			next.pointMap.set(k, v);
		});
		var valid = true;
		current.segments.forEach(function(s){
			if(mirror.contains(s.from) && mirror.contains(s.to)){
				next.segments.push(s.clone());
			}else if(polygon.contains(s.from) < 0 || polygon.contains(s.to) < 0){
				next.segments.push(s.clone());
			}else{
				var t = s.clone();
				next.segments.push(t);
				var u = new Segment(mirror.reflect(s.from), b = mirror.reflect(s.to));
				next.segments.push(u);
				var a = next.pointMap.get(u.from.toString());
				var b = next.pointMap.get(u.to.toString());
				var c = next.pointMap.get(t.from.toString());
				var d = next.pointMap.get(t.to.toString());
				if(a !== undefined && !a.equals(c)){ valid = false; }
				if(b !== undefined && !b.equals(d)){ valid = false; }
				next.pointMap.set(u.from.toString(), c);
				next.pointMap.set(u.to.toString(), d);
			}
		});
		if(!valid){ console.log("maybe invalid"); }
		this.history.push(next);
	};

	this.selectSegment = function(segment){
		var current = this.history[this.history.length - 1];
		var next = new UnfoldState();
		current.pointMap.forEach(function(v, k){
			next.pointMap.set(k, v);
		});
		var intersectMap = new Map();
		current.segments.forEach(function(s){
			if(segment.contains(s.from) && segment.contains(s.to)){ return; }
			if(segment.contains(s.from)){
				var key = s.from.toString();
				var c = intersectMap.get(key);
				if(c === undefined){
					intersectMap.set(key, [s.to]);
				}else{
					c.push(s.to);
				}
			}else if(segment.contains(s.to)){
				var key = s.to.toString();
				var c = intersectMap.get(key);
				if(c === undefined){
					intersectMap.set(key, [s.from]);
				}else{
					c.push(s.from);
				}
			}
		});
		var doneSet = new Set();
		current.segments.forEach(function(s){
			if(segment.contains(s.from) && segment.contains(s.to)){ return; }
			if(segment.contains(s.from)){
				var k = s.from.toString(), c = intersectMap.get(k);
				if(c.length == 2 && (new Segment(c[0], c[1])).ccw(s.from) == 0){
					if(!doneSet.has(k)){
						next.segments.push(new Segment(c[0], c[1]));
						doneSet.add(k);
					}
				}else{
					next.segments.push(s);
				}
			}else if(segment.contains(s.to)){
				var k = s.to.toString(), c = intersectMap.get(k);
				if(c.length == 2 && (new Segment(c[0], c[1])).ccw(s.to) == 0){
					if(!doneSet.has(k)){
						next.segments.push(new Segment(c[0], c[1]));
						doneSet.add(k);
					}
				}else{
					next.segments.push(s);
				}
			}else{
				next.segments.push(s.clone());
			}
		});
		this.history.push(next);
	};

	this.buildSolution = function(){
		var selectBase = function(a, b){
			var t = a.y.compare(b.y);
			if(t == 0){ t = a.x.compare(b.x); }
			return t <= 0 ? a : b;
		};
		var selectDx = function(a, b){
			var t = -a.x.compare(b.x);
			if(t == 0){ t = a.y.compare(b.y); }
			return t <= 0 ? a : b;
		};
		var selectDy = function(a, b){
			var t = a.x.compare(b.x);
			if(t == 0){ t = -a.y.compare(b.y); }
			return t <= 0 ? a : b;
		};
		var state = this.last();
		var base = state.segments[0].from.clone(), dx = base, dy = base;
		state.segments.forEach(function(s){
			base = selectBase(base, s.from); base = selectBase(base, s.to);
			dx   = selectDx  (dx,   s.from); dx   = selectDx  (dx,   s.to);
			dy   = selectDy  (dy,   s.from); dy   = selectDy  (dy,   s.to);
		});
		dx = dx.sub(base); dy = dy.sub(base);
		var valid = true;
		if(!dx.norm().equals(new Rational(1))){
			console.log("width != 1", dx.toString());
			valid = false;
		}
		if(!dy.norm().equals(new Rational(1))){
			console.log("height != 1", dy.toString());
			valid = false;
		}
		if(!dx.dot(dy).equals(new Rational())){ console.log("not 90 deg"); valid = false; }
		if(!valid){ return ""; }

		var transform = function(p){
			return new Vector2(p.sub(base).dot(dx), p.sub(base).dot(dy));
		};
		var indexMap = new Map();
		var sourceVertices = [];
		var destinationVertices = [];
		state.segments.forEach(function(s){
			var sk = s.from.toString(), tk = s.to.toString();
			if(indexMap.get(sk) === undefined){
				var i = sourceVertices.length;
				indexMap.set(sk, i);
				sourceVertices.push(transform(s.from));
				destinationVertices.push(state.pointMap.get(sk));
			}
			if(indexMap.get(tk) === undefined){
				var i = sourceVertices.length;
				indexMap.set(tk, i);
				sourceVertices.push(transform(s.to));
				destinationVertices.push(state.pointMap.get(tk));
			}
		});

		var doneSet = new Set();
		var solutionPolygons = [];
		var extractPolygon = function(s){
			if(doneSet.has(s.toString())){ return; }
			doneSet.add(s.toString());
			var last = s.clone(), poly = [ indexMap.get(s.to.toString()) ];
			while(!last.to.equals(s.from)){
console.log(last.toString());
				var next = last.to.add(last.to.sub(last.from)), modified = false;
				state.segments.forEach(function(t){
					var select = function(a, b){
						var lccw = last.ccw(b);
						if(lccw == -1 || lccw == 2 || lccw == 0){ return a; }
						var ccw = (new Segment(last.to, a)).ccw(b);
						if(ccw == -1 || ccw == 2 || ccw == 0){ return a; }
						modified = true;
						return b;
					};
					if(last.to.equals(t.from)){ next = select(next, t.to); console.log("?", t.to.toString(), next.toString()); }
					if(last.to.equals(t.to)){ next = select(next, t.from); console.log("?", t.from.toString(), next.toString()); }
				});
				if(!modified){
					poly = [];
					break;
				}
				last.from = last.to;
				last.to = next;
				doneSet.add(last.toString());
				poly.push(indexMap.get(next.toString()));
			}
console.log(poly);
			if(poly.length >= 3){
				solutionPolygons.push(poly);
			}
		};
		state.segments.forEach(function(s){
			extractPolygon(s);
			extractPolygon(new Segment(s.to, s.from));
		});

		var out = "";
		out += sourceVertices.length + "\n";
		sourceVertices.forEach(function(v){ out += v.toString() + "\n"; });
		out += solutionPolygons.length + "\n";
		solutionPolygons.forEach(function(p){
			if(p.length <= 2){ return; }
			out += p.length;
			p.forEach(function(x){ out += " " + x; });
			out += "\n";
		});
		destinationVertices.forEach(function(v){ out += v.toString() + "\n"; });
		return out;
	};

	this.undo = function(){
		if(this.history.length > 1){ this.history.pop(); }
	};
};

