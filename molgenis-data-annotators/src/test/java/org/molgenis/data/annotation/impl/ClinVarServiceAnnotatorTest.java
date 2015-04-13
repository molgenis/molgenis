package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.molgenis.data.annotation.impl.datastructures.ClinvarData;
import org.molgenis.data.annotation.provider.ClinvarDataProvider;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClinVarServiceAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private ClinVarServiceAnnotator annotator;
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
		when(settings.getProperty(ClinVarServiceAnnotator.CLINVAR_FILE_LOCATION_PROPERTY)).thenReturn(
				ResourceUtils.getFile(getClass(), "/clinvar_example.txt").getPath());

		metaDataCanAnnotate = new DefaultEntityMetaData("test");
		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);
		attributeMetaDataRef = mock(AttributeMetaData.class);
		attributeMetaDataAlt = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(VcfRepository.CHROM);
		when(attributeMetaDataPos.getName()).thenReturn(VcfRepository.POS);
		when(attributeMetaDataRef.getName()).thenReturn(VcfRepository.REF);
		when(attributeMetaDataAlt.getName()).thenReturn(VcfRepository.ALT);
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
		when(attributeMetaDataCantAnnotateChrom.getName()).thenReturn(VcfRepository.CHROM);
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotatePos = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(VcfRepository.POS);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		attributeMetaDataCantAnnotateRef = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateRef.getName()).thenReturn(VcfRepository.REF);
		when(attributeMetaDataCantAnnotateRef.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotateAlt = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateAlt.getName()).thenReturn(VcfRepository.ALT);
		when(attributeMetaDataCantAnnotateAlt.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(VcfRepository.CHROM)).thenReturn(attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(VcfRepository.POS)).thenReturn(
				attributeMetaDataCantAnnotatePos);
		when(metaDataCantAnnotate.getAttribute(VcfRepository.REF)).thenReturn(attributeMetaDataRef);
		when(metaDataCantAnnotate.getAttribute(VcfRepository.ALT)).thenReturn(attributeMetaDataAlt);

		entity = new MapEntity(metaDataCanAnnotate);

		String chrStr = "12";
		Long chrPos = new Long(57966471);
		String chrRef = "G";
		String chrAlt = "A";
		entity.set(VcfRepository.CHROM, chrStr);
		entity.set(VcfRepository.POS, chrPos);
		entity.set(VcfRepository.REF, chrRef);
		entity.set(VcfRepository.ALT, chrAlt);

		input = new ArrayList<Entity>();
		input.add(entity);

		ClinvarDataProvider clinvarDataProvider = mock(ClinvarDataProvider.class);
		AnnotationService annotationService = mock(AnnotationService.class);

		Map<List<String>, ClinvarData> clinvarDataMap = Collections.singletonMap(Arrays.asList("12", "57966471", "G",
				"A"), new ClinvarData("82492", "single nucleotide variant", "KIF5A:c.1678G>A (p.Glu560Lys)", "3798",
				"KIF5A", "not provided", "142701108", "-", "RCV000062571", "N", "MedGen:C0025202,SNOMED CT:2092003",
				"somatic", "GRCh37", "12", "57966471", "57966471", "12q13.3", "not classified by submitter",
				"NM_004984.2:c.1678G>A", "NP_004975.2:p.Glu560Lys", "1", "-", "-", "ClinVar:NM_004984.2:c.1678G>A",
				"71601"));

		when(clinvarDataProvider.getClinvarData()).thenReturn(clinvarDataMap);
		
		annotator = new ClinVarServiceAnnotator(settings, annotationService, clinvarDataProvider);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(ClinVarServiceAnnotator.ALLELEID, "82492");
		resultMap.put(ClinVarServiceAnnotator.TYPE, "single nucleotide variant");
		resultMap.put(ClinVarServiceAnnotator.GENE_NAME, "KIF5A:c.1678G>A (p.Glu560Lys)");
		resultMap.put(ClinVarServiceAnnotator.GENEID, "3798");
		resultMap.put(ClinVarServiceAnnotator.GENESYMBOL, "KIF5A");
		resultMap.put(ClinVarServiceAnnotator.CLINICALSIGNIFICANCE, "not provided");
		resultMap.put(ClinVarServiceAnnotator.RS_DBSNP, "142701108");
		resultMap.put(ClinVarServiceAnnotator.NSV_DBVAR, "-");
		resultMap.put(ClinVarServiceAnnotator.RCVACCESSION, "RCV000062571");
		resultMap.put(ClinVarServiceAnnotator.TESTEDINGTR, "N");
		resultMap.put(ClinVarServiceAnnotator.PHENOTYPEIDS, "MedGen:C0025202,SNOMED CT:2092003");
		resultMap.put(ClinVarServiceAnnotator.ORIGIN, "somatic");
		resultMap.put(ClinVarServiceAnnotator.ASSEMBLY, "GRCh37");
		resultMap.put(ClinVarServiceAnnotator.CLINVAR_CHROMOSOME, "12");
		resultMap.put(ClinVarServiceAnnotator.START, "57966471");
		resultMap.put(ClinVarServiceAnnotator.STOP, "57966471");
		resultMap.put(ClinVarServiceAnnotator.CYTOGENETIC, "12q13.3");
		resultMap.put(ClinVarServiceAnnotator.REVIEWSTATUS, "not classified by submitter");
		resultMap.put(ClinVarServiceAnnotator.HGVS_C, "NM_004984.2:c.1678G>A");
		resultMap.put(ClinVarServiceAnnotator.HGVS_P, "NP_004975.2:p.Glu560Lys");
		resultMap.put(ClinVarServiceAnnotator.NUMBERSUBMITTERS, "1");
		resultMap.put(ClinVarServiceAnnotator.LASTEVALUATED, "-");
		resultMap.put(ClinVarServiceAnnotator.GUIDELINES, "-");
		resultMap.put(ClinVarServiceAnnotator.OTHERIDS, "ClinVar:NM_004984.2:c.1678G>A");
		resultMap.put(ClinVarServiceAnnotator.VARIANTIDS, "71601");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input);

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
		assertEquals(annotator.canAnnotate(metaDataCanAnnotate), "true");
	}

	@Test
	public void canAnnotateFalseTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCantAnnotate), "a required attribute has the wrong datatype");
	}

}
