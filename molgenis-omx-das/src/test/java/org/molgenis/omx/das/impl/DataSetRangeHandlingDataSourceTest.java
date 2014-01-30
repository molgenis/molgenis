package org.molgenis.omx.das.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.HandleRequestDelegationException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;

import com.google.common.collect.Maps;

public class DataSetRangeHandlingDataSourceTest
{
	DatasetRangeHandlingDataSource source;
	private DasFeature dasFeature;
	private DataService dataService;
	private ArrayList<ObservationSet> resultList;
	private ArrayList<DasFeature> featureList;
	private ArrayList<ObservedValue> valueList;

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		dataService = mock(DataService.class);
		DasType type = new DasType("type", null, "?", "type");
		DasMethod method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
		source = new DatasetRangeHandlingDataSource(dataService, type, method);

		Map<URL, String> linkout = new HashMap<URL, String>();

		List<DasTarget> dasTarget = new ArrayList<DasTarget>();
		dasTarget.add(new MolgenisDasTarget("mutation id", 10, 100, "mutation id"));
		dasFeature = new DasFeature("mutation id", "mutation id", type, method, 10, 100, new Double(0),
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE,
				new ArrayList<String>(), linkout, dasTarget, new ArrayList<String>(), null);

		Query q = new QueryImpl().eq(DatasetRangeHandlingDataSource.MUTATION_CHROMOSOME, "1");
		q.pageSize(100);

		DataSet dataSet = mock(DataSet.class);
		when(dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl().eq(DataSet.IDENTIFIER, "dataset"), DataSet.class))
				.thenReturn(dataSet);

		Map<String, Object> map = Maps.newHashMap();
		map.put(DatasetRangeHandlingDataSource.MUTATION_STOP_POSITION, 100);
		map.put(DatasetRangeHandlingDataSource.MUTATION_NAME, "mutation name");
		map.put(DatasetRangeHandlingDataSource.MUTATION_DESCRIPTION, "description");
		map.put(DatasetRangeHandlingDataSource.MUTATION_START_POSITION, 10);
		map.put(DatasetRangeHandlingDataSource.MUTATION_ID, "mutation id");
		map.put(DatasetRangeHandlingDataSource.MUTATION_CHROMOSOME, "1");

		ObservationSet observationSet = mock(ObservationSet.class);
		resultList = new ArrayList<ObservationSet>();
		resultList.add(observationSet);
		ObservedValue observedValue1 = mock(ObservedValue.class);
		ObservedValue observedValue2 = mock(ObservedValue.class);
		ObservedValue observedValue3 = mock(ObservedValue.class);
		ObservedValue observedValue4 = mock(ObservedValue.class);
		Value value1 = mock(Value.class);
		Value value2 = mock(Value.class);
		Value value3 = mock(Value.class);
		Value value4 = mock(Value.class);

		valueList = new ArrayList<ObservedValue>();
		valueList.add(observedValue1);
		valueList.add(observedValue2);
		valueList.add(observedValue3);
		valueList.add(observedValue4);
		featureList = new ArrayList<DasFeature>();
		featureList.add(dasFeature);

		ObservableFeature observableFeature1 = mock(ObservableFeature.class);
		when(observableFeature1.getIdentifier()).thenReturn(DatasetRangeHandlingDataSource.MUTATION_START_POSITION);
		ObservableFeature observableFeature2 = mock(ObservableFeature.class);
		when(observableFeature2.getIdentifier()).thenReturn(DatasetRangeHandlingDataSource.MUTATION_STOP_POSITION);
		ObservableFeature observableFeature3 = mock(ObservableFeature.class);
		when(observableFeature3.getIdentifier()).thenReturn(DatasetRangeHandlingDataSource.MUTATION_CHROMOSOME);
		ObservableFeature observableFeature4 = mock(ObservableFeature.class);
		when(observableFeature4.getIdentifier()).thenReturn(DatasetRangeHandlingDataSource.MUTATION_ID);

		QueryImpl variantQuery = new QueryImpl();
		variantQuery.eq(ObservationSet.PARTOFDATASET, dataSet);
		when(dataService.findAll(ObservationSet.ENTITY_NAME, variantQuery, ObservationSet.class))
				.thenReturn(resultList);
		when(
				dataService.findAll(ObservedValue.ENTITY_NAME,
						new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet), ObservedValue.class))
				.thenReturn(valueList);
		when(observedValue1.getFeature()).thenReturn(observableFeature1);
		when(observedValue2.getFeature()).thenReturn(observableFeature2);
		when(observedValue3.getFeature()).thenReturn(observableFeature3);
		when(observedValue4.getFeature()).thenReturn(observableFeature4);
		when(observedValue1.getValue()).thenReturn(value1);
		when(observedValue2.getValue()).thenReturn(value2);
		when(observedValue3.getValue()).thenReturn(value3);
		when(observedValue4.getValue()).thenReturn(value4);
		when(value1.getInt("value")).thenReturn(10);
		when(value2.getInt("value")).thenReturn(100);
		when(value3.getString("value")).thenReturn("1");
		when(value4.getString("value")).thenReturn("mutation id");
	}

	@AfterMethod
	public void teardown()
	{
		Mockito.reset(dataService);
	}

	@Test()
	public void getFeaturesRange() throws UnimplementedFeatureException, DataSourceException,
			BadReferenceObjectException, CoordinateErrorException
	{
		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getFeatures(), source
				.getFeatures("1,dataset_dataset", 1, 100000, 100).getFeatures());
		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getSegmentId(), source
				.getFeatures("1,dataset_dataset", 1, 100000, 100).getSegmentId());
		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getStartCoordinate(), source
				.getFeatures("1,dataset_dataset", 1, 100000, 100).getStartCoordinate());
		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getStopCoordinate(), source
				.getFeatures("1,dataset_dataset", 1, 100000, 100).getStopCoordinate());
	}

	@Test()
	public void getTypes() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
			CoordinateErrorException
	{
		assertEquals(Collections.singleton(new DasType("type", null, "?", "type")), source.getTypes());
	}
}
