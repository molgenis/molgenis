package org.molgenis.data.vcf.utils;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class VcfUtilsTest
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfUtilsTest.class);
	private DefaultEntityMetaData annotatedEntityMetadata;
	public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");

	public AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public ArrayList<Entity> input = new ArrayList<Entity>();
	public ArrayList<Entity> input1 = new ArrayList<Entity>();
	public ArrayList<Entity> input2 = new ArrayList<Entity>();
	public ArrayList<Entity> input3 = new ArrayList<Entity>();
	public ArrayList<Entity> input4 = new ArrayList<Entity>();
	public Entity entity;
	public Entity entity1;
	public Entity entity2;
	public Entity entity3;
	public Entity entity4;

	public AttributeMetaData attributeMetaDataCantAnnotateFeature;
	public AttributeMetaData attributeMetaDataCantAnnotatePos;
	public AttributeMetaData attributeMetaDataCantAnnotateRef;
	public AttributeMetaData attributeMetaDataCantAnnotateAlt;

	public MolgenisSettings settings = mock(MolgenisSettings.class);
	public ArrayList<Entity> entities;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		/*
		 * 1 10050000 test21 G A . PASS AC=21;AN=22;GTC=0,1,10 1 10050001 test22 G A . PASS AC=22;AN=23;GTC=1,2,11 1
		 * 10050002 test23 G A . PASS AC=23;AN=24;GTC=2,3,12
		 */
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);

		entity = new MapEntity(metaDataCanAnnotate);
		entity1 = new MapEntity(metaDataCanAnnotate);
		entity2 = new MapEntity(metaDataCanAnnotate);
		entity3 = new MapEntity(metaDataCanAnnotate);
		entity4 = new MapEntity(metaDataCanAnnotate);

		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.ID,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.QUAL,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.FILTER,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		DefaultAttributeMetaData INFO = new DefaultAttributeMetaData(VcfRepository.INFO,
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData AA = new DefaultAttributeMetaData("AC", MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData AN = new DefaultAttributeMetaData("AN", MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData GTC = new DefaultAttributeMetaData("GTC", MolgenisFieldTypes.FieldTypeEnum.STRING);
		INFO.addAttributePart(AA);
		INFO.addAttributePart(AN);
		INFO.addAttributePart(GTC);
		metaDataCanAnnotate.addAttributeMetaData(INFO);

		annotatedEntityMetadata = metaDataCanAnnotate;
		annotatedEntityMetadata.addAttributeMetaData(new DefaultAttributeMetaData("INFO_ANNO",
				MolgenisFieldTypes.FieldTypeEnum.STRING));

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
		entity2.set("AC", "22");
		entity2.set("AN", "23");
		entity2.set("GTC", "1,2,11");

		entity3.set(VcfRepository.CHROM, "1");
		entity3.set(VcfRepository.POS, 10050002);
		entity3.set(VcfRepository.ID, "test23");
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "A");
		entity3.set(VcfRepository.QUAL, ".");
		entity3.set(VcfRepository.FILTER, "PASS");
		entity3.set("AC", "23");
		entity3.set("AN", "24");
		entity3.set("GTC", "2,3,12");

		entities = new ArrayList<>();
		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);
	}

	@Test
	public void vcfWriterRoundtripTest() throws IOException, MolgenisInvalidFormatException
	{
		final File outputVCFFile = File.createTempFile("output", ".vcf");
		try{
			PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

			File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testWriter.vcf").getPath());

			VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, Collections.emptyList(), "");

			for (Entity entity : entities)
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(entity));

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
		final File outputVCFFile = File.createTempFile("output", ".vcf");
		try{
			PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");
	
			File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testWriter.vcf").getPath());
			File resultVCFWriter = new File(ResourceUtils.getFile(getClass(), "/result_vcfWriter.vcf").getPath());
	
			VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter,
					annotatedEntityMetadata.getAttributes(), "INFO_ANNO");
	
			for (Entity entity : entities)
			{
				MapEntity mapEntity = new MapEntity(entity, annotatedEntityMetadata);
				mapEntity.set("INFO_ANNO", "TEST_" + entity.get(VcfRepository.ID));
				outputVCFWriter.println(VcfUtils.convertToVCF(mapEntity));
	
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