$(document).ready(function(){
	var stateManager = new StateManager();
	var hirakigamiView = new HirakigamiView();

	var loadProblem = function(file){
		if(file === null){ return; }
		console.log(file.name);
		var reader = new FileReader(file);
		reader.onload = function(e){
			var data = e.target.result;
			var problem = parseProblem(data);
			stateManager.initialize(problem);
			hirakigamiView.setStateManager(stateManager);
		};
		reader.readAsText(file);
	};

	$(document).keyup(function(e) {
		// Escape
		if(e.keyCode === 27){ hirakigamiView.cancel(); }
	});

	$("#problem-file-browse").click(function(){ $("#problem-file").click(); });
	$("#problem-file").change(function(e){
		var fileElement = $("#problem-file");
		var files = fileElement.prop("files");
		if(files.length >= 1){
			$("#problem-file-text").val(files[0].name);
			loadProblem(files[0]);
		}else{
			$("#problem-file-text").val("");
		}
	});

	$("#generate-button").click(function(){
		var solution = stateManager.buildSolution();
		console.log(solution);
		$("#solution-buffer").val(solution);
	});

	$("#control-undo-button").click(function(){
		stateManager.undo();
		hirakigamiView.repaint();
	});
});

