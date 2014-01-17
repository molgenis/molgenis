package org.molgenis.omx.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class EntityToMrefValueConverter implements EntityToValueConverter<MrefValue, List<Cell<String>>>
{
	private final CharacteristicLoadingCache characteristicLoader;

	public EntityToMrefValueConverter(CharacteristicLoadingCache characteristicLoader)
	{
		if (characteristicLoader == null) throw new IllegalArgumentException("characteristic loader is null");
		this.characteristicLoader = characteristicLoader;
	}

	@Override
	public MrefValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new MrefValue());
	}

	@Override
	public MrefValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof MrefValue))
		{
			throw new ValueConverterException("value is not a " + MrefValue.class.getSimpleName());
		}

		// get identifiers
		List<String> xrefIdentifiers = new ArrayList<String>();
		List<String> xrefIdentifiersPreTrim;

		try
		{
			xrefIdentifiersPreTrim = entity.getList(attributeName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}

		if (xrefIdentifiersPreTrim == null || xrefIdentifiersPreTrim.isEmpty())
		{
			return null;
		}
		else
		{
			for (String identifier : xrefIdentifiersPreTrim)
			{

				xrefIdentifiers.add(identifier.trim());
			}
		}

		// get characteristics for identifiers
		MrefValue mrefValue = (MrefValue) value;
		try
		{
			if (xrefIdentifiers.size() == 1)
			{
				mrefValue.setValue(Arrays.asList(characteristicLoader.findCharacteristic(xrefIdentifiers.get(0))));
			}
			else
			{
				mrefValue.setValue(characteristicLoader.findCharacteristics(xrefIdentifiers));
			}
		}
		catch (DatabaseException e)
		{
			throw new ValueConverterException(e);
		}
		return mrefValue;
	}

	@Override
	public Cell<List<Cell<String>>> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof MrefValue))
		{
			throw new ValueConverterException("value is not a " + MrefValue.class.getSimpleName());
		}
		List<Cell<String>> mrefList = Lists.transform(((MrefValue) value).getValue(),
				new Function<Characteristic, Cell<String>>()
				{
					@Override
					public Cell<String> apply(Characteristic characteristic)
					{
						return new ValueCell<String>(characteristic.getIdentifier(), characteristic.getName());
					}
				});
		return new ValueCell<List<Cell<String>>>(mrefList);
	}
}
