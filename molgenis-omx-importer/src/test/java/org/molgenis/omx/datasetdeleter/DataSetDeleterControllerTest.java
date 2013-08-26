package org.molgenis.omx.datasetdeleter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration
public class DataSetDeleterControllerTest extends AbstractTestNGSpringContextTests
{

	private static List<Category> categories;
	private static DataSet dataset;
	private static Protocol protocolUsed;
	private static Protocol protocol0;
	private static Protocol protocol1;
	private static Protocol protocol2;
	private static Protocol protocol3;
	private static List<Protocol> allProtocols;
	private static List<Protocol> subProtocols;
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

	@Autowired
	private DataSetDeleterController dataSetDeleterController;

	@Autowired
	private Database database;

	@SuppressWarnings("deprecation")
	@Captor
	private final ArgumentCaptor<ArrayList<ObservableFeature>> captorFeatures = new ArgumentCaptor<ArrayList<ObservableFeature>>();

	@SuppressWarnings("deprecation")
	@Captor
	private final ArgumentCaptor<ArrayList<ObservationSet>> captorObservationSets = new ArgumentCaptor<ArrayList<ObservationSet>>();

	@SuppressWarnings("deprecation")
	@Captor
	private final ArgumentCaptor<ArrayList<ObservedValue>> captorObservedValues = new ArgumentCaptor<ArrayList<ObservedValue>>();

	@Configuration
	static class Config
	{
		@Bean
		public DataSetDeleterController dataSetDeleterController()
		{
			return new DataSetDeleterController();
		}

		@Bean
		public Database database() throws Exception
		{
			Database database = mock(Database.class);
			setUp(database);
			return database;
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

	public static void setUp(Database database) throws Exception
	{
		features0 = new ArrayList<ObservableFeature>();
		features1 = new ArrayList<ObservableFeature>();
		allProtocols = new ArrayList<Protocol>();
		subProtocols = new ArrayList<Protocol>();
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
		protocol0.setFeatures(feature0);
		protocol1 = new Protocol();
		protocol1.setDescription("description1");
		protocol1.setIdentifier("identifier1");
		protocol1.setId(1);
		protocol1.setFeatures(feature1);
		protocol2 = new Protocol();
		protocol2.setDescription("description2");
		protocol2.setIdentifier("identifier2");
		protocol2.setSubprotocols(protocol1);
		protocol2.setId(2);
		protocol3 = new Protocol();
		protocol3.setDescription("description3");
		protocol3.setIdentifier("identifier3");
		protocol3.setSubprotocols(protocol1);
		protocol3.setId(3);

		allProtocols.add(protocol0);
		allProtocols.add(protocol1);
		allProtocols.add(protocol2);
		allProtocols.add(protocol3);

		subProtocols.add(protocol0);

		protocolUsed = new Protocol();
		protocolUsed.setDescription("protocolUsed_description");
		protocolUsed.setIdentifier("protocolUsed_identifier");
		protocolUsed.setId(100);
		protocolUsed.setSubprotocols(subProtocols);
		allProtocols.add(protocolUsed);

		dataset = new DataSet();
		dataset.setId(0);
		dataset.setIdentifier("dataset" + 0);
		dataset.setName("datasetname" + 0);
		dataset.setProtocolUsed(protocolUsed);

		observationSet0 = new ObservationSet();
		observationSet0.setId(0);
		observationSet0.setPartOfDataSet(dataset);

		observationSet1 = new ObservationSet();
		observationSet1.setId(1);

		ObservedValue observedValue0 = new ObservedValue();
		observedValue0.setId(0);
		observedValue0.setObservationSet(0);
		observedValue0.setValue_Id(0);
		observedValue0.setFeature_Id(0);
		observedValue0.setFeature_Identifier("feature" + 0);
		observedValue0.setObservationSet(0);
		observedValues0 = new ArrayList<ObservedValue>();
		observedValues0.add(observedValue0);

		ObservedValue observedValue1 = new ObservedValue();
		observedValue1.setId(1);
		observedValue1.setObservationSet(1);
		observedValue1.setValue_Id(1);
		observedValue1.setFeature_Id(1);
		observedValue1.setFeature_Identifier("feature" + 1);
		observedValue1.setObservationSet(1);
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
		when(database.find(DataSet.class, new QueryRule("identifier", Operator.EQUALS, "dataset1"))).thenReturn(
				datasets);

		when(database.find(ObservedValue.class, new QueryRule(ObservedValue.OBSERVATIONSET_ID, Operator.EQUALS, 0)))
				.thenReturn(observedValues0);

		when(database.find(ObservationSet.class, new QueryRule(ObservationSet.PARTOFDATASET, Operator.EQUALS, 0)))
				.thenReturn(observationSets0);

	}

	@BeforeMethod
	public void beforeTest() throws Exception
	{
		reset(database);
		setUp(database);
	}

	@Test
	public void countReferringProtocolsSingleReferences()
	{
		int result = dataSetDeleterController.countReferringProtocols(protocol0, allProtocols);
		assertEquals(1, result);
	}

	@Test
	public void countReferringProtocolMultipleReferences()
	{
		int result = dataSetDeleterController.countReferringProtocols(protocol1, allProtocols);
		assertEquals(2, result);
	}

	@Test
	public void delete() throws DatabaseException, IOException
	{
		dataSetDeleterController.delete("dataset1", true);
		verify(database).remove(dataset);
	}

	@Test
	public void deleteNoMetadata() throws DatabaseException, IOException
	{
		dataSetDeleterController.delete("dataset1", false);
		verify(database, Mockito.times(0)).remove(dataset);
	}

	@Test
	public void deleteCategories() throws DatabaseException
	{
		dataSetDeleterController.deleteCategories(categories);
		verify(database).remove(category0);
	}

	@Test
	public void deleteData() throws DatabaseException
	{
		dataSetDeleterController.deleteData(dataset);
		// verify that only observationsets and abservedvalues belonging to the dataset are removed
		verify(database, Mockito.atLeastOnce()).remove(captorObservationSets.capture());
		assertEquals(new Integer(0), captorObservationSets.getValue().get(0).getId());
		assertEquals(1, captorObservationSets.getValue().size());
	}

	@Test
	public void deleteFeatures() throws DatabaseException
	{
		dataSetDeleterController.deleteFeatures(features0, allProtocols);
		verify(database, Mockito.atLeastOnce()).remove(captorFeatures.capture());
		assertEquals("feature0", captorFeatures.getValue().get(0).getIdentifier());
		assertEquals(1, captorFeatures.getValue().size());
	}

	@Test
	public void deleteObservedValues() throws DatabaseException
	{
		dataSetDeleterController.deleteObservedValues(observationSet0);
		verify(database, Mockito.atLeastOnce()).remove(captorObservedValues.capture());
		assertEquals(new Integer(0), captorObservedValues.getValue().get(0).getId());
		assertEquals(1, captorObservedValues.getValue().size());
	}

	@Test
	public void deleteProtocol() throws DatabaseException
	{
		dataSetDeleterController.deleteProtocol(protocolUsed, allProtocols);
		verify(database).remove(protocolUsed);
		verify(database).remove(subProtocols);
		verify(database, Mockito.times(0)).remove(protocol1);
	}
}
