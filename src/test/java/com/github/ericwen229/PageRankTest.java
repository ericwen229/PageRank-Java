package com.github.ericwen229;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

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

	@Test
	public void testLinkCreateAndDestroy() {
		PageRank rank = new PageRank();
		int a = rank.createEntity();
		int b = rank.createEntity();
		int c = rank.createEntity();
		int d = rank.createEntity();
		rank.putLink(a, b, 1.0);
		rank.putLink(a, c, 2.0);
		rank.putLink(a, d, 3.0);
		rank.putLink(b, c, 1.0);
		rank.putLink(b, d, 2.0);
		rank.putLink(c, d, 1.0);
		assertEquals(rank.getLink(a, b), 1.0, 1e-6);
		assertEquals(rank.getLink(a, c), 2.0, 1e-6);
		assertEquals(rank.getLink(a, d), 3.0, 1e-6);
		assertEquals(rank.getLink(b, c), 1.0, 1e-6);
		assertEquals(rank.getLink(b, d), 2.0, 1e-6);
		assertEquals(rank.getLink(c, d), 1.0, 1e-6);
		assertNull(rank.getLink(a, a));
		assertNull(rank.getLink(b, a));
		assertNull(rank.getLink(b, b));
		assertNull(rank.getLink(c, a));
		assertNull(rank.getLink(c, b));
		assertNull(rank.getLink(c, c));
		assertNull(rank.getLink(d, a));
		assertNull(rank.getLink(d, b));
		assertNull(rank.getLink(d, c));
		assertNull(rank.getLink(d, d));
		assertEquals(rank.removeLink(a, b), 1.0, 1e-6);
		assertEquals(rank.removeLink(a, c), 2.0, 1e-6);
		assertEquals(rank.removeLink(a, d), 3.0, 1e-6);
		assertEquals(rank.removeLink(b, c), 1.0, 1e-6);
		assertEquals(rank.removeLink(b, d), 2.0, 1e-6);
		assertEquals(rank.removeLink(c, d), 1.0, 1e-6);
		assertNull(rank.getLink(a, b));
		assertNull(rank.getLink(a, c));
		assertNull(rank.getLink(a, d));
		assertNull(rank.getLink(b, c));
		assertNull(rank.getLink(b, d));
		assertNull(rank.getLink(c, d));
	}

	@Test
	public void testLinkBehaviourWhenEntityDestroyed() {
		PageRank rank = new PageRank();
		int a = rank.createEntity();
		int b = rank.createEntity();
		int c = rank.createEntity();
		int d = rank.createEntity();
		rank.putLink(a, b, 1.0);
		rank.putLink(a, c, 2.0);
		rank.putLink(a, d, 3.0);
		rank.putLink(b, c, 1.0);
		rank.putLink(b, d, 2.0);
		rank.putLink(c, d, 1.0);
		rank.destroyEntity(c);
		assertFalse(rank.isValidEntity(c));
		assertThrows(IllegalArgumentException.class,
				() -> rank.getLink(a, c));
		assertThrows(IllegalArgumentException.class,
				() -> rank.getLink(b, c));
	}

}