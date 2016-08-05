var Rational = function(nume, deno){
	var gcd = function(a, b){
		if(b.isZero()){ return a; }
		return gcd(b, a.remainder(b));
	};

	if(nume === undefined){ nume = BigInteger.ZERO; }
	if(typeof nume === 'number'){ nume = new BigInteger(nume); }
	if(typeof nume === 'string'){ nume = new BigInteger(nume); }
	if(deno === undefined){ deno = BigInteger.ONE; }
	if(typeof deno === 'number'){ deno = new BigInteger(deno); }
	if(typeof deno === 'string'){ deno = new BigInteger(deno); }
	if(deno.sign() < 0){
		nume = nume.negate();
		deno = deno.negate();
	}
	var g = gcd(nume.abs(), deno.abs());
	this.nume = nume.divide(g);
	this.deno = deno.divide(g);

	this.clone = function(){
		return new Rational(this.nume, this.deno);
	};

	this.toNumber = function(){
		return this.nume.toJSValue() / this.deno.toJSValue();
	};
	this.toString = function(){
		if(this.deno == 1){ return this.nume.toString(); }
		return this.nume.toString() + "/" + this.deno.toString();
	};

	this.add = function(r){
		var g = gcd(this.deno, r.deno);
		var deno = this.deno.divide(g).multiply(r.deno);
		var nume = this.nume.multiply(r.deno.divide(g)).add(r.nume.multiply(this.deno.divide(g)));
		return new Rational(nume, deno);
	};
	this.sub = function(r){
		var g = gcd(this.deno, r.deno);
		var deno = this.deno.divide(g).multiply(r.deno);
		var nume = this.nume.multiply(r.deno.divide(g)).subtract(r.nume.multiply(this.deno.divide(g)));
		return new Rational(nume, deno);
	};
	this.mul = function(r){
		return new Rational(this.nume.multiply(r.nume), this.deno.multiply(r.deno));
	};
	this.div = function(r){
		return new Rational(this.nume.multiply(r.deno), this.deno.multiply(r.nume));
	};

	this.negate = function(){
		return new Rational(this.nume.negate(), this.deno);
	};
	this.abs = function(){
		return new Rational(this.nume.abs(), this.deno);
	};

	this.equals = function(r){
		return this.nume.compare(r.nume) === 0 && this.deno.compare(r.deno) === 0;
	};

	this.compare = function(r){
		var g = gcd(this.deno, r.deno);
		var a = this.nume.multiply(r.deno.divide(g));
		var b = r.nume.multiply(this.deno.divide(g));
		return a.compare(b);
	};

	this.min = function(r){
		return this.compare(r) < 0
			? new Rational(this.nume, this.deno)
			: new Rational(r.nume, r.deno);
	};
	this.max = function(r){
		return this.compare(r) < 0
			? new Rational(r.nume, r.deno)
			: new Rational(this.nume, this.deno);
	};
};

var Vector2 = function(x, y){
	if(x === undefined){ x = new Rational(); }
	if(typeof x === 'number'){ x = new Rational(x); }
	if(y === undefined){ y = new Rational(); }
	if(typeof y === 'number'){ y = new Rational(y); }

	this.x = x.clone();
	this.y = y.clone();

	this.clone = function(){
		return new Vector2(this.x, this.y);
	};
	this.toString = function(){
		return this.x.toString() + "," + this.y.toString();
	};

	this.add = function(v){
		return new Vector2(this.x.add(v.x), this.y.add(v.y));
	};
	this.sub = function(v){
		return new Vector2(this.x.sub(v.x), this.y.sub(v.y));
	};
	this.dot = function(v){
		return this.x.mul(v.x).add(this.y.mul(v.y));
	};
	this.cross = function(v){
		return this.x.mul(v.y).sub(this.y.mul(v.x));
	};

	this.norm = function(){
		return this.x.mul(this.x).add(this.y.mul(this.y));
	};

	this.equals = function(v){
		return this.x.equals(v.x) && this.y.equals(v.y);
	};
};

var Segment = function(from, to){
	if(from === undefined){ from = new Vector2(); }
	if(to   === undefined){ to   = new Vector2(); }

	this.from = from.clone();
	this.to   = to.clone();

	this.clone = function(){
		return new Segment(this.from, this.to);
	};

	this.ccw = function(v){
		var a = this.from, b = this.to, c = v;
		var d = b.sub(a), e = c.sub(a);
		var cross = d.cross(e).compare(new Rational());
		if(cross > 0){ return  1; }
		if(cross < 0){ return -1; }
		if(d.dot(e).compare(new Rational()) < 0){ return 2; }
		if(d.norm() < e.norm()){ return -2; }
		return 0;
	};

	this.intersect = function(s){
		if(this.ccw(s.from) * this.ccw(s.to) > 0){ return false; }
		if(s.ccw(this.from) * s.ccw(this.to) > 0){ return false; }
		return true;
	}
};

var Polygon = function(vertices){
	if(vertices === undefined){ verices = []; }
	this.vertices = vertices.concat();

	this.clone = function(){
		return new Polygon(this.vertices);
	};

	this.edges = function(){
		var n = this.vertices.length;
		var result = [];
		for(var i = 0; i < n; ++i){
			result.push(new Segment(this.vertices[i], this.vertices[(i + 1) % n]));
		}
		return result;
	};

	this.area = function(){
		var s = new Rational(), n = this.vertices.length;
		for(var i = 0; i < n; ++i){
			s = s.add(this.vertices[i].cross(this.vertices[(i + 1) % n]));
		}
		return s.div(new Rational(2));
	};

	this.isClockWise = function(){
		return this.area().compare(new Rational()) >= 0;
	};

	this.isSimple = function(){
		var edges = this.edges(), n = edges.length;
		for(var i = 0; i < n; ++i){
			var prev = (i + n - 1) % n, next = (i + 1) % n;
			for(var j = 0; j < n; ++j){
				if(j == prev || j == i || j == next){ continue; }
				if(edges[i].intersect(edges[j])){ return false; }
			}
		}
		return true;
	};
};

var Problem = function(silhouette, skelton){
	if(silhouette === undefined){ silhouette = []; }
	if(skelton    === undefined){ skelton    = []; }

	var self = this;
	this.silhouette = [];
	this.skelton = [];
	silhouette.forEach(function(p){ self.silhouette.push(p.clone()); });
	skelton.forEach(function(s){ self.skelton.push(s.clone()); });

	this.clone = function(){
		return new Problem(this.silhouette, this.skelton);
	};
};

var Solution = function(vertices, polygons){
	if(vertices === undefined){ vertices = []; }
	if(polygons === undefined){ polygons = []; }

	var self = this;
	this.vertices = [];
	this.polygons = [];
	vertices.forEach(function(p){
		self.vertices.push([p[0].clone(), p[1].clone()]);
	});
	polygons.forEach(function(p){ self.polygons.push(p.concat()); });

	this.clone = function(){
		return new Solution(this.vertices, this.polygons);
	};

	this.sourcePolygons = function(){
		var self = this, polygons = [];
		this.polygons.forEach(function(indices){
			var polygon = [];
			indices.forEach(function(index){
				polygon.push(self.vertices[index][0].clone());
			});
			polygons.push(new Polygon(polygon));
		});
		return polygons;
	};
	this.destinationPolygons = function(){
		var self = this, polygons = [];
		this.polygons.forEach(function(indices){
			var polygon = [];
			indices.forEach(function(index){
				polygon.push(self.vertices[index][1].clone());
			});
			polygons.push(new Polygon(polygon));
		});
		return polygons;
	};
};
