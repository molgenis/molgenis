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

	@Test
	@Override
	public void testAdd()
	{
		super.testAdd();
	}

	@Test
	@Override
	public void testEntityListener()
	{
		super.testEntityListener();
	}

	@Test
	@Override
	public void testCount()
	{
		super.testCount();
	}

	@Test
	@Override
	public void testDelete()
	{
		super.testDelete();
	}

	@Test
	@Override
	public void testDeleteById()
	{
		super.testDeleteById();
	}

	@Test
	@Override
	public void testDeleteStream()
	{
		super.testDeleteStream();
	}

	@Test
	@Override
	public void testDeleteAll()
	{
		super.testDeleteAll();
	}

	@Test
	@Override
	public void testFindAll()
	{
		super.testFindAll();
	}

	@Test
	@Override
	public void testFindAllTyped()
	{
		super.testFindAllTyped();
	}

	@Test
	@Override
	public void testFindAllByIds()
	{
		super.testFindAllByIds();
	}

	@Test
	@Override
	public void testFindAllByIdsTyped()
	{
		super.testFindAllByIdsTyped();
	}

	@Test
	@Override
	public void testFindAllStreamFetch()
	{
		super.testFindAllStreamFetch();
	}

	@Test
	@Override
	public void testFindQuery()
	{
		super.testFindQuery();
	}

	@Test
	@Override
	public void testFindQueryTyped()
	{
		super.testFindQueryTyped();
	}

	@Test
	@Override
	public void testFindOne()
	{
		super.testFindOne();
	}

	@Test
	@Override
	public void testFindOneTyped()
	{
		super.testFindOneTyped();
	}

	@Test
	@Override
	public void testFindOneFetch()
	{
		super.testFindOneFetch();
	}

	@Test
	@Override
	public void testFindOneFetchTyped()
	{
		super.testFindOneFetchTyped();
	}

	@Test
	@Override
	public void testFindOneQuery()
	{
		super.testFindOneQuery();
	}

	@Test
	@Override
	public void testFindOneQueryTyped()
	{
		super.testFindOneQueryTyped();
	}

	@Test
	@Override
	public void testGetCapabilities()
	{
		super.testGetCapabilities();
	}

	@Test
	@Override
	public void testGetEntityMetaData()
	{
		super.testGetEntityMetaData();
	}

	@Test
	@Override
	public void testGetEntityNames()
	{
		super.testGetEntityNames();
	}

	@Test
	@Override
	public void testGetMeta()
	{
		super.testGetMeta();
	}

	@Test
	@Override
	public void testGetRepository()
	{
		super.testGetRepository();
	}

	@Test
	@Override
	public void testHasRepository()
	{
		super.testHasRepository();
	}

	@Test
	@Override
	public void testIterator()
	{
		super.testIterator();
	}

	@Test
	@Override
	public void testQuery()
	{
		super.testQuery();
	}

	@Test
	@Override
	public void testUpdate()
	{
		super.testUpdate();
	}

	@Test
	@Override
	public void testUpdateStream()
	{
		super.testUpdateStream();
	}

}
