package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

public class OmimHpoAnnotatorTest
{
	private EntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private OmimHpoAnnotator annotator;
	private AttributeMetaData attributeMetaDataChrom;
	private AttributeMetaData attributeMetaDataPos;
	private AttributeMetaData attributeMetaDataCantAnnotateFeature;
	private AttributeMetaData attributeMetaDataCantAnnotateChrom;
	private AttributeMetaData attributeMetaDataCantAnnotatePos;
	private Entity entity;
	private ArrayList<Entity> input;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		AnnotationService annotationService = mock(AnnotationService.class);

		metaDataCanAnnotate = mock(EntityMetaData.class);

		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(OmimHpoAnnotator.CHROMOSOME);
		when(attributeMetaDataPos.getName()).thenReturn(OmimHpoAnnotator.POSITION);

		when(attributeMetaDataChrom.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		when(attributeMetaDataPos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.LONG.toString().toLowerCase()));

		when(metaDataCanAnnotate.getAttribute(OmimHpoAnnotator.CHROMOSOME)).thenReturn(attributeMetaDataChrom);
		when(metaDataCanAnnotate.getAttribute(OmimHpoAnnotator.POSITION)).thenReturn(attributeMetaDataPos);

		metaDataCantAnnotate = mock(EntityMetaData.class);
		attributeMetaDataCantAnnotateFeature = mock(AttributeMetaData.class);

		when(attributeMetaDataCantAnnotateFeature.getName()).thenReturn("otherID");
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		attributeMetaDataCantAnnotateChrom = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateChrom.getName()).thenReturn(OmimHpoAnnotator.CHROMOSOME);
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotatePos = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(OmimHpoAnnotator.POSITION);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(OmimHpoAnnotator.CHROMOSOME)).thenReturn(attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(OmimHpoAnnotator.POSITION)).thenReturn(attributeMetaDataCantAnnotatePos);

		entity = mock(Entity.class);

		when(entity.getString(OmimHpoAnnotator.CHROMOSOME)).thenReturn("11");
		when(entity.getLong(OmimHpoAnnotator.POSITION)).thenReturn(new Long(19207841));

		input = new ArrayList<Entity>();
		input.add(entity);

		annotator = new OmimHpoAnnotator(annotationService);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		
		resultMap.put(OmimHpoAnnotator.OMIM_DISORDERS, new HashSet<String>(Arrays.asList("Cardiomyopathy, dilated, 1M", "Cardiomyopathy, familial hypertrophic, 12")));
		resultMap.put(OmimHpoAnnotator.OMIM_CAUSAL_IDENTIFIER, new HashSet<Integer>(Arrays.asList(600824)));
		resultMap.put(OmimHpoAnnotator.OMIM_TYPE, new HashSet<Integer>(Arrays.asList(3)));
		resultMap.put(OmimHpoAnnotator.OMIM_HGNC_IDENTIFIERS, new HashSet<String>(Arrays.asList("CSRP3", "CRP3", "CLP", "CMD1M", "CMH12")));
		resultMap.put(OmimHpoAnnotator.OMIM_CYTOGENIC_LOCATION, new HashSet<String>(Arrays.asList("11p15.1")));
		resultMap.put(OmimHpoAnnotator.OMIM_ENTRY, new HashSet<Integer>(Arrays.asList(607482, 612124)));
		resultMap.put(OmimHpoAnnotator.HPO_IDENTIFIERS, new HashSet<String>(Arrays.asList("HP:0000407", "HP:0001645", "HP:0001706", "HP:0000982", "HP:0001644", "HP:0003457", "HP:0006670", "HP:0100578", "HP:0001639", "HP:0001638", "HP:0001874", "HP:0004757", "HP:0004756", "HP:0003198", "HP:0000006")));
		resultMap.put(OmimHpoAnnotator.HPO_GENE_NAME, new HashSet<String>(Arrays.asList("CSRP3")));
		resultMap.put(OmimHpoAnnotator.HPO_DESCRIPTIONS, new HashSet<String>(Arrays.asList("Myopathy", "Hypertrophic cardiomyopathy", "Palmoplantar keratoderma", "EMG abnormality", "Lipoatrophy", "Sensorineural hearing impairment", "Abnormality of neutrophils", "Cardiomyopathy", "Sudden cardiac death", "Paroxysmal atrial fibrillation", "Autosomal dominant inheritance", "Ventricular tachycardia", "Endocardial fibroelastosis", "Dilated cardiomyopathy", "Impaired myocardial contractility", "Autosomal dominant inheritance")));
		resultMap.put(OmimHpoAnnotator.HPO_DISEASE_DATABASE, new HashSet<String>(Arrays.asList("ORPHANET", "OMIM")));
		resultMap.put(OmimHpoAnnotator.HPO_DISEASE_DATABASE_ENTRY, new HashSet<Integer>(Arrays.asList(607482, 154, 612124)));
		resultMap.put(OmimHpoAnnotator.HPO_ENTREZ_ID, new HashSet<Integer>(Arrays.asList(8048)));

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input.iterator());

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_DISORDERS), expectedEntity.get(OmimHpoAnnotator.OMIM_DISORDERS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_CAUSAL_IDENTIFIER), expectedEntity.get(OmimHpoAnnotator.OMIM_CAUSAL_IDENTIFIER));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_IDENTIFIERS), expectedEntity.get(OmimHpoAnnotator.OMIM_IDENTIFIERS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_TYPE), expectedEntity.get(OmimHpoAnnotator.OMIM_TYPE));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_HGNC_IDENTIFIERS), expectedEntity.get(OmimHpoAnnotator.OMIM_HGNC_IDENTIFIERS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_CYTOGENIC_LOCATION), expectedEntity.get(OmimHpoAnnotator.OMIM_CYTOGENIC_LOCATION));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_ENTRY), expectedEntity.get(OmimHpoAnnotator.OMIM_ENTRY));

		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_IDENTIFIERS), expectedEntity.get(OmimHpoAnnotator.HPO_IDENTIFIERS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_GENE_NAME), expectedEntity.get(OmimHpoAnnotator.HPO_GENE_NAME));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_DESCRIPTIONS), expectedEntity.get(OmimHpoAnnotator.HPO_DESCRIPTIONS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_DISEASE_DATABASE), expectedEntity.get(OmimHpoAnnotator.HPO_DISEASE_DATABASE));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_DISEASE_DATABASE_ENTRY), expectedEntity.get(OmimHpoAnnotator.HPO_DISEASE_DATABASE_ENTRY));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_ENTREZ_ID), expectedEntity.get(OmimHpoAnnotator.HPO_ENTREZ_ID));


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
