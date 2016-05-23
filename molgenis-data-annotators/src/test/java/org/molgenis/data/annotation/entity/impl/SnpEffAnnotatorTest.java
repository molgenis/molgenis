package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffAnnotator.SnpEffRepositoryAnnotator;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffRunner;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SnpEffAnnotatorTest
{
	private DataServiceImpl dataService;

	@Mock
	private SnpEffRunner snpEffRunner;

	@Mock
	private Entity snpEffAnnotatorSettings;

	private SnpEffRepositoryAnnotator annotator;

	@BeforeMethod
	public void beforeMethod()
	{
		dataService = mock(DataServiceImpl.class);

		annotator = new SnpEffRepositoryAnnotator(snpEffRunner, snpEffAnnotatorSettings, dataService);
	}

	@Test
	public void testCanAnnotate()
	{
		EntityMetaData sourceEMD = new DefaultEntityMetaData("source");
		when(dataService.hasRepository("source_EFFECTS")).thenReturn(true);
		assertEquals(annotator.canAnnotate(sourceEMD), "already annotated with SnpEff");
	}
}
