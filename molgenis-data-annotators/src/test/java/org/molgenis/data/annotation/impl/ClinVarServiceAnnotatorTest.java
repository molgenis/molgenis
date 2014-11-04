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
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClinVarServiceAnnotatorTest
{
	private EntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private ClinVarServiceAnnotator annotator;
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
		when(settings.getProperty(ClinVarServiceAnnotator.CLINVAR_FILE_LOCATION_PROPERTY)).thenReturn(
				ResourceUtils.getFile(getClass(), "/clinvar_example.txt").getPath());

		metaDataCanAnnotate = mock(EntityMetaData.class);
		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(ClinVarServiceAnnotator.CHROMOSOME);
		when(attributeMetaDataPos.getName()).thenReturn(ClinVarServiceAnnotator.POSITION);
		when(attributeMetaDataChrom.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		when(attributeMetaDataPos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.LONG.toString().toLowerCase()));

		when(metaDataCanAnnotate.getAttribute(ClinVarServiceAnnotator.CHROMOSOME)).thenReturn(attributeMetaDataChrom);
		when(metaDataCanAnnotate.getAttribute(ClinVarServiceAnnotator.POSITION)).thenReturn(attributeMetaDataPos);

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
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(ClinVarServiceAnnotator.POSITION);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(ClinVarServiceAnnotator.CHROMOSOME)).thenReturn(attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(ClinVarServiceAnnotator.POSITION)).thenReturn(
				attributeMetaDataCantAnnotatePos);

		entity = mock(Entity.class);

		String chrStr = "11";
		Long chrPos = new Long(19207841);
		when(entity.getString(ClinVarServiceAnnotator.CHROMOSOME)).thenReturn(chrStr);
		when(entity.getLong(ClinVarServiceAnnotator.POSITION)).thenReturn(chrPos);

		input = new ArrayList<Entity>();
		input.add(entity);

		AnnotationService annotationService = mock(AnnotationService.class);
		HgncLocationsProvider hgncLocationsProvider = mock(HgncLocationsProvider.class);
		Map<String, HGNCLocations> locationsMap = Collections.singletonMap("CSRP3", new HGNCLocations("CSRP3",
				19207830l, 19207900l, "11"));
		when(hgncLocationsProvider.getHgncLocations()).thenReturn(locationsMap);
		annotator = new ClinVarServiceAnnotator(settings, annotationService, hgncLocationsProvider);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(ClinVarServiceAnnotator.ALLELEID, "53860");
		resultMap.put(ClinVarServiceAnnotator.TYPE, "deletion");
		resultMap.put(ClinVarServiceAnnotator.GENE_NAME, "NM_003476.4:c.282-5_285delAACAGGTCC");
		resultMap.put(ClinVarServiceAnnotator.GENEID, "8048");
		resultMap.put(ClinVarServiceAnnotator.GENESYMBOL, "CSRP3");
		resultMap.put(ClinVarServiceAnnotator.CLINICALSIGNIFICANCE, "Uncertain significance");
		resultMap.put(ClinVarServiceAnnotator.RS_DBSNP, "397516855");
		resultMap.put(ClinVarServiceAnnotator.NSV_DBVAR, "-");
		resultMap.put(ClinVarServiceAnnotator.RCVACCESSION, "RCV000037779");
		resultMap.put(ClinVarServiceAnnotator.TESTEDINGTR, "N");
		resultMap.put(ClinVarServiceAnnotator.PHENOTYPEIDS, "MedGen:CN169374");
		resultMap.put(ClinVarServiceAnnotator.ORIGIN, "germline");
		resultMap.put(ClinVarServiceAnnotator.ASSEMBLY, "GRCh37");
		resultMap.put(ClinVarServiceAnnotator.CLINVAR_CHROMOSOME, "11");
		resultMap.put(ClinVarServiceAnnotator.START, "19207892");
		resultMap.put(ClinVarServiceAnnotator.STOP, "19207900");
		resultMap.put(ClinVarServiceAnnotator.CYTOGENETIC, "11p15.1");
		resultMap.put(ClinVarServiceAnnotator.REVIEWSTATUS, "classified by single submitter");
		resultMap.put(ClinVarServiceAnnotator.HGVS_C, "LRG_440:g.29221_29229delAACAGGTCC");
		resultMap.put(ClinVarServiceAnnotator.HGVS_P, "");
		resultMap.put(ClinVarServiceAnnotator.NUMBERSUBMITTERS, "1");
		resultMap.put(ClinVarServiceAnnotator.LASTEVALUATED, "08 Mar 2012");
		resultMap.put(ClinVarServiceAnnotator.GUIDELINES, "-");
		resultMap.put(ClinVarServiceAnnotator.OTHERIDS, "-");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input.iterator());

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(ClinVarServiceAnnotator.ALLELEID),
				expectedEntity.get(ClinVarServiceAnnotator.ALLELEID));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.TYPE), expectedEntity.get(ClinVarServiceAnnotator.TYPE));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.GENE_NAME),
				expectedEntity.get(ClinVarServiceAnnotator.GENE_NAME));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.GENEID),
				expectedEntity.get(ClinVarServiceAnnotator.GENEID));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.GENESYMBOL),
				expectedEntity.get(ClinVarServiceAnnotator.GENESYMBOL));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.CLINICALSIGNIFICANCE),
				expectedEntity.get(ClinVarServiceAnnotator.CLINICALSIGNIFICANCE));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.RS_DBSNP),
				expectedEntity.get(ClinVarServiceAnnotator.RS_DBSNP));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.NSV_DBVAR),
				expectedEntity.get(ClinVarServiceAnnotator.NSV_DBVAR));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.RCVACCESSION),
				expectedEntity.get(ClinVarServiceAnnotator.RCVACCESSION));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.TESTEDINGTR),
				expectedEntity.get(ClinVarServiceAnnotator.TESTEDINGTR));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.PHENOTYPEIDS),
				expectedEntity.get(ClinVarServiceAnnotator.PHENOTYPEIDS));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.ORIGIN),
				expectedEntity.get(ClinVarServiceAnnotator.ORIGIN));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.ASSEMBLY),
				expectedEntity.get(ClinVarServiceAnnotator.ASSEMBLY));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.CLINVAR_CHROMOSOME),
				expectedEntity.get(ClinVarServiceAnnotator.CLINVAR_CHROMOSOME));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.START), expectedEntity.get(ClinVarServiceAnnotator.START));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.STOP), expectedEntity.get(ClinVarServiceAnnotator.STOP));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.CYTOGENETIC),
				expectedEntity.get(ClinVarServiceAnnotator.CYTOGENETIC));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.REVIEWSTATUS),
				expectedEntity.get(ClinVarServiceAnnotator.REVIEWSTATUS));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.HGVS_C),
				expectedEntity.get(ClinVarServiceAnnotator.HGVS_C));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.HGVS_P),
				expectedEntity.get(ClinVarServiceAnnotator.HGVS_P));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.NUMBERSUBMITTERS),
				expectedEntity.get(ClinVarServiceAnnotator.NUMBERSUBMITTERS));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.LASTEVALUATED),
				expectedEntity.get(ClinVarServiceAnnotator.LASTEVALUATED));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.GUIDELINES),
				expectedEntity.get(ClinVarServiceAnnotator.GUIDELINES));
		assertEquals(resultEntity.get(ClinVarServiceAnnotator.OTHERIDS),
				expectedEntity.get(ClinVarServiceAnnotator.OTHERIDS));
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
