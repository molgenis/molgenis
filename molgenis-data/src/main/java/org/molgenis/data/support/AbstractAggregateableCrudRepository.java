package org.molgenis.data.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Aggregateable;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.validation.EntityValidator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class AbstractAggregateableCrudRepository extends AbstractCrudRepository implements Aggregateable
{

	public AbstractAggregateableCrudRepository(String url, EntityValidator validator)
	{
		super(url, validator);
	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttributeMeta, AttributeMetaData yAttributeMeta, Query query)
	{
		if ((xAttributeMeta == null) && (yAttributeMeta == null))
		{
			throw new MolgenisDataException("Missing aggregate attribute");
		}

		FieldTypeEnum xDataType = null;
		String xAttributeName = null;
		if (xAttributeMeta != null)
		{
			xAttributeName = xAttributeMeta.getName();

			if (!xAttributeMeta.isAggregateable())
			{
				throw new MolgenisDataException("Attribute '" + xAttributeName + "' is not aggregateable");
			}

			xDataType = xAttributeMeta.getDataType().getEnumType();
		}

		FieldTypeEnum yDataType = null;
		String yAttributeName = null;
		if (yAttributeMeta != null)
		{
			yAttributeName = yAttributeMeta.getName();
			if (!yAttributeMeta.isAggregateable())
			{
				throw new MolgenisDataException("Attribute '" + yAttributeName + "' is not aggregateable");
			}

			yDataType = yAttributeMeta.getDataType().getEnumType();
		}

		List<Object> xValues = Lists.newArrayList();
		List<Object> yValues = Lists.newArrayList();
		List<List<Long>> matrix = new ArrayList<List<Long>>();
		Set<String> xLabels = Sets.newLinkedHashSet();
		Set<String> yLabels = Sets.newLinkedHashSet();

		if (xDataType != null)
		{
			addAggregateValuesAndLabels(xAttributeMeta, xValues, xLabels);
		}

		if (yDataType != null)
		{
			addAggregateValuesAndLabels(yAttributeMeta, yValues, yLabels);
		}

		boolean hasXValues = !xValues.isEmpty();
		boolean hasYValues = !yValues.isEmpty();

		if (hasXValues)
		{
			List<Long> totals = Lists.newArrayList();

			for (Object xValue : xValues)
			{
				List<Long> row = Lists.newArrayList();

				if (hasYValues)
				{
					int i = 0;

					for (Object yValue : yValues)
					{

						// Both x and y choosen
						Query finalQ = query.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(query).and();
						finalQ.eq(xAttributeName, xValue).and().eq(yAttributeName, yValue);
						long count = count(finalQ);
						row.add(count);
						if (totals.size() == i)
						{
							totals.add(count);
						}
						else
						{
							totals.set(i, totals.get(i) + count);
						}
						i++;
					}
				}
				else
				{
					// No y attribute chosen
					Query finalQ = query.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(query).and();
					finalQ.eq(xAttributeName, xValue);
					long count = count(finalQ);
					row.add(count);
					if (totals.isEmpty())
					{
						totals.add(count);
					}
					else
					{
						totals.set(0, totals.get(0) + count);
					}

				}

				matrix.add(row);
			}

			yLabels.add(hasYValues ? "Total" : "Count");
			xLabels.add("Total");

			matrix.add(totals);
		}
		else
		{
			// No xattribute chosen
			List<Long> row = Lists.newArrayList();
			for (Object yValue : yValues)
			{
				Query finalQ = query.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(query).and();
				finalQ.eq(yAttributeName, yValue);
				long count = count(finalQ);
				row.add(count);
			}
			matrix.add(row);

			xLabels.add("Count");
			yLabels.add("Total");
		}

		// Count row totals
		if (hasYValues)
		{
			for (List<Long> row : matrix)
			{
				long total = 0;
				for (Long count : row)
				{
					total += count;
				}
				row.add(total);
			}
		}

		return new AggregateResult(matrix, new ArrayList<String>(xLabels), new ArrayList<String>(yLabels));
	}

	protected abstract void addAggregateValuesAndLabels(AttributeMetaData attr, List<Object> values, Set<String> labels);
}
