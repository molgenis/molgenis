package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CaddServiceAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
	private DefaultEntityMetaData metaDataCantAnnotate;
	private CaddServiceAnnotator annotator;
	private AttributeMetaData attributeMetaDataChrom;
	private AttributeMetaData attributeMetaDataPos;
	private AttributeMetaData attributeMetaDataRef;
	private AttributeMetaData attributeMetaDataAlt;
	private AttributeMetaData attributeMetaDataCantAnnotateChrom;
	private ArrayList<Entity> input1;
	private ArrayList<Entity> input2;
	private ArrayList<Entity> input3;
	private ArrayList<Entity> input4;
	private Entity entity1;
	private Entity entity2;
	private Entity entity3;
	private Entity entity4;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		metaDataCanAnnotate = new DefaultEntityMetaData("test");
        metaDataCantAnnotate = new DefaultEntityMetaData("test");

		MolgenisSettings settings = mock(MolgenisSettings.class);

		when(settings.getProperty(CaddServiceAnnotator.CADD_FILE_LOCATION_PROPERTY)).thenReturn(
				ResourceUtils.getFile(getClass(), "/cadd_test.vcf.gz").getPath());

        attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM, FieldTypeEnum.STRING);
        attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS, FieldTypeEnum.LONG);
        attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF, FieldTypeEnum.STRING);
        attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT, FieldTypeEnum.STRING);
        attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(VcfRepository.CHROM, FieldTypeEnum.LONG);

		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);

        entity1 = new MapEntity(metaDataCanAnnotate);
        entity2 = new MapEntity(metaDataCanAnnotate);
        entity3 = new MapEntity(metaDataCanAnnotate);
        entity4 = new MapEntity(metaDataCanAnnotate);

		entity1.set(VcfRepository.CHROM,"1");
		entity1.set(VcfRepository.POS, 100);
		entity1.set(VcfRepository.REF, "C");
		entity1.set(VcfRepository.ALT, "T");
		input1 = new ArrayList<Entity>();
		input1.add(entity1);

		entity2.set(VcfRepository.CHROM, "2");
		entity2.set(VcfRepository.POS, new Long(200));
		entity2.set(VcfRepository.REF, "A");
		entity2.set(VcfRepository.ALT, "C");
		input2 = new ArrayList<Entity>();
		input2.add(entity2);
		annotator = new CaddServiceAnnotator(settings, null);

		entity3.set(VcfRepository.CHROM, "3");
		entity3.set(VcfRepository.POS, new Long(300));
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "C");
		input3 = new ArrayList<Entity>();
		input3.add(entity3);
		annotator = new CaddServiceAnnotator(settings, null);

		entity4.set(VcfRepository.CHROM, "1");
		entity4.set(VcfRepository.POS, new Long(100));
		entity4.set(VcfRepository.REF, "T");
		entity4.set(VcfRepository.ALT, "C");
		input4 = new ArrayList<Entity>();
		input4.add(entity4);
		annotator = new CaddServiceAnnotator(settings, null);
	}

	@Test
	public void testThreeOccurencesOneMatch()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(CaddServiceAnnotator.CADD_ABS, -0.03);
		resultMap.put(CaddServiceAnnotator.CADD_SCALED, 2.003);

		Entity expectedEntity = new MapEntity(resultMap);
		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input1);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_ABS), expectedEntity.get(CaddServiceAnnotator.CADD_ABS));
		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_SCALED),
				expectedEntity.get(CaddServiceAnnotator.CADD_SCALED));
	}

	@Test
	public void testTwoOccurencesNoMatch()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		Entity expectedEntity = new MapEntity(resultMap);
		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input2);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_ABS), expectedEntity.get(CaddServiceAnnotator.CADD_ABS));
		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_SCALED),
				expectedEntity.get(CaddServiceAnnotator.CADD_SCALED));
	}

	@Test
	public void testFourOccurences()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(CaddServiceAnnotator.CADD_ABS, 0.5);
		resultMap.put(CaddServiceAnnotator.CADD_SCALED, 14.5);
		
		Entity expectedEntity = new MapEntity(resultMap);
		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input3);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_ABS), expectedEntity.get(CaddServiceAnnotator.CADD_ABS));
		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_SCALED),
				expectedEntity.get(CaddServiceAnnotator.CADD_SCALED));
	}

	@Test
	public void testSwappedAllelesThreeOccurencesOneMatch()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(CaddServiceAnnotator.CADD_ABS, -0.03);
		resultMap.put(CaddServiceAnnotator.CADD_SCALED, 2.003);

		Entity expectedEntity = new MapEntity(resultMap);
		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input4);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_ABS), expectedEntity.get(CaddServiceAnnotator.CADD_ABS));
		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_SCALED),
				expectedEntity.get(CaddServiceAnnotator.CADD_SCALED));
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
