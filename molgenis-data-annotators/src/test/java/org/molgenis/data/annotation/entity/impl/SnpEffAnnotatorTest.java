package org.molgenis.data.annotation.entity.impl;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffAnnotator;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffRepositoryAnnotator;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffRunner;
import org.molgenis.data.annotation.meta.effects.EffectsMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { SnpEffAnnotatorTest.Config.class })
public class SnpEffAnnotatorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	EffectsMetaData effectsMetaData;

	private DataServiceImpl dataService;

	@Mock
	private SnpEffRunner snpEffRunner;

	@Mock
	private Entity snpEffAnnotatorSettings;

	private SnpEffRepositoryAnnotator annotator;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		dataService = mock(DataServiceImpl.class);

		annotator = new SnpEffRepositoryAnnotator(SnpEffAnnotator.NAME);
		annotator.init(snpEffRunner, snpEffAnnotatorSettings, dataService, vcfAttributes, effectsMetaData);
	}

	@Test
	public void testCanAnnotate()
	{
		EntityMetaData sourceEMD = entityMetaDataFactory.create().setName("source");
		when(dataService.hasRepository("source_EFFECTS")).thenReturn(true);
		assertEquals(annotator.canAnnotate(sourceEMD), "already annotated with SnpEff");
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model", "org.molgenis.data.annotation.meta.effects" })
	public static class Config
	{
	}
}