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
import org.molgenis.omx.converters.observedvalue.ValueConverter;
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

	public DataSetTableIterator(Database db, List<Field> columns, Query<ObservationSet> query) throws DatabaseException
	{

		this.db = db;
		currentRow = 0;
		this.columns = columns;

		// find out total rows
		// TODO

		if (query == null)
		{
			this.observationSets = Collections.emptyList();
		}
		else
		{
			this.observationSets = query.find();
		}

	}

	@Override
	public boolean hasNext()
	{
		return (currentRow < observationSets.size());

	}

	@Override
	public Tuple next()
	{
		long now = System.currentTimeMillis();

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

				Object value = ValueConverter.fromString(v.getValue(), db, feature);

				tuple.set(feature.getIdentifier(), value);
			}
		}
		catch (DatabaseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		currentRow++;

		long later = System.currentTimeMillis();

		System.out.println("deze rij duurde dus " + (later - now) + " mieliesekonduh");
		return tuple;
	}

	@Override
	public void remove()
	{

		throw new UnsupportedOperationException("remove not supported");
	}

}
