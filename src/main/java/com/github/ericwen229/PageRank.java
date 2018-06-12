package com.github.ericwen229;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Adjacency list implementation of a weighted directed graph and
 * PageRank algorithm.
 *
 * <p>Each <tt>PageRank</tt> object is initialized as an empty weighted
 * directed graph without any nodes (referred to as entities here)
 * or arrows (referred to as links here). This class implements
 * basic operations like creating/destroying entities and links.
 *
 * <p>This class has its own mechanism of managing entities. It uses
 * an <tt>IDPool</tt> object to allocate IDs and recycle IDs that are abandoned.
 * Each time an entity is created, an ID is returned, which is the
 * only identifier of this entity and is needed when performing
 * further actions. Besides it indicates that one need to maintain
 * an extra mapping between IDs used here and the names of the entities
 * in applications.
 *
 * <p>This class provides out-of-the-box PageRank algorithm to run
 * on the graph. It uses an iterative method to compute rank
 * value of each entity. The damping factor (referred to as alpha here,
 * see <a href="https://en.wikipedia.org/wiki/PageRank">
 * https://en.wikipedia.org/wiki/PageRank</a> for details of the algorithm)
 * and the threshold used to determine convergence can be customized,
 * though their default values are already proven to be effective.
 *
 * <p>Note that this implementation is not synchronized.
 *
 * @author     ericwen229
 * @see        IDPool
 * @since      1.0
 */
public class PageRank {

	/**
	 * Implementation of a single entry of adjacency list.
	 *
	 * <p>Each <tt>Entity</tt> object represents an entity, which is identified
	 * by an ID and also identifies other entities with IDs. Its neighbours and
	 * weights of links are stored in a dictionary. It also manages the
	 * rank value of this entity.
	 *
	 * @author     ericwen229
	 * @since      1.0
	 */
	private class Entity {

		/**
		 * ID used for identification of an entity. Specified by constructor
		 * argument.
		 */
		private final int id;

		/**
		 * Mapping from neighbours' IDs to the weights of the links.
		 */
		private final Map<Integer, Double> weightByID = new HashMap<>();

		/**
		 * Rank value of current entity. Will be modified by PageRank algorithm.
		 * Invalid if initialized and never modified by PageRank algorithm.
		 */
		private double rankValue = 1.0;

		/**
		 * <tt>true</tt> if rank value is valid (if and only if PageRank
		 * algorithm has been run for at least once).
		 */
		private boolean rankValueValid = false;

		/**
		 * Sum of weights of all neighbours for ease of calculation.
		 */
		private double totalWeight = 0.0;

		/**
		 * Constructs an entity with specified ID. Uniqueness of IDs
		 * is not checked, hence needs to be guaranteed with an external
		 * ID pool.
		 *
		 * @param id ID of the entity
		 */
		Entity(int id) {
			this.id = id;
		}

		/**
		 * Returns ID of the entity.
		 *
		 * @return ID of the entity
		 */
		int getID() {
			return id;
		}

		/**
		 * Returns rank value of the entity.
		 *
		 * @return rank value of the entity
		 */
		double getRankValue() {
			return rankValue;
		}

		/**
		 * Set rank value of the entity (without argument checking).
		 *
		 * @param newRankValue new rank value of the entity
		 */
		void setRankValue(double newRankValue) {
			this.rankValue = newRankValue;
		}

		/**
		 * Returns <tt>true</tt> if current rank value is valid.
		 * A rank value is valid if and only if PageRank algorithm has
		 * been run for at least once.
		 *
		 * @return <tt>true</tt> if current rank value is valid.
		 */
		boolean isRankValueValid() {
			return rankValueValid;
		}

		/**
		 * Set rank value state to the given one. Normally used to
		 * set rank value state to valid right after PageRank has been
		 * run.
		 *
		 * @param isValid new rank value state
		 */
		void setRankValueValid(boolean isValid) {
			rankValueValid = isValid;
		}

		/**
		 * Update link towards entity identified by given ID. Update link weight
		 * if link already exists (possibly with a different weight),
		 * otherwise create a new entry with ID as key and weight as value.
		 *
		 * @param id ID of entity at tail end of the link
		 * @param weight weight of link
		 * @return old link weight, <tt>null</tt> if there wasn't a link before
		 */
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

		/**
		 * Remove link towards entity identified by given ID. Do nothing
		 * if there isn't such a link.
		 *
		 * @param id ID of entity at tail end of the link
		 * @return old link weight, <tt>null</tt> if there isn't such a link
		 */
		Double removeLink(int id) {
			Double oldWeight = weightByID.remove(id);
			if (oldWeight != null) {
				// modifications do take place
				// then update states
				totalWeight -= oldWeight;
			}
			return oldWeight; // return old value just like a map does
		}

		/**
		 * Returns weight of link whose tail end identified by given ID. Returns
		 * <tt>null</tt> if there isn't such a link.
		 *
		 * @param id ID of entity at tail end of the link
		 * @return weight of the link, <tt>null</tt> if there isn't such a link
		 */
		Double getLink(int id) {
			return weightByID.get(id);
		}

		/**
		 * Compute partial rank value from current entity to the entity
		 * identified by given ID. In each iteration, the rank value of one identify
		 * comes from the sum of such partial values from every entity. Therefore
		 * to update rank value of one entity in an iteration, invoke this method to
		 * compute partial rank values (to the entity with the ID) for every
		 * entity and add them up.
		 *
		 * @param id ID of entity whom contribution goes towards
		 * @param prevPRValue previous rank value of current entity
		 * @return rank value contribution from current entity to the entity
		 * identified by ID
		 */
		double computePartialPRValueByID(int id, double prevPRValue) {
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

	/**
	 * Damping factor of PageRank algorithm.
	 */
	private final double alpha;

	/**
	 * Threshold used to determine if PageRank has converged.
	 */
	private final double threshold;

	/**
	 * Collection of entities (i.e. adjacency list).
	 */
	private final List<Entity> entities = new ArrayList<>();

	/**
	 * Mapping from ID of entity to index in adjacency list.
	 */
	private final Map<Integer, Integer> indexByID = new HashMap<>();

	/**
	 * ID pool used to manage ID allocation and recycle.
	 */
	private final IDPool idPool = new IDPool();

	/**
	 * <tt>true</tt> if the rank value of every entity is up-to-date (if
	 * and only if PageRank have been run for at least once and no modifications
	 * to the graph are made after the last run).
	 */
	private boolean rankValueUpToDate = false;

	/**
	 * Constructs an PageRank object with specified parameters.
	 *
	 * @param alpha damping factor of PageRank algorithm
	 * @param threshold threshold used to determine convergence
	 * @throws IllegalArgumentException if (<tt>alpha</tt> &lt; 0 || <tt>alpha</tt> &gt; 1 || threshold &lt;= 0)
	 */
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

	/**
	 * Constructs an PageRank object with default parameters.
	 */
	public PageRank() {
		this(0.85, 1e-6);
	}

	/**
	 * Create a new entity using newly borrowed ID from ID pool.
	 *
	 * @return ID of newly created entity
	 * @throws RuntimeException if no available IDs left in ID pool
	 */
	public int createEntity() {
		Integer newEntityID = idPool.borrowID();
		if (newEntityID == null) {
			throw new RuntimeException("Can't create entities anymore. Maximum number reached.");
		}
		Entity newEntity = new Entity(newEntityID);
		entities.add(newEntity);
		indexByID.put(newEntityID, entities.size() - 1);
		return newEntityID;
	}

	/**
	 * Destroy an entity identified by given ID and all links towards it.
	 * Return ID to ID pool.
	 *
	 * @param id ID of entity to destroy
	 * @throws IllegalArgumentException if given ID is invalid
	 */
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

	/**
	 * Returns entity identified by ID.
	 *
	 * @param id ID specifying the entity
	 * @return entity identified by ID, <tt>null</tt> if not found
	 */
	private Entity getEntityWithID(int id) {
		Integer entityIndex = indexByID.get(id);
		return entityIndex == null? null: entities.get(entityIndex);
	}

	/**
	 * Returns <tt>true</tt> if ID is associated with an entity.
	 *
	 * @param id ID to be determined whether associated with entity
	 * @return <tt>true</tt> if ID is associated with an entity
	 */
	public boolean isValidEntity(int id) {
		return indexByID.get(id) != null;
	}

	/**
	 * Determines whether given argument ID is valid and throws an
	 * exception if not.
	 *
	 * @param id ID to be determined whether valid
	 * @param paramName name of parameter to be reported
	 * @throws IllegalArgumentException if given ID is invalid
	 */
	private void validateArgumentID(int id, String paramName) {
		if (!isValidEntity(id)) {
			throw new IllegalArgumentException(String.format(
					"Illegal argument '%s': invalid ID %d.", paramName, id));
		}
	}

	/**
	 * Update link identified by given IDs. Update link weight
	 * if link already exists (possibly with a different weight),
	 * otherwise create a new entry with ID as key and weight as value.
	 *
	 * <p>Note that this method will, if returned normally, always mark
	 * rank value as not up-to-date.
	 *
	 * @param fromID ID of entity at head end of the link
	 * @param toID ID of entity at tail end of the link
	 * @param weight weight of link
	 * @return old link weight, <tt>null</tt> if there wasn't a link before
	 * @throws IllegalArgumentException if any of given IDs is invalid
	 */
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

	/**
	 * Update link using default weight.
	 *
	 * <p>Note that this method will, if returned normally, always mark
	 * rank value as not up-to-date.
	 *
	 * @param fromID ID of entity at head end of the link
	 * @param toID ID of entity at tail end of the link
	 * @return old link weight, <tt>null</tt> if there wasn't a link before
	 * @throws IllegalArgumentException if any of given IDs is invalid
	 */
	public Double putLink(int fromID, int toID) {
		return putLink(fromID, toID, 1.0);
	}

	/**
	 * Remove link identified by given IDs. Do nothing if
	 * there wasn't a link before.
	 *
	 * <p>Note that this method will, if returned normally, always mark
	 * rank value as not up-to-date.
	 *
	 * @param fromID ID of entity at head end of the link
	 * @param toID ID of entity at tail end of the link
	 * @return old link weight, <tt>null</tt> if there wasn't a link before
	 * @throws IllegalArgumentException if any of given IDs is invalid
	 */
	public Double removeLink(int fromID, int toID) {
		validateArgumentID(fromID, "fromID");
		validateArgumentID(toID, "toID");

		rankValueUpToDate = false;

		Entity fromEntity = getEntityWithID(fromID);
		return fromEntity.removeLink(toID);
	}

	/**
	 * Get link weight identified by given IDs.
	 *
	 * @param fromID ID of entity at head end of the link
	 * @param toID ID of entity at tail end of the link
	 * @return link weight, <tt>null</tt> if there isn't such a link
	 * @throws IllegalArgumentException if any of given IDs is invalid
	 */
	public Double getLink(int fromID, int toID) {
		validateArgumentID(fromID, "fromID");
		validateArgumentID(toID, "toID");
		Entity fromEntity = getEntityWithID(fromID);
		return fromEntity.getLink(toID);
	}

	/**
	 * Run PageRank algorithm and update all rank values.
	 *
	 * <p>Note that this method can be quite time-consuming if the graph
	 * gets considerably large.
	 */
	public void runPageRank() {
		runPageRankIterativeVersion();
	}

	/**
	 * Run the iterative version of PageRank algorithm. Starting from current
	 * rank values, iterate until convergence which is determined using the
	 * constant threshold.
	 *
	 * <p>Note that this method will, if returned normally, mark rank values as
	 * up-to-date and also valid.
	 *
	 * <p>Note that this method can be quite time-consuming if the graph
	 * gets considerably large.
	 */
	private void runPageRankIterativeVersion() {
		if (entities.size() <= 1) return;

		double[] PRValues = new double[entities.size()];
		double[] prevPRValues = new double[entities.size()];
		for (int i = 0; i < entities.size(); ++i) {
			PRValues[i] = entities.get(i).getRankValue();
			prevPRValues[i] = -PRValues[i];
		}

		// compute squared euclidean norm of arr1 - arr2
		BiFunction<double[], double[], Double> squaredEuclideanNorm = (arr1, arr2) -> {
			assert arr1.length == arr2.length: "Input arrays should have same size." +
					" Something is terribly wrong";
			double sum = 0.0;
			for (int i = 0; i < arr1.length; ++i) {
				double diff = arr1[i] - arr2[i];
				sum += diff * diff;
			}
			return sum;
		};

		// compute rank value of given ID
		Function<Integer, Double> computePRValueByID = (id) -> {
			double PRValue = 0.0;
			for (int i = 0; i < entities.size(); ++i) {
				Entity currentEntity = entities.get(i);
				PRValue += currentEntity.computePartialPRValueByID(id, prevPRValues[i]);
			}
			return PRValue;
		};

		// main iteration
		while (squaredEuclideanNorm.apply(PRValues, prevPRValues) > threshold) {
			System.arraycopy(PRValues, 0, prevPRValues, 0, PRValues.length);
			for (int i = 0; i < entities.size(); ++i) {
				int entityID = entities.get(i).getID();
				PRValues[i] = computePRValueByID.apply(entityID);
			}
		}

		rankValueUpToDate = true;
		for (int i = 0; i < entities.size(); ++i) {
			entities.get(i).setRankValue(PRValues[i]);
			entities.get(i).setRankValueValid(true);
		}
	}

	/**
	 * Returns rank value of entity specified by given ID.
	 *
	 * @param id ID specifying the entity
	 * @return rank value of entity specified by given ID
	 * @throws IllegalArgumentException if given ID is invalid
	 */
	public double getRankValue(int id) {
		validateArgumentID(id, "id");
		Entity entity = getEntityWithID(id);
		assert entity != null : "Argument validation failed. Something is terribly wrong.";
		return entity.getRankValue();
	}

	/**
	 * Returns <tt>true</tt> if rank value of entity specified by given ID is valid.
	 *
	 * @param id ID specifying the entity
	 * @return rank value of entity specified by given ID
	 * @throws IllegalArgumentException if given ID is invalid
	 */
	public boolean isRankValueValid(int id) {
		validateArgumentID(id, "id");
		Entity entity = getEntityWithID(id);
		assert entity != null : "Argument validation failed. Something is terribly wrong.";
		return entity.isRankValueValid();
	}

	/**
	 * Returns <tt>true</tt> if current rank values are up-to-date.
	 *
	 * @return <tt>true</tt> if current rank values are up-to-date
	 */
	public boolean isRankValueUpToDate() {
		return rankValueUpToDate;
	}

}
