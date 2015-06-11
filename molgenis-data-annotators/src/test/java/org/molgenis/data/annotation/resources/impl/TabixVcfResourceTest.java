package org.molgenis.data.annotation.resources.impl;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TabixVcfResourceTest
{
	@Mock
	MolgenisSettings molgenisSettings;

	private TabixVcfResource resource;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		resource = new TabixVcfResource("cadd_test", molgenisSettings, "cadd_key", null);
	}

	@Test
	public void ifSettingIsNotDefinedResourceIsUnavailable()
	{
		when(molgenisSettings.getProperty("cadd_key")).thenReturn(null);
		Assert.assertFalse(resource.isAvailable());
	}

	@Test
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
		resource = new TabixVcfResource("cadd_test", molgenisSettings, "cadd_key", "defaultNoExist");
		Assert.assertFalse(resource.isAvailable());
	}

	@Test
	public void testFindAllReturnsResult()
	{
		when(molgenisSettings.getProperty("cadd_key", null)).thenReturn(
				"src/test/resources/gonl.chr1.snps_indels.r5.vcf.gz");
		Query query = QueryImpl.EQ("#CHROM", "1").and().eq("POS", "126108");

		System.out.println(resource.findAll(query));
	}
}
