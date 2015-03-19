package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.molgenis.data.annotation.impl.datastructures.CgdData;
import org.molgenis.data.annotation.impl.datastructures.HGNCLocations;
import org.molgenis.data.annotation.provider.CgdDataProvider;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClinicalGenomicsDatabaseServiceAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private ClinicalGenomicsDatabaseServiceAnnotator annotator;
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
		MolgenisSettings settings = mock(MolgenisSettings.class);

		when(settings.getProperty(CgdDataProvider.CGD_FILE_LOCATION_PROPERTY)).thenReturn(
				ResourceUtils.getFile(getClass(), "/cgd_example.txt").getPath());

		metaDataCanAnnotate = new DefaultEntityMetaData("test");
		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(ClinicalGenomicsDatabaseServiceAnnotator.CHROMOSOME);
		when(attributeMetaDataPos.getName()).thenReturn(ClinicalGenomicsDatabaseServiceAnnotator.POSITION);
		when(attributeMetaDataChrom.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		when(attributeMetaDataPos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.LONG.toString().toLowerCase()));

		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
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
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(ClinicalGenomicsDatabaseServiceAnnotator.POSITION);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(ClinicalGenomicsDatabaseServiceAnnotator.CHROMOSOME)).thenReturn(
				attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(ClinicalGenomicsDatabaseServiceAnnotator.POSITION)).thenReturn(
				attributeMetaDataCantAnnotatePos);

		entity = mock(Entity.class);

		String chrStr = "1";
		Long chrPos = new Long(66067385);
		when(entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.CHROMOSOME)).thenReturn(chrStr);
		when(entity.getLong(ClinicalGenomicsDatabaseServiceAnnotator.POSITION)).thenReturn(chrPos);

		input = new ArrayList<Entity>();
		input.add(entity);

		CgdDataProvider cgdDataProvider = mock(CgdDataProvider.class);
		AnnotationService annotationService = mock(AnnotationService.class);
		HgncLocationsProvider hgncLocationsProvider = mock(HgncLocationsProvider.class);
		Map<String, HGNCLocations> locationsMap = Collections.singletonMap("LEPR", new HGNCLocations("LEPR", 65886248l,
				66107242l, "1"));

		Map<String, CgdData> cgdDataMap = Collections
				.singletonMap(
						"LEPR",
						new CgdData(
								"6554",
								"3953",
								"Leptin receptor deficiency",
								"AR",
								"Pediatric",
								"",
								"Allergy/Immunology/Infectious; Endocrine",
								"Allergy/Immunology/Infectious; Endocrine",
								"Standard treatments for obesity, such as gastric surgery, have been described as beneficial",
								"In addition to endocrine manifestations, individuals may be susceptible to infections (eg, respiratory infections), which, coupled with other manifestations (eg, severe obesity) can have severe sequelae such that prophylaxis and rapid treatment may be beneficial",
								"8666155; 9537324; 17229951; 21306929; 23275530; 23616257"));

		when(hgncLocationsProvider.getHgncLocations()).thenReturn(locationsMap);
		when(cgdDataProvider.getCgdData()).thenReturn(cgdDataMap);

		annotator = new ClinicalGenomicsDatabaseServiceAnnotator(settings, annotationService, hgncLocationsProvider,
				cgdDataProvider);

		when(entity.getEntityMetaData()).thenReturn(metaDataCanAnnotate);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.GENE, "LEPR");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.HGNC_ID, "6554");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.ENTREZ_GENE_ID, "3953");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.CONDITION, "Leptin receptor deficiency");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.INHERITANCE, "AR");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.AGE_GROUP, "Pediatric");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.ALLELIC_CONDITIONS, "");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.MANIFESTATION_CATEGORIES,
				"Allergy/Immunology/Infectious; Endocrine");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.INTERVENTION_CATEGORIES,
				"Allergy/Immunology/Infectious; Endocrine");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.COMMENTS,
				"Standard treatments for obesity, such as gastric surgery, have been described as beneficial");
		resultMap
				.put(ClinicalGenomicsDatabaseServiceAnnotator.INTERVENTION_RATIONALE,
						"In addition to endocrine manifestations, individuals may be susceptible to infections (eg, respiratory infections), which, coupled with other manifestations (eg, severe obesity) can have severe sequelae such that prophylaxis and rapid treatment may be beneficial");
		resultMap.put(ClinicalGenomicsDatabaseServiceAnnotator.REFERENCES,
				"8666155; 9537324; 17229951; 21306929; 23275530; 23616257");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input);

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.GENE),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.GENE));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.ENTREZ_GENE_ID),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.ENTREZ_GENE_ID));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.CONDITION),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.CONDITION));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.INHERITANCE),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.INHERITANCE));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.AGE_GROUP),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.AGE_GROUP));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.ALLELIC_CONDITIONS),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.ALLELIC_CONDITIONS));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.MANIFESTATION_CATEGORIES),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.MANIFESTATION_CATEGORIES));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.INTERVENTION_CATEGORIES),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.INTERVENTION_CATEGORIES));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.COMMENTS),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.COMMENTS));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.INTERVENTION_RATIONALE),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.INTERVENTION_RATIONALE));
		assertEquals(resultEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.REFERENCES),
				expectedEntity.get(ClinicalGenomicsDatabaseServiceAnnotator.REFERENCES));
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
