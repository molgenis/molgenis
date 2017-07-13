package org.molgenis.das.impl;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.GenomicDataSettings;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.support.GenomicDataSettings.Meta.*;
import static org.testng.Assert.assertEquals;
import static uk.ac.ebi.mydas.model.DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE;

public class RepositoryRangeHandlingDataSourceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;
	@Autowired
	private AttributeFactory attrMetaFactory;

	RepositoryRangeHandlingDataSource source;
	private DasFeature dasFeature;
	private DataService dataService;
	private ArrayList<DasFeature> featureList;
	private GenomicDataSettings genomicDataSettings;

	@BeforeMethod
	public void setUp() throws DataSourceException, MalformedURLException
	{
		dataService = mock(DataService.class);
		genomicDataSettings = mock(GenomicDataSettings.class);

		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBean(DataService.class)).thenReturn(dataService);
		when(ctx.getBean(GenomicDataSettings.class)).thenReturn(genomicDataSettings);
		new ApplicationContextProvider().setApplicationContext(ctx);

		EntityType metaData = entityTypeFactory.create("dataset");
		when(dataService.getEntityType("dataset")).thenReturn(metaData);
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_CHROM, metaData)).thenReturn("CHROM");
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_POS, metaData)).thenReturn("POS");
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_STOP, metaData)).thenReturn("STOP");

		DasType type = new DasType("0", "", "", "type");
		DasMethod method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
		source = new RepositoryRangeHandlingDataSource();
		DataSourceConfiguration dataSourceConfig = mock(DataSourceConfiguration.class);
		PropertyType propertyType = new PropertyType();
		propertyType.setValue("type");
		when(dataSourceConfig.getDataSourceProperties()).thenReturn(Collections.singletonMap("type", propertyType));
		source.init(null, null, dataSourceConfig);
		Map<URL, String> linkout = new HashMap<>();
		linkout.put(new URL("http://www.molgenis.org/"), "Link");

		List<DasTarget> dasTarget = new ArrayList<>();
		dasTarget.add(new MolgenisDasTarget("mutation id", 10, 1000, "description"));
		List<String> notes = new ArrayList<>();
		notes.add("track:dataset");
		notes.add("source:MOLGENIS");

		dasFeature = new DasFeature("mutation id", "mutation name,description", type, method, 10, 1000, new Double(0),
				ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, notes, linkout, dasTarget, new ArrayList<>(),
				null);

		Query<Entity> q = new QueryImpl<>().eq("CHROM", "1");
		q.and().nest();
		q.le("POS", 100000);
		q.and().ge("STOP", 1);
		q.unnest();
		q.pageSize(100);

		EntityType emd = entityTypeFactory.create("DAS");
		emd.addAttribute(attrMetaFactory.create().setName("STOP").setDataType(INT));
		emd.addAttribute(attrMetaFactory.create().setName("linkout"));
		emd.addAttribute(attrMetaFactory.create().setName("NAME"), ROLE_LABEL);
		emd.addAttribute(attrMetaFactory.create().setName("INFO"));
		emd.addAttribute(attrMetaFactory.create().setName("POS").setDataType(INT));
		emd.addAttribute(attrMetaFactory.create().setName("ID"), ROLE_ID);
		emd.addAttribute(attrMetaFactory.create().setName("CHROM"));

		DynamicEntity entity = new DynamicEntity(emd);

		Map<String, Object> map = new HashMap<>();
		map.put("STOP", 1000);
		map.put("linkout", "http://www.molgenis.org/");
		map.put("NAME", "mutation name");
		map.put("INFO", "description");
		map.put("POS", 10);
		map.put("ID", "mutation id");
		map.put("CHROM", "1");

		for (String key : map.keySet())
			entity.set(key, map.get(key));

		featureList = new ArrayList<>();
		featureList.add(dasFeature);
		when(dataService.findAll("dataset", q)).thenAnswer(new Answer<Stream<DynamicEntity>>()
		{
			@Override
			public Stream<DynamicEntity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(entity);
			}
		});

		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_CHROM, entity.getEntityType())).thenReturn(
				"CHROM");
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_POS, entity.getEntityType())).thenReturn(
				"POS");
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_STOP, entity.getEntityType())).thenReturn(
				"STOP");
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_IDENTIFIER,
				entity.getEntityType())).thenReturn("ID");
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_DESCRIPTION,
				entity.getEntityType())).thenReturn("INFO");
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_NAME, entity.getEntityType())).thenReturn(
				"NAME");
		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_LINKOUT,
				entity.getEntityType())).thenReturn("linkout");

	}

	@AfterMethod
	public void teardown()
	{
		Mockito.reset(dataService);
	}

	@Test
	public void getFeaturesSize()
			throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
			CoordinateErrorException
	{
		assertEquals(source.getFeatures("1,dasdataset_dataset", 1, 100000, 100).getFeatures().size(),
				new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getFeatures().size());
	}

	@Test
	public void getFeaturesLabel()
			throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
			CoordinateErrorException
	{
		assertEquals(source.getFeatures("1,dasdataset_dataset", 1, 100000, 100)
						   .getFeatures()
						   .iterator()
						   .next()
						   .getFeatureLabel(),
				new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getFeatures()
																				 .iterator()
																				 .next()
																				 .getFeatureLabel());
	}

	@Test
	public void getFeaturesId() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
			CoordinateErrorException
	{
		assertEquals(source.getFeatures("1,dasdataset_dataset", 1, 100000, 100)
						   .getFeatures()
						   .iterator()
						   .next()
						   .getFeatureId(),
				new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getFeatures()
																				 .iterator()
																				 .next()
																				 .getFeatureId());
	}

	@Test
	public void getFeaturesSegment()
			throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
			CoordinateErrorException
	{
		assertEquals(source.getFeatures("1,dasdataset_dataset", 1, 100000, 100).getSegmentId(),
				new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getSegmentId());
	}

	@Test
	public void getFeaturesStart()
			throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
			CoordinateErrorException
	{
		assertEquals(source.getFeatures("1,dasdataset_dataset", 1, 100000, 100).getStartCoordinate(),
				new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getStartCoordinate());
	}

	@Test
	public void getFeaturesStop()
			throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
			CoordinateErrorException
	{
		assertEquals(source.getFeatures("1,dasdataset_dataset", 1, 100000, 100).getStopCoordinate(),
				new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getStopCoordinate());
	}

	@Test
	public void getTypes() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
			CoordinateErrorException
	{
		assertEquals(Collections.singleton(new DasType("type", null, "?", "type")), source.getTypes());
	}
}
