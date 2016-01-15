package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractDataServiceTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchDataServiceTest.DataServiceElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DataServiceElasticsearchTestConfig.class)
public class ElasticsearchDataServiceTest extends AbstractDataServiceTest
{
	@Configuration
	public static class DataServiceElasticsearchTestConfig extends AbstractElasticsearchTestConfig
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
}
