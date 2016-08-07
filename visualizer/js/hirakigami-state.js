var UnfoldState = function(){
	this.segments = [];
	this.pointMap = new Map();

	this.clone = function(){
		var state = new UnfoldState();
		this.segments.forEach(function(s){
			var t = s.clone(); t.mark = s.mark;
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

	this.selectPolygon = function(vertices, allFlip){
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
				var t = s.clone(); t.mark = true;
				next.segments.push(t);
			}else if(polygon.contains(s.from) < 0 || polygon.contains(s.to) < 0){
				var t = s.clone(); t.mark = s.mark;
				next.segments.push(t);
			}else{
				var t = s.clone(); t.mark = s.mark;
				if(!allFlip){ next.segments.push(t); }
				var u = new Segment(mirror.reflect(s.from), mirror.reflect(s.to));
				u.mark = s.mark;
				next.segments.push(u);
				var a = next.pointMap.get(u.from.toString());
				var b = next.pointMap.get(u.to.toString());
				var c = next.pointMap.get(s.from.toString());
				var d = next.pointMap.get(s.to.toString());
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
		var fromSet = [], toSet = [];
		current.segments.forEach(function(s){
			var from_contains = segment.contains(s.from);
			var to_contains   = segment.contains(s.to);
			if(from_contains && to_contains){
				return;
			}else if(from_contains){
				var key = s.from.toString();
				var c = intersectMap.get(key);
				var p = s.to.clone(); p.mark = s.mark;
				if(c === undefined){
					intersectMap.set(key, [p]);
				}else{
					c.push(p);
				}
				fromSet.push(s);
			}else if(to_contains){
				var key = s.to.toString();
				var c = intersectMap.get(key);
				var p = s.from.clone(); p.mark = s.mark;
				if(c === undefined){
					intersectMap.set(key, [p]);
				}else{
					c.push(p);
				}
				toSet.push(s);
			}else{
				var t = s.clone(); t.mark = s.mark;
				next.segments.push(t);
			}
		});
		var doneSet = new Set();
		fromSet.forEach(function(s){
			var k = s.from.toString(), c = intersectMap.get(k);
			if(c.length == 2 && (new Segment(c[0], c[1])).ccw(s.from) == 0){
				if(!doneSet.has(k)){
					var t = new Segment(c[0], c[1]);
					t.mark = c[0].mark & c[1].mark;
					next.segments.push(t);
					doneSet.add(k);
				}
			}else{
				var t = s.clone(); t.mark = s.mark;
				next.segments.push(t);
			}
		});
		toSet.forEach(function(s){
			var k = s.to.toString(), c = intersectMap.get(k);
			if(c.length == 2 && (new Segment(c[0], c[1])).ccw(s.to) == 0){
				if(!doneSet.has(k)){
					var t = new Segment(c[0], c[1]);
					t.mark = c[0].mark & c[1].mark;
					next.segments.push(t);
					doneSet.add(k);
				}
			}else{
				var t = s.clone(); t.mark = s.mark;
				next.segments.push(t);
			}
		});
		this.history.push(next);
	};

	this.buildSolution = function(doCleanup){
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
			if(doCleanup && !s.mark){ return; }
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
			if(doCleanup && !s.mark){ return; }
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
			if(doCleanup && !s.mark){ return; }
			if(doneSet.has(s.toString())){ return; }
			doneSet.add(s.toString());
			var last = s.clone(), poly = [ indexMap.get(s.to.toString()) ];
			while(!last.to.equals(s.from)){
				var next = last.to.add(last.to.sub(last.from)), modified = false;
				state.segments.forEach(function(t){
					if(doCleanup && !t.mark){ return; }
					var select = function(a, b){
						var lccw = last.ccw(b);
						if(lccw == -1 || lccw == 2 || lccw == 0){ return a; }
						var ccw = (new Segment(last.to, a)).ccw(b);
						if(ccw == -1 || ccw == 2 || ccw == 0){ return a; }
						modified = true;
						return b;
					};
					if(last.to.equals(t.from)){ next = select(next, t.to); }
					if(last.to.equals(t.to)){ next = select(next, t.from); }
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
			if(poly.length >= 3){
				solutionPolygons.push(poly);
			}
		};
		state.segments.forEach(function(s){
			if(doCleanup && !s.mark){ return; }
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

