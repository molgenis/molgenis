package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.LONG;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.VcfAttributes.ALT;
import static org.molgenis.data.vcf.VcfAttributes.CHROM;
import static org.molgenis.data.vcf.VcfAttributes.POS;
import static org.molgenis.data.vcf.VcfAttributes.REF;
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

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.utils.JarRunner;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfAttributes;
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
	private final ArrayList<Entity> entities = new ArrayList<>();
	private EntityMetaData metaDataCanAnnotate;
	private SnpEffAnnotator.SnpEffRepositoryAnnotator snpEffRepositoryAnnotator;
	private JarRunner jarRunner;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		jarRunner = mock(JarRunner.class);

		snpEffRepositoryAnnotator = new SnpEffAnnotator.SnpEffRepositoryAnnotator(new MapEntity(), jarRunner);

		metaDataCanAnnotate = new EntityMetaData("test");
		AttributeMetaData attributeMetaDataChrom = new AttributeMetaData(CHROM,
				STRING);
		AttributeMetaData attributeMetaDataPos = new AttributeMetaData(POS,
				LONG);
		AttributeMetaData attributeMetaDataRef = new AttributeMetaData(REF,
				STRING);
		AttributeMetaData attributeMetaDataAlt = new AttributeMetaData(ALT,
				STRING);
		metaDataCanAnnotate.addAttribute(attributeMetaDataChrom, ROLE_ID);
		metaDataCanAnnotate.addAttribute(attributeMetaDataPos);
		metaDataCanAnnotate.addAttribute(attributeMetaDataRef);
		metaDataCanAnnotate.addAttribute(attributeMetaDataAlt);

		Entity entity1 = new MapEntity(metaDataCanAnnotate);
		entity1.set(VcfAttributes.CHROM, "1");
		entity1.set(VcfAttributes.POS, 13380);
		entity1.set(VcfAttributes.REF, "C");
		entity1.set(VcfAttributes.ALT, "G");

		Entity entity2 = new MapEntity(metaDataCanAnnotate);
		entity2.set(VcfAttributes.CHROM, "1");
		entity2.set(VcfAttributes.POS, 13980);
		entity2.set(VcfAttributes.REF, "T");
		entity2.set(VcfAttributes.ALT, "C");

		Entity entity3 = new MapEntity(metaDataCanAnnotate);
		entity3.set(VcfAttributes.CHROM, "1");
		entity3.set(VcfAttributes.POS, 78383467);
		entity3.set(VcfAttributes.REF, "G");
		entity3.set(VcfAttributes.ALT, "A");

		Entity entity4 = new MapEntity(metaDataCanAnnotate);
		entity4.set(VcfAttributes.CHROM, "1");
		entity4.set(VcfAttributes.POS, 231094050);
		entity4.set(VcfAttributes.REF, "GAA");
		entity4.set(VcfAttributes.ALT, "G,GAAA,GA");

		Entity entity5 = new MapEntity(metaDataCanAnnotate);
		entity5.set(VcfAttributes.CHROM, "2");
		entity5.set(VcfAttributes.POS, 171570151);
		entity5.set(VcfAttributes.REF, "C");
		entity5.set(VcfAttributes.ALT, "T");

		Entity entity6 = new MapEntity(metaDataCanAnnotate);
		entity6.set(VcfAttributes.CHROM, "4");
		entity6.set(VcfAttributes.POS, 69964234);
		entity6.set(VcfAttributes.REF, "CT");
		entity6.set(VcfAttributes.ALT, "CTT,CTTT,C");

		Entity entity7 = new MapEntity(metaDataCanAnnotate);
		entity7.set(VcfAttributes.CHROM, "15");
		entity7.set(VcfAttributes.POS, 66641732);
		entity7.set(VcfAttributes.REF, "G");
		entity7.set(VcfAttributes.ALT, "A,C,T");

		Entity entity8 = new MapEntity(metaDataCanAnnotate);
		entity8.set(VcfAttributes.CHROM, "21");
		entity8.set(VcfAttributes.POS, 46924425);
		entity8.set(VcfAttributes.REF, "CGGCCCCCCA");
		entity8.set(VcfAttributes.ALT, "C");

		Entity entity9 = new MapEntity(metaDataCanAnnotate);
		entity9.set(VcfAttributes.CHROM, "X");
		entity9.set(VcfAttributes.POS, 79943569);
		entity9.set(VcfAttributes.REF, "T");
		entity9.set(VcfAttributes.ALT, "C");

		Entity entity10 = new MapEntity(metaDataCanAnnotate);
		entity10.set(VcfAttributes.CHROM, "2");
		entity10.set(VcfAttributes.POS, 191904021);
		entity10.set(VcfAttributes.REF, "G");
		entity10.set(VcfAttributes.ALT, "T");

		Entity entity11 = new MapEntity(metaDataCanAnnotate);
		entity11.set(VcfAttributes.CHROM, "3");
		entity11.set(VcfAttributes.POS, 53219680);
		entity11.set(VcfAttributes.REF, "G");
		entity11.set(VcfAttributes.ALT, "C");

		Entity entity12 = new MapEntity(metaDataCanAnnotate);
		entity12.set(VcfAttributes.CHROM, "2");
		entity12.set(VcfAttributes.POS, 219142023);
		entity12.set(VcfAttributes.REF, "G");
		entity12.set(VcfAttributes.ALT, "A");

		Entity entity13 = new MapEntity(metaDataCanAnnotate);
		entity13.set(VcfAttributes.CHROM, "1");
		entity13.set(VcfAttributes.POS, 1115548);
		entity13.set(VcfAttributes.REF, "G");
		entity13.set(VcfAttributes.ALT, "A");

		Entity entity14 = new MapEntity(metaDataCanAnnotate);
		entity14.set(VcfAttributes.CHROM, "21");
		entity14.set(VcfAttributes.POS, 45650009);
		entity14.set(VcfAttributes.REF, "T");
		entity14.set(VcfAttributes.ALT, "TG, A, G");

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

		Iterator<Entity> results = snpEffRepositoryAnnotator.annotateRepository(entities,
				new File("src/test/resources/test-edgecases.vcf"));
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
