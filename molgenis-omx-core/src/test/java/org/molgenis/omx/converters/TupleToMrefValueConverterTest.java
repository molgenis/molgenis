package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToMrefValueConverterTest
{
	@Test
	public void extractValue()
	{
		Characteristic ch1 = when(mock(Characteristic.class).getLabelValue()).thenReturn("ch1").getMock();
		Characteristic ch2 = when(mock(Characteristic.class).getLabelValue()).thenReturn("ch2").getMock();

		MrefValue value = new MrefValue();
		value.setValue(Arrays.asList(ch1, ch2));
		assertEquals(new TupleToMrefValueConverter().extractValue(value), Arrays.asList("ch1", "ch2"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fromTuple() throws ValueConverterException, DatabaseException
	{
		Characteristic ch1 = when(mock(Characteristic.class).getLabelValue()).thenReturn("ch1").getMock();
		Characteristic ch2 = when(mock(Characteristic.class).getLabelValue()).thenReturn("ch2").getMock();

		Database database = mock(Database.class);
		Query<Characteristic> query = mock(Query.class);
		when(database.query(Characteristic.class)).thenReturn(query);
		Query<Characteristic> query2 = mock(Query.class);
		when(query.in(Characteristic.IDENTIFIER, Arrays.asList("ch1", "ch2"))).thenReturn(query2);
		when(query2.find()).thenReturn(Arrays.asList(ch1, ch2));

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, Arrays.asList("ch1", "ch2"));
		MrefValue value = new TupleToMrefValueConverter().fromTuple(tuple, colName, database, null);
		assertEquals(value.getValue(), Arrays.asList(ch1, ch2));
	}
}
