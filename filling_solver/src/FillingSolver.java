import java.util.ArrayList;

public class FillingSolver {

	PartsDecomposer decomposer;

	FillingSolver(PartsDecomposer decomposer) {
		this.decomposer = decomposer;
	}

	void solve() {
		ArrayList<Part> parts = decomposer.parts;
		Part largest = parts.get(0);
		for (int i = 1; i < parts.size(); ++i) {
			if (parts.get(i).area.compareTo(largest.area) > 0) {
				largest = parts.get(i);
			}
		}
		System.err.println("largest part:" + largest);
		// TODO
	}

}
