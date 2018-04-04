public class PageRank {

	public PageRank() {
	}

	public void addRef(int fromID, int toID) {}

	public void removeRef(int fromID, int toID) {}

	public boolean hasRef(int fromID, int toID) {
		return false;
	}

	public int createEntity() {
		return 0;
	}

	public void destroyEntity(int id) {}

	public void updateRankValue() {}

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
