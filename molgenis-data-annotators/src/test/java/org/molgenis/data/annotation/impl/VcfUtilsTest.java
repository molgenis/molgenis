package org.molgenis.data.annotation.impl;

import org.apache.commons.io.FileUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class VcfUtilsTest
{
	private ArrayList<Entity> entities;
	private DefaultEntityMetaData annotatedEntityMetadata;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		/*
		 * 1 10050000 test21 G A . PASS AC=21;AN=22;GTC=0,1,10 1 10050001 test22 G A . PASS AC=22;AN=23;GTC=1,2,11 1
		 * 10050002 test23 G A . PASS AC=23;AN=24;GTC=2,3,12
		 */

		DefaultEntityMetaData metaData = new DefaultEntityMetaData("TestEntity");
		metaData.addAttributeMetaData(new DefaultAttributeMetaData("ID", MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaData.setIdAttribute("ID");
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.CHROM,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.POS,
				MolgenisFieldTypes.FieldTypeEnum.LONG));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.ID,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.REF,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.ALT,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.QUAL,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.FILTER,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		DefaultAttributeMetaData INFO = new DefaultAttributeMetaData(VcfRepository.INFO,
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData AA = new DefaultAttributeMetaData("AC", MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData AN = new DefaultAttributeMetaData("AN", MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData GTC = new DefaultAttributeMetaData("GTC", MolgenisFieldTypes.FieldTypeEnum.STRING);
		INFO.addAttributePart(AA);
		INFO.addAttributePart(AN);
		INFO.addAttributePart(GTC);
		metaData.addAttributeMetaData(INFO);

		annotatedEntityMetadata = metaData;
		((DefaultAttributeMetaData) annotatedEntityMetadata.getAttribute(VcfRepository.INFO))
				.addAttributePart(new DefaultAttributeMetaData("INFO_ANNO", MolgenisFieldTypes.FieldTypeEnum.STRING));

		Entity entity1 = new MapEntity(metaData);
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
		Entity entity2 = new MapEntity(metaData);
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
		Entity entity3 = new MapEntity(metaData);
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
	public void vcfWriterRoundtripTest() throws IOException
	{
		File outputVCFFile = File.createTempFile("output", ".vcf");
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testWriter.vcf").getPath());

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, Collections.emptyList(), "");

		for (Entity entity : entities)
		{
			outputVCFWriter.println(VcfUtils.convertToVCF(entity));

		}
		outputVCFWriter.close();
		System.out.print(outputVCFFile.getAbsolutePath());
		assertTrue(FileUtils.contentEqualsIgnoreEOL(inputVcfFile, outputVCFFile, "UTF8"));
	}

	@Test
	public void vcfWriterAnnotateTest() throws IOException
	{
		File outputVCFFile = File.createTempFile("output", ".vcf");
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		final List<String> infoFields = Arrays.asList(new String[]
		{ "##INFO=<ID=INFO_ANNO,Number=1,Type=Float,Description=\"\">" });

		File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testWriter.vcf").getPath());
		File outputVcfFile = new File(ResourceUtils.getFile(getClass(), "/result_vcfWriter.vcf").getPath());

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields, "INFO_ANNO");
		for (Entity entity : entities)
		{
			MapEntity mapEntity = new MapEntity(entity, annotatedEntityMetadata);
			mapEntity.set("INFO_ANNO", "TEST_" + entity.get(VcfRepository.ID));
			outputVCFWriter.println(VcfUtils.convertToVCF(mapEntity));

		}
		outputVCFWriter.close();
		System.out.println(outputVCFFile.getAbsoluteFile());
		assertTrue(FileUtils.contentEqualsIgnoreEOL(outputVcfFile, outputVCFFile, "UTF8"));
	}

}
