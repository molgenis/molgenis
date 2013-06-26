package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToMrefValueConverterTest
{
	@Test
	public void extractValue()
	{
		Characteristic ch1 = when(mock(Characteristic.class).getName()).thenReturn("ch1").getMock();
		Characteristic ch2 = when(mock(Characteristic.class).getName()).thenReturn("ch2").getMock();

		MrefValue value = new MrefValue();
		value.setValue(Arrays.asList(ch1, ch2));
		CharacteristicLoadingCache characteristicLoadingCache = mock(CharacteristicLoadingCache.class);
		assertEquals(new TupleToMrefValueConverter(characteristicLoadingCache).extractValue(value),
				Arrays.asList("ch1", "ch2"));
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
