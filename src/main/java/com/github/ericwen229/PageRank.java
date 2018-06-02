package com.github.ericwen229;

import java.util.*;

public class PageRank {

	private class Entity {

		final int id;
		final Map<Integer, Double> weightByID = new HashMap<>();

		double rankValue = 1.0;
		boolean rankValueValid = false; // valid if PageRank's been run at least once
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

		void setRankValue(double newRankValue) {
			this.rankValue = newRankValue;
		}

		boolean isRankValueValid() {
			return rankValueValid;
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
			}
			return oldWeight; // return old value just like a map does
		}

		Double removeLink(int id) {
			Double oldWeight = weightByID.remove(id);
			if (oldWeight != null) {
				// modifications do take place
				// then update states
				totalWeight -= oldWeight;
			}
			return oldWeight; // return old value just like a map does
		}

		Double getLink(int id) {
			return weightByID.get(id);
		}

		double computePRValueByID(int id, double prevPRValue) {
			double factor = 0.0;
			if (weightByID.isEmpty()) {
				factor = 1.0 / entities.size();
			}
			else {
				Double weight = weightByID.get(id);
				if (weight == null) {
					factor = (1.0 - alpha) / entities.size();
				} else {
					factor = alpha * (weight / totalWeight) + (1.0 - alpha) / entities.size();
				}
			}
			return factor * prevPRValue;
		}
	}

	private final double alpha;
	private final double threshold;
	private final List<Entity> entities = new ArrayList<>();
	private final Map<Integer, Integer> indexByID = new HashMap<>();
	private final IDPool idPool = new IDPool();
	private boolean rankValueUpToDate = false;

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

	public Double putLink(int fromID, int toID) {
		return putLink(fromID, toID, 1.0);
	}

	public Double putLink(int fromID, int toID, double weight) {
		validateArgumentID(fromID, "fromID");
		validateArgumentID(toID, "toID");
		if (weight < 0) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument 'weight':" +
							" weight >= 0 expected, %.2f provided", weight));
		}

		rankValueUpToDate = false;

		Entity fromEntity = getEntityWithID(fromID);
		return fromEntity.putLink(toID, weight);
	}

	public Double removeLink(int fromID, int toID) {
		validateArgumentID(fromID, "fromID");
		validateArgumentID(toID, "toID");

		rankValueUpToDate = false;

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

		rankValueUpToDate = false;

		// remove all links pointing to the entity
		for (Entity entity: entities) {
			entity.removeLink(id);
		}
		// swap the entity to be deleted to the tail
		int indexOfEntityToDelete = indexByID.get(id);
		int indexOfTail = entities.size() - 1;
		if (indexOfEntityToDelete != indexOfTail) {
			int tailEntityID = entities.get(indexOfTail).getID();
			indexByID.put(tailEntityID, indexOfEntityToDelete);
			Collections.swap(entities, indexOfEntityToDelete, indexOfTail);
		}

		// remove it from the tail
		entities.remove(indexOfTail);
		indexByID.remove(id);
		idPool.returnID(id);
	}

	public void runPageRank() {
		runPageRankIterativeVersion();
	}

	private double euclideanNormSquared(double[] arr1, double[] arr2) {
		assert arr1.length == arr2.length: "Input arrays should have same size." +
				" Something is terribly wrong";
		double sum = 0.0;
		for (int i = 0; i < arr1.length; ++i) {
			double diff = arr1[i] - arr2[i];
			sum += diff * diff;
		}
		return sum;
	}

	private double computePRValueByID(int id, double[] prevPRValues) {
		double PRValue = 0.0;
		for (int i = 0; i < entities.size(); ++i) {
			Entity currentEntity = entities.get(i);
			PRValue += currentEntity.computePRValueByID(id, prevPRValues[i]);
		}
		return PRValue;
	}

	private void runPageRankIterativeVersion() {
		if (entities.size() == 0) return;

		double[] PRValues = new double[entities.size()];
		double[] prevPRValues = new double[entities.size()];
		for (int i = 0; i < entities.size(); ++i) {
			PRValues[i] = entities.get(i).getRankValue();
			prevPRValues[i] = -PRValues[i];
		}

		while (euclideanNormSquared(PRValues, prevPRValues) > threshold) {
			System.arraycopy(PRValues, 0, prevPRValues, 0, PRValues.length);
			for (int i = 0; i < entities.size(); ++i) {
				int entityID = entities.get(i).getID();
				PRValues[i] = computePRValueByID(entityID, prevPRValues);
			}
		}

		rankValueUpToDate = true;
		for (int i = 0; i < entities.size(); ++i) {
			entities.get(i).setRankValue(PRValues[i]);
			entities.get(i).setRankValueValid(true);
		}
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

	public boolean isRankValueUpToDate() {
		return rankValueUpToDate;
	}

}
