package org.molgenis.omx.converters;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToXrefValueConverter implements TupleToValueConverter<XrefValue, String>
{
	private final CharacteristicLoadingCache characteristicLoader;

	public TupleToXrefValueConverter(CharacteristicLoadingCache characteristicLoader)
	{
		if (characteristicLoader == null) throw new IllegalArgumentException("characteristic loader is null");
		this.characteristicLoader = characteristicLoader;
	}

	@Override
	public XrefValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new XrefValue());
	}

	@Override
	public XrefValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof XrefValue))
		{
			throw new ValueConverterException("value is not a " + XrefValue.class.getSimpleName());
		}

		String xrefIdentifier = tuple.getString(colName);
		if (xrefIdentifier == null) return null;

		Characteristic characteristic;
		try
		{
			characteristic = characteristicLoader.findCharacteristic(xrefIdentifier);
		}
		catch (DatabaseException e)
		{
			throw new ValueConverterException(e);
		}
		if (characteristic == null)
		{
			throw new ValueConverterException("unknown characteristic identifier [" + xrefIdentifier + ']');
		}

		XrefValue xrefValue = (XrefValue) value;
		xrefValue.setValue(characteristic);
		return xrefValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof XrefValue))
		{
			throw new ValueConverterException("value is not a " + XrefValue.class.getSimpleName());
		}
		Characteristic xrefCharacteristic = ((XrefValue) value).getValue();
		return new ValueCell<String>(xrefCharacteristic.getIdentifier(), xrefCharacteristic.getName());
	}
}
