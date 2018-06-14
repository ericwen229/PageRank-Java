package com.github.ericwen229;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of ID pool.
 *
 * <p>This class uses a series of intervals (each consists of interval
 * start and end) to represent currently available IDs. An <tt>IDPool</tt>
 * object is initialized with every ID available, hence there's only
 * one interval [<tt>startID</tt>, <tt>null</tt>) (<tt>startID</tt> can be
 * specified when construction and <tt>null</tt> represents positive infinity).
 * Afterwards when IDs get constantly borrowed and returned, intervals
 * get shrinked, created, merged and deleted.
 *
 * @author ericwen229
 * @since 1.0
 */
public class IDPool {

	/**
	 * The smallest ID that can be borrowed.
	 */
	private final int startID;

	/**
	 * Intervals of IDs that can be borrowed.
	 */
	private final List<Interval> availableIntervals;

	/**
	 * Constructs an IDPool object containing non-negative IDs in [0, INT_MAX-1].
	 */
	public IDPool() {
		this(0);
	}

	/**
	 * Constructs an IDPool object containing IDs in [start, INT_MAX-1].
	 *
	 * @param start the smallest ID that can be borrowed
	 */
	public IDPool(int start) {
		startID = start;
		availableIntervals = new LinkedList<>();
		availableIntervals.add(createInterval(startID, Integer.MAX_VALUE));
	}

	/**
	 * Creates an interval with given endpoints.
	 *
	 * @param start start endpoint of interval
	 * @param end end endpoint of interval
	 * @return interval created
	 */
	private Interval createInterval(int start, int end) {
		return new Interval(start, end);
	}

	/**
	 * Returns <tt>true</tt> if there're any available ID left.
	 *
	 * @return <tt>true</tt> if there're any available ID left
	 */
	public synchronized boolean canBorrowID() {
		// whether all non-negative IDs have been used
		// i.e. there's only one interval [INT_MAX, INT_MAX] left
		// we can't use INT_MAX, or the interval can't be represented correctly
		Interval firstInterval = availableIntervals.get(0);
		return firstInterval.getStart() < Integer.MAX_VALUE;
	}

	/**
	 * Borrows the smallest ID available in pool.
	 *
	 * @return ID borrowed, <tt>null</tt> if no available IDs
	 */
	public synchronized Integer borrowID() {
		if (!canBorrowID()) {
			return null;
		}
		Interval firstInterval = availableIntervals.get(0);
		assert firstInterval != null: "There's supposed to be at lease one interval." +
				" Something is terribly wrong.";
		int intervalStart = firstInterval.getStart();
		int intervalEnd = firstInterval.getEnd();
		int IDToBeBorrowed = intervalStart;
		if (intervalStart < intervalEnd) {
			firstInterval.incrementStartBy(1);
		}
		else {
			availableIntervals.remove(0);
		}
		assert !availableIntervals.isEmpty(): "There's supposed to be at lease one interval." +
				" Something is terribly wrong.";
		return IDToBeBorrowed;
	}

	/**
	 * Returns given ID to the pool.
	 *
	 * @param id ID to return
	 * @throws IllegalArgumentException if ID not borrowed or illegal (less than smallest legal ID)
	 */
	public synchronized void returnID(int id) {
		if (id < startID) {
			throw new IllegalArgumentException(String.format("Illegal argument 'id': " +
					"id >= %d expected, %d provided.", startID, id));
		}
		ListIterator<Interval> intervalsIterator = availableIntervals.listIterator();
		assert intervalsIterator.hasNext(): "There's supposed to be at lease one interval." +
				" Something is terribly wrong.";
		while (intervalsIterator.hasNext()) {
			Interval nextInterval = intervalsIterator.next();
			int nextIntervalStart = nextInterval.getStart();
			if (nextIntervalStart > id) {
				Interval currentInterval = null;
				// insert interval [id, id]
				// current cursor:
				//                     |
				//                     v
				// ... -> nextInterval -> ...
				// move the cursor backwards
				intervalsIterator.previous();
				// current cursor:
				//     |
				//     v
				// ... -> nextInterval -> ...
				if (nextIntervalStart == id + 1) {
					// if the new interval can be concatenated
					// to the next interval
					// then just expand the next interval
					nextInterval.incrementStartBy(-1);
					currentInterval = nextInterval;
				}
				else {
					// elsewise
					// insert new interval [id, id]
					currentInterval = createInterval(id, id);
					intervalsIterator.add(currentInterval);
					// current cursor:
					//                        |
					//                        v
					// ... -> currentInterval -> nextInterval -> ...
					// move the cursor backwards
					intervalsIterator.previous();
				}
				// current cursor:
				//     |
				//     v
				// ... -> currentInterval -> ...
				assert currentInterval.getStart() == id: "Start of current interval is" +
						" supposed to be id. Something is terribly wrong.";
				if (intervalsIterator.hasPrevious()) {
					Interval previousInterval = intervalsIterator.previous();
					int previousIntervalEnd = previousInterval.getEnd();
					assert previousIntervalEnd < id: "End of previous interval is" +
							" supposed to be less than id. Something is terribly wrong";
					// current cursor:
					//     |
					//     v
					// ... -> previousInterval -> currentInterval -> ...
					if (previousIntervalEnd == id - 1) {
						// if current interval can be concatenated
						// with the previous interval
						// then merge the two availableIntervals
						// by extending the current interval
						// and removing the previous interval
						currentInterval.setStart(previousInterval.getStart());
						intervalsIterator.remove();
					}
				}
				return;
			}
			else if (nextInterval.include(id)) {
				throw new IllegalArgumentException(String.format("Illegal argument 'id':" +
						" provided id %d is never borrowed or already returned.", id));
			}
		}
	}

	/**
	 * Returns number of intervals. Used for testing.
	 *
	 * @return number of intervals
	 */
	synchronized int intervalCount() {
		return availableIntervals.size();
	}

	/**
	 * Implementation of closed integer interval represented as [<tt>start</tt>,
	 * <tt>end</tt>].
	 */
	private class Interval {

		/**
		 * Starting endpoint of interval (included in interval).
		 */
		private int start;

		/**
		 * Ending endpoint of interval (included in interval).
		 */
		private int end;

		/**
		 * Constructs an interval with specified endpoints.
		 *
		 * @param start starting endpoint of interval
		 * @param end ending endpoint of interval
		 * @throws IllegalArgumentException if <tt>start</tt> &gt; <tt>end</tt>
		 */
		Interval(int start, int end) {
			if (start > end) {
				throw new IllegalArgumentException(String.format(
						"Illegal argument 'start' and 'end': start <= end expected," +
								"%d and %d provided.", start, end));
			}
			this.start = start;
			this.end = end;
		}

		/**
		 * Returns starting endpoint of interval.
		 *
		 * @return starting endpoint of interval
		 */
		int getStart() {
			return start;
		}

		/**
		 * Returns ending endpoint of interval.
		 *
		 * @return ending endpoint of interval
		 */
		int getEnd() {
			return end;
		}

		/**
		 * Sets starting endpoint of interval.
		 *
		 * @param newStart new starting endpoint of interval
		 * @throws IllegalArgumentException if <tt>newStart</tt> &gt;= <tt>end</tt>
		 */
		void setStart(int newStart) {
			if (newStart > end) {
				throw new IllegalArgumentException(String.format(
						"Illegal argument 'newStart': newStart <= end expected," +
								"%d provided.", newStart));
			}
			start = newStart;
		}

		/**
		 * Sets ending endpoint of interval.
		 *
		 * @param newEnd new ending endpoint of interval
		 * @throws IllegalArgumentException if <tt>start</tt> &gt; <tt>newEnd</tt>
		 */
		void setEnd(int newEnd) {
			if (start > newEnd) {
				throw new IllegalArgumentException(String.format(
						"Illegal argument 'newEnd': start <= newEnd expected," +
								"%d provided.", newEnd));
			}
			end = newEnd;
		}

		/**
		 * Increments starting endpoint by given step. One can also pass
		 * a negative argument to perform decrementing.
		 *
		 * @param step step to increment
		 */
		void incrementStartBy(int step) {
			if (start + step > end) {
				throw new IllegalArgumentException(String.format(
						"Illegal argument 'step': start + step <= end expected," +
								"%d provided.", step));
			}
			start += step;
		}

		/**
		 * Increments ending endpoint by given step. One can also pass
		 * a negative argument to perform decrementing.
		 *
		 * @param step step to increment
		 */
		void incrementEndBy(int step) {
			if (start > end + step) {
				throw new IllegalArgumentException(String.format(
						"Illegal argument 'step': start <= end + step expected," +
								"%d provided.", step));
			}
			end += step;
		}

		/**
		 * Returns <tt>true</tt> if current interval includes argument integer.
		 *
		 * @param i integer to be determined whether included in interval
		 * @return <tt>true</tt> if current interval includes argument integer.
		 */
		boolean include(int i) {
			return i >= start && i <= end;
		}

	}

}
