package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortMode;
import org.molgenis.data.elasticsearch.generator.model.Sort;
import org.molgenis.data.elasticsearch.generator.model.SortDirection;
import org.molgenis.data.elasticsearch.generator.model.SortOrder;

import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class SortContentBuilder
{
	List<SortBuilder> createSorts(Sort sort)
	{
		return sort.getOrders().stream().map(this::createSort).collect(toList());
	}

	private SortBuilder createSort(SortOrder sortOrder)
	{
		String field = sortOrder.getField();
		org.elasticsearch.search.sort.SortOrder order = toSortOrder(sortOrder.getDirection());
		return SortBuilders.fieldSort(field).order(order).sortMode(SortMode.MIN);
	}

	private org.elasticsearch.search.sort.SortOrder toSortOrder(SortDirection sortDirection)
	{
		switch (sortDirection)
		{

			case ASC:
				return org.elasticsearch.search.sort.SortOrder.ASC;
			case DESC:
				return org.elasticsearch.search.sort.SortOrder.DESC;
			default:
				throw new RuntimeException(format("Unknown sort order '%s'", sortDirection));
		}
	}
}
