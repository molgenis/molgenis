package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.settings.ClinvarAnnotatorSettings;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { ClinvarAnnotatorTest.Config.class, ClinvarAnnotator.class })
public class ClinvarAnnotatorTest extends AbstractMolgenisSpringTest
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
	private RepositoryAnnotator clinvarAnnotator;

	@Autowired
	private DataService dataService;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(dataService.findAll(any(String.class), any(Query.class))).thenReturn(Stream.empty());
	}

	@Test
	public void annotateIterable()
	{
		EntityType sourceMeta = entityTypeFactory.create("clinvar");
		sourceMeta.addAttribute(vcfAttributes.getChromAttribute(), ROLE_ID);
		sourceMeta.addAttribute(vcfAttributes.getPosAttribute());
		sourceMeta.addAttribute(vcfAttributes.getRefAttribute());
		sourceMeta.addAttribute(vcfAttributes.getAltAttribute());

		EntityType annotatedSourceMeta = sourceMeta;
		annotatedSourceMeta.addAttribute(attributeFactory.create().setName(ClinvarAnnotator.CLINVAR_CLNSIG));
		annotatedSourceMeta.addAttribute(attributeFactory.create().setName(ClinvarAnnotator.CLINVAR_CLNALLE));

		// no clinvar annotation
		Entity source0 = new DynamicEntity(sourceMeta);
		source0.set(CHROM, "1");
		source0.set(POS, 883515);
		source0.set(REF, "G");
		source0.set(ALT, "A");

		// single-allelic source
		Entity source1 = new DynamicEntity(sourceMeta);
		source1.set(CHROM, "1");
		source1.set(POS, 883516);
		source1.set(REF, "G");
		source1.set(ALT, "A");

		// multi-allelic source (start)
		Entity source2 = new DynamicEntity(sourceMeta);
		source2.set(CHROM, "1");
		source2.set(POS, 883516);
		source2.set(REF, "G");
		source2.set(ALT, "A,T,C");

		// multi-allelic source (middle)
		Entity source3 = new DynamicEntity(sourceMeta);
		source3.set(CHROM, "1");
		source3.set(POS, 883516);
		source3.set(REF, "G");
		source3.set(ALT, "C,A,T");

		// multi-allelic source (end)
		Entity source4 = new DynamicEntity(sourceMeta);
		source4.set(CHROM, "1");
		source4.set(POS, 883516);
		source4.set(REF, "G");
		source4.set(ALT, "C,T,A");

		// single-allelic source and multi-allelic target
		Entity source5 = new DynamicEntity(sourceMeta);
		source5.set(CHROM, "1");
		source5.set(POS, 17349179);
		source5.set(REF, "C");
		source5.set(ALT, "A");

		// single-allelic source and multi-allelic target
		Entity source6 = new DynamicEntity(sourceMeta);
		source6.set(CHROM, "1");
		source6.set(POS, 17349179);
		source6.set(REF, "C");
		source6.set(ALT, "T");

		// single-allelic source and multi-allelic target
		Entity source7 = new DynamicEntity(sourceMeta);
		source7.set(CHROM, "1");
		source7.set(POS, 17349179);
		source7.set(REF, "C");
		source7.set(ALT, "A,T");

		// single-allelic source and multi-allelic target
		Entity source8 = new DynamicEntity(sourceMeta);
		source8.set(CHROM, "1");
		source8.set(POS, 17349179);
		source8.set(REF, "C");
		source8.set(ALT, "T,A");

		// single-allelic source and multi-allelic target
		Entity source9 = new DynamicEntity(sourceMeta);
		source9.set(CHROM, "1");
		source9.set(POS, 17349179);
		source9.set(REF, "C");
		source9.set(ALT, "C,T,A");

		Entity expectedTarget0 = new DynamicEntity(sourceMeta);
		expectedTarget0.set(CHROM, "1");
		expectedTarget0.set(POS, 883515);
		expectedTarget0.set(REF, "G");
		expectedTarget0.set(ALT, "A");
		expectedTarget0.set(ClinvarAnnotator.CLINVAR_CLNSIG, null);
		expectedTarget0.set(ClinvarAnnotator.CLINVAR_CLNALLE, null);

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget1 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget1.set(CHROM, "1");
		expectedTarget1.set(POS, 883516);
		expectedTarget1.set(REF, "G");
		expectedTarget1.set(ALT, "A");
		expectedTarget1.set(ClinvarAnnotator.CLINVAR_CLNSIG, "1");
		expectedTarget1.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget2 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget2.set(CHROM, "1");
		expectedTarget2.set(POS, 883516);
		expectedTarget2.set(REF, "G");
		expectedTarget2.set(ALT, "A,T,C");
		expectedTarget2.set(ClinvarAnnotator.CLINVAR_CLNSIG, "1,.,.");
		expectedTarget2.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1,.,.");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget3 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget3.set(CHROM, "1");
		expectedTarget3.set(POS, 883516);
		expectedTarget3.set(REF, "G");
		expectedTarget3.set(ALT, "C,A,T");
		expectedTarget3.set(ClinvarAnnotator.CLINVAR_CLNSIG, ".,1,.");
		expectedTarget3.set(ClinvarAnnotator.CLINVAR_CLNALLE, ".,2,.");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget4 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget4.set(CHROM, "1");
		expectedTarget4.set(POS, 883516);
		expectedTarget4.set(REF, "G");
		expectedTarget4.set(ALT, "C,T,A");
		expectedTarget4.set(ClinvarAnnotator.CLINVAR_CLNSIG, ".,.,1");
		expectedTarget4.set(ClinvarAnnotator.CLINVAR_CLNALLE, ".,.,3");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget5 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget5.set(CHROM, "1");
		expectedTarget5.set(POS, 17349179);
		expectedTarget5.set(REF, "C");
		expectedTarget5.set(ALT, "A");
		expectedTarget5.set(ClinvarAnnotator.CLINVAR_CLNSIG, "4");
		expectedTarget5.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget6 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget6.set(CHROM, "1");
		expectedTarget6.set(POS, 17349179);
		expectedTarget6.set(REF, "C");
		expectedTarget6.set(ALT, "T");
		expectedTarget6.set(ClinvarAnnotator.CLINVAR_CLNSIG, "5");
		expectedTarget6.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget7 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget7.set(CHROM, "1");
		expectedTarget7.set(POS, 17349179);
		expectedTarget7.set(REF, "C");
		expectedTarget7.set(ALT, "A,T");
		expectedTarget7.set(ClinvarAnnotator.CLINVAR_CLNSIG, "4,5");
		expectedTarget7.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1,2");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget8 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget8.set(CHROM, "1");
		expectedTarget8.set(POS, 17349179);
		expectedTarget8.set(REF, "C");
		expectedTarget8.set(ALT, "T,A");
		expectedTarget8.set(ClinvarAnnotator.CLINVAR_CLNSIG, "5,4");
		expectedTarget8.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1,2");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		Entity expectedTarget9 = new DynamicEntity(annotatedSourceMeta);
		expectedTarget9.set(CHROM, "1");
		expectedTarget9.set(POS, 17349179);
		expectedTarget9.set(REF, "C");
		expectedTarget9.set(ALT, "C,T,A");
		expectedTarget9.set(ClinvarAnnotator.CLINVAR_CLNSIG, ".,5,4");
		expectedTarget9.set(ClinvarAnnotator.CLINVAR_CLNALLE, ".,2,3");

		Iterator<Entity> target0 = clinvarAnnotator.annotate(Collections.singletonList(source0));
		Iterator<Entity> target1 = clinvarAnnotator.annotate(Collections.singletonList(source1));
		Iterator<Entity> target2 = clinvarAnnotator.annotate(Collections.singletonList(source2));
		Iterator<Entity> target3 = clinvarAnnotator.annotate(Collections.singletonList(source3));
		Iterator<Entity> target4 = clinvarAnnotator.annotate(Collections.singletonList(source4));
		Iterator<Entity> target5 = clinvarAnnotator.annotate(Collections.singletonList(source5));
		Iterator<Entity> target6 = clinvarAnnotator.annotate(Collections.singletonList(source6));
		Iterator<Entity> target7 = clinvarAnnotator.annotate(Collections.singletonList(source7));
		Iterator<Entity> target8 = clinvarAnnotator.annotate(Collections.singletonList(source8));
		Iterator<Entity> target9 = clinvarAnnotator.annotate(Collections.singletonList(source9));
		assertTrue(EntityUtils.equals(target0.next(), expectedTarget0));
		assertTrue(EntityUtils.equals(target1.next(), expectedTarget1));
		assertTrue(EntityUtils.equals(target2.next(), expectedTarget2));
		assertTrue(EntityUtils.equals(target3.next(), expectedTarget3));
		assertTrue(EntityUtils.equals(target4.next(), expectedTarget4));
		assertTrue(EntityUtils.equals(target5.next(), expectedTarget5));
		assertTrue(EntityUtils.equals(target6.next(), expectedTarget6));
		assertTrue(EntityUtils.equals(target7.next(), expectedTarget7));
		assertTrue(EntityUtils.equals(target8.next(), expectedTarget8));
		assertTrue(EntityUtils.equals(target9.next(), expectedTarget9));

	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
		@Bean
		public Entity clinvarAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
			when(settings.getString(ClinvarAnnotatorSettings.Meta.CLINVAR_LOCATION)).thenReturn(
					ResourceUtils.getFile(getClass(), "/clinvar/clinvar_20150629.vcf.gz").getPath());
			return settings;
		}

		@Bean
		public Resources resources()
		{
			return new ResourcesImpl();
		}
	}
}
