import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRank {

	private class Entity {

		final int id;
		final List<Integer> linkIDs = new ArrayList<>();

		double rankValue = 1.0;

		Entity(int id) {
			this.id = id;
		}

		int getID() {
			return id;
		}

	}

	private final double alpha;
	private final List<Entity> entities = new ArrayList<>();
	private final Map<Integer, Integer> indexMap = new HashMap<>();

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

	private Entity getEntityWithID(int id) {
		Integer entityIndex = indexMap.get(id);
		return entityIndex == null? null: entities.get(entityIndex);
	}

	private boolean isValidID(int id) {
		Entity entityWithID = getEntityWithID(id);
		return entityWithID != null;
	}

	private void validateId(int id, String argName) {
		if (!isValidID(id)) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument '%s': invalid ID %d.", argName, id));
		}
	}

	public void addRef(int fromID, int toID) {
		validateId(fromID, "fromID");
		validateId(toID, "toID");
	}

	public void removeRef(int fromID, int toID) {
		validateId(fromID, "fromID");
		validateId(toID, "toID");
	}

	public boolean hasRef(int fromID, int toID) {
		validateId(fromID, "fromID");
		validateId(toID, "toID");
		return false;
	}

	public int createEntity() {
		return 0;
	}

	public void destroyEntity(int id) {
		validateId(id, "id");
	}

	public void updateRankValue() {
	}

	public double getRankValue(int id) {
		validateId(id, "id");
		return 0.0;
	}

	public boolean isRankValueValid(int id) {
		return true;
	}

	public boolean isRankValueUpToDate(int id) {
		return true;
	}

}
