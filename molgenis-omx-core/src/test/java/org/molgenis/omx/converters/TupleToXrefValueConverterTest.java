package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.util.Cell;
import org.testng.annotations.Test;

public class TupleToXrefValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		Characteristic ch1 = mock(Characteristic.class);
		when(ch1.getIdentifier()).thenReturn("ch1");
		when(ch1.getName()).thenReturn("ch #1");
		XrefValue value = new XrefValue();
		value.setValue(ch1);
		CharacteristicLoadingCache characteristicLoadingCache = mock(CharacteristicLoadingCache.class);
		Cell<String> cell = new EntityToXrefValueConverter(characteristicLoadingCache).toCell(value);
		assertEquals(cell.getKey(), "ch1");
		assertEquals(cell.getValue(), "ch #1");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		CharacteristicLoadingCache characteristicLoadingCache = mock(CharacteristicLoadingCache.class);
		Characteristic ch1 = when(mock(Characteristic.class).getName()).thenReturn("ch1").getMock();
		Characteristic ch2 = when(mock(Characteristic.class).getName()).thenReturn("ch2").getMock();
		when(characteristicLoadingCache.findCharacteristic("ch1")).thenReturn(ch1);
		when(characteristicLoadingCache.findCharacteristic("ch2")).thenReturn(ch2);
		String colName = "col";
		Entity entity = new MapEntity(colName, "ch1");
		XrefValue value = new EntityToXrefValueConverter(characteristicLoadingCache).fromEntity(entity, colName, null);
		assertEquals(value.getValue(), ch1);
	}
}
