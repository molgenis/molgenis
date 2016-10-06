package org.molgenis.data.annotation.test.core.entity.impl.hpo;

import org.molgenis.data.annotation.core.entity.impl.hpo.HPORepository;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class HPORepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	private HPORepository repo;

	@BeforeClass
	public void setUp()
	{
		repo = new HPORepository(ResourceUtils.getFile(getClass(), "/hpo/hpo.txt"), entityMetaDataFactory,
				attributeFactory);
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
