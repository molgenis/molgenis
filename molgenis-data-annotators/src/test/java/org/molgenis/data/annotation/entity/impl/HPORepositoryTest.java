package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.annotation.entity.impl.hpo.HPORepository;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

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
}
