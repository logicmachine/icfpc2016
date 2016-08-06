import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProblemSummary {
	public static void main(String[] args) throws Exception {
		ArrayList<String> ids = new ArrayList<>();
		ArrayList<Integer> problemSizes = new ArrayList<>();
		ArrayList<Integer> solutionSizes = new ArrayList<>();
		Path dir = Paths.get("../problems/");
		Pattern p = Pattern.compile("../problems/problem_(\\d+)_(\\d+)_(\\d+)\\.txt");
		Files.list(dir).forEach((Path path) -> {
			String filename = path.toString();
			Matcher matcher = p.matcher(filename);
			if (!matcher.matches()) return;
			System.out.println(filename);
			ids.add(matcher.group(1));
			problemSizes.add(Integer.parseInt(matcher.group(2)));
			solutionSizes.add(Integer.parseInt(matcher.group(3)));
			String imgFilename = String.format("../problems/img/%s.png", matcher.group(1));
			if (Files.exists(Paths.get(imgFilename))) return;
			PartsDecomposer decomposer = new PartsDecomposer();
			try {
				decomposer.readInput(new FileInputStream(filename));
			} catch (FileNotFoundException e) {
				System.err.println(e);
				return;
			}
			decomposer.edgesArrangement();
			decomposer.decompositeToParts();
			decomposer.outputImages(imgFilename);
		});

		try (PrintWriter writer = new PrintWriter(new FileOutputStream("../problems/summary.html"))) {
			writer.println("<!DOCTYPE html>");
			writer.println("<html>");
			writer.println("<head>");
			writer.println("<title>ICFPC2016 problems summary</title>");
			writer.println(
					"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\" />");
			writer.println("</head>");
			writer.println("<body><div class=\"container\">");
			writer.println("<div class=\"page-header\"><h1>ICFPC2016 problems</h1></div>");
			writer.println("<table class=\"table table-bordered\">");
			writer.println("<thead><tr><th>id</th><th>problem size</th><th>solution size</th><th>image</th></tr></thead>");
			writer.println("<tbody>");
			for (int i = 0; i < ids.size(); ++i) {
				String id = ids.get(i);
				writer.println("<tr>");
				writer.println(String.format("<td><a href=\"http://2016sv.icfpcontest.org/problem/view/%d\">%s</td>", Integer.parseInt(id), id));
				writer.println(String.format("<td>%d</td>", problemSizes.get(i)));
				writer.println(String.format("<td>%d</td>", solutionSizes.get(i)));
				writer.println(String.format("<td><img src=\"img/%s.png\" height=400px /></td>", id));
				writer.println("</tr>");
			}
			writer.println("</tbody>");
			writer.println("</div></body>");
			writer.println("</html>");
		}

	}
}
