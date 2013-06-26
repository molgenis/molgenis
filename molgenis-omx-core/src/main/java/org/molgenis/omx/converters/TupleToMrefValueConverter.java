package org.molgenis.omx.converters;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class TupleToMrefValueConverter implements TupleToValueConverter<MrefValue, List<String>>
{
	private final CharacteristicLoadingCache characteristicLoader;

	public TupleToMrefValueConverter(CharacteristicLoadingCache characteristicLoader)
	{
		if (characteristicLoader == null) throw new IllegalArgumentException("characteristic loader is null");
		this.characteristicLoader = characteristicLoader;
	}

	@Override
	public MrefValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		// get identifiers
		List<String> xrefIdentifiers;
		try
		{
			xrefIdentifiers = tuple.getList(colName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}
		if (xrefIdentifiers == null || xrefIdentifiers.isEmpty()) return null;

		// get characteristics for identifiers
		MrefValue mrefValue = new MrefValue();
		try
		{
			if (xrefIdentifiers.size() == 1)
			{
				mrefValue.setValue(characteristicLoader.findCharacteristic(xrefIdentifiers.get(0)));
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
	public List<String> extractValue(Value value)
	{
		return Lists.transform(((MrefValue) value).getValue(), new Function<Characteristic, String>()
		{
			@Override
			public String apply(Characteristic characteristic)
			{
				return characteristic.getName();
			}
		});
	}
}
