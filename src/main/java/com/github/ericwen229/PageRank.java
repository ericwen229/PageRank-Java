package com.github.ericwen229;

import java.util.*;

public class PageRank {

	private class Entity {

		final int id;
		final Map<Integer, Double> weightByID = new HashMap<>();

		double rankValue = 1.0;
		boolean rankValueValid = false; // valid if PageRank's been run at least once
		boolean rankValueUpToDate = false; // up to date if not any modification after last run
		double totalWeight = 0.0;

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

		void setRankValueUpToDate(boolean isUpToDate) {
			rankValueUpToDate = isUpToDate;
		}

		void setRankValueValid(boolean isValid) {
			rankValueValid = isValid;
		}

		Double putLink(int id, double weight) {
			Double oldWeight = weightByID.put(id, weight);
			if (!(new Double(weight).equals(oldWeight))) {
				// modifications do take place
				// then update states
				if (oldWeight != null) {
					totalWeight -= oldWeight;
				}
				totalWeight += weight;
				setRankValueUpToDate(false);
			}
			return oldWeight; // return old value just like a map does
		}

		Double removeLink(int id) {
			Double oldWeight = weightByID.remove(id);
			if (oldWeight != null) {
				// modifications do take place
				// then update states
				totalWeight -= oldWeight;
				setRankValueUpToDate(false);
			}
			return oldWeight; // return old value just like a map does
		}

		Double getLink(int id) {
			return weightByID.get(id);
		}
	}

	private final double alpha;
	private final double threshold;
	private final List<Entity> entities = new ArrayList<>();
	private final Map<Integer, Integer> indexByID = new HashMap<>();
	private final IDPool idPool = new IDPool();

	public PageRank() {
		this(0.85, 1e-6);
	}

	public PageRank(double alpha, double threshold) {
		if (alpha < 0 || alpha > 1) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument 'alpha': alpha >= 0 &&" +
							" alpha <= 1 expected, %.2f provided.", alpha));
		}
		if (threshold <= 0) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument 'threshold': threshold > 0" +
							" expected, %.2f provided.", threshold));
		}
		this.alpha = alpha;
		this.threshold = threshold;
	}

	private Entity getEntityWithID(int id) {
		Integer entityIndex = indexByID.get(id);
		return entityIndex == null? null: entities.get(entityIndex);
	}

	public boolean isValidEntity(int id) {
		return indexByID.get(id) != null;
	}

	private void validateArgumentID(int id, String argName) {
		if (!isValidEntity(id)) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument '%s': invalid ID %d.", argName, id));
		}
	}

	public Double putLink(int fromID, int toID, double weight) {
		validateArgumentID(fromID, "fromID");
		validateArgumentID(toID, "toID");
		if (weight < 0) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument 'weight':" +
							" weight >= 0 expected, %.2f provided", weight));
		}
		Entity fromEntity = getEntityWithID(fromID);
		return fromEntity.putLink(toID, weight);
	}

	public Double removeLink(int fromID, int toID) {
		validateArgumentID(fromID, "fromID");
		validateArgumentID(toID, "toID");
		Entity fromEntity = getEntityWithID(fromID);
		return fromEntity.removeLink(toID);
	}

	public Double getLink(int fromID, int toID) {
		validateArgumentID(fromID, "fromID");
		validateArgumentID(toID, "toID");
		Entity fromEntity = getEntityWithID(fromID);
		return fromEntity.getLink(toID);
	}

	public int createEntity() {
		int newEntityID = idPool.borrowID();
		Entity newEntity = new Entity(newEntityID);
		entities.add(newEntity);
		indexByID.put(newEntityID, entities.size() - 1);
		return newEntityID;
	}

	public void destroyEntity(int id) {
		validateArgumentID(id, "id");
		for (Entity entity: entities) {
			entity.removeLink(id);
		}
		int indexOfEntityToDelete = indexByID.get(id);
		int indexOfTail = entities.size() - 1;
		if (indexOfEntityToDelete != indexOfTail) {
			int tailEntityID = entities.get(indexOfTail).getID();
			indexByID.put(tailEntityID, indexOfEntityToDelete);
			Collections.swap(entities, indexOfEntityToDelete, indexOfTail);
		}
		entities.remove(indexOfTail);
		indexByID.remove(id);
		idPool.returnID(id);
	}

	public void updateRankValue() {
		// TODO: run PageRank here
	}

	public double getRankValue(int id) {
		validateArgumentID(id, "id");
		Entity entity = getEntityWithID(id);
		assert entity != null : "Argument validation failed. Something is terribly wrong.";
		return entity.getRankValue();
	}

	public boolean isRankValueValid(int id) {
		validateArgumentID(id, "id");
		Entity entity = getEntityWithID(id);
		assert entity != null : "Argument validation failed. Something is terribly wrong.";
		return entity.isRankValueValid();
	}

	public boolean isRankValueUpToDate(int id) {
		validateArgumentID(id, "id");
		Entity entity = getEntityWithID(id);
		assert entity != null : "Argument validation failed. Something is terribly wrong.";
		return entity.isRankValueUpToDate();
	}

}
