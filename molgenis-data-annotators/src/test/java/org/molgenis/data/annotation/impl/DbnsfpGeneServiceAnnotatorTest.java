package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DbnsfpGeneServiceAnnotatorTest
{
	
	private EntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private DbnsfpGeneServiceAnnotator annotator;
	private AttributeMetaData attributeMetaDataChrom;
	private AttributeMetaData attributeMetaDataPos;
	private AttributeMetaData attributeMetaDataCantAnnotateFeature;
	private AttributeMetaData attributeMetaDataCantAnnotateChrom;
	private AttributeMetaData attributeMetaDataCantAnnotatePos;
	private String annotatorOutput;
	private Entity entity;
	private ArrayList<Entity> input;

	@BeforeMethod
	public void beforeMethod()
	{

		annotator = new DbnsfpGeneServiceAnnotator();

		metaDataCanAnnotate = mock(EntityMetaData.class);

		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(DbnsfpGeneServiceAnnotator.CHROMOSOME);
		when(attributeMetaDataPos.getName()).thenReturn(DbnsfpGeneServiceAnnotator.POSITION);

		when(attributeMetaDataChrom.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		when(attributeMetaDataPos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.LONG.toString().toLowerCase()));

		when(metaDataCanAnnotate.getAttribute(DbnsfpGeneServiceAnnotator.CHROMOSOME))
				.thenReturn(attributeMetaDataChrom);
		when(metaDataCanAnnotate.getAttribute(DbnsfpGeneServiceAnnotator.POSITION)).thenReturn(attributeMetaDataPos);

		metaDataCantAnnotate = mock(EntityMetaData.class);
		attributeMetaDataCantAnnotateFeature = mock(AttributeMetaData.class);

		when(attributeMetaDataCantAnnotateFeature.getName()).thenReturn("otherID");
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		attributeMetaDataCantAnnotateChrom = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateChrom.getName()).thenReturn(DbnsfpGeneServiceAnnotator.CHROMOSOME);
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotatePos = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(DbnsfpGeneServiceAnnotator.POSITION);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(DbnsfpGeneServiceAnnotator.CHROMOSOME)).thenReturn(
				attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(DbnsfpGeneServiceAnnotator.POSITION)).thenReturn(
				attributeMetaDataCantAnnotatePos);

		entity = mock(Entity.class);

		when(entity.getString(DbnsfpGeneServiceAnnotator.CHROMOSOME)).thenReturn("12");
		when(entity.getLong(DbnsfpGeneServiceAnnotator.POSITION)).thenReturn(new Long(6968292));

		input = new ArrayList<Entity>();
		input.add(entity);

		annotatorOutput = "USP5	ENSG00000111667	12	.	IsoT	P45974	UBP5_HUMAN	8078	CCDS31733.1;CCDS41743.1	NM_001098536	uc001qri.4";
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(annotator.FEATURES[0], "USP5");
		resultMap.put(annotator.FEATURES[1], "ENSG00000111667");
		resultMap.put(annotator.FEATURES[2], "12");
		resultMap.put(annotator.FEATURES[3], ".");
		resultMap.put(annotator.FEATURES[4], "IsoT");
		resultMap.put(annotator.FEATURES[5], "P45974");
		resultMap.put(annotator.FEATURES[6], "UBP5_HUMAN");
		resultMap.put(annotator.FEATURES[7], "8078");
		resultMap.put(annotator.FEATURES[8], "CCDS31733.1;CCDS41743.1");
		resultMap.put(annotator.FEATURES[9], "NM_001098536");
		resultMap.put(annotator.FEATURES[10], "uc001qri.4");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input.iterator());

		Entity resultEntity = results.next();
		for (int i = 0; i < 11; i++)
		{
			assertEquals(resultEntity.get(annotator.FEATURES[i]),
					expectedEntity.get(annotator.FEATURES[i]));
		}
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