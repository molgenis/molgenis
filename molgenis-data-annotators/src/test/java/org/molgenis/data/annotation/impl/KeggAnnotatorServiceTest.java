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

public class KeggAnnotatorServiceTest
{
	private EntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private KeggServiceAnnotator annotator;
	private AttributeMetaData attributeMetaDataChrom;
	private AttributeMetaData attributeMetaDataPos;
	private AttributeMetaData attributeMetaDataCantAnnotate;
	private AttributeMetaData attributeMetaDataCantAnnotate2;
	private String annotatorOutput;
	private Entity entity;
	private ArrayList<Entity> input;

	@BeforeMethod
	public void beforeMethod()
	{
		annotator = new KeggServiceAnnotator();

		metaDataCanAnnotate = mock(EntityMetaData.class);
		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(KeggServiceAnnotator.CHROMOSOME);
		when(attributeMetaDataPos.getName()).thenReturn(KeggServiceAnnotator.POSITION);
		when(attributeMetaDataChrom.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		when(attributeMetaDataPos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.LONG.toString().toLowerCase()));

		when(metaDataCanAnnotate.getAttribute(KeggServiceAnnotator.CHROMOSOME)).thenReturn(attributeMetaDataChrom);
		when(metaDataCanAnnotate.getAttribute(KeggServiceAnnotator.POSITION)).thenReturn(attributeMetaDataPos);

		metaDataCantAnnotate = mock(EntityMetaData.class);
		attributeMetaDataCantAnnotate = mock(AttributeMetaData.class);

		when(attributeMetaDataCantAnnotate.getName()).thenReturn("otherID");
		when(attributeMetaDataCantAnnotate.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		attributeMetaDataCantAnnotate2 = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotate2.getName()).thenReturn(KeggServiceAnnotator.POSITION);
		when(attributeMetaDataCantAnnotate2.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(KeggServiceAnnotator.CHROMOSOME)).thenReturn(attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(KeggServiceAnnotator.POSITION)).thenReturn(
				attributeMetaDataCantAnnotate2);

		entity = mock(Entity.class);

		when(entity.getString(KeggServiceAnnotator.CHROMOSOME)).thenReturn("2");
		when(entity.getLong(KeggServiceAnnotator.POSITION)).thenReturn(new Long(58453844l));

		input = new ArrayList<Entity>();
		input.add(entity);

		annotatorOutput = "hsa:55120	path:hsa03460, path:hsa04120	Fanconi anemia pathway - Homo sapiens (human), Ubiquitin mediated proteolysis - Homo sapiens (human)";
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(KeggServiceAnnotator.KEGG_GENE_ID, "hsa:55120");
		resultMap.put(KeggServiceAnnotator.KEGG_PATHWAYS_IDS, "path:hsa03460, path:hsa04120");
		resultMap.put(KeggServiceAnnotator.KEGG_PATHWAYS_NAMES,
				"Fanconi anemia pathway - Homo sapiens (human), Ubiquitin mediated proteolysis - Homo sapiens (human)");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input.iterator());

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(KeggServiceAnnotator.KEGG_GENE_ID),
				expectedEntity.get(KeggServiceAnnotator.KEGG_GENE_ID));
		assertEquals(resultEntity.get(KeggServiceAnnotator.KEGG_PATHWAYS_IDS),
				expectedEntity.get(KeggServiceAnnotator.KEGG_PATHWAYS_IDS));
		assertEquals(resultEntity.get(KeggServiceAnnotator.KEGG_PATHWAYS_NAMES),
				expectedEntity.get(KeggServiceAnnotator.KEGG_PATHWAYS_NAMES));
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