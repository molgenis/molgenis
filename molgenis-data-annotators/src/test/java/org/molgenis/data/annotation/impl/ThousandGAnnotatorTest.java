package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AbstractAnnotatorTest;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ThousandGAnnotatorTest extends AbstractAnnotatorTest
{
	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		File file = new File(ResourceUtils.getFile(getClass(),
				"/ALL.chr1.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz").getPath());
		String absolutePath = file.getAbsolutePath();
		String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));

		when(settings.getProperty(ThousandGenomesServiceAnnotator.THGEN_DIRECTORY_LOCATION_PROPERTY)).thenReturn(
				filePath);

		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 10352);
		entity1.set(VcfRepository.REF, "T");
		entity1.set(VcfRepository.ALT, "TA");

		input1.add(entity1);

		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 123456);
		entity2.set(VcfRepository.REF, "G");
		entity2.set(VcfRepository.ALT, "A");

		input2.add(entity2);

		annotator = new ThousandGenomesServiceAnnotator(settings);
	}

	@Test
	public void testAnnotate()
	{
		DefaultEntityMetaData annotatedMetadata = new DefaultEntityMetaData("test");
		annotatedMetadata.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		annotatedMetadata.addAttributeMetaData(attributeMetaDataPos);
		annotatedMetadata.addAttributeMetaData(attributeMetaDataRef);
		annotatedMetadata.addAttributeMetaData(attributeMetaDataAlt);
		annotatedMetadata.setIdAttribute(attributeMetaDataCantAnnotateChrom.getName());
		annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(ThousandGenomesServiceAnnotator.THGEN_MAF,
				FieldTypeEnum.DECIMAL));

		Iterator<Entity> results = annotator.annotate(input1);

		MapEntity mapEntity = new MapEntity(entity1, annotatedMetadata);
		mapEntity.set(ThousandGenomesServiceAnnotator.THGEN_MAF, 0.4375);

		assertEquals(results.next(), mapEntity);
	}

	@Test
	public void testAnnotateNoResult()
	{
		DefaultEntityMetaData annotatedMetadata = new DefaultEntityMetaData("test");
		annotatedMetadata.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		annotatedMetadata.addAttributeMetaData(attributeMetaDataPos);
		annotatedMetadata.addAttributeMetaData(attributeMetaDataRef);
		annotatedMetadata.addAttributeMetaData(attributeMetaDataAlt);
		annotatedMetadata.setIdAttribute(attributeMetaDataCantAnnotateChrom.getName());
		annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(ThousandGenomesServiceAnnotator.THGEN_MAF,
				FieldTypeEnum.DECIMAL));

		Iterator<Entity> results = annotator.annotate(input2);

		MapEntity mapEntity = new MapEntity(entity2, annotatedMetadata);
		mapEntity.set(ThousandGenomesServiceAnnotator.THGEN_MAF, null);

		assertEquals(results.next(), mapEntity);
	}
}
