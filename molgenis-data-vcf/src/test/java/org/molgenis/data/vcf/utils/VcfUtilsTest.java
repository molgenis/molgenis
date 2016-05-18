package org.molgenis.data.vcf.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.LONG;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.FILTER;
import static org.molgenis.data.vcf.VcfRepository.FILTER_META;
import static org.molgenis.data.vcf.VcfRepository.ID;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@Test
public class VcfUtilsTest
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfUtilsTest.class);
	private final EntityMetaData annotatedEntityMetadata = new EntityMetaDataImpl("test");
	public EntityMetaData metaDataCanAnnotate = new EntityMetaDataImpl("test");
	public EntityMetaData metaDataCantAnnotate = new EntityMetaDataImpl("test");

	public AttributeMetaData attributeMetaDataChrom = new AttributeMetaData(CHROM,
			STRING);
	public AttributeMetaData attributeMetaDataPos = new AttributeMetaData(POS, LONG);
	public AttributeMetaData attributeMetaDataRef = new AttributeMetaData(REF, STRING);
	public AttributeMetaData attributeMetaDataAlt = new AttributeMetaData(ALT, STRING);
	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new AttributeMetaData(CHROM,
			LONG);
	public ArrayList<Entity> input = new ArrayList<Entity>();
	public Entity entity;
	public Entity entity1;
	public Entity entity2;
	public Entity entity3;
	public Entity entity4;

	public ArrayList<Entity> entities;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		/*
		 * 1 10050000 test21 G A . PASS AC=21;AN=22;GTC=0,1,10 1 10050001 test22 G A . PASS AC=22;AN=23;GTC=1,2,11 1
		 * 10050002 test23 G A . PASS AC=23;AN=24;GTC=2,3,12
		 */
		metaDataCanAnnotate.addAttribute(attributeMetaDataChrom, ROLE_ID);
		metaDataCanAnnotate.addAttribute(attributeMetaDataPos);
		metaDataCanAnnotate.addAttribute(attributeMetaDataRef);
		metaDataCanAnnotate.addAttribute(attributeMetaDataAlt);

		metaDataCantAnnotate.addAttribute(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttribute(attributeMetaDataPos);
		metaDataCantAnnotate.addAttribute(attributeMetaDataRef);
		metaDataCantAnnotate.addAttribute(attributeMetaDataAlt);

		entity = new MapEntity(metaDataCanAnnotate);
		entity1 = new MapEntity(metaDataCanAnnotate);
		entity2 = new MapEntity(metaDataCanAnnotate);
		entity3 = new MapEntity(metaDataCanAnnotate);
		entity4 = new MapEntity(metaDataCanAnnotate);

		metaDataCanAnnotate.addAttribute(
				new AttributeMetaData(ID, STRING));
		metaDataCanAnnotate.addAttribute(
				new AttributeMetaData(QUAL, STRING));
		metaDataCanAnnotate.addAttribute(
				new AttributeMetaData(FILTER, STRING));
		AttributeMetaData INFO = new AttributeMetaData("INFO", COMPOUND);
		AttributeMetaData AC = new AttributeMetaData("AC", STRING);
		AttributeMetaData AN = new AttributeMetaData("AN", STRING);
		AttributeMetaData GTC = new AttributeMetaData("GTC", STRING);
		INFO.addAttributePart(AC);
		INFO.addAttributePart(AN);
		INFO.addAttributePart(GTC);
		metaDataCanAnnotate.addAttribute(INFO);

		annotatedEntityMetadata.addAttribute(attributeMetaDataChrom, ROLE_ID);
		annotatedEntityMetadata.addAttribute(attributeMetaDataPos);
		annotatedEntityMetadata.addAttribute(attributeMetaDataRef);
		annotatedEntityMetadata.addAttribute(attributeMetaDataAlt);

		annotatedEntityMetadata.addAttribute(
				new AttributeMetaData(ID, STRING));
		annotatedEntityMetadata.addAttribute(
				new AttributeMetaData(QUAL, STRING));
		annotatedEntityMetadata.addAttribute(
				(new AttributeMetaData(FILTER, STRING))
						.setDescription(
								"Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'"));
		INFO.addAttributePart(new AttributeMetaData("ANNO", STRING));
		annotatedEntityMetadata.addAttribute(INFO);

		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 10050000);
		entity1.set(VcfRepository.ID, "test21");
		entity1.set(VcfRepository.REF, "G");
		entity1.set(VcfRepository.ALT, "A");
		entity1.set(VcfRepository.QUAL, ".");
		entity1.set(VcfRepository.FILTER, "PASS");
		entity1.set("AC", "21");
		entity1.set("AN", "22");
		entity1.set("GTC", "0,1,10");

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

		EntityMetaData sampleEntityMeta = new EntityMetaDataImpl("vcfSampleEntity");
		sampleEntityMeta.addAttribute(sampleIdAttrName, ROLE_ID);
		sampleEntityMeta.addAttribute(formatDpAttrName);
		sampleEntityMeta.addAttribute(formatEcAttrName);
		sampleEntityMeta.addAttribute(formatGtAttrName);

		EntityMetaData entityMeta = new EntityMetaDataImpl("vcfEntity");
		entityMeta.addAttribute(idAttrName, ROLE_ID);
		entityMeta.addAttribute(CHROM_META);
		entityMeta.addAttribute(POS_META);
		entityMeta.addAttribute(ID_META);
		entityMeta.addAttribute(REF_META);
		entityMeta.addAttribute(ALT_META);
		entityMeta.addAttribute(QUAL_META);
		entityMeta.addAttribute(FILTER_META);
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
		vcfEntity.set(ID, "rs1578391");
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
			VcfUtils.writeToVcf(vcfEntity, writer);
		}
		finally
		{
			writer.close();
		}
		assertEquals(strWriter.toString(), "1	565286	rs1578391	C	T	.	flt	.	GT:DP:EC	1/1:5:5");
	}

	@Test
	public void createId()
	{
		assertEquals(VcfUtils.createId(entity1), "yCiiynjHRAtJPcdn7jFDGA");
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

			VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, Collections.emptyList());

			for (Entity entity : entities)
			{
				VcfUtils.writeToVcf(entity, outputVCFWriter);
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

			VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter,
					Lists.newArrayList(annotatedEntityMetadata.getAttributes()));

			for (Entity entity : entities)
			{
				MapEntity mapEntity = new MapEntity(entity, annotatedEntityMetadata);
				VcfUtils.writeToVcf(mapEntity, outputVCFWriter);
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

}