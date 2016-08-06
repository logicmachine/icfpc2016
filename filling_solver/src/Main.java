import java.util.Arrays;

public class Main {
	public static void main(String[] args) {
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
		solver.solve();
	}
}
