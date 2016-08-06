import java.util.Arrays;

public class Main {
	public static void main(String[] args) {
		PartsDecomposer decomposer = new PartsDecomposer();
		decomposer.readInput();
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
