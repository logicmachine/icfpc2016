import java.util.Arrays;

public class Main {
	public static void main(String[] args) {
		PartsDecomposer decomposer = new PartsDecomposer();
		decomposer.readInput();
		decomposer.edgesArrangement();
		decomposer.decompositeToParts();
		System.out.println(decomposer.points);
		System.out.println(Arrays.toString(decomposer.polygons));
		System.out.println(Arrays.toString(decomposer.edges));
		System.out.println(decomposer.parts);
		FillingSolver solver = new FillingSolver(decomposer);
		solver.solve();
	}
}
