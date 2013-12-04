package org.molgenis.omx.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

public class DataSetTableIterator implements Iterator<Tuple>
{

	private final DataService dataService;
	private int currentRow;
	private List<ObservationSet> observationSets;
	private final List<Field> columns;
	private final ValueConverter valueConverter;

	public DataSetTableIterator(DataService dataService, List<Field> columns, Query query) throws DatabaseException
	{
		this.dataService = dataService;
		this.columns = columns;

		this.observationSets = Collections.<ObservationSet> emptyList();
		if (query != null)
		{
			this.observationSets = dataService.findAllAsList(ObservationSet.ENTITY_NAME, query);
		}

		this.currentRow = 0;
		this.valueConverter = new ValueConverter(dataService);
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

		try
		{
			Query q = new QueryImpl().eq(ObservedValue.OBSERVATIONSET, currentRowToGet);
			Iterable<ObservedValue> observedValues = dataService.findAll(ObservedValue.ENTITY_NAME, q);

			for (ObservedValue v : observedValues)
			{
				ObservableFeature feature = v.getFeature();
				Value value = v.getValue();
				tuple.set(feature.getIdentifier(), valueConverter.toCell(value));
			}
            tuple.set("partOfDataset", currentRowToGet.getPartOfDataSet().getIdentifier());
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
