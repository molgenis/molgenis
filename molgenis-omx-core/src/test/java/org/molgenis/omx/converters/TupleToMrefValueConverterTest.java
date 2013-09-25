package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToMrefValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		Characteristic ch1 = mock(Characteristic.class);
		when(ch1.getIdentifier()).thenReturn("ch1");
		when(ch1.getName()).thenReturn("ch #1");
		Characteristic ch2 = mock(Characteristic.class);
		when(ch2.getIdentifier()).thenReturn("ch2");
		when(ch2.getName()).thenReturn("ch #2");

		MrefValue value = new MrefValue();
		value.setValue(Arrays.asList(ch1, ch2));
		CharacteristicLoadingCache characteristicLoadingCache = mock(CharacteristicLoadingCache.class);
		List<Cell<String>> cells = new TupleToMrefValueConverter(characteristicLoadingCache).toCell(value).getValue();

		Iterator<Cell<String>> it = cells.iterator();
		assertTrue(it.hasNext());
		Cell<String> cell1 = it.next();
		assertEquals(cell1.getKey(), "ch1");
		assertEquals(cell1.getValue(), "ch #1");
		assertTrue(it.hasNext());
		Cell<String> cell2 = it.next();
		assertEquals(cell2.getKey(), "ch2");
		assertEquals(cell2.getValue(), "ch #2");
		assertFalse(it.hasNext());
	}

	@Test
	public void fromTuple() throws ValueConverterException, DatabaseException
	{
		CharacteristicLoadingCache characteristicLoadingCache = mock(CharacteristicLoadingCache.class);
		Characteristic ch1 = when(mock(Characteristic.class).getName()).thenReturn("ch1").getMock();
		Characteristic ch2 = when(mock(Characteristic.class).getName()).thenReturn("ch2").getMock();
		when(characteristicLoadingCache.findCharacteristics(Arrays.asList("ch1", "ch2"))).thenReturn(
				Arrays.asList(ch1, ch2));
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, Arrays.asList("ch1", "ch2"));
		MrefValue value = new TupleToMrefValueConverter(characteristicLoadingCache).fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), Arrays.asList(ch1, ch2));
	}
}
