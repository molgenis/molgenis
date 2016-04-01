package org.molgenis.data.vcf.utils;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.LONG;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.FILTER;
import static org.molgenis.data.vcf.VcfRepository.FILTER_META;
import static org.molgenis.data.vcf.VcfRepository.ID_META;
import static org.molgenis.data.vcf.VcfRepository.INFO;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.QUAL;
import static org.molgenis.data.vcf.VcfRepository.QUAL_META;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.REF_META;
import static org.molgenis.data.vcf.VcfRepository.SAMPLES;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class VcfWriterUtilsIntegrationTest
{
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
	private static DefaultAttributeMetaData INFO_ATTR;
	private static DefaultAttributeMetaData GTC_ATTR;
	private static DefaultAttributeMetaData AC_ATTR;
	private static DefaultAttributeMetaData AN_ATTR;
	private static DefaultAttributeMetaData PUTATIVE_IMPACT_ATTR;
	private static DefaultAttributeMetaData EFFECT_ATTR;
	private static DefaultAttributeMetaData GENES_ATTR;
	private final DefaultEntityMetaData annotatedEntityMetadata = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData geneMeta = new DefaultEntityMetaData(GENES);

	DefaultEntityMetaData effectMeta = new DefaultEntityMetaData(EFFECT);
	DefaultEntityMetaData vcfMeta = new DefaultEntityMetaData("vcfMeta");
	DefaultEntityMetaData sampleEntityMeta = new DefaultEntityMetaData("vcfSampleEntity");
	public AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(CHROM,
			MolgenisFieldTypes.FieldTypeEnum.STRING);

	public AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(POS,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(REF,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(ALT,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(CHROM,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public Entity entity;
	public Entity entity1;
	public Entity entity2;
	public Entity entity3;
	public Entity entity4;
	public ArrayList<Entity> entities;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom, ROLE_ID);

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);

		entity = new MapEntity(metaDataCanAnnotate);
		entity1 = new MapEntity(metaDataCanAnnotate);
		entity2 = new MapEntity(metaDataCanAnnotate);
		entity3 = new MapEntity(metaDataCanAnnotate);
		entity4 = new MapEntity(metaDataCanAnnotate);

		metaDataCanAnnotate.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.ID, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.QUAL, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.FILTER, MolgenisFieldTypes.FieldTypeEnum.STRING));
		INFO_ATTR = new DefaultAttributeMetaData(VcfRepository.INFO,
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		AC_ATTR = new DefaultAttributeMetaData(VcfWriterUtilsIntegrationTest.AC,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		AN_ATTR = new DefaultAttributeMetaData(VcfWriterUtilsIntegrationTest.AN,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		GTC_ATTR = new DefaultAttributeMetaData(VcfWriterUtilsIntegrationTest.GTC,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		INFO_ATTR.addAttributePart(AC_ATTR);
		INFO_ATTR.addAttributePart(AN_ATTR);
		INFO_ATTR.addAttributePart(GTC_ATTR);
		metaDataCanAnnotate.addAttributeMetaData(INFO_ATTR);

		annotatedEntityMetadata.addAttributeMetaData(attributeMetaDataChrom, ROLE_ID);
		annotatedEntityMetadata.addAttributeMetaData(attributeMetaDataPos);
		annotatedEntityMetadata.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.ID, MolgenisFieldTypes.FieldTypeEnum.STRING));
		annotatedEntityMetadata.addAttributeMetaData(attributeMetaDataRef);
		annotatedEntityMetadata.addAttributeMetaData(attributeMetaDataAlt);

		annotatedEntityMetadata.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.QUAL, MolgenisFieldTypes.FieldTypeEnum.STRING));
		annotatedEntityMetadata.addAttributeMetaData(
				(new DefaultAttributeMetaData(VcfRepository.FILTER, MolgenisFieldTypes.FieldTypeEnum.STRING))
						.setDescription(
								"Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'"));
		INFO_ATTR.addAttributePart(new DefaultAttributeMetaData("ANNO", MolgenisFieldTypes.FieldTypeEnum.STRING));
		annotatedEntityMetadata.addAttributeMetaData(INFO_ATTR);

		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 10050000);
		entity1.set(VcfRepository.ID, "test21");
		entity1.set(VcfRepository.REF, "G");
		entity1.set(VcfRepository.ALT, "A");
		entity1.set(VcfRepository.QUAL, ".");
		entity1.set(VcfRepository.FILTER, "PASS");
		entity1.set(VcfWriterUtilsIntegrationTest.AC, "21");
		entity1.set(VcfWriterUtilsIntegrationTest.AN, "22");
		entity1.set(VcfWriterUtilsIntegrationTest.GTC, "0,1,10");

		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 10050001);
		entity2.set(VcfRepository.ID, "test22");
		entity2.set(VcfRepository.REF, "G");
		entity2.set(VcfRepository.ALT, "A");
		entity2.set(VcfRepository.QUAL, ".");
		entity2.set(VcfRepository.FILTER, "PASS");

		entity3.set(VcfRepository.CHROM, "1");
		entity3.set(VcfRepository.POS, 10050002);
		entity3.set(VcfRepository.ID, "test23");
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "A");
		entity3.set(VcfRepository.QUAL, ".");
		entity3.set(VcfRepository.FILTER, "PASS");

		entities = new ArrayList<>();
		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);

		geneMeta.addAttribute("id", ROLE_ID).setDataType(STRING).setDescription("Random generated ID")
				.setVisible(false);
		geneMeta.addAttribute("Gene").setDataType(STRING).setDescription("HGNC symbol");
		effectMeta.addAttribute("id", ROLE_ID).setDataType(STRING).setDescription("effect identifier")
				.setVisible(false);
		effectMeta.addAttribute(ALT).setDataType(STRING).setDescription("Alternative allele");
		effectMeta.addAttribute("ALT_GENE").setDataType(STRING).setDescription("Alternative allele and gene");
		effectMeta.addAttribute("GENE").setDataType(STRING).setDescription("Gene identifier (HGNC symbol)");
		effectMeta.addAttribute(PUTATIVE_IMPACT).setDataType(STRING).setDescription("Level of effect on the gene");
		effectMeta.addAttribute(TYPE).setDataType(STRING).setDescription("Type of mutation");

		PUTATIVE_IMPACT_ATTR = new DefaultAttributeMetaData(PUTATIVE_IMPACT, MolgenisFieldTypes.FieldTypeEnum.STRING);
		EFFECT_ATTR = new DefaultAttributeMetaData(EFFECT, MolgenisFieldTypes.FieldTypeEnum.MREF)
				.setRefEntity(effectMeta);
		GENES_ATTR = new DefaultAttributeMetaData(GENES, MolgenisFieldTypes.FieldTypeEnum.MREF).setRefEntity(geneMeta);

		String formatDpAttrName = "DP";
		String formatEcAttrName = "EC";
		String formatGtAttrName = VcfRepository.FORMAT_GT;
		String sampleIdAttrName = VcfRepository.NAME;

		sampleEntityMeta.addAttribute(sampleIdAttrName, ROLE_ID);
		sampleEntityMeta.addAttribute(formatDpAttrName);
		sampleEntityMeta.addAttribute(formatEcAttrName);
		sampleEntityMeta.addAttribute(formatGtAttrName);
	}

	// regression test for https://github.com/molgenis/molgenis/issues/3643
	@Test
	public void convertToVcfInfoGtFirst() throws MolgenisDataException, IOException
	{
		String formatDpAttrName = "DP";
		String formatEcAttrName = "EC";
		String formatGtAttrName = VcfRepository.FORMAT_GT;

		String idAttrName = "idAttr";
		String sampleIdAttrName = VcfRepository.NAME;

		DefaultEntityMetaData sampleEntityMeta = new DefaultEntityMetaData("vcfSampleEntity");
		sampleEntityMeta.addAttribute(sampleIdAttrName, ROLE_ID);
		sampleEntityMeta.addAttribute(formatDpAttrName);
		sampleEntityMeta.addAttribute(formatEcAttrName);
		sampleEntityMeta.addAttribute(formatGtAttrName);

		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("vcfEntity");
		entityMeta.addAttribute(idAttrName, ROLE_ID);
		entityMeta.addAttributeMetaData(CHROM_META);
		entityMeta.addAttributeMetaData(POS_META);
		entityMeta.addAttributeMetaData(ID_META);
		entityMeta.addAttributeMetaData(REF_META);
		entityMeta.addAttributeMetaData(ALT_META);
		entityMeta.addAttributeMetaData(QUAL_META);
		entityMeta.addAttributeMetaData(FILTER_META);
		entityMeta.addAttribute(VcfRepository.INFO).setDataType(MolgenisFieldTypes.COMPOUND);
		entityMeta.addAttribute(SAMPLES).setDataType(MolgenisFieldTypes.MREF).setRefEntity(sampleEntityMeta);

		Entity sampleEntity = new MapEntity(sampleEntityMeta);
		sampleEntity.set(sampleIdAttrName, "0");
		sampleEntity.set(formatDpAttrName, "5");
		sampleEntity.set(formatEcAttrName, "5");
		sampleEntity.set(formatGtAttrName, "1/1");

		Entity vcfEntity = new MapEntity(entityMeta);
		vcfEntity.set(idAttrName, "0");
		vcfEntity.set(CHROM, "1");
		vcfEntity.set(POS, "565286");
		vcfEntity.set(VcfRepository.ID, "rs1578391");
		vcfEntity.set(REF, "C");
		vcfEntity.set(ALT, "T");
		vcfEntity.set(QUAL, null);
		vcfEntity.set(FILTER, "flt");
		vcfEntity.set(INFO, null);
		vcfEntity.set(SAMPLES, Arrays.asList(sampleEntity));
		vcfEntity.set(formatDpAttrName, "AB_val");
		vcfEntity.set(formatEcAttrName, "AD_val");
		vcfEntity.set(formatGtAttrName, "GT_val");

		StringWriter strWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(strWriter);
		try
		{
			VcfWriterUtils.writeToVcf(vcfEntity, new ArrayList<>(), new ArrayList<>(), writer);
		}
		finally
		{
			writer.close();
		}
		assertEquals(strWriter.toString(), "1	565286	rs1578391	C	T	.	flt	.	GT:DP:EC	1/1:5:5");
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
				VcfWriterUtils.writeToVcf(entity, new ArrayList<>(), new ArrayList<>(), outputVCFWriter);
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
					Lists.newArrayList(Collections.singletonList(new DefaultAttributeMetaData("ANNO"))));

			for (Entity entity : entities)
			{
				MapEntity mapEntity = new MapEntity(entity, annotatedEntityMetadata);
				VcfWriterUtils.writeToVcf(mapEntity, new ArrayList<>(), new ArrayList<>(), outputVCFWriter);
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
		vcfMeta.addAttribute(CHROM, ROLE_ID).setDataType(STRING);
		vcfMeta.addAttribute(POS).setDataType(LONG);
		vcfMeta.addAttribute(VcfRepository.ID).setDataType(STRING);
		vcfMeta.addAttribute(REF).setDataType(STRING);
		vcfMeta.addAttribute(ALT).setDataType(STRING);
		vcfMeta.addAttribute(FILTER).setDataType(COMPOUND);
		vcfMeta.addAttribute(QUAL).setDataType(STRING);
		vcfMeta.addAttributeMetaData(INFO_ATTR);
		vcfMeta.addAttribute(EFFECT).setDataType(MREF).setRefEntity(effectMeta);
		vcfMeta.addAttribute(GENES).setDataType(MREF).setRefEntity(geneMeta);
		vcfMeta.addAttribute(SAMPLES).setDataType(MolgenisFieldTypes.MREF).setRefEntity(sampleEntityMeta);

		Entity sampleEntity1 = new MapEntity(sampleEntityMeta);
		sampleEntity1.set(VcfRepository.NAME, "0");
		sampleEntity1.set(VcfRepository.FORMAT_GT, "0/1");

		Entity sampleEntity2 = new MapEntity(sampleEntityMeta);
		sampleEntity2.set(VcfRepository.NAME, "0");
		sampleEntity2.set(VcfRepository.FORMAT_GT, "1/0");

		// 1 48554748 . T A,G 100 PASS AC=0;AN=6;GTC=1,0,10 GT 0|1
		Entity vcfEntity1 = new MapEntity(vcfMeta);
		vcfEntity1.set(CHROM, "1");
		vcfEntity1.set(POS, "48554748");
		vcfEntity1.set(VcfRepository.ID, ".");
		vcfEntity1.set(REF, "T");
		vcfEntity1.set(ALT, "A,G");
		vcfEntity1.set(QUAL, "100");
		vcfEntity1.set(FILTER, "PASS");
		vcfEntity1.set(AC, "0");
		vcfEntity1.set(AN, "6");
		vcfEntity1.set(GTC, "0,1,10");
		vcfEntity1.set(EFFECT, getEffectEntities(Arrays.asList("A", "G")));
		vcfEntity1.set(GENES, getGeneEntities(Arrays.asList("A", "G")));
		vcfEntity1.set(SAMPLES, Arrays.asList(sampleEntity1));

		// 7 50356137 . T A,C 100 PASS AC=0;AN=6;GTC=1,0,10 GT 1|0
		Entity vcfEntity2 = new MapEntity(vcfMeta);
		vcfEntity2.set(CHROM, "7");
		vcfEntity2.set(POS, "50356137");
		vcfEntity2.set(VcfRepository.ID, ".");
		vcfEntity2.set(REF, "T");
		vcfEntity2.set(ALT, "A,C");
		vcfEntity2.set(QUAL, "100");
		vcfEntity2.set(FILTER, "PASS");
		vcfEntity2.set(AC, "0");
		vcfEntity2.set(AN, "6");
		vcfEntity2.set(GTC, "1,0,10");
		vcfEntity2.set(EFFECT, getEffectEntities(Arrays.asList("A", "C")));
		vcfEntity2.set(GENES, getGeneEntities(Arrays.asList("A", "C")));
		vcfEntity2.set(SAMPLES, Arrays.asList(sampleEntity2));

		// 17 57281092 . A G,T 100 PASS AC=0;AN=6;GTC=10,1,0 GT 0|1
		Entity vcfEntity3 = new MapEntity(vcfMeta);
		vcfEntity3.set(CHROM, "17");
		vcfEntity3.set(POS, "57281092");
		vcfEntity3.set(VcfRepository.ID, ".");
		vcfEntity3.set(REF, "A");
		vcfEntity3.set(ALT, "G,T");
		vcfEntity3.set(QUAL, "100");
		vcfEntity3.set(FILTER, "PASS");
		vcfEntity3.set(AC, "0");
		vcfEntity3.set(AN, "6");
		vcfEntity3.set(GTC, "10,1,0");
		vcfEntity3.set(EFFECT, getEffectEntities(Arrays.asList("G", "T")));
		vcfEntity3.set(GENES, getGeneEntities(Arrays.asList("G", "T")));
		vcfEntity3.set(SAMPLES, Arrays.asList(sampleEntity1));

		// X 48536966 . T A,C,G 100 PASS AC=0;AN=6;GTC=0,10,1 GT 0|1
		Entity vcfEntity4 = new MapEntity(vcfMeta);
		vcfEntity4.set(CHROM, "X");
		vcfEntity4.set(POS, "48536966");
		vcfEntity4.set(VcfRepository.ID, ".");
		vcfEntity4.set(REF, "T");
		vcfEntity4.set(ALT, "A,C,G");
		vcfEntity4.set(QUAL, "100");
		vcfEntity4.set(FILTER, "PASS");
		vcfEntity4.set(AC, "0");
		vcfEntity4.set(AN, "6");
		vcfEntity4.set(GTC, "0,10,1");
		vcfEntity4.set(EFFECT, getEffectEntities(Arrays.asList("A", "C", "G")));
		vcfEntity4.set(GENES, getGeneEntities(Arrays.asList("A", "C", "G")));
		vcfEntity4.set(SAMPLES, Arrays.asList(sampleEntity1));

		// X 56032934 . G T,C,A 100 PASS AC=0;AN=3;GTC=1,2,3 GT 1|1
		Entity vcfEntity5 = new MapEntity(vcfMeta);
		vcfEntity5.set(CHROM, "X");
		vcfEntity5.set(POS, "56032934");
		vcfEntity5.set(VcfRepository.ID, ".");
		vcfEntity5.set(REF, "G");
		vcfEntity5.set(ALT, "T,C,A");
		vcfEntity5.set(QUAL, "100");
		vcfEntity5.set(FILTER, "PASS");
		vcfEntity5.set(AC, "0");
		vcfEntity5.set(AN, "3");
		vcfEntity5.set(GTC, "1,2,3");
		vcfEntity5.set(EFFECT, null);
		vcfEntity5.set(GENES, getGeneEntities(Arrays.asList("T", "C", "A")));
		vcfEntity5.set(SAMPLES, Arrays.asList(sampleEntity1));

		ArrayList<Entity> vcfEntities = newArrayList(vcfEntity1, vcfEntity2, vcfEntity3, vcfEntity4, vcfEntity5);

		try
		{
			BufferedWriter actualOutputFileWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(actualOutputFile), UTF_8));

			File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testMrefVcfWriter_input.vcf").getPath());
			File expectedVcfFile = new File(
					ResourceUtils.getFile(getClass(), "/testMrefVcfWriter_expected_output.vcf").getPath());
			VcfWriterUtils.writeVcfHeader(inputVcfFile, actualOutputFileWriter, Arrays.asList(EFFECT_ATTR, GENES_ATTR));

			for (Entity entity : vcfEntities)
			{
				MapEntity mapEntity = new MapEntity(entity, vcfMeta);
				VcfWriterUtils.writeToVcf(mapEntity, Arrays.asList(EFFECT_ATTR, GENES_ATTR), Collections.emptyList(),
						actualOutputFileWriter);
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
			Entity geneEntity = new MapEntity(geneMeta);
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
			Entity effectEntity = new MapEntity(effectMeta);
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
}