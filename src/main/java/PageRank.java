import java.util.ArrayList;
import java.util.List;

public class PageRank {

	private class Entity {

		final int id;
		final List<Integer> linkIDs = new ArrayList<>();

		Entity(int id) {
			this.id = id;
		}

	}

	private final double alpha;
	private final List<Entity> entities = new ArrayList<>();

	public PageRank() {
		this.alpha = 0.85;
	}

	public PageRank(double alpha) {
		if (alpha < 0 || alpha > 1) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument 'alpha': number inside the intervel [0.0, 1.0] expected, " +
							"%.2f provided.", alpha));
		}
		this.alpha = alpha;
	}

	public void addRef(int fromID, int toID) {
	}

	public void removeRef(int fromID, int toID) {
	}

	public boolean hasRef(int fromID, int toID) {
		return false;
	}

	public int createEntity() {
		return 0;
	}

	public void destroyEntity(int id) {
	}

	public void updateRankValue() {
	}

	public double getRankValue(int id) {
		return 0.0;
	}

	public boolean isValid(int id) {
		return true;
	}

	public boolean isUpToDate(int id) {
		return true;
	}

}
