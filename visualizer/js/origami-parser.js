var parseRational = function(str){
	var tokens = str.split(/\s*\/\s*/);
	var nume = tokens.shift();
	if(tokens.length === 0){
		return new Rational(nume);
	}else{
		var deno = tokens.shift();
		return new Rational(nume, deno);
	}
};

var parseVector2 = function(str){
	var tokens = str.split(/\s*,\s*/);
	var x = parseRational(tokens.shift());
	var y = parseRational(tokens.shift());
	return new Vector2(x, y);
}

var parseProblem = function(str){
	var tokens = str.split(/\s+/);
	var n = parseInt(tokens.shift(), 10);
	var silhouette = [];
	for(var i = 0; i < n; ++i){
		var m = parseInt(tokens.shift(), 10);
		var vertices = [];
		for(var j = 0; j < m; ++j){
			vertices.push(parseVector2(tokens.shift()));
		}
		silhouette.push(new Polygon(vertices));
	}
	var m = parseInt(tokens.shift(), 10);
	var skelton = [];
	for(var i = 0; i < m; ++i){
		var from = parseVector2(tokens.shift());
		var to   = parseVector2(tokens.shift());
		skelton.push(new Segment(from, to));
	}
	return new Problem(silhouette, skelton);
};

var parseSolution = function(str){
	var tokens = str.split(/\s+/);
	var n = parseInt(tokens.shift(), 10);
	var vertices = [];
	for(var i = 0; i < n; ++i){
		vertices.push([ parseVector2(tokens.shift()) ]);
	}
	var m = parseInt(tokens.shift(), 10);
	var polygons = [];
	for(var i = 0; i < m; ++i){
		var k = parseInt(tokens.shift(), 10), poly = [];
		for(var j = 0; j < k; ++j){
			poly.push(vertices[parseInt(tokens.shift(), 10)][0]);
		}
		polygons.push(new Polygon(poly));
	}
	for(var i = 0; i < n; ++i){
		vertices[i].push(parseVector2(tokens.shift()));
	}
	return new Solution(vertices, polygons);
};
