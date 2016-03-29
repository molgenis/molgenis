package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.ANNOTATION;
import static org.molgenis.data.support.VcfEffectsMetaData.CDS_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.C_DNA_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.DISTANCE_TO_FEATURE;
import static org.molgenis.data.support.VcfEffectsMetaData.ERRORS;
import static org.molgenis.data.support.VcfEffectsMetaData.FEATURE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.FEATURE_TYPE;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE_NAME;
import static org.molgenis.data.support.VcfEffectsMetaData.HGVS_C;
import static org.molgenis.data.support.VcfEffectsMetaData.HGVS_P;
import static org.molgenis.data.support.VcfEffectsMetaData.PROTEIN_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.PUTATIVE_IMPACT;
import static org.molgenis.data.support.VcfEffectsMetaData.RANK_TOTAL;
import static org.molgenis.data.support.VcfEffectsMetaData.TRANSCRIPT_BIOTYPE;
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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.annotation.utils.JarRunner;
import org.molgenis.data.annotation.utils.JarRunnerImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.data.support.VcfEffectsMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;

public class SnpEffRunnerTest
{
	private final ArrayList<Entity> entities = new ArrayList<>();;
	private DefaultEntityMetaData metaDataCanAnnotate;

	private VcfEffectsMetaData effectsEMD;

	@InjectMocks
	private SnpEffRunner snpEffRunner;

	@Mock
	private JarRunner jarRunner;

	@Mock
	private Entity snpEffAnnotatorSettings;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		jarRunner = mock(JarRunnerImpl.class);

		IdGenerator idGenerator = new UuidGenerator();

		snpEffRunner = new SnpEffRunner(jarRunner, snpEffAnnotatorSettings, idGenerator);

		metaDataCanAnnotate = new DefaultEntityMetaData("test");
		AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS,
				MolgenisFieldTypes.FieldTypeEnum.LONG);
		AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom, ROLE_ID);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);

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

		effectsEMD = new VcfEffectsMetaData(metaDataCanAnnotate);
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

		Iterator<Entity> results = snpEffRunner.getSnpEffects(entities.iterator(),
				new File("src/test/resources/test-edgecases.vcf"));
		assertEquals(Iterators.size(results), 24);
	}

	@Test
	public void getSnpEffectsTest()
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
		Iterator<Entity> results = snpEffRunner.getSnpEffects(Collections.singletonList(entities.get(0)).iterator(),
				new File("src/test/resources/test-snpeff.vcf"));

		results.forEachRemaining(result -> {
			Entity expected = new MapEntity(effectsEMD);

			// copy id from result because it's auto generated
			expected.set(VcfEffectsMetaData.ID, result.getIdValue());
			expected.set(VcfEffectsMetaData.ALT, result.get(VcfEffectsMetaData.ALT));
			expected.set(VcfEffectsMetaData.GENE, "DDX11L1");
			expected.set(VcfEffectsMetaData.VARIANT, entities.get(0));

			expected.set(ANNOTATION, "non_coding_exon_variant");
			expected.set(PUTATIVE_IMPACT, "MODIFIER");
			expected.set(GENE_NAME, "DDX11L1");
			expected.set(GENE_ID, "DDX11L1");
			expected.set(FEATURE_TYPE, "transcript");
			expected.set(FEATURE_ID, "NR_046018.2");
			expected.set(TRANSCRIPT_BIOTYPE, "Noncoding");
			expected.set(RANK_TOTAL, "3/3");
			expected.set(HGVS_C, "n.623C>G");
			expected.set(HGVS_P, "");
			expected.set(C_DNA_POSITION, "");
			expected.set(CDS_POSITION, "");
			expected.set(PROTEIN_POSITION, "");
			expected.set(DISTANCE_TO_FEATURE, "");
			expected.set(ERRORS, "");

			for (AttributeMetaData attributeMetaData : effectsEMD.getAtomicAttributes())
			{
				System.out.println(attributeMetaData.getName());
				assertEquals(result.get(attributeMetaData.getName()), expected.get(attributeMetaData.getName()));
			}
		});
	}

	@Test
	public void getInputVcfFileTest()
	{
		BufferedReader br = null;
		try
		{
			File file = snpEffRunner.getInputVcfFile(entities.iterator());
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));

			assertEquals(br.readLine(), "1 13380 . C G".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 13980 . T C".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 78383467 . G A".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 231094050 . GAA G,GAAA,GA".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 171570151 . C T".replace(" ", "\t"));
			assertEquals(br.readLine(), "4 69964234 . CT CTT,CTTT,C".replace(" ", "\t"));
			assertEquals(br.readLine(), "15 66641732 . G A,C,T".replace(" ", "\t"));
			assertEquals(br.readLine(), "21 46924425 . CGGCCCCCCA C".replace(" ", "\t"));
			assertEquals(br.readLine(), "X 79943569 . T C".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 191904021 . G T".replace(" ", "\t"));
			assertEquals(br.readLine(), "3 53219680 . G C".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 219142023 . G A".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 1115548 . G A".replace(" ", "\t"));
			assertEquals(br.readLine(), "21\t45650009\t.\tT\tTG, A, G");
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

}
