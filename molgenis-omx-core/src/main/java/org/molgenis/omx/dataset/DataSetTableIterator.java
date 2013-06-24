package org.molgenis.omx.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class DataSetTableIterator implements Iterator<Tuple>
{

	private final Database db;
	private int currentRow;
	private final List<ObservationSet> observationSets;
	private final List<Field> columns;
	private final ValueConverter valueConverter;

	public DataSetTableIterator(Database db, List<Field> columns, Query<ObservationSet> query) throws DatabaseException
	{
		this.db = db;
		this.columns = columns;
		this.observationSets = query != null ? query.find() : Collections.<ObservationSet> emptyList();
		this.currentRow = 0;
		this.valueConverter = new ValueConverter(db);
	}

	@Override
	public boolean hasNext()
	{
		return (currentRow < observationSets.size());

	}

	@Override
	public Tuple next()
	{
		ObservationSet currentRowToGet = this.observationSets.get(currentRow);

		WritableTuple tuple = new KeyValueTuple();

		Query<ObservedValue> queryObservedValue = db.query(ObservedValue.class);

		// Only retrieve the visible columns
		Collection<String> fieldNames = Collections2.transform(columns, new Function<Field, String>()
		{
			@Override
			public String apply(final Field field)
			{
				return field.getName();
			}
		});

		try
		{
			for (ObservedValue v : queryObservedValue.eq(ObservedValue.OBSERVATIONSET, currentRowToGet.getId())
					.in(ObservedValue.FEATURE_IDENTIFIER, new ArrayList<String>(fieldNames)).find())
			{
				ObservableFeature feature = v.getFeature();
				Object value = valueConverter.extractValue(v.getValue());
				tuple.set(feature.getIdentifier(), value);
			}
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		catch (ValueConverterException e)
		{
			throw new RuntimeException(e);
		}

		currentRow++;

		return tuple;
	}

	@Override
	public void remove()
	{

		throw new UnsupportedOperationException("remove not supported");
	}

}
