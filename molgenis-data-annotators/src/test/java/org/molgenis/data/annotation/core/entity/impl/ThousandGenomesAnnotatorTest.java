package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.ThousendGenomesAnnotatorSettings;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.annotation.core.entity.impl.ThousandGenomesAnnotator.THOUSAND_GENOME_AF;
import static org.molgenis.data.annotation.core.entity.impl.ThousandGenomesAnnotator.THOUSAND_GENOME_AF_LABEL;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { ThousandGenomesAnnotatorTest.Config.class, ThousandGenomesAnnotator.class })
public class ThousandGenomesAnnotatorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;
	private final static String THOUSAND_GENOME_TEST_PATTERN = "ALL.chr%s.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz";
	private final static String THOUSAND_GENOME_TEST_FOLDER_PROPERTY = "/1000g";
	private final static String THOUSAND_GENOME_TEST_CHROMOSOMES = "1";  //,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,Y,X
	private final static String THOUSAND_GENOME_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY = "";//X:ALL.chrX.phase3_shapeit2_mvncall_integrated.20130502.genotypes.vcf.gz,Y:ALL.chrY.phase3_integrated.20130502.genotypes.vcf.gz";

	@Autowired
	RepositoryAnnotator annotator;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();
	}

	@Test
	public void testAnnotate()
	{
		EntityType emdIn = entityTypeFactory.create("test");
		emdIn.addAttribute(vcfAttributes.getChromAttribute(), ROLE_ID);
		emdIn.addAttribute(vcfAttributes.getPosAttribute());
		emdIn.addAttribute(vcfAttributes.getRefAttribute());
		emdIn.addAttribute(vcfAttributes.getAltAttribute());
		emdIn.addAttribute(attributeFactory.create()
										   .setName(THOUSAND_GENOME_AF)
										   .setDataType(STRING)
										   .setDescription(
												   "The allele frequency for variants seen in the population used for the thousand genomes project")
										   .setLabel(THOUSAND_GENOME_AF_LABEL));

		Entity inputEntity = new DynamicEntity(emdIn);
		inputEntity.set(VcfAttributes.CHROM, "1");
		inputEntity.set(VcfAttributes.POS, 249240543);
		inputEntity.set(VcfAttributes.REF, "AGG");
		inputEntity.set(VcfAttributes.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(VcfAttributes.CHROM), "1");
		assertEquals(resultEntity.get(VcfAttributes.POS), 249240543);
		assertEquals(resultEntity.get(VcfAttributes.REF), "AGG");
		assertEquals(resultEntity.get(VcfAttributes.ALT), "A");
		assertEquals(resultEntity.get(THOUSAND_GENOME_AF), "0.61861");
	}

	@Test
	public void testAnnotateNegative()
	{
		EntityType emdIn = entityTypeFactory.create("test");
		emdIn.addAttribute(vcfAttributes.getChromAttribute(), ROLE_ID);
		emdIn.addAttribute(vcfAttributes.getPosAttribute());
		emdIn.addAttribute(vcfAttributes.getRefAttribute());
		emdIn.addAttribute(vcfAttributes.getAltAttribute());

		Entity inputEntity = new DynamicEntity(emdIn);
		inputEntity.set(vcfAttributes.CHROM, "1");
		inputEntity.set(vcfAttributes.POS, 249240543);
		inputEntity.set(vcfAttributes.REF, "A");
		inputEntity.set(vcfAttributes.ALT, "G");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(VcfAttributes.CHROM), "1");
		assertEquals(resultEntity.get(VcfAttributes.POS), 249240543);
		assertEquals(resultEntity.get(VcfAttributes.REF), "A");
		assertEquals(resultEntity.get(VcfAttributes.ALT), "G");
		assertEquals(resultEntity.get(THOUSAND_GENOME_AF), null);
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public Entity thousendGenomesAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
			when(settings.getString(ThousendGenomesAnnotatorSettings.Meta.ROOT_DIRECTORY)).thenReturn(
					ResourceUtils.getFile(getClass(), THOUSAND_GENOME_TEST_FOLDER_PROPERTY).getPath());
			when(settings.getString(ThousendGenomesAnnotatorSettings.Meta.CHROMOSOMES)).thenReturn(
					THOUSAND_GENOME_TEST_CHROMOSOMES);
			when(settings.getString(ThousendGenomesAnnotatorSettings.Meta.FILEPATTERN)).thenReturn(
					THOUSAND_GENOME_TEST_PATTERN);
			when(settings.getString(ThousendGenomesAnnotatorSettings.Meta.OVERRIDE_CHROMOSOME_FILES)).thenReturn(
					THOUSAND_GENOME_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY);

			return settings;
		}

		@Bean
		public AnnotationService annotationService()
		{
			return mock(AnnotationService.class);
		}

		@Bean
		public Resources resources()
		{
			return new ResourcesImpl();
		}
	}
}
