package org.molgenis.omx.converters;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
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
	@Override
	public MrefValue fromTuple(Tuple tuple, String colName, Database db, ObservableFeature feature)
			throws ValueConverterException
	{
		List<String> identifierList;
		try
		{
			identifierList = tuple.getList(colName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}
		if (identifierList == null || identifierList.isEmpty()) return null;

		List<Characteristic> characteristics;
		try
		{
			characteristics = db.query(Characteristic.class).in(Characteristic.IDENTIFIER, identifierList).find();
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		if (characteristics.size() != identifierList.size()) throw new ValueConverterException(
				"one or more mref characteristics does not exist [" + StringUtils.join(identifierList, ',') + "]");

		MrefValue mrefValue = new MrefValue();
		mrefValue.setValue(characteristics);
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
				return characteristic.getLabelValue();
			}
		});
	}
}
