package org.molgenis.data.annotation.core.entity.impl.snpeff;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
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
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	EffectsMetaData effectsMetaData;

	@Mock
	private SnpEffRunner snpEffRunner;

	@Mock
	private Entity snpEffAnnotatorSettings;

	private SnpEffRepositoryAnnotator annotator;

	@Autowired
	DataService dataService;

	@BeforeClass
	public void beforeClass() throws IOException
	{

		annotator = new SnpEffRepositoryAnnotator(SnpEffAnnotator.NAME);
		annotator.init(snpEffRunner, snpEffAnnotatorSettings, vcfAttributes, effectsMetaData, dataService);
	}

	@Test
	public void testCanAnnotate()
	{
		EntityType sourceEMD = entityTypeFactory.create().setFullyQualifiedName("source");
		when(dataService.hasRepository("source_EFFECTS")).thenReturn(true);
		assertEquals(annotator.canAnnotate(sourceEMD), "already annotated with SnpEff");
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model", "org.molgenis.data.annotation.core.effects" })
	public static class Config
	{
		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}
	}
}