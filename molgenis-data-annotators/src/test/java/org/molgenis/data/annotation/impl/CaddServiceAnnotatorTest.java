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
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CaddServiceAnnotatorTest
{
	private EntityMetaData metaDataCanAnnotate;
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
	private Entity entity;
	private ArrayList<Entity> input;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		metaDataCanAnnotate = mock(EntityMetaData.class);

		MolgenisSettings settings = mock(MolgenisSettings.class);

		when(settings.getProperty(CaddServiceAnnotator.CADD_FILE_LOCATION_PROPERTY))
				.thenReturn(getClass().getResource("/1000G.vcf.gz").getFile());

		when(settings.getProperty(CaddServiceAnnotator.TABIX_LOCATION_PROPERTY)).thenReturn(getClass().getResource("/tabix").getFile());

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

		when(metaDataCanAnnotate.getAttribute(CaddServiceAnnotator.CHROMOSOME)).thenReturn(attributeMetaDataChrom);
		when(metaDataCanAnnotate.getAttribute(CaddServiceAnnotator.POSITION)).thenReturn(attributeMetaDataPos);
		when(metaDataCanAnnotate.getAttribute(CaddServiceAnnotator.REFERENCE)).thenReturn(attributeMetaDataRef);
		when(metaDataCanAnnotate.getAttribute(CaddServiceAnnotator.ALTERNATIVE)).thenReturn(attributeMetaDataAlt);

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

		entity = mock(Entity.class);

		when(entity.getString(CaddServiceAnnotator.CHROMOSOME)).thenReturn("10");
		when(entity.getLong(CaddServiceAnnotator.POSITION)).thenReturn(new Long(17463221));
		when(entity.getString(CaddServiceAnnotator.REFERENCE)).thenReturn("C");
		when(entity.getString(CaddServiceAnnotator.ALTERNATIVE)).thenReturn("T");

		input = new ArrayList<Entity>();
		input.add(entity);

		annotator = new CaddServiceAnnotator(settings, null);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(CaddServiceAnnotator.CADD_ABS, "0.180916");
		resultMap.put(CaddServiceAnnotator.CADD_SCALED, "4.974");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input.iterator());

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_ABS), expectedEntity.get(CaddServiceAnnotator.CADD_ABS));
		assertEquals(resultEntity.get(CaddServiceAnnotator.CADD_SCALED),
				expectedEntity.get(CaddServiceAnnotator.CADD_SCALED));
	}

	@Test
	public void canAnnotateTrueTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCanAnnotate), true);
	}

	@Test
	public void canAnnotateFalseTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCantAnnotate), false);
	}
}
