package org.molgenis.omx.harmonization.tupleTables;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;
import org.molgenis.model.elements.Model;
import org.molgenis.omx.biobankconnect.utils.StoreMappingTable;
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
	Database db;
	private static final String dataSetIdentifer = "test-set";
	private static final String OBSERVATION_SET = "observation_set";
	private static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	private static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	private static final List<String> columns = Arrays.asList(STORE_MAPPING_FEATURE, STORE_MAPPING_MAPPED_FEATURE,
			STORE_MAPPING_CONFIRM_MAPPING, OBSERVATION_SET);

	@BeforeMethod
	public void setUp() throws DatabaseException, ValueConverterException
	{
		db = mock(Database.class);

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

		when(db.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, dataSetIdentifer))).thenReturn(
				Arrays.asList(dataSet));

		ObservationSet observation1 = mock(ObservationSet.class);
		when(observation1.getId()).thenReturn(1);

		when(
				db.find(ObservationSet.class, new QueryRule(ObservationSet.PARTOFDATASET_IDENTIFIER, Operator.EQUALS,
						dataSet.getIdentifier()))).thenReturn(Arrays.asList(observation1));

		BoolValue value1 = new BoolValue();
		value1.setValue(false);

		ObservedValue ov1 = mock(ObservedValue.class);
		when(ov1.getObservationSet_Id()).thenReturn(1);
		when(ov1.getFeature_Identifier()).thenReturn(STORE_MAPPING_CONFIRM_MAPPING);
		when(ov1.getValue()).thenReturn(value1);

		Characteristic ch2 = mock(Characteristic.class);
		when(ch2.getId()).thenReturn(2);
		XrefValue value2 = new XrefValue();
		value2.setValue(ch2);

		ObservedValue ov2 = mock(ObservedValue.class);
		when(ov2.getObservationSet_Id()).thenReturn(1);
		when(ov2.getFeature_Identifier()).thenReturn(STORE_MAPPING_FEATURE);
		when(ov2.getValue()).thenReturn(value2);

		Characteristic ch3 = mock(Characteristic.class);
		when(ch3.getId()).thenReturn(3);
		XrefValue value3 = new XrefValue();
		value3.setValue(ch3);

		ObservedValue ov3 = mock(ObservedValue.class);
		when(ov3.getObservationSet_Id()).thenReturn(1);
		when(ov3.getFeature_Identifier()).thenReturn(STORE_MAPPING_MAPPED_FEATURE);
		when(ov3.getValue()).thenReturn(value3);

		when(
				db.find(ObservedValue.class,
						new QueryRule(ObservedValue.OBSERVATIONSET_ID, Operator.IN, Arrays.asList(1)))).thenReturn(
				Arrays.asList(ov1, ov2, ov3));

		Model model = mock(Model.class);
		Entity entity = mock(Entity.class);
		when(model.getEntity(ObservableFeature.class.getSimpleName())).thenReturn(entity);
		when(db.getMetaData()).thenReturn(model);

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
	public void getDb()
	{
		assertEquals(table.getDb(), db);
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
