package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToXrefValueConverter implements EntityToValueConverter<XrefValue, String>
{
	private final CharacteristicLoadingCache characteristicLoader;

	public EntityToXrefValueConverter(CharacteristicLoadingCache characteristicLoader)
	{
		if (characteristicLoader == null) throw new IllegalArgumentException("characteristic loader is null");
		this.characteristicLoader = characteristicLoader;
	}

	@Override
	public XrefValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new XrefValue());
	}

	@Override
	public XrefValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof XrefValue))
		{
			throw new ValueConverterException("value is not a " + XrefValue.class.getSimpleName());
		}

		String xrefIdentifier = entity.getString(attributeName);
		if (xrefIdentifier == null) return null;

		Characteristic characteristic;
		try
		{
			characteristic = characteristicLoader.findCharacteristic(xrefIdentifier);
		}
		catch (MolgenisDataException e)
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
