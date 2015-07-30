package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.REF_META;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.MultiResourceConfigImpl;
import org.molgenis.data.annotation.resources.impl.ResourcesImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@ContextConfiguration(classes =
{ ClinvarAnnotatorTest.Config.class, ClinvarAnnotator.class })
public class ClinvarAnnotatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private RepositoryAnnotator clinvarAnnotator;

	@Test
	public void annotateIterable()
	{
		DefaultEntityMetaData sourceMeta = new DefaultEntityMetaData("clinvar");
		sourceMeta.addAttribute(CHROM).setIdAttribute(true).setNillable(false);
		sourceMeta.addAttributeMetaData(POS_META);
		sourceMeta.addAttributeMetaData(REF_META);
		sourceMeta.addAttributeMetaData(ALT_META);

		// no clinvar annotation
		MapEntity source0 = new MapEntity(sourceMeta);
		source0.set(CHROM, "1");
		source0.set(POS, 883515l);
		source0.set(REF, "G");
		source0.set(ALT, "A");

		// single-allelic source
		MapEntity source1 = new MapEntity(sourceMeta);
		source1.set(CHROM, "1");
		source1.set(POS, 883516l);
		source1.set(REF, "G");
		source1.set(ALT, "A");

		// multi-allelic source (start)
		MapEntity source2 = new MapEntity(sourceMeta);
		source2.set(CHROM, "1");
		source2.set(POS, 883516l);
		source2.set(REF, "G");
		source2.set(ALT, "A,T,C");

		// multi-allelic source (middle)
		MapEntity source3 = new MapEntity(sourceMeta);
		source3.set(CHROM, "1");
		source3.set(POS, 883516l);
		source3.set(REF, "G");
		source3.set(ALT, "C,A,T");

		// multi-allelic source (end)
		MapEntity source4 = new MapEntity(sourceMeta);
		source4.set(CHROM, "1");
		source4.set(POS, 883516l);
		source4.set(REF, "G");
		source4.set(ALT, "C,T,A");

		// single-allelic source and multi-allelic target
		MapEntity source5 = new MapEntity(sourceMeta);
		source5.set(CHROM, "1");
		source5.set(POS, 17349179l);
		source5.set(REF, "C");
		source5.set(ALT, "A");

		// single-allelic source and multi-allelic target
		MapEntity source6 = new MapEntity(sourceMeta);
		source6.set(CHROM, "1");
		source6.set(POS, 17349179l);
		source6.set(REF, "C");
		source6.set(ALT, "T");

		// single-allelic source and multi-allelic target
		MapEntity source7 = new MapEntity(sourceMeta);
		source7.set(CHROM, "1");
		source7.set(POS, 17349179l);
		source7.set(REF, "C");
		source7.set(ALT, "A,T");

		// single-allelic source and multi-allelic target
		MapEntity source8 = new MapEntity(sourceMeta);
		source8.set(CHROM, "1");
		source8.set(POS, 17349179l);
		source8.set(REF, "C");
		source8.set(ALT, "T,A");

		// single-allelic source and multi-allelic target
		MapEntity source9 = new MapEntity(sourceMeta);
		source9.set(CHROM, "1");
		source9.set(POS, 17349179l);
		source9.set(REF, "C");
		source9.set(ALT, "C,T,A");

		MapEntity expectedTarget0 = new MapEntity(sourceMeta);
		expectedTarget0.set(CHROM, "1");
		expectedTarget0.set(POS, 883515l);
		expectedTarget0.set(REF, "G");
		expectedTarget0.set(ALT, "A");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget1 = new MapEntity(sourceMeta);
		expectedTarget1.set(CHROM, "1");
		expectedTarget1.set(POS, 883516l);
		expectedTarget1.set(REF, "G");
		expectedTarget1.set(ALT, "A");
		expectedTarget1.set(ClinvarAnnotator.CLINVAR_CLNSIG, "1");
		expectedTarget1.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget2 = new MapEntity(sourceMeta);
		expectedTarget2.set(CHROM, "1");
		expectedTarget2.set(POS, 883516l);
		expectedTarget2.set(REF, "G");
		expectedTarget2.set(ALT, "A,T,C");
		expectedTarget2.set(ClinvarAnnotator.CLINVAR_CLNSIG, "1,.,.");
		expectedTarget2.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1,.,.");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget3 = new MapEntity(sourceMeta);
		expectedTarget3.set(CHROM, "1");
		expectedTarget3.set(POS, 883516l);
		expectedTarget3.set(REF, "G");
		expectedTarget3.set(ALT, "C,A,T");
		expectedTarget3.set(ClinvarAnnotator.CLINVAR_CLNSIG, ".,1,.");
		expectedTarget3.set(ClinvarAnnotator.CLINVAR_CLNALLE, ".,2,.");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget4 = new MapEntity(sourceMeta);
		expectedTarget4.set(CHROM, "1");
		expectedTarget4.set(POS, 883516l);
		expectedTarget4.set(REF, "G");
		expectedTarget4.set(ALT, "C,T,A");
		expectedTarget4.set(ClinvarAnnotator.CLINVAR_CLNSIG, ".,.,1");
		expectedTarget4.set(ClinvarAnnotator.CLINVAR_CLNALLE, ".,.,3");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget5 = new MapEntity(sourceMeta);
		expectedTarget5.set(CHROM, "1");
		expectedTarget5.set(POS, 17349179l);
		expectedTarget5.set(REF, "C");
		expectedTarget5.set(ALT, "A");
		expectedTarget5.set(ClinvarAnnotator.CLINVAR_CLNSIG, "4");
		expectedTarget5.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget6 = new MapEntity(sourceMeta);
		expectedTarget6.set(CHROM, "1");
		expectedTarget6.set(POS, 17349179l);
		expectedTarget6.set(REF, "C");
		expectedTarget6.set(ALT, "T");
		expectedTarget6.set(ClinvarAnnotator.CLINVAR_CLNSIG, "5");
		expectedTarget6.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget7 = new MapEntity(sourceMeta);
		expectedTarget7.set(CHROM, "1");
		expectedTarget7.set(POS, 17349179l);
		expectedTarget7.set(REF, "C");
		expectedTarget7.set(ALT, "A,T");
		expectedTarget7.set(ClinvarAnnotator.CLINVAR_CLNSIG, "4,5");
		expectedTarget7.set(ClinvarAnnotator.CLINVAR_CLNALLE, "1,2");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget8 = new MapEntity(sourceMeta);
		expectedTarget8.set(CHROM, "1");
		expectedTarget8.set(POS, 17349179l);
		expectedTarget8.set(REF, "C");
		expectedTarget8.set(ALT, "T,A");
		expectedTarget8.set(ClinvarAnnotator.CLINVAR_CLNSIG, "5,4");
		expectedTarget8.set(ClinvarAnnotator.CLINVAR_CLNALLE, "2,1");

		// FIXME see https://github.com/molgenis/molgenis/issues/3433
		MapEntity expectedTarget9 = new MapEntity(sourceMeta);
		expectedTarget9.set(CHROM, "1");
		expectedTarget9.set(POS, 17349179l);
		expectedTarget9.set(REF, "C");
		expectedTarget9.set(ALT, "C,T,A");
		expectedTarget9.set(ClinvarAnnotator.CLINVAR_CLNSIG, ".,5,4");
		expectedTarget9.set(ClinvarAnnotator.CLINVAR_CLNALLE, ".,2,1");

		Iterator<Entity> targets = clinvarAnnotator.annotate(Arrays.asList(source0, source1, source2, source3, source4,
				source5, source6, source7, source8, source9));
		assertEquals(Lists.newArrayList(targets),
				Arrays.asList(expectedTarget0, expectedTarget1, expectedTarget2, expectedTarget3, expectedTarget4,
						expectedTarget5, expectedTarget6, expectedTarget7, expectedTarget8, expectedTarget9));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public MolgenisSettings molgenisSettings()
		{
			MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
			when(molgenisSettings.getProperty(ClinvarAnnotator.CLINVAR_FILE_LOCATION_PROPERTY)).thenReturn(
					ResourceUtils.getFile(getClass(), "/clinvar/clinvar_20150629.vcf.gz").getAbsolutePath());
			when(molgenisSettings.getProperty(ClinvarAnnotator.CLINVAR_FILE_LOCATION_PROPERTY,
					MultiResourceConfigImpl.DEFAULT_ROOT_DIRECTORY)).thenReturn(
							ResourceUtils.getFile(getClass(), "/clinvar/clinvar_20150629.vcf.gz").getAbsolutePath());
			return molgenisSettings;
		}

		@Bean
		public DataService dataService()
		{
			DataService dataService = mock(DataService.class);
			when(dataService.findAll(any(String.class), any(Query.class))).thenReturn(Collections.emptyList());
			return dataService;
		}

		@Bean
		public Resources resources()
		{
			return new ResourcesImpl();
		}
	}
}
