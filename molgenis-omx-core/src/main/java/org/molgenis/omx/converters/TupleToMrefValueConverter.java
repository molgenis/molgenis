package org.molgenis.omx.converters;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class TupleToMrefValueConverter implements TupleToValueConverter<MrefValue, List<Cell<String>>>
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
		List<String> xrefIdentifiers = new ArrayList<String>();
		List<String> xrefIdentifiersPreTrim;

		try
		{
			xrefIdentifiersPreTrim = tuple.getList(colName);
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
			for (String e : xrefIdentifiersPreTrim)
			{

				xrefIdentifiers.add(e.trim());
			}
		}

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
	public Cell<List<Cell<String>>> toCell(Value value)
	{
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
