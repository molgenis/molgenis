package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToXrefValueConverterTest
{
	@Test
	public void extractValue()
	{
		Characteristic ch1 = when(mock(Characteristic.class).getLabelValue()).thenReturn("ch1").getMock();

		XrefValue value = new XrefValue();
		value.setValue(ch1);
		assertEquals(new TupleToXrefValueConverter().extractValue(value), "ch1");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fromTuple() throws ValueConverterException, DatabaseException
	{
		Characteristic ch1 = when(mock(Characteristic.class).getLabelValue()).thenReturn("ch1").getMock();

		Database database = mock(Database.class);

		Query<Characteristic> query = mock(Query.class);
		when(database.query(Characteristic.class)).thenReturn(query);
		when(query.eq(Characteristic.IDENTIFIER, "ch1")).thenReturn(query);
		when(query.find()).thenReturn(Arrays.asList(ch1));

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, Arrays.asList("ch1", "ch2"));
		XrefValue value = new TupleToXrefValueConverter().fromTuple(tuple, colName, database, null);
		assertEquals(value.getValue(), ch1);
	}
}
