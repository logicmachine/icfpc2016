$(document).ready(function(){
	function register_file_handlers(file, text, button, callback){
		button.click(function(){ file.click(); });
		file.change(function(e){
			text.val($(this).val());
			if(e.target.files.length > 0){
				var reader = new FileReader(e.target.files[0]);
				reader.onload = function(e){ callback(e.target.result); };
				reader.readAsText(e.target.files[0]);
			}
		});
	}

	var sourceRenderer = new SourceRenderer();
	var destinationRenderer = new DestinationRenderer();

	sourceRenderer.onselect = function(index){
		destinationRenderer.enableHilight(index);
	};
	sourceRenderer.onunselect = function(index){
		destinationRenderer.disableHilight(index);
	};

	register_file_handlers(
		$("#problem-file"),
		$("#problem-file-text"),
		$("#problem-file-browse"),
		function(data){
			var problem = parseProblem(data);
			destinationRenderer.setProblem(problem);
		});
	register_file_handlers(
		$("#solution-file"),
		$("#solution-file-text"),
		$("#solution-file-browse"),
		function(data){
			var solution = parseSolution(data);
			sourceRenderer.setSolution(solution);
			destinationRenderer.setSolution(solution);
		});
});

