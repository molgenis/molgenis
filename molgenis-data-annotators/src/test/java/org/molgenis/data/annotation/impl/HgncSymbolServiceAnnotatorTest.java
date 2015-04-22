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
import org.molgenis.data.annotation.impl.datastructures.HGNCLocations;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HgncSymbolServiceAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private HgncSymbolServiceAnnotator annotator;
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
		HgncLocationsProvider hgncLocationsProvider = mock(HgncLocationsProvider.class);
		Map<String, HGNCLocations> hgncLocations = Collections.singletonMap("BRCA1", new HGNCLocations("BRCA1",
				41196312l - 10, 41277500l + 10, "17"));
		when(hgncLocationsProvider.getHgncLocations()).thenReturn(hgncLocations);

		annotator = new HgncSymbolServiceAnnotator(null, hgncLocationsProvider);

		metaDataCanAnnotate = new DefaultEntityMetaData("test");

		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(VcfRepository.CHROM);
		when(attributeMetaDataPos.getName()).thenReturn(VcfRepository.POS);

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
		when(attributeMetaDataCantAnnotateChrom.getName()).thenReturn(VcfRepository.CHROM);
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotatePos = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(VcfRepository.POS);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(VcfRepository.CHROM)).thenReturn(attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(VcfRepository.POS)).thenReturn(
				attributeMetaDataCantAnnotatePos);

		entity = new MapEntity(metaDataCanAnnotate);

		entity.set(VcfRepository.CHROM,"17");
		entity.set(VcfRepository.POS,new Long(41196312));

		input = new ArrayList<Entity>();
		input.add(entity);

	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(HgncSymbolServiceAnnotator.HGNC_SYMBOL, "BRCA1");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input);

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(HgncSymbolServiceAnnotator.HGNC_SYMBOL),
				expectedEntity.get(HgncSymbolServiceAnnotator.HGNC_SYMBOL));

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
