package org.molgenis.data.annotation.core.resources.impl;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.core.resources.ResourceConfig;
import org.molgenis.data.annotation.core.resources.impl.tabix.TabixRepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.tabix.TabixVcfRepositoryFactory;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = { ResourceImplTest.Config.class })
public class ResourceImplTest extends AbstractMolgenisSpringTest
{

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Mock
	ResourceConfig config;

	@Autowired
	VcfAttributes vcfAttributes;

	@Mock
	TabixRepositoryFactory factory;

	private ResourceImpl resource;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		resource = new ResourceImpl("cadd_test", config)
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new TabixVcfRepositoryFactory("cadd", vcfAttributes, entityTypeFactory, attributeFactory);
			}
		};
	}

	@Test
	public void testFindAllReturnsResult()
	{
		File file = ResourceUtils.getFile(getClass(), "/gonl/gonl.chr1.snps_indels.r5.vcf.gz");
		when(config.getFile()).thenReturn(file);
		Query<Entity> query = QueryImpl.EQ("#CHROM", "1").and().eq("POS", 126108);

		System.out.println(resource.findAll(query));
	}

	@Test
	public void testFindAllReturnsResultFile2()
	{
		File file = ResourceUtils
				.getFile(getClass(), "/1000g/ALL.chr1.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz");
		when(config.getFile()).thenReturn(file);

		Query<Entity> query = QueryImpl.EQ("#CHROM", "1").and().eq("POS", 10352);

		System.out.println(resource.findAll(query));
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model", "org.molgenis.data.annotation.core.effects" })
	public static class Config
	{
	}

}
