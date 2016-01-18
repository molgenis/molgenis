package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractDataServiceTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlDataServiceTest.DataServiceMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DataServiceMySqlTestConfig.class)
public class MySqlDataServiceTest extends AbstractDataServiceTest
{
	@Configuration
	public static class DataServiceMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testAddIterable()
	{
		super.testAddIterable();
	}

	@Override
	@Test
	public void testAddStream()
	{
		super.testAddStream();
	}

	@Override
	@Test
	public void testEntityListener()
	{
		super.testEntityListener();
	}

	@Override
	@Test
	public void testCount()
	{
		super.testCount();
	}

	@Override
	@Test
	public void testDelete()
	{
		super.testDelete();
	}

	@Override
	@Test
	public void testDeleteById()
	{
		super.testDeleteById();
	}

	@Override
	@Test
	public void testDeleteIterable()
	{
		super.testDeleteIterable();
	}

	@Override
	@Test
	public void testDeleteStream()
	{
		super.testDeleteStream();
	}

	@Override
	@Test
	public void testDeleteAll()
	{
		super.testDeleteAll();
	}

	@Override
	@Test
	public void testFindAll()
	{
		super.testFindAll();
	}

	@Override
	@Test
	public void testFindAllTyped()
	{
		super.testFindAllTyped();
	}

	@Override
	@Test
	public void testFindAllStream()
	{
		super.testFindAllStream();
	}

	@Override
	@Test
	public void testFindAllTypedStream()
	{
		super.testFindAllTypedStream();
	}

	@Override
	@Test
	public void testFindAllIterableTyped()
	{
		super.testFindAllIterableTyped();
	}

	@Override
	@Test
	public void testFindAllIterable()
	{
		super.testFindAllIterable();
	}

	@Override
	@Test
	public void testFindAllIterableFetch()
	{
		super.testFindAllIterableFetch();
	}

	@Override
	@Test
	public void testFindAllStreamFetch()
	{
		super.testFindAllStreamFetch();
	}

	@Override
	@Test
	public void testFindAllTypedFetchStream()
	{
		super.testFindAllTypedFetchStream();
	}

	@Override
	@Test
	public void testFindAllAsStream()
	{
		super.testFindAllAsStream();
	}

	@Override
	@Test
	public void testFindAllAsStreamTyped()
	{
		super.testFindAllAsStreamTyped();
	}

	@Override
	@Test
	public void testFindAsStreamQuery()
	{
		super.testFindAsStreamQuery();
	}

	@Override
	@Test
	public void testFindAsStreamQueryTyped()
	{
		super.testFindAsStreamQueryTyped();
	}

	@Override
	@Test
	public void testFindOne()
	{
		super.testFindOne();
	}

	@Override
	@Test
	public void testFindOneTyped()
	{
		super.testFindOneTyped();
	}

	@Override
	@Test
	public void testFindOneFetch()
	{
		super.testFindOneFetch();
	}

	@Override
	@Test
	public void testFindOneFetchTyped()
	{
		super.testFindOneFetchTyped();
	}

	@Override
	@Test
	public void testFindOneQuery()
	{
		super.testFindOneQuery();
	}

	@Override
	@Test
	public void testFindOneQueryTyped()
	{
		super.testFindOneQueryTyped();
	}

	@Override
	@Test
	public void testGetCapabilities()
	{
		super.testGetCapabilities();
	}

	@Override
	@Test
	public void testGetEntityMetaData()
	{
		super.testGetEntityMetaData();
	}

	@Override
	@Test
	public void testGetEntityNames()
	{
		super.testGetEntityNames();
	}

	@Override
	@Test
	public void testGetMeta()
	{
		super.testGetMeta();
	}

	@Override
	@Test
	public void testGetRepository()
	{
		super.testGetRepository();
	}

	@Override
	@Test
	public void testHasRepository()
	{
		super.testHasRepository();
	}

	@Override
	@Test
	public void testIterator()
	{
		super.testIterator();
	}

	@Override
	@Test
	public void testQuery()
	{
		super.testQuery();
	}

	@Override
	@Test
	public void testUpdate()
	{
		super.testUpdate();
	}

	@Override
	@Test
	public void testUpdateStream()
	{
		super.testUpdateStream();
	}
}
