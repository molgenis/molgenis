package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.utils.JarRunner;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;

@ContextConfiguration(classes =
{ SnpEffAnnotatorTest.Config.class, SnpEffAnnotator.class })
public class SnpEffAnnotatorTest extends AbstractTestNGSpringContextTests
{
	private final ArrayList<Entity> entities = new ArrayList<>();;
	private DefaultEntityMetaData metaDataCanAnnotate;
	private SnpEffAnnotator.SnpEffRepositoryAnnotator snpEffRepositoryAnnotator;
	private JarRunner jarRunner;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		jarRunner = mock(JarRunner.class);

		snpEffRepositoryAnnotator = new SnpEffAnnotator.SnpEffRepositoryAnnotator(new MapEntity(), jarRunner);

		metaDataCanAnnotate = new DefaultEntityMetaData("test");
		AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS,
				MolgenisFieldTypes.FieldTypeEnum.LONG);
		AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());

		Entity entity1 = new MapEntity(metaDataCanAnnotate);
		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 13380);
		entity1.set(VcfRepository.REF, "C");
		entity1.set(VcfRepository.ALT, "G");

		Entity entity2 = new MapEntity(metaDataCanAnnotate);
		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 13980);
		entity2.set(VcfRepository.REF, "T");
		entity2.set(VcfRepository.ALT, "C");

		Entity entity3 = new MapEntity(metaDataCanAnnotate);
		entity3.set(VcfRepository.CHROM, "1");
		entity3.set(VcfRepository.POS, 78383467);
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "A");

		Entity entity4 = new MapEntity(metaDataCanAnnotate);
		entity4.set(VcfRepository.CHROM, "1");
		entity4.set(VcfRepository.POS, 231094050);
		entity4.set(VcfRepository.REF, "GAA");
		entity4.set(VcfRepository.ALT, "G,GAAA,GA");

		Entity entity5 = new MapEntity(metaDataCanAnnotate);
		entity5.set(VcfRepository.CHROM, "2");
		entity5.set(VcfRepository.POS, 171570151);
		entity5.set(VcfRepository.REF, "C");
		entity5.set(VcfRepository.ALT, "T");

		Entity entity6 = new MapEntity(metaDataCanAnnotate);
		entity6.set(VcfRepository.CHROM, "4");
		entity6.set(VcfRepository.POS, 69964234);
		entity6.set(VcfRepository.REF, "CT");
		entity6.set(VcfRepository.ALT, "CTT,CTTT,C");

		Entity entity7 = new MapEntity(metaDataCanAnnotate);
		entity7.set(VcfRepository.CHROM, "15");
		entity7.set(VcfRepository.POS, 66641732);
		entity7.set(VcfRepository.REF, "G");
		entity7.set(VcfRepository.ALT, "A,C,T");

		Entity entity8 = new MapEntity(metaDataCanAnnotate);
		entity8.set(VcfRepository.CHROM, "21");
		entity8.set(VcfRepository.POS, 46924425);
		entity8.set(VcfRepository.REF, "CGGCCCCCCA");
		entity8.set(VcfRepository.ALT, "C");

		Entity entity9 = new MapEntity(metaDataCanAnnotate);
		entity9.set(VcfRepository.CHROM, "X");
		entity9.set(VcfRepository.POS, 79943569);
		entity9.set(VcfRepository.REF, "T");
		entity9.set(VcfRepository.ALT, "C");

		Entity entity10 = new MapEntity(metaDataCanAnnotate);
		entity10.set(VcfRepository.CHROM, "2");
		entity10.set(VcfRepository.POS, 191904021);
		entity10.set(VcfRepository.REF, "G");
		entity10.set(VcfRepository.ALT, "T");

		Entity entity11 = new MapEntity(metaDataCanAnnotate);
		entity11.set(VcfRepository.CHROM, "3");
		entity11.set(VcfRepository.POS, 53219680);
		entity11.set(VcfRepository.REF, "G");
		entity11.set(VcfRepository.ALT, "C");

		Entity entity12 = new MapEntity(metaDataCanAnnotate);
		entity12.set(VcfRepository.CHROM, "2");
		entity12.set(VcfRepository.POS, 219142023);
		entity12.set(VcfRepository.REF, "G");
		entity12.set(VcfRepository.ALT, "A");

		Entity entity13 = new MapEntity(metaDataCanAnnotate);
		entity13.set(VcfRepository.CHROM, "1");
		entity13.set(VcfRepository.POS, 1115548);
		entity13.set(VcfRepository.REF, "G");
		entity13.set(VcfRepository.ALT, "A");

		Entity entity14 = new MapEntity(metaDataCanAnnotate);
		entity14.set(VcfRepository.CHROM, "21");
		entity14.set(VcfRepository.POS, 45650009);
		entity14.set(VcfRepository.REF, "T");
		entity14.set(VcfRepository.ALT, "TG, A, G");

		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);
		entities.add(entity4);
		entities.add(entity5);
		entities.add(entity6);
		entities.add(entity7);
		entities.add(entity8);
		entities.add(entity9);
		entities.add(entity10);
		entities.add(entity11);
		entities.add(entity12);
		entities.add(entity13);
		entities.add(entity14);
	}

	@Test
	public void getInputTempFileTest()
	{
		BufferedReader br = null;
		try
		{
			File file = snpEffRepositoryAnnotator.getInputVcfTempFile(entities);
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));

			assertEquals(br.readLine(), "1	13380	.	C	G");
			assertEquals(br.readLine(), "1	13980	.	T	C");
			assertEquals(br.readLine(), "1	78383467	.	G	A");
			assertEquals(br.readLine(), "1	231094050	.	GAA	G,GAAA,GA");
			assertEquals(br.readLine(), "2	171570151	.	C	T");
			assertEquals(br.readLine(), "4	69964234	.	CT	CTT,CTTT,C");
			assertEquals(br.readLine(), "15	66641732	.	G	A,C,T");
			assertEquals(br.readLine(), "21	46924425	.	CGGCCCCCCA	C");
			assertEquals(br.readLine(), "X	79943569	.	T	C");
			assertEquals(br.readLine(), "2	191904021	.	G	T");
			assertEquals(br.readLine(), "3	53219680	.	G	C");
			assertEquals(br.readLine(), "2	219142023	.	G	A");
			assertEquals(br.readLine(), "1	1115548	.	G	A");
			assertEquals(br.readLine(), "21	45650009	.	T	TG, A, G");
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
	public void annotateCountTest()
	{
		try
		{
			List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud",
					"0", "-spliceSiteSize", "5");
			when(jarRunner.runJar(SnpEffAnnotator.NAME, params, new File("src/test/resources/test-edgecases.vcf")))
					.thenReturn(new File("src/test/resources/snpEffOutputCount.vcf"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Iterator<Entity> results = snpEffRepositoryAnnotator.annotateRepository(entities, new File(
				"src/test/resources/test-edgecases.vcf"));
		int size = Iterators.size(results);
		assertEquals(size, 14);
	}

	@Test
	public void annotateTest()
	{

		try
		{
			List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud",
					"0", "-spliceSiteSize", "5");
			when(jarRunner.runJar(SnpEffAnnotator.NAME, params, new File("src/test/resources/test-snpeff.vcf")))
					.thenReturn(new File("src/test/resources/snpEffOutput.vcf"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Iterator<Entity> results = snpEffRepositoryAnnotator.annotateRepository(
				Collections.singletonList(entities.get(0)), new File("src/test/resources/test-snpeff.vcf"));

		while (results.hasNext())
		{
			Entity result = results.next();
			Entity expected = new MapEntity(metaDataCanAnnotate);

			expected.set(SnpEffAnnotator.ANNOTATION, "non_coding_exon_variant");
			expected.set(SnpEffAnnotator.PUTATIVE_IMPACT, "MODIFIER");
			expected.set(SnpEffAnnotator.GENE_NAME, "DDX11L1");
			expected.set(SnpEffAnnotator.GENE_ID, "DDX11L1");
			expected.set(SnpEffAnnotator.FEATURE_TYPE, "transcript");
			expected.set(SnpEffAnnotator.FEATURE_ID, "NR_046018.2");
			expected.set(SnpEffAnnotator.TRANSCRIPT_BIOTYPE, "Noncoding");
			expected.set(SnpEffAnnotator.RANK_TOTAL, "3/3");
			expected.set(SnpEffAnnotator.HGVS_C, "n.623C>G");
			expected.set(SnpEffAnnotator.HGVS_P, "");
			expected.set(SnpEffAnnotator.C_DNA_POSITION, "");
			expected.set(SnpEffAnnotator.CDS_POSITION, "");
			expected.set(SnpEffAnnotator.PROTEIN_POSITION, "");
			expected.set(SnpEffAnnotator.DISTANCE_TO_FEATURE, "");
			expected.set(SnpEffAnnotator.ERRORS, "");
			expected.set(SnpEffAnnotator.LOF, "");
			expected.set(SnpEffAnnotator.NMD, "");

			for (AttributeMetaData attributeMetaData : snpEffRepositoryAnnotator.getOutputMetaData().get(0)
					.getAttributeParts())
			{
				assertEquals(result.get(attributeMetaData.getName()), expected.get(attributeMetaData.getName()));
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

	public static class Config
	{
		@Bean
		public Entity snpEffAnnotatorSettings()
		{
			return new MapEntity();
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

	}
}
