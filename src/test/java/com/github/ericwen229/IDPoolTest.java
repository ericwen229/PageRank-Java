package com.github.ericwen229;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IDPoolTest {

	@Test
	public void testBorrowFromPool() {
		IDPool pool = new IDPool();
		for (int i = 0; i < 1000; ++i) {
			int newID = pool.borrowID();
			assertEquals(newID, i);
		}
	}

	@Test
	public void testReturnToPool() {
		IDPool pool = new IDPool();
		for (int i = 0; i < 1000; ++ i) {
			int newId = pool.borrowID();
			pool.returnID(newId);
		}
		for (int i = 0; i < 1000; ++ i) {
			pool.borrowID();
		}
		for (int i = 0; i < 1000; ++ i) {
			pool.returnID(i);
		}
		for (int i = 0; i < 1000; ++i) {
			int newID = pool.borrowID();
			assertEquals(newID, i);
		}
	}

	@Test
	public void testIntervalMerging() {
		IDPool pool = new IDPool();
		List<Integer> newIDList = new ArrayList<>();
		for (int i = 0; i < 1000; ++ i) {
			int newID = pool.borrowID();
			newIDList.add(newID);
		}
		Random rnd = new Random(233);
		Collections.shuffle(newIDList, rnd);
		for (Integer id: newIDList) {
			pool.returnID(id);
		}
		assertEquals(pool.intervalCount(), 1);
		for (int i = 0; i < 1000; ++i) {
			int newID = pool.borrowID();
			assertEquals(newID, i);
		}
	}

}