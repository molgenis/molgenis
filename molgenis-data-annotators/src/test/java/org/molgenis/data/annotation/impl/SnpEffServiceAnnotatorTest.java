package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ SnpEffServiceAnnotatorTest.Config.class, SnpEffAnnotator.class })
public class SnpEffServiceAnnotatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private SnpEffAnnotator.SnpEffRepositoryAnnotator snpEffRepositoryAnnotator;
	private final ArrayList<Entity> entities = new ArrayList<>();;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		Entity entity1 = new MapEntity();
		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 1234);
		entity1.set(VcfRepository.REF, "A");
		entity1.set(VcfRepository.ALT, "T");

		Entity entity2 = new MapEntity();
		entity2.set(VcfRepository.CHROM, "X");
		entity2.set(VcfRepository.POS, 12345);
		entity2.set(VcfRepository.REF, "A");
		entity2.set(VcfRepository.ALT, "C");

		Entity entity3 = new MapEntity();
		entity3.set(VcfRepository.CHROM, "3");
		entity3.set(VcfRepository.POS, 123);
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "T");

		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);
	}

	@Test
	public void getInputTempFileTest()
	{
		BufferedReader br = null;
		try
		{
			File file = snpEffRepositoryAnnotator.getInputVcfTempFile(entities);
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));

			assertEquals(br.readLine(), "1\t1234\t.\tA\tT");
			assertEquals(br.readLine(), "X\t12345\t.\tA\tC");
			assertEquals(br.readLine(), "3\t123\t.\tG\tT");

		}
		catch (Exception e)
		{
			fail();
		}
		finally
		{
			try
			{
				br.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Test
	public void parseOutputLineToEntityTest()
	{
		Entity entity = new MapEntity();
		snpEffRepositoryAnnotator.parseOutputLineToEntity(
				"X\t12345\t.\tA\tT\tqual\tfilter\t0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15", entity);
		assertEquals(entity.get(SnpEffAnnotator.ANNOTATION), "1");
		assertEquals(entity.get(SnpEffAnnotator.PUTATIVE_IMPACT), "2");
		assertEquals(entity.get(SnpEffAnnotator.GENE_NAME), "3");
		assertEquals(entity.get(SnpEffAnnotator.GENE_ID), "4");
		assertEquals(entity.get(SnpEffAnnotator.FEATURE_TYPE), "5");
		assertEquals(entity.get(SnpEffAnnotator.FEATURE_ID), "6");
		assertEquals(entity.get(SnpEffAnnotator.TRANSCRIPT_BIOTYPE), "7");
		assertEquals(entity.get(SnpEffAnnotator.RANK_TOTAL), "8");
		assertEquals(entity.get(SnpEffAnnotator.HGVS_C), "9");
		assertEquals(entity.get(SnpEffAnnotator.HGVS_P), "10");
		assertEquals(entity.get(SnpEffAnnotator.C_DNA_POSITION), "11");
		assertEquals(entity.get(SnpEffAnnotator.CDS_POSITION), "12");
		assertEquals(entity.get(SnpEffAnnotator.PROTEIN_POSITION), "13");
		assertEquals(entity.get(SnpEffAnnotator.DISTANCE_TO_FEATURE), "14");
		assertEquals(entity.get(SnpEffAnnotator.ERRORS), "15");
		assertEquals(entity.get(SnpEffAnnotator.LOF), "");
		assertEquals(entity.get(SnpEffAnnotator.NMD), "");
	}

	@Configuration
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

	}
}
