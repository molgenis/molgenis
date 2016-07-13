package org.molgenis.data.annotation.core.entity.test.snpeff;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.impl.snpeff.SnpEffAnnotator;
import org.molgenis.data.annotation.core.entity.impl.snpeff.SnpEffRepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.impl.snpeff.SnpEffRunner;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;

import java.io.IOException;

import static org.mockito.Mockito.mock;
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

	@Mock
	private SnpEffRunner snpEffRunner;

	@Mock
	private Entity snpEffAnnotatorSettings;

	private SnpEffRepositoryAnnotator annotator;

	@BeforeClass
	public void beforeClass() throws IOException
	{

		annotator = new SnpEffRepositoryAnnotator(SnpEffAnnotator.NAME);
		annotator.init(snpEffRunner, snpEffAnnotatorSettings, vcfAttributes, effectsMetaData);
	}

	// FIXME: how to do this without dataservice
//	@Test
//	public void testCanAnnotate()
//	{
//		EntityMetaData sourceEMD = entityMetaDataFactory.create().setName("source");
//		when(dataService.hasRepository("source_EFFECTS")).thenReturn(true);
//		assertEquals(annotator.canAnnotate(sourceEMD), "already annotated with SnpEff");
//	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model", "org.molgenis.data.annotation.core.effects" })
	public static class Config
	{
	}
}