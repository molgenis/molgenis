package org.molgenis.data.annotation.resources.impl;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

public class ResourceImplTest
{
	@Mock
	MolgenisSettings molgenisSettings;

	@Mock
	ResourceConfig config;

	@Mock
	TabixRepositoryFactory factory;

	private ResourceImpl resource;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		resource = new ResourceImpl("cadd_test", config, new TabixVcfRepositoryFactory(
				"cadd"));
	}

	@Test
	public void ifSettingIsNotDefinedResourceIsUnavailable()
	{
		when(molgenisSettings.getProperty("cadd_key")).thenReturn(null);
		Assert.assertFalse(resource.isAvailable());
	}

	/**
	 * FIXME: reuse in config test
	 * @Test
	public void ifSettingBecomesDefinedAndFileExistsResourceBecomesAvailable()
	{
		Assert.assertFalse(resource.isAvailable());
		when(molgenisSettings.getProperty("cadd_key", null)).thenReturn("src/test/resources/cadd_test.vcf.gz");
		Assert.assertTrue(resource.isAvailable());
		when(molgenisSettings.getProperty("cadd_key", null)).thenReturn("nonsense");
		Assert.assertFalse(resource.isAvailable());
		when(molgenisSettings.getProperty("cadd_key", null)).thenReturn("src/test/resources/cadd_test.vcf.gz");
		Assert.assertTrue(resource.isAvailable());
	}

	@Test
	public void ifDefaultDoesNotExistResourceIsUnavailable()
	{
		resource = new ResourceImpl("cadd_test", config,
				new TabixVcfRepositoryFactory("cadd"));
		Assert.assertFalse(resource.isAvailable());
	}**/

	@Test
	public void testFindAllReturnsResult()
	{
		File file = ResourceUtils.getFile(getClass(), "/gonl.chr1.snps_indels.r5.vcf.gz");
		when(config.getFile()).thenReturn(file);
		Query query = QueryImpl.EQ("#CHROM", "1").and().eq("POS", 126108);

		System.out.println(resource.findAll(query));
	}

	@Test
	public void testFindAllReturnsResultFile2()
	{
		File file = ResourceUtils.getFile(getClass(), "/ALL.chr1.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz");
		when(config.getFile()).thenReturn(file);

		Query query = QueryImpl.EQ("#CHROM", "1").and().eq("POS", 10352);

		System.out.println(resource.findAll(query));
	}
}
