package com.github.ericwen229;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

class IDPool {

	private class Interval {

		int start;
		Integer end;

		Interval(int start, Integer end) {
			this.start = start;
			this.end = end;
		}

		int getStart() {
			return start;
		}

		Integer getEnd() {
			return end;
		}

		void setStart(int newStart) {
			start = newStart;
		}

		void incrementStartBy(int step) {
			start += step;
		}

		boolean include(int id) {
			return (id >= start) && (end == null || id <= end);
		}

	}

	private final List<Interval> intervals;

	IDPool() {
		intervals = new LinkedList<>();
		intervals.add(createInterval(0, null));
	}

	private Interval createInterval(int start, Integer end) {
		return new Interval(start, end);
	}

	int borrowID() {
		// TODO: overflow may need handling in the future
		Interval firstInterval = intervals.get(0);
		assert firstInterval != null: "There's supposed to be at lease one interval." +
				" Something is terribly wrong.";
		int intervalStart = firstInterval.getStart();
		Integer intervalEnd = firstInterval.getEnd();
		int IDToBeBorrowed = intervalStart;
		if (intervalEnd == null || intervalStart < intervalEnd) {
			firstInterval.incrementStartBy(1);
		}
		else {
			intervals.remove(0);
		}
		assert !intervals.isEmpty(): "There's supposed to be at lease one interval." +
				" Something is terribly wrong.";
		return IDToBeBorrowed;
	}

	void returnID(int id) {
		if (id < 0) {
			throw new IllegalArgumentException(String.format("Illegal argument 'id': " +
					"id >= 0 expected, %d provided.", id));
		}
		ListIterator<Interval> intervalsIterator = intervals.listIterator();
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
						// then merge the two intervals
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

	int intervalCount() {
		// this method is only used for testing
		return intervals.size();
	}

}
