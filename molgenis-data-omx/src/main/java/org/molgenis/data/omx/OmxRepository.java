package org.molgenis.data.omx;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Writable;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.dataset.AbstractDataSetMatrixRepository;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.search.SearchService;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Repository around an omx DataSet matrix.
 * 
 * Uses the DataService to get the metadata and the SearchService to get the actual data itself
 */
public class OmxRepository extends AbstractDataSetMatrixRepository implements Queryable, Writable
{
	public static final String BASE_URL = "omx://";
	private static int FLUSH_SIZE = 20;
	private static final String DATASET_ROW_IDENTIFIER_HEADER = "DataSet_Row_Id";
	private final SearchService searchService;
	private final ValueConverter valueConverter;
	private LoadingCache<String, ObservableFeature> observableFeatureCache = null;

	public OmxRepository(DataService dataService, SearchService searchService, String dataSetIdentifier)
	{
		super(BASE_URL + dataSetIdentifier, dataService, dataSetIdentifier);
		this.searchService = searchService;
		this.valueConverter = new ValueConverter(dataService);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new OmxRepositoryIterator(dataSetIdentifier, searchService, new QueryImpl(), getAttributeNames());
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public long count(Query q)
	{
		return searchService.count(dataSetIdentifier, q);
	}

	@Override
	public Iterable<Entity> findAll(final Query q)
	{
		return new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return new OmxRepositoryIterator(dataSetIdentifier, searchService, q, getAttributeNames());
			}
		};
	}

	@Override
	public Entity findOne(Query q)
	{
		q.pageSize(1);
		Iterator<Entity> it = findAll(q).iterator();
		if (!it.hasNext())
		{
			return null;
		}

		return it.next();
	}

	@Override
	public Entity findOne(Integer id)
	{
		Query q = new QueryImpl().eq(ObservationSet.ID, id);
		return findOne(q);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		Query q = new QueryImpl().in(ObservationSet.ID, ids);
		return findAll(q);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(q));
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(ids));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		Entity entity = findOne(id);
		if (entity == null)
		{
			return null;
		}

		if (clazz.isAssignableFrom(entity.getClass()))
		{
			return (E) entity;
		}

		E e = BeanUtils.instantiate(clazz);
		e.set(entity);
		return e;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		Entity entity = findOne(q);
		if (entity == null)
		{
			return null;
		}

		if (clazz.isAssignableFrom(entity.getClass()))
		{
			return (E) entity;
		}

		E e = BeanUtils.instantiate(clazz);
		e.set(entity);
		return e;
	}

	@Transactional
	@Override
	public Integer add(Entity entity)
	{
		add(Arrays.asList(entity));
		return entity.getIdValue();
	}

	@Transactional
	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		DataSet dataSet = getDataSet();
		CrudRepository repo = dataService.getCrudRepository(ObservableFeature.ENTITY_NAME);

		int rownr = 0;
		for (Entity entity : entities)
		{
			// Skip empty rows
			if (!EntityUtils.isEmpty(entity))
			{
				String rowIdentifier = entity.getString(DATASET_ROW_IDENTIFIER_HEADER);
				if (rowIdentifier == null) rowIdentifier = UUID.randomUUID().toString();

				ObservationSet observationSet = new ObservationSet();
				observationSet.setIdentifier(rowIdentifier);
				observationSet.setPartOfDataSet(dataSet);
				dataService.add(ObservationSet.ENTITY_NAME, observationSet);

				for (AttributeMetaData attr : getAttributes())
				{
					if (!attr.isIdAtrribute())
					{
						ObservableFeature observableFeature;
						try
						{
							observableFeature = getObservableFeatureCache().get(attr.getName());
						}
						catch (ExecutionException e)
						{
							throw new MolgenisDataException("Exception getting [" + attr.getName() + "]  from cache", e);
						}

						Value value = null;
						try
						{
							value = valueConverter.fromEntity(entity, observableFeature.getIdentifier(),
									observableFeature);
						}
						catch (ValueConverterException e)
						{
							throw new MolgenisDataException("Failed to convert ");
						}

						if (value != null)
						{
							dataService.add(value.getEntityName(), value);

							// create observed value
							ObservedValue observedValue = new ObservedValue();
							observedValue.setFeature(observableFeature);
							observedValue.setValue(value);
							observedValue.setObservationSet(observationSet);
							dataService.add(ObservedValue.ENTITY_NAME, observedValue);
						}
					}
				}

			}

			if (++rownr % FLUSH_SIZE == 0)
			{
				repo.flush();
				repo.clearCache();
			}
		}

	}

	private LoadingCache<String, ObservableFeature> getObservableFeatureCache()
	{
		if (observableFeatureCache == null)
		{
			observableFeatureCache = CacheBuilder.newBuilder().maximumSize(10000)
					.expireAfterAccess(30, TimeUnit.MINUTES).build(new CacheLoader<String, ObservableFeature>()
					{
						@Override
						public ObservableFeature load(String identifier) throws Exception
						{
							return dataService.findOne(ObservableFeature.ENTITY_NAME,
									new QueryImpl().eq(ObservableFeature.IDENTIFIER, identifier),
									ObservableFeature.class);
						}

					});
		}

		return observableFeatureCache;
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void clearCache()
	{
	}

}
