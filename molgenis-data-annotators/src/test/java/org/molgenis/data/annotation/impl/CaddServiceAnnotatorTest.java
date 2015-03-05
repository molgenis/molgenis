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
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CaddServiceAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private CaddServiceAnnotator annotator;
	private AttributeMetaData attributeMetaDataChrom;
	private AttributeMetaData attributeMetaDataPos;
	private AttributeMetaData attributeMetaDataRef;
	private AttributeMetaData attributeMetaDataAlt;
	private AttributeMetaData attributeMetaDataCantAnnotateFeature;
	private AttributeMetaData attributeMetaDataCantAnnotateChrom;
	private AttributeMetaData attributeMetaDataCantAnnotatePos;
	private AttributeMetaData attributeMetaDataCantAnnotateRef;
	private AttributeMetaData attributeMetaDataCantAnnotateAlt;
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

		MolgenisSettings settings = mock(MolgenisSettings.class);

		when(settings.getProperty(CaddServiceAnnotator.CADD_FILE_LOCATION_PROPERTY)).thenReturn(
				ResourceUtils.getFile(getClass(), "/cadd_test.vcf.gz").getPath());

		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);
		attributeMetaDataRef = mock(AttributeMetaData.class);
		attributeMetaDataAlt = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(CaddServiceAnnotator.CHROMOSOME);
		when(attributeMetaDataPos.getName()).thenReturn(CaddServiceAnnotator.POSITION);
		when(attributeMetaDataRef.getName()).thenReturn(CaddServiceAnnotator.REFERENCE);
		when(attributeMetaDataAlt.getName()).thenReturn(CaddServiceAnnotator.ALTERNATIVE);

		when(attributeMetaDataChrom.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		when(attributeMetaDataPos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.LONG.toString().toLowerCase()));
		when(attributeMetaDataRef.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		when(attributeMetaDataAlt.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());
		metaDataCantAnnotate = mock(EntityMetaData.class);

		attributeMetaDataCantAnnotateFeature = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateFeature.getName()).thenReturn("otherID");
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		attributeMetaDataCantAnnotateChrom = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateChrom.getName()).thenReturn(DbnsfpVariantServiceAnnotator.CHROMOSOME);
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotatePos = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(CaddServiceAnnotator.POSITION);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		attributeMetaDataCantAnnotateRef = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateRef.getName()).thenReturn(CaddServiceAnnotator.REFERENCE);
		when(attributeMetaDataCantAnnotateRef.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotateAlt = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateAlt.getName()).thenReturn(CaddServiceAnnotator.ALTERNATIVE);
		when(attributeMetaDataCantAnnotateAlt.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(CaddServiceAnnotator.CHROMOSOME)).thenReturn(attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(CaddServiceAnnotator.POSITION)).thenReturn(
				attributeMetaDataCantAnnotatePos);
		when(metaDataCantAnnotate.getAttribute(CaddServiceAnnotator.REFERENCE)).thenReturn(
				attributeMetaDataCantAnnotateRef);
		when(metaDataCantAnnotate.getAttribute(CaddServiceAnnotator.ALTERNATIVE)).thenReturn(
				attributeMetaDataCantAnnotateAlt);

		entity1 = mock(Entity.class);
		when(entity1.getString(CaddServiceAnnotator.CHROMOSOME)).thenReturn("1");
		when(entity1.getLong(CaddServiceAnnotator.POSITION)).thenReturn(new Long(100));
		when(entity1.getString(CaddServiceAnnotator.REFERENCE)).thenReturn("C");
		when(entity1.getString(CaddServiceAnnotator.ALTERNATIVE)).thenReturn("T");
		input1 = new ArrayList<Entity>();
		input1.add(entity1);
		when(entity1.getEntityMetaData()).thenReturn(metaDataCanAnnotate);

		entity2 = mock(Entity.class);
		when(entity2.getString(CaddServiceAnnotator.CHROMOSOME)).thenReturn("2");
		when(entity2.getLong(CaddServiceAnnotator.POSITION)).thenReturn(new Long(200));
		when(entity2.getString(CaddServiceAnnotator.REFERENCE)).thenReturn("A");
		when(entity2.getString(CaddServiceAnnotator.ALTERNATIVE)).thenReturn("C");
		input2 = new ArrayList<Entity>();
		input2.add(entity2);
		annotator = new CaddServiceAnnotator(settings, null);
		when(entity2.getEntityMetaData()).thenReturn(metaDataCanAnnotate);

		entity3 = mock(Entity.class);
		when(entity3.getString(CaddServiceAnnotator.CHROMOSOME)).thenReturn("3");
		when(entity3.getLong(CaddServiceAnnotator.POSITION)).thenReturn(new Long(300));
		when(entity3.getString(CaddServiceAnnotator.REFERENCE)).thenReturn("G");
		when(entity3.getString(CaddServiceAnnotator.ALTERNATIVE)).thenReturn("C");
		input3 = new ArrayList<Entity>();
		input3.add(entity3);
		annotator = new CaddServiceAnnotator(settings, null);
		when(entity3.getEntityMetaData()).thenReturn(metaDataCanAnnotate);

		entity4 = mock(Entity.class);
		when(entity4.getString(CaddServiceAnnotator.CHROMOSOME)).thenReturn("1");
		when(entity4.getLong(CaddServiceAnnotator.POSITION)).thenReturn(new Long(100));
		when(entity4.getString(CaddServiceAnnotator.REFERENCE)).thenReturn("T");
		when(entity4.getString(CaddServiceAnnotator.ALTERNATIVE)).thenReturn("C");
		input4 = new ArrayList<Entity>();
		input4.add(entity4);
		annotator = new CaddServiceAnnotator(settings, null);
		when(entity4.getEntityMetaData()).thenReturn(metaDataCanAnnotate);
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

		Iterator<Entity> results = annotator.annotate(input1.iterator());
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

		Iterator<Entity> results = annotator.annotate(input2.iterator());
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

		Iterator<Entity> results = annotator.annotate(input3.iterator());
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

		Iterator<Entity> results = annotator.annotate(input4.iterator());
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
