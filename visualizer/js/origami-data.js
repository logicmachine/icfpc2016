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

	this.equals = function(r){
		return this.nume.compare(r.nume) === 0 && this.deno.compare(r.deno) === 0;
	};

	this.lessThan = function(r){
		var g = gcd(this.deno, r.deno);
		var a = this.nume.multiply(r.deno.divide(g));
		var b = r.nume.multiply(this.deno.divide(g));
		return a.compare(b) < 0;
	};

	this.min = function(r){
		return this.lessThan(r)
			? new Rational(this.nume, this.deno)
			: new Rational(r.nume, r.deno);
	};
	this.max = function(r){
		return this.lessThan(r)
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

	this.equals = function(v){
		return this.x === v.x && this.y === v.y;
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
		return !this.area().lessThan(new Rational());
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
};

var Solution = function(vertices, polygons){
	if(vertices === undefined){ vertices = []; }
	if(polygons === undefined){ polygons = []; }

	var self = this;
	this.vertices = [];
	this.polygons = [];
	vertices.forEach(function(p){ self.vertices.push(p.concat()); });
	polygons.forEach(function(p){ self.polygons.push(p.clone()); });
};
