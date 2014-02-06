package org.molgenis.omx.dataset;

import java.util.Iterator;

import org.molgenis.data.Countable;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.value.Value;

/**
 * Repository around an omx DataSet matrix.
 * 
 * Uses the DatService only in contrast to the OmxRepository that uses the SearchService.
 * 
 * So everything is read from the database.
 * 
 * This repository is not registered by the DataService, it is used for indexing.
 */
public class DataSetMatrixRepository extends AbstractDataSetMatrixRepository implements Countable
{
	public static final String BASE_URL = "dataset://";

	public DataSetMatrixRepository(DataService dataService, String dataSetIdentifier)
	{
		super(BASE_URL + dataSetIdentifier, dataService, dataSetIdentifier);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new Iterator<Entity>()
		{
			private final ValueConverter valueConverter = new ValueConverter(dataService);
			private final Iterable<ObservationSet> observationSets = dataService.findAll(ObservationSet.ENTITY_NAME,
					new QueryImpl().eq(ObservationSet.PARTOFDATASET, getDataSet()), ObservationSet.class);
			private final Iterator<ObservationSet> it = observationSets.iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Entity next()
			{
				ObservationSet currentRowToGet = it.next();

				Entity entity = new MapEntity("id", currentRowToGet.getId());

				try
				{
					Query q = new QueryImpl().eq(ObservedValue.OBSERVATIONSET, currentRowToGet);
					Iterable<ObservedValue> observedValues = dataService.findAll(ObservedValue.ENTITY_NAME, q,
							ObservedValue.class);

					for (ObservedValue v : observedValues)
					{
						ObservableFeature feature = v.getFeature();
						Value value = v.getValue();
						entity.set(feature.getIdentifier(), valueConverter.toCell(value));
					}
					entity.set("partOfDataset", currentRowToGet.getPartOfDataSet().getIdentifier());
				}
				catch (ValueConverterException e)
				{
					throw new MolgenisDataException(e);
				}

				return entity;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException("remove not supported");
			}

		};
	}

	@Override
	public long count()
	{
		return dataService.count(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, getDataSet()));
	}

}
