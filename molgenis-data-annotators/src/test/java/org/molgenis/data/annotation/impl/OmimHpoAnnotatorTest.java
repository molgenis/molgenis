package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.molgenis.data.annotation.provider.HpoMappingProvider;
import org.molgenis.data.annotation.provider.OmimMorbidMapProvider;
import org.molgenis.data.annotation.provider.UrlPinger;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OmimHpoAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
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

		metaDataCanAnnotate = new org.molgenis.data.support.DefaultEntityMetaData("test");

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
		when(metaDataCantAnnotate.getAttribute(VcfRepository.POS)).thenReturn(attributeMetaDataCantAnnotatePos);

		entity = new MapEntity(metaDataCanAnnotate);

		entity.set(VcfRepository.CHROM, "11");
		entity.set(VcfRepository.POS, new Long(19207841));

		input = new ArrayList<Entity>();
		input.add(entity);

		String morbidMapData = "3-M syndrome 1, 273750 (3)|CUL7|609577|6p21.1";
		OmimMorbidMapProvider omimMorbidMapProvider = mock(OmimMorbidMapProvider.class);
		when(omimMorbidMapProvider.getOmimMorbidMap()).thenReturn(new StringReader(morbidMapData));

		String hpoMappingData = "#Format: diseaseId<tab>gene-symbol<tab>gene-id(entrez)<tab>HPO-ID<tab>HPO-term-name\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000463	Anteverted nares\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0004209	Clinodactyly of the 5th finger\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0004322	Short stature\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0003298	Spina bifida occulta\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000574	Thick eyebrow\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0004570	Increased vertebral height\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0001511	Intrauterine growth retardation\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0008734	Decreased testicular size\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0001518	Small for gestational age\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000272	Malar flattening\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000268	Dolichocephaly\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000767	Pectus excavatum\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0010306	Short thorax\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000007	Autosomal recessive inheritance\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0001763	Pes planus\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0002827	Hip dislocation\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000470	Short neck\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0003691	Scapular winging\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0008897	Postnatal growth retardation\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000047	Hypospadias\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000307	Pointed chin\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0002007	Frontal bossing\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0008839	Hypoplastic pelvis\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0003307	Hyperlordosis\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000303	Mandibular prognathia\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0002750	Delayed skeletal maturation\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000325	Triangular face\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0002643	Neonatal respiratory distress\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000773	Short ribs\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000179	Thick lower lip vermilion\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0005280	Depressed nasal bridge\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0000343	Long philtrum\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0001382	Joint hypermobility\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0009237	Short 5th finger\r\n"
				+ "OMIM:273750	CUL7	9820	HP:0003100	Slender long bone";
		HpoMappingProvider hpoMappingProvider = mock(HpoMappingProvider.class);
		when(hpoMappingProvider.getHpoMapping()).thenReturn(new StringReader(hpoMappingData));

		HgncLocationsProvider hgncLocationsProvider = mock(HgncLocationsProvider.class);
		Map<String, HGNCLocations> hgncLocations = Collections.singletonMap("CUL7", new HGNCLocations("CUL7",
				19207841l - 10, 19207841l + 10, "11"));
		when(hgncLocationsProvider.getHgncLocations()).thenReturn(hgncLocations);

		UrlPinger urlPinger = mock(UrlPinger.class);
		MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
		when(molgenisSettings.getProperty(HpoMappingProvider.KEY_HPO_MAPPING, "")).thenReturn("testUrl1");
		when(molgenisSettings.getProperty(HgncLocationsProvider.KEY_HGNC_LOCATIONS_VALUE, "")).thenReturn("testUrl2");
		when(urlPinger.ping("testUrl1", 500)).thenReturn(true);
		when(urlPinger.ping("testUrl2", 500)).thenReturn(true);
		
		annotator = new OmimHpoAnnotator(annotationService, omimMorbidMapProvider, hgncLocationsProvider,
				hpoMappingProvider, molgenisSettings, urlPinger);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(OmimHpoAnnotator.OMIM_DISORDERS, new HashSet<String>(Arrays.asList("3-M syndrome 1")));
		resultMap.put(OmimHpoAnnotator.OMIM_CAUSAL_IDENTIFIER, new HashSet<Integer>(Arrays.asList(609577)));
		resultMap.put(OmimHpoAnnotator.OMIM_TYPE, new HashSet<Integer>(Arrays.asList(3)));
		resultMap.put(OmimHpoAnnotator.OMIM_HGNC_IDENTIFIERS, new HashSet<String>(Arrays.asList("CUL7")));
		resultMap.put(OmimHpoAnnotator.OMIM_CYTOGENIC_LOCATION, new HashSet<String>(Arrays.asList("6p21.1")));
		resultMap.put(OmimHpoAnnotator.OMIM_ENTRY, new HashSet<Integer>(Arrays.asList(273750)));
		resultMap.put(
				OmimHpoAnnotator.HPO_IDENTIFIERS,
				new HashSet<String>(Arrays.asList("HP:0002827", "HP:0009237", "HP:0003100", "HP:0000272", "HP:0000179",
						"HP:0003691", "HP:0008734", "HP:0000463", "HP:0003307", "HP:0000268", "HP:0000767",
						"HP:0005280", "HP:0000574", "HP:0004570", "HP:0000007", "HP:0001763", "HP:0008897",
						"HP:0004209", "HP:0010306", "HP:0000470", "HP:0000343", "HP:0001518", "HP:0000047",
						"HP:0000307", "HP:0004322", "HP:0002007", "HP:0008839", "HP:0000303", "HP:0001511",
						"HP:0002643", "HP:0000325", "HP:0000773", "HP:0002750", "HP:0001382", "HP:0003298")));
		resultMap.put(OmimHpoAnnotator.HPO_GENE_NAME, new HashSet<String>(Arrays.asList("CUL7")));
		resultMap.put(
				OmimHpoAnnotator.HPO_DESCRIPTIONS,
				new LinkedHashSet<String>(Arrays.asList("Long philtrum", "Short thorax", "Hyperlordosis",
						"Short stature", "Anteverted nares", "Hypoplastic pelvis", "Spina bifida occulta",
						"Pes planus", "Clinodactyly of the 5th finger", "Postnatal growth retardation",
						"Joint hypermobility", "Hypospadias", "Malar flattening", "Depressed nasal bridge",
						"Short 5th finger", "Autosomal recessive inheritance", "Mandibular prognathia", "Short neck",
						"Scapular winging", "Small for gestational age", "Triangular face", "Slender long bone",
						"Hip dislocation", "Delayed skeletal maturation", "Frontal bossing", "Pointed chin",
						"Neonatal respiratory distress", "Pectus excavatum", "Decreased testicular size",
						"Thick lower lip vermilion", "Short ribs", "Thick eyebrow", "Increased vertebral height",
						"Intrauterine growth retardation", "Dolichocephaly")));
		resultMap.put(OmimHpoAnnotator.HPO_DISEASE_DATABASE, new HashSet<String>(Arrays.asList("OMIM")));
		resultMap.put(OmimHpoAnnotator.HPO_DISEASE_DATABASE_ENTRY, new HashSet<Integer>(Arrays.asList(273750)));
		resultMap.put(OmimHpoAnnotator.HPO_ENTREZ_ID, new HashSet<Integer>(Arrays.asList(9820)));

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input);

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_DISORDERS),
				expectedEntity.get(OmimHpoAnnotator.OMIM_DISORDERS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_CAUSAL_IDENTIFIER),
				expectedEntity.get(OmimHpoAnnotator.OMIM_CAUSAL_IDENTIFIER));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_IDENTIFIERS),
				expectedEntity.get(OmimHpoAnnotator.OMIM_IDENTIFIERS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_TYPE), expectedEntity.get(OmimHpoAnnotator.OMIM_TYPE));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_HGNC_IDENTIFIERS),
				expectedEntity.get(OmimHpoAnnotator.OMIM_HGNC_IDENTIFIERS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_CYTOGENIC_LOCATION),
				expectedEntity.get(OmimHpoAnnotator.OMIM_CYTOGENIC_LOCATION));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_ENTRY), expectedEntity.get(OmimHpoAnnotator.OMIM_ENTRY));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_IDENTIFIERS),
				expectedEntity.get(OmimHpoAnnotator.HPO_IDENTIFIERS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_GENE_NAME),
				expectedEntity.get(OmimHpoAnnotator.HPO_GENE_NAME));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_DESCRIPTIONS),
				expectedEntity.get(OmimHpoAnnotator.HPO_DESCRIPTIONS));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_DISEASE_DATABASE),
				expectedEntity.get(OmimHpoAnnotator.HPO_DISEASE_DATABASE));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_DISEASE_DATABASE_ENTRY),
				expectedEntity.get(OmimHpoAnnotator.HPO_DISEASE_DATABASE_ENTRY));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_ENTREZ_ID),
				expectedEntity.get(OmimHpoAnnotator.HPO_ENTREZ_ID));
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
