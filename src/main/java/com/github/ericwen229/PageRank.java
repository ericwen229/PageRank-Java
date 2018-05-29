package com.github.ericwen229;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRank {

	private class Entity {

		final int id;
		final List<Integer> linkIDs = new ArrayList<>();
		final List<Double> linkWeights = new ArrayList<>();

		double rankValue = 1.0;
		boolean rankValueValid = false;
		boolean rankValueUpToDate = false;
		double weightSum = 0.0;

		Entity(int id) {
			this.id = id;
		}

		int getID() {
			return id;
		}

		double getRankValue() {
			return rankValue;
		}

		boolean isRankValueUpToDate() {
			return rankValueUpToDate;
		}

		boolean isRankValueValid() {
			return rankValueValid;
		}
	}

	private final double alpha;
	private final double threshold;
	private final List<Entity> entities = new ArrayList<>();
	private final Map<Integer, Integer> indexMap = new HashMap<>();

	public PageRank() {
		this(0.85, 1e-6);
	}

	public PageRank(double alpha, double threshold) {
		if (alpha < 0 || alpha > 1) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument 'alpha': alpha >= 0 && alpha <= 1 expected, " +
							"%.2f provided.", alpha));
		}
		if (threshold <= 0) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument 'threshold': threshold > 0 expected, " +
							"%.2f provided.", threshold));
		}
		this.alpha = alpha;
		this.threshold = threshold;
	}

	private Entity getEntityWithID(int id) {
		Integer entityIndex = indexMap.get(id);
		return entityIndex == null? null: entities.get(entityIndex);
	}

	private boolean isValidID(int id) {
		Entity entityWithID = getEntityWithID(id);
		return entityWithID != null;
	}

	private void validateArgumentId(int id, String argName) {
		if (!isValidID(id)) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument '%s': invalid ID %d.", argName, id));
		}
	}

	public void addLink(int fromID, int toID, double weight) {
		validateArgumentId(fromID, "fromID");
		validateArgumentId(toID, "toID");
	}

	public void removeLink(int fromID, int toID) {
		validateArgumentId(fromID, "fromID");
		validateArgumentId(toID, "toID");
	}

	public boolean hasLink(int fromID, int toID) {
		validateArgumentId(fromID, "fromID");
		validateArgumentId(toID, "toID");
		return false;
	}

	public int createEntity() {
		return 0;
	}

	public void destroyEntity(int id) {
		validateArgumentId(id, "id");
	}

	public void updateRankValue() {
		// TODO: run PageRank here
	}

	public double getRankValue(int id) {
		validateArgumentId(id, "id");
		Entity entity = getEntityWithID(id);
		assert entity != null : "Argument validation failed. Something is terribly wrong.";
		return entity.getRankValue();
	}

	public boolean isRankValueValid(int id) {
		validateArgumentId(id, "id");
		Entity entity = getEntityWithID(id);
		assert entity != null : "Argument validation failed. Something is terribly wrong.";
		return entity.isRankValueValid();
	}

	public boolean isRankValueUpToDate(int id) {
		validateArgumentId(id, "id");
		Entity entity = getEntityWithID(id);
		assert entity != null : "Argument validation failed. Something is terribly wrong.";
		return entity.isRankValueUpToDate();
	}

}
