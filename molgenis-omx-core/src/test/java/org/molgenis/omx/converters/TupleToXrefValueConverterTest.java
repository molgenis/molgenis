package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToXrefValueConverterTest
{
	@Test
	public void extractValue()
	{
		Characteristic ch1 = when(mock(Characteristic.class).getName()).thenReturn("ch1").getMock();

		XrefValue value = new XrefValue();
		value.setValue(ch1);
		CharacteristicLoadingCache characteristicLoadingCache = mock(CharacteristicLoadingCache.class);
		assertEquals(new TupleToXrefValueConverter(characteristicLoadingCache).extractValue(value), "ch1");
	}

	@Test
	public void fromTuple() throws ValueConverterException, DatabaseException
	{
		CharacteristicLoadingCache characteristicLoadingCache = mock(CharacteristicLoadingCache.class);
		Characteristic ch1 = when(mock(Characteristic.class).getName()).thenReturn("ch1").getMock();
		Characteristic ch2 = when(mock(Characteristic.class).getName()).thenReturn("ch2").getMock();
		when(characteristicLoadingCache.findCharacteristic("ch1")).thenReturn(ch1);
		when(characteristicLoadingCache.findCharacteristic("ch2")).thenReturn(ch2);
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "ch1");
		XrefValue value = new TupleToXrefValueConverter(characteristicLoadingCache).fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), ch1);
	}
}
