package org.molgenis.data.vcf.utils;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.molgenis.data.vcf.utils.VcfWriterUtils.writeToVcf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { VcfWriterUtilsIntegrationTest.Config.class })
public class VcfWriterUtilsIntegrationTest extends AbstractMolgenisSpringTest
{
	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	private static final Logger LOG = LoggerFactory.getLogger(VcfWriterUtilsIntegrationTest.class);
	public static final String ID = "ID";
	public static final String PUTATIVE_IMPACT = "Putative_impact";
	public static final String TYPE = "TYPE";
	public static final String VARIANT = "VARIANT";
	public static final String EFFECT = "EFFECT";
	public static final String GENES = "GENES";
	public static final String GTC = "GTC";
	public static final String AN = "AN";
	public static final String AC = "AC";
	private static Attribute INFO_ATTR;
	private static Attribute GTC_ATTR;
	private static Attribute AC_ATTR;
	private static Attribute AN_ATTR;
	private static Attribute PUTATIVE_IMPACT_ATTR;
	private static Attribute EFFECT_ATTR;
	private static Attribute GENES_ATTR;
	private EntityMetaData annotatedEntityMetadata;
	public EntityMetaData metaDataCanAnnotate;
	public EntityMetaData metaDataCantAnnotate;
	public EntityMetaData geneMeta;
	EntityMetaData effectMeta;
	EntityMetaData vcfMeta;
	EntityMetaData sampleEntityMeta;
	public Attribute attributeChrom;
	public Attribute attributePos;
	public Attribute attributeRef;
	public Attribute attributeAlt;
	public Attribute attributeCantAnnotateChrom;
	public Entity entity;
	public Entity entity1;
	public Entity entity2;
	public Entity entity3;
	public Entity entity4;
	public ArrayList<Entity> entities;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		annotatedEntityMetadata = entityMetaDataFactory.create().setName("test");
		metaDataCanAnnotate = entityMetaDataFactory.create().setName("test");
		metaDataCantAnnotate = entityMetaDataFactory.create().setName("test");
		geneMeta = entityMetaDataFactory.create().setName(GENES);

		effectMeta = entityMetaDataFactory.create().setName(EFFECT);
		vcfMeta = entityMetaDataFactory.create().setName("vcfMeta");
		sampleEntityMeta = entityMetaDataFactory.create().setName("vcfSampleEntity");
		attributeChrom = attributeFactory.create().setName(CHROM).setDataType(STRING);
		attributePos = attributeFactory.create().setName(POS).setDataType(INT);
		attributeRef = attributeFactory.create().setName(REF).setDataType(STRING);
		attributeAlt = attributeFactory.create().setName(ALT).setDataType(STRING);
		attributeCantAnnotateChrom = attributeFactory.create().setName(CHROM).setDataType(LONG);

		metaDataCanAnnotate.addAttribute(attributePos);
		metaDataCanAnnotate.addAttribute(attributeChrom);

		metaDataCantAnnotate.addAttribute(attributeCantAnnotateChrom);
		metaDataCantAnnotate.addAttribute(attributePos);
		metaDataCantAnnotate.addAttribute(attributeRef);
		metaDataCantAnnotate.addAttribute(attributeAlt);

		metaDataCanAnnotate.setIdAttribute(attributeChrom);
		metaDataCantAnnotate.setIdAttribute(attributeCantAnnotateChrom);

		entity = new DynamicEntity(metaDataCanAnnotate);
		entity1 = new DynamicEntity(metaDataCanAnnotate);
		entity2 = new DynamicEntity(metaDataCanAnnotate);
		entity3 = new DynamicEntity(metaDataCanAnnotate);
		entity4 = new DynamicEntity(metaDataCanAnnotate);

		metaDataCanAnnotate.addAttribute(attributeFactory.create().setName(ID).setDataType(STRING));
		metaDataCanAnnotate.addAttribute(attributeRef);
		metaDataCanAnnotate.addAttribute(attributeAlt);
		metaDataCanAnnotate.addAttribute(attributeFactory.create().setName(QUAL).setDataType(STRING));
		metaDataCanAnnotate.addAttribute(attributeFactory.create().setName(FILTER).setDataType(STRING));
		INFO_ATTR = attributeFactory.create().setName(INFO).setDataType(COMPOUND);
		AC_ATTR = attributeFactory.create().setName(VcfWriterUtilsIntegrationTest.AC).setDataType(STRING);
		AN_ATTR = attributeFactory.create().setName(VcfWriterUtilsIntegrationTest.AN).setDataType(STRING);
		GTC_ATTR = attributeFactory.create().setName(VcfWriterUtilsIntegrationTest.GTC).setDataType(STRING);
		INFO_ATTR.addAttributePart(AC_ATTR);
		INFO_ATTR.addAttributePart(AN_ATTR);
		INFO_ATTR.addAttributePart(GTC_ATTR);
		metaDataCanAnnotate.addAttribute(INFO_ATTR);

		annotatedEntityMetadata.addAttribute(attributeChrom);
		annotatedEntityMetadata.setIdAttribute(attributeChrom);
		annotatedEntityMetadata.addAttribute(attributePos);
		annotatedEntityMetadata.addAttribute(attributeFactory.create().setName(ID).setDataType(STRING));
		annotatedEntityMetadata.addAttribute(attributeRef);
		annotatedEntityMetadata.addAttribute(attributeAlt);

		annotatedEntityMetadata.addAttribute(attributeFactory.create().setName(QUAL).setDataType(STRING));
		annotatedEntityMetadata.addAttribute(attributeFactory.create().setName(FILTER).setDataType(STRING))
				.setDescription("Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'");
		INFO_ATTR.addAttributePart(attributeFactory.create().setName("ANNO").setDataType(STRING));
		annotatedEntityMetadata.addAttribute(INFO_ATTR);

		entity1.set(CHROM, "1");
		entity1.set(POS, 10050000);
		entity1.set(ID, "test21");
		entity1.set(REF, "G");
		entity1.set(ALT, "A");
		entity1.set(QUAL, ".");
		entity1.set(FILTER, "PASS");
		entity1.set(VcfWriterUtilsIntegrationTest.AC, "21");
		entity1.set(VcfWriterUtilsIntegrationTest.AN, "22");
		entity1.set(VcfWriterUtilsIntegrationTest.GTC, "0,1,10");

		entity2.set(CHROM, "1");
		entity2.set(POS, 10050001);
		entity2.set(ID, "test22");
		entity2.set(REF, "G");
		entity2.set(ALT, "A");
		entity2.set(QUAL, ".");
		entity2.set(FILTER, "PASS");

		entity3.set(CHROM, "1");
		entity3.set(POS, 10050002);
		entity3.set(ID, "test23");
		entity3.set(REF, "G");
		entity3.set(ALT, "A");
		entity3.set(QUAL, ".");
		entity3.set(FILTER, "PASS");

		entities = new ArrayList<>();
		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);

		Attribute geneId = attributeFactory.create().setName("id").setDataType(STRING)
				.setDescription("Random generated ID").setVisible(false);
		geneMeta.addAttribute(geneId);
		geneMeta.setIdAttribute(geneId);
		geneMeta.addAttribute(
				attributeFactory.create().setName("Gene").setDataType(STRING).setDescription("HGNC symbol"));
		Attribute id = attributeFactory.create().setName("id").setDataType(STRING)
				.setDescription("effect identifier").setVisible(false);
		effectMeta.addAttribute(id);
		effectMeta.setIdAttribute(id);
		effectMeta.addAttribute(attributeFactory.create().setName(ALT).setDataType(STRING)
				.setDescription("Alternative allele"));
		effectMeta.addAttribute(attributeFactory.create().setName("ALT_GENE").setDataType(STRING)
				.setDescription("Alternative allele and gene"));
		effectMeta.addAttribute(attributeFactory.create().setName("GENE").setDataType(STRING)
				.setDescription("Gene identifier (HGNC symbol)"));
		effectMeta.addAttribute(attributeFactory.create().setName(PUTATIVE_IMPACT).setDataType(STRING)
				.setDescription("Level of effect on the gene"));
		effectMeta.addAttribute(
				attributeFactory.create().setName(TYPE).setDataType(STRING).setDescription("Type of mutation"));

		PUTATIVE_IMPACT_ATTR = attributeFactory.create().setName(PUTATIVE_IMPACT).setDataType(STRING);
		EFFECT_ATTR = attributeFactory.create().setName(EFFECT).setDataType(MREF).setRefEntity(effectMeta);
		GENES_ATTR = attributeFactory.create().setName(GENES).setDataType(MREF).setRefEntity(geneMeta);

		String formatDpAttrName = "DP";
		String formatEcAttrName = "EC";
		String formatGtAttrName = FORMAT_GT;
		String sampleIdAttrName = VcfRepository.NAME;

		Attribute sampleId = attributeFactory.create().setName(sampleIdAttrName);
		sampleEntityMeta.addAttribute(sampleId);
		sampleEntityMeta.setIdAttribute(sampleId);
		sampleEntityMeta.addAttribute(attributeFactory.create().setName(formatDpAttrName));
		sampleEntityMeta.addAttribute(attributeFactory.create().setName(formatEcAttrName));
		sampleEntityMeta.addAttribute(attributeFactory.create().setName(formatGtAttrName));
	}

	// regression test for https://github.com/molgenis/molgenis/issues/3643
	@Test
	public void convertToVcfInfoGtFirst() throws MolgenisDataException, IOException
	{
		String formatDpAttrName = "DP";
		String formatEcAttrName = "EC";
		String formatGtAttrName = FORMAT_GT;

		String idAttrName = "idAttr";
		String sampleIdAttrName = VcfRepository.NAME;

		EntityMetaData sampleEntityMeta = entityMetaDataFactory.create().setName("vcfSampleEntity");
		Attribute sampleId = attributeFactory.create().setName(sampleIdAttrName);
		sampleEntityMeta.addAttribute(sampleId);
		sampleEntityMeta.setIdAttribute(sampleId);
		sampleEntityMeta.addAttribute(attributeFactory.create().setName(formatDpAttrName));
		sampleEntityMeta.addAttribute(attributeFactory.create().setName(formatEcAttrName));
		sampleEntityMeta.addAttribute(attributeFactory.create().setName(formatGtAttrName));

		EntityMetaData entityMeta = entityMetaDataFactory.create().setName("vcfEntity");
		Attribute id = attributeFactory.create().setName(idAttrName);
		entityMeta.addAttribute(id);
		entityMeta.setIdAttribute(id);
		entityMeta.addAttribute(vcfAttributes.getChromAttribute());
		entityMeta.addAttribute(vcfAttributes.getPosAttribute());
		entityMeta.addAttribute(vcfAttributes.getIdAttribute());
		entityMeta.addAttribute(vcfAttributes.getRefAttribute());
		entityMeta.addAttribute(vcfAttributes.getAltAttribute());
		entityMeta.addAttribute(vcfAttributes.getQualAttribute());
		entityMeta.addAttribute(vcfAttributes.getFilterAttribute());
		entityMeta.addAttribute(attributeFactory.create().setName(INFO).setDataType(COMPOUND));
		entityMeta.addAttribute(
				attributeFactory.create().setName(SAMPLES).setDataType(MREF).setRefEntity(sampleEntityMeta));

		Entity sampleEntity = new DynamicEntity(sampleEntityMeta);
		sampleEntity.set(sampleIdAttrName, "0");
		sampleEntity.set(formatDpAttrName, "5");
		sampleEntity.set(formatEcAttrName, "5");
		sampleEntity.set(formatGtAttrName, "1/1");

		Entity vcfEntity = new DynamicEntity(entityMeta);
		vcfEntity.set(idAttrName, "0");
		vcfEntity.set(CHROM, "1");
		vcfEntity.set(POS, 565286);
		vcfEntity.set(ID, "rs1578391");
		vcfEntity.set(REF, "C");
		vcfEntity.set(ALT, "T");
		vcfEntity.set(QUAL, null);
		vcfEntity.set(FILTER, "flt");
		vcfEntity.set(INFO, null);
		vcfEntity.set(SAMPLES, newArrayList(sampleEntity));

		StringWriter strWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(strWriter);
		try
		{
			writeToVcf(vcfEntity, newArrayList(), newArrayList(), writer);
		}
		finally
		{
			writer.close();
		}
		assertEquals(strWriter.toString(),
				"1	565286	rs1578391	C	T	.	flt	idAttr=0;	GT:DP:EC	1/1:5:5");
	}

	@Test
	public void vcfWriterRoundtripTest() throws IOException, MolgenisInvalidFormatException
	{
		final File outputVCFFile = File.createTempFile("output", ".vcf");
		try
		{
			BufferedWriter outputVCFWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputVCFFile), UTF_8));

			File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testWriter.vcf").getPath());

			VcfWriterUtils.writeVcfHeader(inputVcfFile, outputVCFWriter, Collections.emptyList());

			for (Entity entity : entities)
			{
				writeToVcf(entity, new ArrayList<>(), new ArrayList<>(), outputVCFWriter);
				outputVCFWriter.newLine();
			}
			outputVCFWriter.close();

			assertTrue(FileUtils.contentEqualsIgnoreEOL(inputVcfFile, outputVCFFile, "UTF8"));
		}
		finally
		{
			boolean outputVCFFileIsDeleted = outputVCFFile.delete();
			LOG.info("Result test file named: " + outputVCFFile.getName() + " is "
					+ (outputVCFFileIsDeleted ? "" : "not ") + "deleted");
		}
	}

	@Test
	public void vcfWriterAnnotateTest() throws IOException, MolgenisInvalidFormatException
	{
		entity1.set("ANNO", "TEST_test21");
		entity2.set("ANNO", "TEST_test22");
		final File outputVCFFile = File.createTempFile("output", ".vcf");
		try
		{
			BufferedWriter outputVCFWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputVCFFile), UTF_8));

			File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testWriter.vcf").getPath());
			File resultVCFWriter = new File(ResourceUtils.getFile(getClass(), "/result_vcfWriter.vcf").getPath());

			VcfWriterUtils.writeVcfHeader(inputVcfFile, outputVCFWriter,
					newArrayList(Collections.singletonList(attributeFactory.create().setName("ANNO"))));

			for (Entity entity : entities)
			{
				Entity mapEntity = new DynamicEntity(annotatedEntityMetadata);
				mapEntity.set(entity);
				writeToVcf(mapEntity, new ArrayList<>(), new ArrayList<>(), outputVCFWriter);
				outputVCFWriter.newLine();
			}
			outputVCFWriter.close();
			assertTrue(FileUtils.contentEqualsIgnoreEOL(resultVCFWriter, outputVCFFile, "UTF8"));
		}
		finally
		{
			boolean outputVCFFileIsDeleted = outputVCFFile.delete();
			LOG.info("Result test file named: " + outputVCFFile.getName() + " is "
					+ (outputVCFFileIsDeleted ? "" : "not ") + "deleted");
		}
	}

	@Test
	public void vcfWriteMrefTest() throws IOException, MolgenisInvalidFormatException
	{
		final File actualOutputFile = File.createTempFile("output", ".vcf");

		INFO_ATTR.addAttributePart(AC_ATTR);
		INFO_ATTR.addAttributePart(AN_ATTR);
		INFO_ATTR.addAttributePart(GTC_ATTR);

		Attribute attr = attributeFactory.create().setName(CHROM).setDataType(STRING);
		vcfMeta.addAttribute(attr);
		vcfMeta.setIdAttribute(attr);
		vcfMeta.addAttribute(attributeFactory.create().setName(POS).setDataType(INT));
		vcfMeta.addAttribute(attributeFactory.create().setName(ID).setDataType(STRING));
		vcfMeta.addAttribute(attributeFactory.create().setName(REF).setDataType(STRING));
		vcfMeta.addAttribute(attributeFactory.create().setName(ALT).setDataType(STRING));
		vcfMeta.addAttribute(attributeFactory.create().setName(FILTER).setDataType(STRING));
		vcfMeta.addAttribute(attributeFactory.create().setName(QUAL).setDataType(STRING));
		vcfMeta.addAttribute(INFO_ATTR);
		vcfMeta.addAttribute(
				attributeFactory.create().setName(EFFECT).setDataType(MREF).setRefEntity(effectMeta));
		vcfMeta.addAttribute(attributeFactory.create().setName(GENES).setDataType(MREF).setRefEntity(geneMeta));
		vcfMeta.addAttribute(
				attributeFactory.create().setName(SAMPLES).setDataType(MREF).setRefEntity(sampleEntityMeta));

		Entity sampleEntity1 = new DynamicEntity(sampleEntityMeta);
		sampleEntity1.set(VcfRepository.NAME, "0");
		sampleEntity1.set(FORMAT_GT, "0/1");

		Entity sampleEntity2 = new DynamicEntity(sampleEntityMeta);
		sampleEntity2.set(VcfRepository.NAME, "0");
		sampleEntity2.set(FORMAT_GT, "1/0");

		// 1 48554748 . T A,G 100 PASS AC=0;AN=6;GTC=1,0,10 GT 0|1
		Entity vcfEntity1 = new DynamicEntity(vcfMeta);
		vcfEntity1.set(CHROM, "1");
		vcfEntity1.set(POS, 48554748);
		vcfEntity1.set(ID, ".");
		vcfEntity1.set(REF, "T");
		vcfEntity1.set(ALT, "A,G");
		vcfEntity1.set(QUAL, "100");
		vcfEntity1.set(FILTER, "PASS");
		vcfEntity1.set(AC, "0");
		vcfEntity1.set(AN, "6");
		vcfEntity1.set(GTC, "0,1,10");
		vcfEntity1.set(EFFECT, getEffectEntities(asList("A", "G")));
		vcfEntity1.set(GENES, getGeneEntities(asList("A", "G")));
		vcfEntity1.set(SAMPLES, asList(sampleEntity1));

		// 7 50356137 . T A,C 100 PASS AC=0;AN=6;GTC=1,0,10 GT 1|0
		Entity vcfEntity2 = new DynamicEntity(vcfMeta);
		vcfEntity2.set(CHROM, "7");
		vcfEntity2.set(POS, 50356137);
		vcfEntity2.set(ID, ".");
		vcfEntity2.set(REF, "T");
		vcfEntity2.set(ALT, "A,C");
		vcfEntity2.set(QUAL, "100");
		vcfEntity2.set(FILTER, "PASS");
		vcfEntity2.set(AC, "0");
		vcfEntity2.set(AN, "6");
		vcfEntity2.set(GTC, "1,0,10");
		vcfEntity2.set(EFFECT, getEffectEntities(asList("A", "C")));
		vcfEntity2.set(GENES, getGeneEntities(asList("A", "C")));
		vcfEntity2.set(SAMPLES, asList(sampleEntity2));

		// 17 57281092 . A G,T 100 PASS AC=0;AN=6;GTC=10,1,0 GT 0|1
		Entity vcfEntity3 = new DynamicEntity(vcfMeta);
		vcfEntity3.set(CHROM, "17");
		vcfEntity3.set(POS, 57281092);
		vcfEntity3.set(ID, ".");
		vcfEntity3.set(REF, "A");
		vcfEntity3.set(ALT, "G,T");
		vcfEntity3.set(QUAL, "100");
		vcfEntity3.set(FILTER, "PASS");
		vcfEntity3.set(AC, "0");
		vcfEntity3.set(AN, "6");
		vcfEntity3.set(GTC, "10,1,0");
		vcfEntity3.set(EFFECT, getEffectEntities(asList("G", "T")));
		vcfEntity3.set(GENES, getGeneEntities(asList("G", "T")));
		vcfEntity3.set(SAMPLES, asList(sampleEntity1));

		// X 48536966 . T A,C,G 100 PASS AC=0;AN=6;GTC=0,10,1 GT 0|1
		Entity vcfEntity4 = new DynamicEntity(vcfMeta);
		vcfEntity4.set(CHROM, "X");
		vcfEntity4.set(POS, 48536966);
		vcfEntity4.set(ID, ".");
		vcfEntity4.set(REF, "T");
		vcfEntity4.set(ALT, "A,C,G");
		vcfEntity4.set(QUAL, "100");
		vcfEntity4.set(FILTER, "PASS");
		vcfEntity4.set(AC, "0");
		vcfEntity4.set(AN, "6");
		vcfEntity4.set(GTC, "0,10,1");
		vcfEntity4.set(EFFECT, getEffectEntities(asList("A", "C", "G")));
		vcfEntity4.set(GENES, getGeneEntities(asList("A", "C", "G")));
		vcfEntity4.set(SAMPLES, asList(sampleEntity1));

		// X 56032934 . G T,C,A 100 PASS AC=0;AN=3;GTC=1,2,3 GT 1|1
		Entity vcfEntity5 = new DynamicEntity(vcfMeta);
		vcfEntity5.set(CHROM, "X");
		vcfEntity5.set(POS, 56032934);
		vcfEntity5.set(ID, ".");
		vcfEntity5.set(REF, "G");
		vcfEntity5.set(ALT, "T,C,A");
		vcfEntity5.set(QUAL, "100");
		vcfEntity5.set(FILTER, "PASS");
		vcfEntity5.set(AC, "0");
		vcfEntity5.set(AN, "3");
		vcfEntity5.set(GTC, "1,2,3");
		vcfEntity5.set(EFFECT, null);
		vcfEntity5.set(GENES, getGeneEntities(asList("T", "C", "A")));
		vcfEntity5.set(SAMPLES, asList(sampleEntity1));

		ArrayList<Entity> vcfEntities = newArrayList(vcfEntity1, vcfEntity2, vcfEntity3, vcfEntity4, vcfEntity5);

		try
		{
			BufferedWriter actualOutputFileWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(actualOutputFile), UTF_8));

			File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testMrefVcfWriter_input.vcf").getPath());
			File expectedVcfFile = new File(
					ResourceUtils.getFile(getClass(), "/testMrefVcfWriter_expected_output.vcf").getPath());
			VcfWriterUtils.writeVcfHeader(inputVcfFile, actualOutputFileWriter, asList(EFFECT_ATTR, GENES_ATTR));

			for (Entity entity : vcfEntities)
			{
				Entity mapEntity = new DynamicEntity(vcfMeta);
				mapEntity.set(entity);
				writeToVcf(mapEntity, asList(EFFECT_ATTR, GENES_ATTR), Collections.emptyList(), actualOutputFileWriter);
				actualOutputFileWriter.newLine();
			}
			actualOutputFileWriter.close();
			assertTrue(FileUtils.contentEqualsIgnoreEOL(expectedVcfFile, actualOutputFile, "UTF8"));
		}
		finally
		{
			boolean outputVCFFileIsDeleted = actualOutputFile.delete();
			LOG.info("Result test file named: " + actualOutputFile.getName() + " is "
					+ (outputVCFFileIsDeleted ? "" : "not ") + "deleted");
		}
	}

	private List<Entity> getGeneEntities(List<String> altAlleles)
	{
		List<Entity> geneEntities = newArrayList();
		for (String allele : altAlleles)
		{
			Entity geneEntity = new DynamicEntity(geneMeta);
			geneEntity.set("id", UUID.randomUUID().toString());
			switch (allele)
			{
				case "A":
					geneEntity.set("Gene", "BRCA1");
					break;
				case "T":
					geneEntity.set("Gene", "COL7A1");
					break;
				case "C":
					geneEntity.set("Gene", "AIP");
					break;
				case "G":
					geneEntity.set("Gene", "CHD7");
					break;
				default:
					geneEntity.set("Gene", "NONE");
					break;
			}

			geneEntities.add(geneEntity);
		}
		return geneEntities;
	}

	private List<Entity> getEffectEntities(List<String> altAlleles)
	{
		List<Entity> effectEntities = newArrayList();
		for (int i = 0; i < altAlleles.size(); i++)
		{
			String gene = "BRCA" + (i + 1);
			Entity effectEntity = new DynamicEntity(effectMeta);
			String altAllele = altAlleles.get(i);
			effectEntity.set("id", "eff" + (i + 1));
			effectEntity.set(ALT, altAllele);
			effectEntity.set("ALT_GENE", altAllele + "_" + gene);
			effectEntity.set("GENE", gene);
			effectEntity.set(PUTATIVE_IMPACT, "HIGH");
			effectEntity.set(TYPE, "STOP_GAIN");

			effectEntities.add(effectEntity);
		}

		return effectEntities;
	}

	@Configuration
	@Import(VcfAttributes.class)
	public static class Config
	{
	}
}