package org.molgenis.data.omx;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.molgenis.data.AggregateResult;
import org.molgenis.data.Aggregateable;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.dataset.AbstractDataSetMatrixRepository;
import org.molgenis.omx.dataset.DataSetMatrixRepository;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.DateValue;
import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.omx.observ.value.EmailValue;
import org.molgenis.omx.observ.value.HtmlValue;
import org.molgenis.omx.observ.value.HyperlinkValue;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.omx.observ.value.LongValue;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.omx.observ.value.TextValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Repository around an omx DataSet matrix.
 * 
 * Uses the DataService to get the metadata and the SearchService to get the actual data itself
 */
public class OmxRepository extends AbstractDataSetMatrixRepository implements CrudRepository, Aggregateable
{
	public static final String BASE_URL = "omx://";
	private static final String DATASET_ROW_IDENTIFIER_HEADER = "DataSet_Row_Id";
	private static int FLUSH_SIZE = 20;
	private final SearchService searchService;
	private final DataService dataService;
	private final ValueConverter valueConverter;
	private final EntityValidator entityValidator;
	private LoadingCache<String, ObservableFeature> observableFeatureCache = null;

	public OmxRepository(DataService dataService, SearchService searchService, String dataSetIdentifier,
			EntityValidator entityValidator)
	{
		super(BASE_URL + dataSetIdentifier, dataService, dataSetIdentifier);
		this.searchService = searchService;
		this.dataService = dataService;
		this.valueConverter = new ValueConverter(dataService);
		this.entityValidator = entityValidator;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new OmxRepositoryIterator(dataSetIdentifier, searchService, dataService, new QueryImpl(),
				getAttributeNames());
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
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
				return new OmxRepositoryIterator(dataSetIdentifier, searchService, dataService, q, getAttributeNames());
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
	public Entity findOne(Object id)
	{
		Query q = new QueryImpl().eq(DataSetMatrixRepository.ENTITY_ID_COLUMN_NAME, id);
		return findOne(q);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
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
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(ids));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
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
	public void add(Entity entity)
	{
		add(Lists.newArrayList(entity));
	}

	@Transactional
	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		EntityMetaData entityMetaData = this.getEntityMetaData();
		entityValidator.validate(entities, entityMetaData, DatabaseAction.ADD);

		DataSet dataSet = getDataSet();
		CrudRepository repo = dataService.getCrudRepository(ObservableFeature.ENTITY_NAME);

		int rownr = 0;
		for (Entity entity : entities)
		{
			rownr++;

			// Skip empty rows
			if (!EntityUtils.isEmpty(entity))
			{
				String rowIdentifier = entity.getString(DATASET_ROW_IDENTIFIER_HEADER);
				if (rowIdentifier == null) rowIdentifier = UUID.randomUUID().toString();

				ObservationSet observationSet = new ObservationSet();
				observationSet.setIdentifier(rowIdentifier);
				observationSet.setPartOfDataSet(dataSet);
				dataService.add(ObservationSet.ENTITY_NAME, observationSet);

				for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
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
							// Create a value for the ObservableFeature to add to the ObservedValue
							value = valueConverter.fromEntity(entity, observableFeature.getIdentifier(),
									observableFeature);

						}
						catch (ValueConverterException e)
						{
							// Error creating Value, this can only be a wrong xref,mref or catagory. All other datatypes
							// are already validated by the EntityValidator (see above)

							Object invalidValue = entity.get(observableFeature.getIdentifier());
							String message = String.format("Invalid value '%s' for attribute '%s' of entity '%s'. %s.",
									invalidValue, observableFeature.getIdentifier(), getName(), e.getMessage());

							throw new MolgenisValidationException(Sets.newHashSet(new ConstraintViolation(message,
									invalidValue, entity, attr, entityMetaData, rownr)));
						}

						if (value != null)
						{
							// Save the value
							dataService.add(value.getEntityName(), value);

							// create observed value
							ObservedValue observedValue = new ObservedValue();
							observedValue.setFeature(observableFeature);
							observedValue.setValue(value);
							observedValue.setObservationSet(observationSet);

							// Save ObservedValue
							dataService.add(ObservedValue.ENTITY_NAME, observedValue);
						}

					}
				}

			}

			if (rownr % FLUSH_SIZE == 0)
			{
				repo.flush();
				repo.clearCache();
			}
		}
		return rownr;
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
		// no-op
	}

	@Override
	public void clearCache()
	{
		// no-op
	}

	/**
	 * Update needs to be implemented for DateTimeValues, CategoricalValues, XrefValues and MrefValues
	 */
	@Override
	public void update(Entity entity)
	{
		Integer observationSetId = entity.getInt(getEntityMetaData().getIdAttribute().getName());
		ObservationSet os = dataService.findOne(ObservationSet.ENTITY_NAME, observationSetId, ObservationSet.class);

		// Get the ES document id for this ObservationSet
		SearchResult sr = searchService.search(new SearchRequest(dataSetIdentifier, new QueryImpl().eq(
				"observationsetid", observationSetId), Lists.newArrayList("observationsetid")));

		if (sr.getTotalHitCount() != 1)
		{
			throw new MolgenisDataException("Should have one searchresult for observationSetId [" + observationSetId
					+ "] but got [" + sr.getTotalHitCount() + "] results");
		}

		String documentId = sr.getSearchHits().get(0).getId();

		Query q = new QueryImpl().eq(ObservedValue.OBSERVATIONSET, os);
		Iterable<ObservedValue> observedValues = dataService.findAll(ObservedValue.ENTITY_NAME, q, ObservedValue.class);
		for (ObservedValue observedValue : observedValues)
		{
			Value value = observedValue.getValue();
			
			// update for every value type (boolean, string, integer etc..) except dateTime, xref, mref and categorical
			if (value instanceof BoolValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				BoolValue boolValue = (BoolValue) value;
				boolValue.setValue(entity.getBoolean(attrName));

				// Update database
				dataService.update(BoolValue.ENTITY_NAME, boolValue);

				// Update ES
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "=" + boolValue.getValue());
			
			}
			else if(value instanceof StringValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				StringValue stringValue = (StringValue) value;
				stringValue.setValue(entity.getString(attrName));
				
				dataService.update(StringValue.ENTITY_NAME, stringValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "='" + stringValue.getValue() + "'");
				
			}
			else if(value instanceof TextValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				TextValue textValue = (TextValue) value;
				textValue.setValue(entity.getString(attrName));
				
				dataService.update(TextValue.ENTITY_NAME, textValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "='" + textValue.getValue() + "'");
			}
			else if(value instanceof LongValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				LongValue longValue = (LongValue) value;
				longValue.setValue(Long.parseLong(entity.getString(attrName)));
				
				dataService.update(LongValue.ENTITY_NAME, longValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "=" + longValue.getValue());
			}
			else if(value instanceof IntValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				IntValue intValue = (IntValue) value;
				intValue.setValue(Integer.parseInt(entity.getString(attrName)));
				
				dataService.update(IntValue.ENTITY_NAME, intValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "=" + intValue.getValue());
				
			}
			else if(value instanceof DecimalValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				DecimalValue decimalValue = (DecimalValue) value;
				decimalValue.setValue(Double.parseDouble(entity.getString(attrName)));
				
				dataService.update(DecimalValue.ENTITY_NAME, decimalValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "=" + decimalValue.getValue());
			}
			else if(value instanceof HtmlValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				HtmlValue htmlValue = (HtmlValue) value;
				htmlValue.setValue(entity.getString(attrName));

				dataService.update(HtmlValue.ENTITY_NAME, htmlValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "='" + htmlValue.getValue() + "'");
			}
			else if(value instanceof HyperlinkValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				HyperlinkValue hyperlinkValue = (HyperlinkValue) value;
				hyperlinkValue.setValue(entity.getString(attrName));
				
				dataService.update(HyperlinkValue.ENTITY_NAME, hyperlinkValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "='" + hyperlinkValue.getValue() + "'");
			}
			else if(value instanceof EmailValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				EmailValue emailValue = (EmailValue) value;
				emailValue.setValue(entity.getString(attrName));

				dataService.update(EmailValue.ENTITY_NAME, emailValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "='" + emailValue.getValue() + "'");
			}
			else if(value instanceof DateValue)
			{
				String attrName = observedValue.getFeature().getIdentifier();
				DateValue dateValue = (DateValue) value;
				dateValue.setValue(entity.getDate(attrName));
				
				dataService.update(DateValue.ENTITY_NAME, dateValue);
				searchService.updateDocumentById(dataSetIdentifier, documentId, attrName + "='" + dateValue.getValue() + "'");
			}
		}
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttr, AttributeMetaData yAttr, Query q)
	{
		if ((xAttr == null) && (yAttr == null))
		{
			throw new MolgenisDataException("Missing aggregate attribute");
		}

		SearchRequest request = new SearchRequest(dataSetIdentifier, q, null, xAttr, yAttr);
		SearchResult result = searchService.search(request);

		return result.getAggregate();
	}
}
