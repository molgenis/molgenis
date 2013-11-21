package org.molgenis.omx.biobankconnect.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.model.elements.Model;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Tuple;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StoreMappingTableTest
{
	StoreMappingTable table;
	DataService db;
	private static final String dataSetIdentifer = "test-set";
	private static final String OBSERVATION_SET = "observation_set";
	private static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	private static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	private static final List<String> columns = Arrays.asList(STORE_MAPPING_FEATURE, STORE_MAPPING_MAPPED_FEATURE,
			STORE_MAPPING_CONFIRM_MAPPING, OBSERVATION_SET);

	@BeforeMethod
	public void setUp() throws ValueConverterException
	{
		db = mock(DataService.class);

		ObservableFeature feature1 = mock(ObservableFeature.class);
		when(feature1.getIdentifier()).thenReturn(STORE_MAPPING_FEATURE);
		when(feature1.getName()).thenReturn(STORE_MAPPING_FEATURE);
		when(feature1.getDataType()).thenReturn("xref");

		ObservableFeature feature2 = mock(ObservableFeature.class);
		when(feature2.getIdentifier()).thenReturn(STORE_MAPPING_MAPPED_FEATURE);
		when(feature2.getName()).thenReturn(STORE_MAPPING_MAPPED_FEATURE);
		when(feature2.getDataType()).thenReturn("xref");

		ObservableFeature feature3 = mock(ObservableFeature.class);
		when(feature3.getIdentifier()).thenReturn(STORE_MAPPING_CONFIRM_MAPPING);
		when(feature3.getName()).thenReturn(STORE_MAPPING_CONFIRM_MAPPING);
		when(feature3.getDataType()).thenReturn("bool");

		Protocol p = mock(Protocol.class);
		when(p.getFeatures()).thenReturn(Arrays.asList(feature1, feature2, feature3));

		DataSet dataSet = mock(DataSet.class);
		when(dataSet.getIdentifier()).thenReturn(dataSetIdentifer);
		when(dataSet.getProtocolUsed()).thenReturn(p);

		Query q = new QueryImpl();
		q.eq(DataSet.IDENTIFIER, dataSetIdentifer);
		when(db.findOne(DataSet.ENTITY_NAME, q)).thenReturn(dataSet);

		ObservationSet observationSet1 = mock(ObservationSet.class);
		when(observationSet1.getId()).thenReturn(1);
		when(observationSet1.getIdentifier()).thenReturn("1");

		Query q2 = new QueryImpl();
		q2.eq(ObservationSet.PARTOFDATASET, dataSet.getIdentifier());
		when(db.findOne(ObservationSet.ENTITY_NAME, q2)).thenReturn(observationSet1);

		ObservedValue observedValue1 = mock(ObservedValue.class);
		when(observedValue1.getObservationSet()).thenReturn(observationSet1);
		BoolValue value1 = new BoolValue();
		value1.setValue(false);
		when(observedValue1.getValue()).thenReturn(value1);
		
		ObservableFeature observableFeature1 = mock(ObservableFeature.class);
		when(observableFeature1.getIdentifier()).thenReturn(STORE_MAPPING_CONFIRM_MAPPING);
		when(observedValue1.getFeature()).thenReturn(observableFeature1);
		
		Characteristic ch2 = mock(Characteristic.class);
		when(ch2.getId()).thenReturn(2);
		XrefValue value2 = new XrefValue();
		value2.setValue(ch2);

		ObservedValue ov2 = mock(ObservedValue.class);
		when(ov2.getObservationSet().getId()).thenReturn(1);
		when(ov2.getObservationSet().getIdentifier()).thenReturn("1");
		when(ov2.getFeature().getIdentifier()).thenReturn(STORE_MAPPING_FEATURE);
		when(ov2.getValue()).thenReturn(value2);

		Characteristic ch3 = mock(Characteristic.class);
		when(ch3.getId()).thenReturn(3);
		XrefValue value3 = new XrefValue();
		value3.setValue(ch3);

		ObservedValue ov3 = mock(ObservedValue.class);
		when(ov3.getObservationSet().getId()).thenReturn(1);
		when(ov3.getObservationSet().getIdentifier()).thenReturn("1");
		when(ov3.getFeature().getIdentifier()).thenReturn(STORE_MAPPING_MAPPED_FEATURE);
		when(ov3.getValue()).thenReturn(value3);

		Query q3 = new QueryImpl();
		q3.in(ObservedValue.OBSERVATIONSET, Arrays.asList("1"));
		Iterable<Entity> observedValues = Arrays.<Entity>asList(observedValue1, ov2, ov3);
		when(db.findAll(ObservedValue.ENTITY_NAME, q3)).thenReturn(observedValues);

		Model model = mock(Model.class);
		org.molgenis.model.elements.Entity entity = mock(org.molgenis.model.elements.Entity.class);
		when(model.getEntity(ObservableFeature.class.getSimpleName())).thenReturn(entity);

		table = new StoreMappingTable(dataSetIdentifer, db);
	}

	@Test
	public void getAllColumns() throws TableException
	{
		for (Field field : table.getAllColumns())
		{
			assertTrue(columns.contains(field.getName()));
		}
	}

	@Test
	public void getCount() throws TableException
	{
		assertEquals(table.getCount(), 1);
	}

	@Test
	public void iterator()
	{
		Iterator<Tuple> iterator = table.iterator();

		assertTrue(iterator.hasNext());
		Tuple tuple1 = iterator.next();
		assertEquals(tuple1.get(STORE_MAPPING_FEATURE), 2);
		assertEquals(tuple1.get(STORE_MAPPING_MAPPED_FEATURE), 3);
		@SuppressWarnings("unchecked")
		ValueCell<BoolValue> cell = (ValueCell<BoolValue>) tuple1.get(STORE_MAPPING_CONFIRM_MAPPING);
		assertEquals(cell.getValue(), Boolean.FALSE);
	}
}
