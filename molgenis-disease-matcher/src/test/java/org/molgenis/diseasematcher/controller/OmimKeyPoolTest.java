package org.molgenis.diseasematcher.controller;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PoolUtils;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.molgenis.diseasematcher.service.PooledOmimKeyFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author tommydeboer
 * 
 */
public class OmimKeyPoolTest
{

	ObjectPool<String> pool;

	@BeforeMethod
	public void beforeClass()
	{
		List<String> keys = Arrays.asList("a", "b", "c");

		PooledOmimKeyFactory keyFactory = new PooledOmimKeyFactory(keys);

		PooledObjectFactory<String> syncFactory = PoolUtils.synchronizedPooledFactory(keyFactory);

		GenericObjectPool<String> genericPool = new GenericObjectPool<String>(syncFactory);
		genericPool.setBlockWhenExhausted(true);
		genericPool.setLifo(false);
		genericPool.setMaxTotal(keys.size());

		// solely for testing purposes (so the test doesn't hang)
		genericPool.setMaxWaitMillis(100);

		// doing this removes all configuration settings
		pool = PoolUtils.synchronizedPool(genericPool);
	}

	@AfterMethod
	public void afterMethod() throws UnsupportedOperationException, Exception
	{
		pool = null;
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFillPool() throws Exception
	{
		// borrow three objects to fill the pool
		String key1 = pool.borrowObject();
		String key2 = pool.borrowObject();
		String key3 = pool.borrowObject();

		// check if the keys came out correctly
		assertEquals(key1, "a");
		assertEquals(key2, "b");
		assertEquals(key3, "c");

	}

	@Test(expectedExceptions = NoSuchElementException.class)
	public void testBlockedPoolWhenEmpty() throws Exception
	{
		// borrow three objects to fill the pool
		String key1 = pool.borrowObject();
		String key2 = pool.borrowObject();
		String key3 = pool.borrowObject();

		// borrow one more, should time out and throw NoSuchElementException
		String key4 = pool.borrowObject();
	}

	@Test
	public void testKeyTimeOut() throws NoSuchElementException, IllegalStateException, Exception
	{
		// a key can be used 4 times per second
		// quickly borrow same key four times (within 1 second)
		String key1 = pool.borrowObject();
		pool.returnObject(key1);

		String key2 = pool.borrowObject();
		pool.returnObject(key2);

		String key3 = pool.borrowObject();
		pool.returnObject(key3);

		String key4 = pool.borrowObject();
		pool.returnObject(key4);

		// key should be unavailable now and the next key should be added (b or c, not a)
		String key5 = pool.borrowObject();
		pool.returnObject(key5);

		assertNotEquals(key5, "a");
	}

	@Test
	public void testKeyTimeOutWithWait() throws NoSuchElementException, IllegalStateException, Exception
	{
		long startTime = System.currentTimeMillis();

		String key1 = pool.borrowObject();
		pool.returnObject(key1);

		String key2 = pool.borrowObject();
		pool.returnObject(key2);

		String key3 = pool.borrowObject();
		pool.returnObject(key3);

		String key4 = pool.borrowObject();
		pool.returnObject(key4);

		// wait a second and try again
		String key5 = pool.borrowObject();
		pool.returnObject(key5);

		long stopTime = System.currentTimeMillis();

		// should take less than a second, but test can fail when build is slow
		if (stopTime - startTime < 1000)
		{
			assertNotEquals(key5, "a");
		}
		else
		{
			assertEquals(key5, "a");
		}

	}

}
