package org.molgenis.data.rest.v2;

import org.molgenis.data.aggregation.AggregateAnonymizer;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.aggregation.AnonymizedAggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.data.support.EntityTypeUtils.isReferenceType;

public class EntityAggregatesResponse extends EntityCollectionResponseV2
{
	private final AggregateResultResponse aggs;
	private final AttributeResponseV2 xAttr;
	private final AttributeResponseV2 yAttr;

	public EntityAggregatesResponse(AggregateResult aggs, AttributeResponseV2 xAttr, AttributeResponseV2 yAttr,
			String href)
	{
		super(href);
		this.aggs = checkNotNull(AggregateResultResponse.toResponse(aggs));
		this.xAttr = xAttr;
		this.yAttr = yAttr;
	}

	public AggregateResultResponse getAggs()
	{
		return aggs;
	}

	public AttributeResponseV2 getXAttr()
	{
		return xAttr;
	}

	public AttributeResponseV2 getYAttr()
	{
		return yAttr;
	}

	public static class AggregateResultResponse
	{
		private final List<List<Long>> matrix;
		private final List<Object> xLabels;
		private final List<Object> yLabels;
		private final Integer threshold;

		public AggregateResultResponse(List<List<Long>> matrix, List<Object> xLabels, List<Object> yLabels,
				Integer threshold)
		{
			this.matrix = matrix;
			this.xLabels = xLabels;
			this.yLabels = yLabels;
			this.threshold = threshold;
		}

		public static AggregateResultResponse toResponse(AggregateResult aggs)
		{
			List<List<Long>> matrix = aggs.getMatrix();
			List<Object> xLabels = convert(aggs.getxLabels());
			List<Object> yLabels = convert(aggs.getyLabels());
			Integer threshold = toAggregateThreshold(aggs);
			return new AggregateResultResponse(matrix, xLabels, yLabels, threshold);
		}

		private static Integer toAggregateThreshold(AggregateResult aggs)
		{
			Integer threshold;
			if (aggs instanceof AnonymizedAggregateResult)
			{
				int thresholdInt = ((AnonymizedAggregateResult) aggs).getAnonymizationThreshold();
				if (thresholdInt != AggregateAnonymizer.AGGREGATE_ANONYMIZATION_VALUE)
				{
					threshold = thresholdInt;
				}
				else
				{
					threshold = null;
				}
			}
			else
			{
				threshold = null;
			}
			return threshold;
		}

		private static List<Object> convert(List<Object> xLabels)
		{
			return xLabels.stream().map(xLabel ->
			{
				Object value;
				if (xLabel instanceof Entity)
				{
					Map<String, Object> valueMap = new HashMap<>();
					Entity entity = (Entity) xLabel;
					for (Attribute attr : entity.getEntityType().getAtomicAttributes())
					{
						if (!isReferenceType(attr))
						{
							valueMap.put(attr.getName(), entity.getString(attr.getName()));
						}
					}
					value = valueMap;
				}
				else
				{
					value = xLabel;
				}
				return value;
			}).collect(Collectors.toList());
		}

		public List<List<Long>> getMatrix()
		{
			return matrix;
		}

		public List<Object> getxLabels()
		{
			return xLabels;
		}

		public List<Object> getyLabels()
		{
			return yLabels;
		}

		public Integer getThreshold()
		{
			return threshold;
		}
	}
}
