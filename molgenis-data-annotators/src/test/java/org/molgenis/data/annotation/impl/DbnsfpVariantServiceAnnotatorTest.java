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

public class DbnsfpVariantServiceAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private DbnsfpVariantServiceAnnotator annotator;
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
		MolgenisSettings settings = mock(MolgenisSettings.class);
		when(settings.getProperty(DbnsfpVariantServiceAnnotator.CHROMOSOME_FILE_LOCATION_PROPERTY)).thenReturn(
				ResourceUtils.getFile(getClass(), "/dbNSFP_variant_example_chr").getPath());

		metaDataCanAnnotate = new DefaultEntityMetaData("test");

		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);
		attributeMetaDataRef = mock(AttributeMetaData.class);
		attributeMetaDataAlt = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(DbnsfpVariantServiceAnnotator.CHROMOSOME);
		when(attributeMetaDataPos.getName()).thenReturn(DbnsfpVariantServiceAnnotator.POSITION);
		when(attributeMetaDataRef.getName()).thenReturn(DbnsfpVariantServiceAnnotator.REFERENCE);
		when(attributeMetaDataAlt.getName()).thenReturn(DbnsfpVariantServiceAnnotator.ALTERNATIVE);

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
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(DbnsfpVariantServiceAnnotator.POSITION);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		attributeMetaDataCantAnnotateRef = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateRef.getName()).thenReturn(DbnsfpVariantServiceAnnotator.REFERENCE);
		when(attributeMetaDataCantAnnotateRef.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotateAlt = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateAlt.getName()).thenReturn(DbnsfpVariantServiceAnnotator.ALTERNATIVE);
		when(attributeMetaDataCantAnnotateAlt.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(DbnsfpVariantServiceAnnotator.CHROMOSOME)).thenReturn(
				attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(DbnsfpVariantServiceAnnotator.POSITION)).thenReturn(
				attributeMetaDataCantAnnotatePos);
		when(metaDataCantAnnotate.getAttribute(DbnsfpVariantServiceAnnotator.REFERENCE)).thenReturn(
				attributeMetaDataCantAnnotateRef);
		when(metaDataCantAnnotate.getAttribute(DbnsfpVariantServiceAnnotator.ALTERNATIVE)).thenReturn(
				attributeMetaDataCantAnnotateAlt);

		entity = mock(Entity.class);

		when(entity.getString(DbnsfpVariantServiceAnnotator.CHROMOSOME)).thenReturn("Y");
		when(entity.getLong(DbnsfpVariantServiceAnnotator.POSITION)).thenReturn(new Long(2655049));
		when(entity.getString(DbnsfpVariantServiceAnnotator.REFERENCE)).thenReturn("C");
		when(entity.getString(DbnsfpVariantServiceAnnotator.ALTERNATIVE)).thenReturn("A");

		input = new ArrayList<Entity>();
		input.add(entity);

		when(entity.getEntityMetaData()).thenReturn(metaDataCanAnnotate);

		annotator = new DbnsfpVariantServiceAnnotator(settings, null);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(DbnsfpVariantServiceAnnotator.AAREF, "Q");
		resultMap.put(DbnsfpVariantServiceAnnotator.AAALT, "H");
		resultMap.put(DbnsfpVariantServiceAnnotator.HG18_POS_1_COOR, "2715049");
		resultMap.put(DbnsfpVariantServiceAnnotator.GENENAME, "SRY");
		resultMap.put(DbnsfpVariantServiceAnnotator.UNIPROT_ACC, ".");
		resultMap.put(DbnsfpVariantServiceAnnotator.UNIPROT_ID, ".");
		resultMap.put(DbnsfpVariantServiceAnnotator.UNIPROT_AAPOS, ".");
		resultMap.put(DbnsfpVariantServiceAnnotator.INTERPRO_DOMAIN, ".");
		resultMap.put(DbnsfpVariantServiceAnnotator.CDS_STRAND, "-");
		resultMap.put(DbnsfpVariantServiceAnnotator.REFCODON, "CAG");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input.iterator());

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.AAREF),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.AAREF));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.AAALT),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.AAALT));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.HG18_POS_1_COOR),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.HG18_POS_1_COOR));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.GENENAME),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.GENENAME));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_ACC),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_ACC));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_ID),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_ID));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_AAPOS),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_AAPOS));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.INTERPRO_DOMAIN),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.INTERPRO_DOMAIN));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.CDS_STRAND),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.CDS_STRAND));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.REFCODON),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.REFCODON));
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
