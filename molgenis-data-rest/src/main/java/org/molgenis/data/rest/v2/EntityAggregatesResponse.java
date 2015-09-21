package org.molgenis.data.rest.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

public class EntityAggregatesResponse extends EntityCollectionResponseV2
{
	private final AggregateResultResponse aggs;

	public EntityAggregatesResponse(AggregateResult aggs, String href)
	{
		super(href);
		this.aggs = checkNotNull(AggregateResultResponse.toResponse(aggs));
	}

	public AggregateResultResponse getAggs()
	{
		return aggs;
	}

	public static class AggregateResultResponse
	{
		private final List<List<Long>> matrix;
		private final List<Object> xLabels;
		private final List<Object> yLabels;

		public AggregateResultResponse(List<List<Long>> matrix, List<Object> xLabels, List<Object> yLabels)
		{
			this.matrix = matrix;
			this.xLabels = xLabels;
			this.yLabels = yLabels;
		}

		public static AggregateResultResponse toResponse(AggregateResult aggs)
		{
			List<List<Long>> matrix = aggs.getMatrix();
			List<Object> xLabels = convert(aggs.getxLabels());
			List<Object> yLabels = convert(aggs.getyLabels());
			return new AggregateResultResponse(matrix, xLabels, yLabels);
		}

		private static List<Object> convert(List<Object> xLabels)
		{
			return xLabels.stream().map(xLabel -> {
				Object value;
				if (xLabel instanceof Entity)
				{
					Map<String, Object> valueMap = new HashMap<String, Object>();
					Entity entity = (Entity) xLabel;
					for (AttributeMetaData attr : entity.getEntityMetaData().getAtomicAttributes())
					{
						switch (attr.getDataType().getEnumType())
						{
							case XREF:
							case CATEGORICAL:
							case MREF:
							case CATEGORICAL_MREF:
							case COMPOUND:
								break;
							default:
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
	}
}
