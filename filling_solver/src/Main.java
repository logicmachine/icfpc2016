import java.util.Arrays;

public class Main {
	static int timeOutMs = 0;

	public static void main(String[] args) {
		parseOptions(args);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> System.err.println("Run Shutdown Hook.")));
		PartsDecomposer decomposer = new PartsDecomposer();
		decomposer.readInput(System.in);
		decomposer.edgesArrangement();
		decomposer.decompositeToParts();
		System.err.println(decomposer.points);
		System.err.println(Arrays.toString(decomposer.polygons));
		System.err.println(Arrays.toString(decomposer.edges));
		System.err.println(decomposer.parts);
		FillingSolver solver = new FillingSolver(decomposer);
		solver.solve(timeOutMs);
	}

	static void parseOptions(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("-t") || args[i].equals("--timelimit")) {
				timeOutMs = Integer.parseInt(args[i + 1]);
				++i;
			}
		}
	}

}
