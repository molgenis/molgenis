package org.molgenis.omx.datasetdeleter;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.omx.observ.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration
public class DataSetDeleterServiceImplTest extends AbstractTestNGSpringContextTests
{
	private static List<Category> categories;
	private static DataSet dataset;
	private static Protocol protocolUsed;
	private static Protocol protocol0;
	private static Protocol protocol1;
	private static Protocol protocol2;
	private static Protocol protocol3;
	private static List<Entity> allEntities;
	private static ArrayList<Protocol> subProtocols;
	private static ObservationSet observationSet0;
	private static ObservationSet observationSet1;
	private static List<ObservedValue> observedValues0;
	private static List<ObservedValue> observedValues1;
	private static List<ObservableFeature> features0;
	private static List<ObservableFeature> features1;
	private static ObservableFeature feature1;
	private static ObservableFeature feature0;
	private static Category category0;
	private static List<ObservationSet> observationSets0;
	private static List<Protocol> subProtocols1;
	private static CategoricalValue categoricalValue;

	@Autowired
	private DataSetDeleterServiceImpl dataSetDeleterServiceImpl;

	@Autowired
	private DataService dataService;

	@SuppressWarnings("deprecation")
	@Captor
	private final ArgumentCaptor<ObservableFeature> captorFeature = new ArgumentCaptor<ObservableFeature>();

	@SuppressWarnings("deprecation")
	@Captor
	private final ArgumentCaptor<LinkedList<ObservationSet>> captorObservationSets = new ArgumentCaptor<LinkedList<ObservationSet>>();

	@SuppressWarnings("deprecation")
	@Captor
	private final ArgumentCaptor<ObservationSet> captorObservationSet = new ArgumentCaptor<ObservationSet>();

	@SuppressWarnings("deprecation")
	@Captor
	private final ArgumentCaptor<ArrayList<ObservedValue>> captorObservedValues = new ArgumentCaptor<ArrayList<ObservedValue>>();

	@Configuration
	static class Config
	{
		@Bean
		public DataSetDeleterServiceImpl dataSetDeleterServiceImpl() throws Exception
		{
			return new DataSetDeleterServiceImpl(dataService(), searchService());
		}

		@Bean
		public DataService dataService() throws Exception
		{
			DataService dataService = mock(DataService.class);
			setUp(dataService);
			return dataService;
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public SearchService searchService()
		{
			return mock(SearchService.class);
		}
	}

	public static void setUp(DataService dataService) throws Exception
	{
		features0 = new ArrayList<ObservableFeature>();
		features1 = new ArrayList<ObservableFeature>();
		allEntities = new ArrayList<Entity>();
		subProtocols = new ArrayList<Protocol>();
		subProtocols1 = new ArrayList<Protocol>();
		categories = new ArrayList<Category>();

		feature0 = new ObservableFeature();
		feature0.setId(0);
		feature0.setName("featureName" + 0);
		feature0.setIdentifier("feature" + 0);

		feature1 = new ObservableFeature();
		feature1.setId(1);
		feature1.setName("featureName" + 1);
		feature1.setIdentifier("feature" + 1);

		features0.add(feature0);
		features1.add(feature1);

		protocol0 = new Protocol();
		protocol0.setDescription("description0");
		protocol0.setIdentifier("identifier0");
		protocol0.setId(0);
		protocol0.setFeatures(features0);
		protocol1 = new Protocol();
		protocol1.setDescription("description1");
		protocol1.setIdentifier("identifier1");
		protocol1.setId(1);
		protocol1.setFeatures(features1);
		subProtocols1.add(protocol1);
		protocol2 = new Protocol();
		protocol2.setDescription("description2");
		protocol2.setIdentifier("identifier2");
		protocol2.setSubprotocols(subProtocols1);
		protocol2.setId(2);
		protocol3 = new Protocol();
		protocol3.setDescription("description3");
		protocol3.setIdentifier("identifier3");
		protocol3.setSubprotocols(subProtocols1);
		protocol3.setId(3);

		allEntities.add(protocol0);
		allEntities.add(protocol1);
		allEntities.add(protocol2);
		allEntities.add(protocol3);

		subProtocols.add(protocol0);

		protocolUsed = new Protocol();
		protocolUsed.setDescription("protocolUsed_description");
		protocolUsed.setIdentifier("protocolUsed_identifier");
		protocolUsed.setId(100);
		protocolUsed.setSubprotocols(subProtocols);
		allEntities.add(protocolUsed);

		dataset = new DataSet();
		dataset.setId(0);
		dataset.setIdentifier("dataset" + 0);
		dataset.setName("datasetname" + 0);
		dataset.setProtocolUsed(protocolUsed);

		observationSet0 = new ObservationSet();
		observationSet0.setId(0);
		observationSet0.setIdentifier("observationSet0");
		observationSet0.setPartOfDataSet(dataset);

		observationSets0 = new ArrayList<ObservationSet>();
		observationSets0.add(observationSet0);

		observationSet1 = new ObservationSet();
		observationSet1.setId(1);

		ObservedValue observedValue0 = new ObservedValue();
		observedValue0.setId(0);
		observedValue0.setObservationSet(observationSet0);
		observedValue0.setValue(new Value());
		observedValue0.setFeature(feature0);
		observedValue0.setObservationSet(observationSet0);
		observedValues0 = new ArrayList<ObservedValue>();
		observedValues0.add(observedValue0);

		ObservedValue observedValue1 = new ObservedValue();
		observedValue1.setId(1);
		observedValue1.setObservationSet(observationSet1);
		Value v1 = new Value();
		v1.setId(1);
		observedValue1.setValue(v1);
		observedValue1.setFeature(feature1);
		observedValue1.setObservationSet(observationSet1);
		observedValues1 = new ArrayList<ObservedValue>();
		observedValues1.add(observedValue1);

		observationSets0 = new ArrayList<ObservationSet>();
		observationSets0.add(observationSet0);

		category0 = new Category();
		category0.setId(0);
		category0.setIdentifier("category" + 0);
		categories.add(category0);

		List<DataSet> datasets = new ArrayList<DataSet>();
		datasets.add(dataset);
	}

	@BeforeMethod
	public void beforeTest() throws Exception
	{
		reset(dataService);
		setUp(dataService);
	}

	@Test
	public void countReferringProtocolsSingleReferences()
	{
		int result = dataSetDeleterServiceImpl.countReferringEntities(protocol0, allEntities);
		assertEquals(1, result);
	}

	@Test
	public void countReferringProtocolMultipleReferences()
	{
		int result = dataSetDeleterServiceImpl.countReferringEntities(protocol1, allEntities);
		assertEquals(2, result);
	}

	@Test
	public void deleteOnlyData() throws IOException
	{
		when(
				dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl().eq(DataSet.IDENTIFIER, "dataset0"),
						DataSet.class)).thenReturn(dataset);
		when(
				dataService.findAll(ObservationSet.ENTITY_NAME,
						new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataset), ObservationSet.class)).thenReturn(
				observationSets0);

		when(
				dataService.findAll(ObservedValue.ENTITY_NAME,
						new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet0), ObservedValue.class))
				.thenReturn(observedValues0);

		dataSetDeleterServiceImpl.deleteData("dataset0", false);
		verify(dataService).delete(eq(ObservedValue.ENTITY_NAME), captorObservedValues.capture());
		verify(dataService).delete(ObservationSet.ENTITY_NAME, observationSet0);
	}

	@Test
	public void deleteCategories()
	{
		CategoricalValue categoricalValue = mock(CategoricalValue.class);
		when(
				dataService.findAll(CategoricalValue.ENTITY_NAME,
						new QueryImpl().eq(CategoricalValue.VALUE, category0), CategoricalValue.class)).thenReturn(
				Arrays.asList(categoricalValue));

		dataSetDeleterServiceImpl.deleteCategories(categories);

		verify(dataService).delete(Category.ENTITY_NAME, category0);
		verify(dataService).delete(CategoricalValue.ENTITY_NAME, categoricalValue);
	}

	@Test
	public void deleteData()
	{
		when(
				dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl().eq(DataSet.IDENTIFIER, "dataset0"),
						DataSet.class)).thenReturn(dataset);
		when(
				dataService.findAll(ObservationSet.ENTITY_NAME,
						new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataset), ObservationSet.class)).thenReturn(
				observationSets0);

		when(
				dataService.findAll(ObservedValue.ENTITY_NAME,
						new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet0), ObservedValue.class))
				.thenReturn(observedValues0);

		dataSetDeleterServiceImpl.deleteData(dataset);

		// verify that only observationsets and abservedvalues belonging to the dataset are removed
		verify(dataService, Mockito.atLeastOnce()).delete(eq(ObservationSet.ENTITY_NAME),
				captorObservationSet.capture());

		assertEquals(new Integer(0), captorObservationSet.getValue().getId());
	}

	@Test
	public void deleteFeatures()
	{
		when(dataService.hasRepository("feature0-LUT")).thenReturn(false);
		when(
				dataService.findOne(Characteristic.ENTITY_NAME,
						new QueryImpl().eq(Characteristic.IDENTIFIER, "feature0"), Characteristic.class)).thenReturn(
				feature0);
		when(
				dataService.findOne(ObservableFeature.ENTITY_NAME,
						new QueryImpl().eq(ObservableFeature.IDENTIFIER, "feature0"), ObservableFeature.class))
				.thenReturn(feature0);

		dataSetDeleterServiceImpl.deleteFeatures(features0, allEntities);
		verify(dataService, Mockito.atLeastOnce()).delete(eq(Characteristic.ENTITY_NAME), captorFeature.capture());
		verify(dataService, Mockito.atLeastOnce()).delete(eq(ObservableFeature.ENTITY_NAME), captorFeature.capture());

		assertEquals("feature0", captorFeature.getValue().getIdentifier());
	}

	@Test
	public void deleteProtocol()
	{
		dataSetDeleterServiceImpl.deleteProtocol(protocolUsed, allEntities);
		verify(dataService).delete(Protocol.ENTITY_NAME, protocolUsed);
		verify(dataService).delete(Protocol.ENTITY_NAME, protocol0);
		verify(dataService, Mockito.times(0)).delete(Protocol.ENTITY_NAME, protocol1);
	}
}
