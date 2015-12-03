package org.molgenis.data.annotation.entity.impl;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import autovalue.shaded.com.google.common.common.collect.Iterables;

public class HPORepositoryTest
{
	private HPORepository repo;

	@BeforeClass
	public void setUp()
	{
		repo = new HPORepository(new File("src/test/resources/hpo/hpo.txt"));
	}

	@AfterClass
	public void shutDown() throws IOException
	{
		repo.close();
	}

	@Test
	public void count()
	{
		Assert.assertEquals(repo.count(), 24);
	}

	@Test
	public void find()
	{
		Iterable<Entity> entities = repo.findAll(QueryImpl.EQ("gene-id", "HSD3B2"));
		Assert.assertNotNull(entities);
		Assert.assertEquals(Iterables.size(entities), 3);
	}
}
