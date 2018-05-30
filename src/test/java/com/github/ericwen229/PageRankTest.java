package com.github.ericwen229;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PageRankTest {

	@Test
	public void testEntityCreateAndDestroy() {
		PageRank rank = new PageRank();
		List<Integer> IDs = new ArrayList<>();
		for (int i = 0; i < 1000; ++i) {
			int id = rank.createEntity();
			IDs.add(id);
		}
		for (Integer id: IDs) {
			assertTrue(rank.isValidEntity(id));
			rank.destroyEntity(id);
			assertFalse(rank.isValidEntity(id));
		}
	}

}