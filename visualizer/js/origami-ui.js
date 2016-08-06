var ProblemManager = function(){
	this.default_file = null;
	this.files = { };

	var name2id = function(name){
		if(typeof name !== 'string'){ return null; }
		var matches = name.match(/\d+/);
		if(matches.length > 0){
			return parseInt(matches[0], 10);
		}else{
			return null;
		}
	};

	this.open = function(files){
		this.default_file = null;
		this.files = { };
		for(var i = 0; i < files.length; ++i){
			var file = files[i];
			this.default_file = file;
			this.files[name2id(file.name)] = file;
		}
	};
	this.load = function(name){
		var id = name2id(name);
		if(this.files[id] === undefined){ return this.default_file; }
		return this.files[id];
	};
};

$(document).ready(function(){
	var problemManager = new ProblemManager();
	var sourceRenderer = new SourceRenderer();
	var destinationRenderer = new DestinationRenderer();

	sourceRenderer.onselect = function(index){
		destinationRenderer.enableHilight(index);
	};
	sourceRenderer.onunselect = function(index){
		destinationRenderer.disableHilight(index);
	};

	var loadProblem = function(file){
		if(file === null){
			destinationRenderer.setProblem(new Problem());
			return;
		}
		var reader = new FileReader(file);
		reader.onload = function(e){
			var data = e.target.result;
			var problem = parseProblem(data);
			destinationRenderer.setProblem(problem);
		};
		reader.readAsText(file);
	};
	$("#problem-file-browse").click(function(){ $("#problem-file").click(); });
	$("#problem-file").change(function(e){
		var file_element = $("#problem-file");
		var files = file_element.prop("files");
		if(files.length > 1){
			$("#problem-file-text").val("(multiple files)");
		}else if(files.length == 1){
			$("#problem-file-text").val(files[0].name);
		}else{
			$("#problem-file-text").val("");
		}
		problemManager.open(files);
		loadProblem(problemManager.load());
	});

	var loadSolution = function(){
		var file_element = $("#solution-file");
		var files = file_element.prop("files");
		$("#solution-file-text").val(file_element.val());
		if(files.length == 0){ return; }
		var reader = new FileReader(files[0]);
		reader.onload = function(e){
			var data = e.target.result;
			var solution = parseSolution(data);
			sourceRenderer.setSolution(solution);
			destinationRenderer.setSolution(solution);
			loadProblem(problemManager.load(files[0].name));

			var validator = new SolutionValidator();
			var messages = validator.validate(solution);
			$("#validation-messages").children("div").remove();
			messages.forEach(function(message){
				$("#validation-messages").append(
					"<div class=\"alert alert-danger\" role=\"alert\">" + message + "</div>");
			});
		};
		reader.readAsText(files[0]);
	};
	$("#solution-file-browse").click(function(){ $("#solution-file").click(); });
	$("#solution-file").change(function(){ loadSolution(); });
	$("#solution-file-reload").click(function(){ loadSolution(); });

	$(window).on("resize", function(){
		sourceRenderer.redraw();
		destinationRenderer.redraw();
	});
});

