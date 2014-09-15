package org.molgenis.data.elasticsearch.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.suggest.term.TermSuggestion.Score;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Generates a SearchResult from the ElasticSearch SearchResponse object
 * 
 * @author erwin
 * 
 */
public class ResponseParser
{
	public SearchResult parseSearchResponse(SearchRequest request, SearchResponse response,
			EntityMetaData entityMetaData, DataService dataService)
	{
		ShardSearchFailure[] failures = response.getShardFailures();
		if ((failures != null) && (failures.length > 0))
		{
			StringBuilder sb = new StringBuilder("Exception while searching:\n");
			for (ShardSearchFailure failure : failures)
			{
				sb.append(failure.shard()).append(":").append(failure.reason());
			}

			return new SearchResult(sb.toString());
		}

		List<Hit> searchHits = new ArrayList<Hit>();
		long totalCount = response.getHits().totalHits();

		for (SearchHit hit : response.getHits().hits())
		{
			Map<String, Object> columnValueMap = new LinkedHashMap<String, Object>();

			// If fieldsToReturn is used the "fields" field of the SearchHit is
			// filled if not the "source" field is filled
			if ((hit.fields() != null) && !hit.fields().isEmpty())
			{
				for (SearchHitField searchHitField : hit.fields().values())
				{
					columnValueMap.put(searchHitField.name(), searchHitField.value());
				}
			}

			if ((hit.sourceAsMap() != null) && !hit.sourceAsMap().isEmpty())
			{
				for (Map.Entry<String, Object> entry : hit.sourceAsMap().entrySet())
				{
					// Check if the field is MREF, if so, only extract the
					// information for labelAttribute from refeEntity and put it
					// in the Hit result map
					String fieldName = entry.getKey();
					if (entityMetaData == null
							|| entityMetaData.getAttribute(fieldName) == null
							|| !entityMetaData.getAttribute(fieldName).getDataType().getEnumType().toString()
									.equalsIgnoreCase(MolgenisFieldTypes.MREF.toString()))

					{
						columnValueMap.put(entry.getKey(), entry.getValue());
					}
					else
					{
						AttributeMetaData attributeMetaData = entityMetaData.getAttribute(fieldName).getRefEntity()
								.getLabelAttribute();
						List<Object> values = new ArrayList<Object>();
						if (entry.getValue() instanceof List<?>)
						{
							for (Object eachElement : (List<?>) entry.getValue())
							{
								if (eachElement instanceof Map<?, ?>)
								{
									for (Map.Entry<?, ?> entrySet : ((Map<?, ?>) eachElement).entrySet())
									{
										if (entrySet.getKey().toString().equalsIgnoreCase(attributeMetaData.getName()))
										{
											Object value = entrySet.getValue();
											if (value != null)
											{
												if (value instanceof List<?>)
												{
													values.addAll((List<?>) value);
												}
												else
												{
													values.add(value);
												}
												break;
											}
										}
									}
								}
							}
						}
						columnValueMap.put(entry.getKey(), Joiner.on(',').join(values));
					}
				}
				columnValueMap.put(Score.class.getSimpleName().toLowerCase(),
						String.valueOf(hit.getScore()).equals("NaN") ? 0 : hit.getScore());
			}

			searchHits.add(new Hit(hit.id(), hit.type(), columnValueMap));
		}

		AggregateResult aggregate = null;
		Aggregations aggregations = response.getAggregations();
		if (aggregations != null)
		{
			List<List<Long>> matrix = Lists.newArrayList();
			Set<String> xLabelsSet = Sets.newHashSet();
			Set<String> yLabelsSet = Sets.newHashSet();
			List<String> xLabels = new ArrayList<String>();
			List<String> yLabels = new ArrayList<String>();
			final int nrAggregations = Iterables.size(aggregations);
			if (nrAggregations != 1)
			{
				throw new RuntimeException("Multiple aggregations [" + nrAggregations + "] not supported");
			}

			Terms terms = getTermsAggregation(aggregations);

			Collection<Bucket> buckets = terms.getBuckets();
			int nrBuckets = buckets.size();
			if (nrBuckets > 0)
			{
				// create initial values
				for (int i = 0; i < nrBuckets; ++i)
					matrix.add(null);

				// distinguish between 1D and 2D aggregation
				boolean is2dAggregation = false;
				for (Bucket bucket : buckets)
				{
					Aggregations subAggregations = bucket.getAggregations();
					if (subAggregations != null && Iterables.size(subAggregations) > 0)
					{
						is2dAggregation = hasTermsAggregation(subAggregations);
						break;
					}
				}

				// create (sorted) labels for x-axis
				for (Bucket bucket : buckets)
				{
					if (!xLabelsSet.contains(bucket.getKey())) xLabelsSet.add(bucket.getKey());
				}

				xLabels = new ArrayList<String>(xLabelsSet);
				Collections.sort(xLabels);
				xLabels.add("Total");

				int xIdx = 0;
				Map<String, Integer> xLabelMap = new HashMap<String, Integer>();
				for (String xLabel : xLabels)
				{
					xLabelMap.put(xLabel, xIdx++);
				}

				if (is2dAggregation)
				{
					// create labels
					for (Bucket bucket : buckets)
					{
						Aggregations subAggregations = bucket.getAggregations();
						if (subAggregations != null)
						{
							if (Iterables.size(subAggregations) > 1)
							{
								throw new RuntimeException("Multiple aggregations [" + nrAggregations
										+ "] not supported");
							}
							Terms subTerms = getTermsAggregation(subAggregations);

							for (Bucket subBucket : subTerms.getBuckets())
							{
								yLabelsSet.add(subBucket.getKey());
							}

						}
					}

					yLabels = new ArrayList<String>(yLabelsSet);
					Collections.sort(yLabels);
					yLabels.add("Total");

					int yIdx = 0;
					Map<String, Integer> yLabelMap = new HashMap<String, Integer>();
					for (String yLabel : yLabels)
					{
						yLabelMap.put(yLabel, yIdx++);
					}

					for (Bucket bucket : buckets)
					{
						// create values
						List<Long> yValues = new ArrayList<Long>();
						for (int i = 0; i < yIdx; ++i)
						{
							yValues.add(0l);
						}

						Aggregations subAggregations = bucket.getAggregations();
						if (subAggregations != null)
						{
							long count = 0;
							Terms subTerms = getTermsAggregation(subAggregations);
							for (Bucket subBucket : subTerms.getBuckets())
							{
								Aggregations distinctAggregations = subBucket.getAggregations();
								long bucketCount;
								if (distinctAggregations != null && !Iterables.isEmpty(distinctAggregations))
								{
									bucketCount = getCardinalityAggregation(distinctAggregations).getValue();
								}
								else
								{
									bucketCount = subBucket.getDocCount();
								}
								yValues.set(yLabelMap.get(subBucket.getKey()), bucketCount);
								count += bucketCount;
							}
							yValues.set(yLabelMap.get("Total"), count);
						}

						matrix.set(xLabelMap.get(bucket.getKey()), yValues);
					}

					// create value totals
					List<Long> xTotals = new ArrayList<Long>();
					for (int i = 0; i < yIdx; ++i)
					{
						xTotals.add(0l);
					}

					for (List<Long> values : matrix)
					{
						int nrValues = values.size();
						for (int i = 0; i < nrValues; ++i)
						{
							xTotals.set(i, xTotals.get(i) + values.get(i));
						}
					}
					matrix.add(xTotals);
				}
				else
				{
					long total = 0;
					for (Bucket bucket : buckets)
					{
						Aggregations distinctAggregations = bucket.getAggregations();
						long bucketCount;
						if (distinctAggregations != null)
						{
							bucketCount = getCardinalityAggregation(distinctAggregations).getValue();
						}
						else
						{
							bucketCount = bucket.getDocCount();
						}
						matrix.set(xLabelMap.get(bucket.getKey()), Lists.newArrayList(Long.valueOf(bucketCount)));
						total += bucketCount;
					}
					matrix.add(Lists.newArrayList(Long.valueOf(total)));
					yLabels.add("Count");
				}
			}

			// matrix labels are ids for categorical/xref/mref aggregates, convert to label attribute values
			AttributeMetaData xAggregateField = request.getAggregateField1();
			if (xAggregateField != null)
			{
				FieldTypeEnum xDataType = xAggregateField.getDataType().getEnumType();
				switch (xDataType)
				{
					case CATEGORICAL:
					case MREF:
					case XREF:
						convertIdtoLabelLabels(xLabels, xAggregateField.getRefEntity(), dataService);
						// $CASES-OMITTED$
					default:
						break;
				}
			}
			AttributeMetaData yAggregateField = request.getAggregateField2();
			if (xAggregateField != null)
			{
				FieldTypeEnum yDataType = yAggregateField.getDataType().getEnumType();
				switch (yDataType)
				{
					case CATEGORICAL:
					case MREF:
					case XREF:
						convertIdtoLabelLabels(yLabels, yAggregateField.getRefEntity(), dataService);
						// $CASES-OMITTED$
					default:
						break;
				}
			}

			aggregate = new AggregateResult(matrix, xLabels, yLabels);
		}

		return new SearchResult(totalCount, searchHits, aggregate);
	}

	private Terms getTermsAggregation(Aggregations aggregations)
	{
		Aggregation aggregation = aggregations.iterator().next();
		if (aggregation instanceof ReverseNested)
		{
			Aggregations reverseNestedAggregations = ((ReverseNested) aggregation).getAggregations();
			aggregation = reverseNestedAggregations.iterator().next();
		}
		if (aggregation instanceof Nested)
		{
			Aggregations nestedAggregations = ((Nested) aggregation).getAggregations();
			aggregation = nestedAggregations.iterator().next();
		}
		if (!(aggregation instanceof Terms))
		{
			throw new RuntimeException("Aggregation of type [" + aggregation.getClass().getName() + "] not supported");
		}
		return (Terms) aggregation;
	}

	/**
	 * Unwrap possible nested and reverse nested aggregations and check if resulting aggregation is a terms aggregation
	 * 
	 * @param aggregations
	 * @return
	 */
	private boolean hasTermsAggregation(Aggregations aggregations)
	{
		Aggregation aggregation = aggregations.iterator().next();
		if (aggregation instanceof ReverseNested)
		{
			Aggregations reverseNestedAggregations = ((ReverseNested) aggregation).getAggregations();
			aggregation = reverseNestedAggregations.iterator().next();
		}
		if (aggregation instanceof Nested)
		{
			Aggregations nestedAggregations = ((Nested) aggregation).getAggregations();
			aggregation = nestedAggregations.iterator().next();
		}
		return aggregation != null && aggregation instanceof Terms;
	}

	private Cardinality getCardinalityAggregation(Aggregations aggregations)
	{
		int nrCardinalityAggregations = Iterables.size(aggregations);
		if (nrCardinalityAggregations > 1)
		{
			throw new RuntimeException("Multiple aggregations [" + nrCardinalityAggregations + "] not supported");
		}
		Aggregation aggregation = aggregations.iterator().next();
		if (aggregation instanceof ReverseNested)
		{
			Aggregations reverseNestedAggregations = ((ReverseNested) aggregation).getAggregations();
			aggregation = reverseNestedAggregations.iterator().next();
		}
		if (!(aggregation instanceof Cardinality))
		{
			throw new RuntimeException("Aggregation of type [" + aggregation.getClass().getName() + "] not supported");
		}
		return (Cardinality) aggregation;
	}

	/**
	 * Convert matrix labels that contain ids to label attribute values. Keeps in mind that the last label on a axis is
	 * "Total".
	 * 
	 * @param idLabels
	 * @param entityMetaData
	 * @param dataService
	 */
	private void convertIdtoLabelLabels(List<String> idLabels, EntityMetaData entityMetaData, DataService dataService)
	{
		// Replace id labels with label labels (skip last label for "Total")
		int nrLabelsWithoutTotalLabel = idLabels.size() - 1;

		// Get entities for ids
		// Use Iterables.transform to work around List<String> to Iterable<Object> cast error
		Iterable<Entity> entities = dataService.findAll(entityMetaData.getName(), Iterables.transform(
				Iterables.limit(idLabels, nrLabelsWithoutTotalLabel), new Function<String, Object>()
				{
					@Override
					public Object apply(String id)
					{
						return id;
					}
				}));

		// Map entity ids to labels
		Map<String, String> idToLabelMap = new HashMap<String, String>();
		for (Entity entity : entities)
		{
			idToLabelMap.put(entity.getIdValue().toString(), entity.getLabelValue());
		}

		for (int i = 0; i < nrLabelsWithoutTotalLabel; ++i)
		{
			String id = idLabels.get(i);
			idLabels.set(i, idToLabelMap.get(id));
		}
	}
}
