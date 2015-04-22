package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.annotators.annotator.test.data.AnnotatorTestData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class GoNLAnnotatorTest extends AnnotatorTestData
{

	private GoNLServiceAnnotator annotator;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		File file = new File(ResourceUtils.getFile(getClass(), "/gonl.chr1.snps_indels.r5.vcf.gz").getPath());
		String absolutePath = file.getAbsolutePath();
		String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));

		when(settings.getProperty(GoNLServiceAnnotator.GONL_DIRECTORY_LOCATION_PROPERTY)).thenReturn(filePath);

		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 126108);
		entity1.set(VcfRepository.REF, "G");
		entity1.set(VcfRepository.ALT, "A");

		input1.add(entity1);

		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 123456);
		entity2.set(VcfRepository.REF, "G");
		entity2.set(VcfRepository.ALT, "A");

		input2.add(entity2);

		annotator = new GoNLServiceAnnotator(settings, null);
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
		annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(GoNLServiceAnnotator.GONL_MAF,
				FieldTypeEnum.DECIMAL));
		annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(GoNLServiceAnnotator.GONL_GTC,
				FieldTypeEnum.STRING)); // FIXME: correct type?

		Iterator<Entity> results = annotator.annotate(input1);

		MapEntity mapEntity = new MapEntity(entity1, annotatedMetadata);
		mapEntity.set(GoNLServiceAnnotator.GONL_MAF, 0.03714859437751004);
		mapEntity.set(GoNLServiceAnnotator.GONL_GTC, "461,37,0");

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
		annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(GoNLServiceAnnotator.GONL_MAF,
				FieldTypeEnum.DECIMAL));
		annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(GoNLServiceAnnotator.GONL_GTC,
				FieldTypeEnum.STRING)); // FIXME: correct type?

		Iterator<Entity> results = annotator.annotate(input2);

		MapEntity mapEntity = new MapEntity(entity2, annotatedMetadata);
		mapEntity.set(GoNLServiceAnnotator.GONL_MAF, null);
		mapEntity.set(GoNLServiceAnnotator.GONL_GTC, null);

		assertEquals(results.next(), mapEntity);
	}

	@Test
	public void canAnnotateTrueTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCanAnnotate), "true");
	}

	@Test
	public void canAnnotateFalseTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCantAnnotate), "a required attribute has the wrong datatype");
	}
}
