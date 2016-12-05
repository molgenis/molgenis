package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

/**
 * Adds Sort to the SearchRequestBuilder object.
 *
 * @author erwin
 */
public class SortGenerator implements QueryPartGenerator
{

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query<Entity> query, EntityType entityType)
	{
		if (query.getSort() != null)
		{
			for (Sort.Order sort : query.getSort())
			{
				String sortAttrName = sort.getAttr();
				if (sortAttrName == null) throw new IllegalArgumentException("Sort property is null");

				Direction sortDirection = sort.getDirection();
				if (sortDirection == null) throw new IllegalArgumentException("Missing sort direction");

				Attribute sortAttr = entityType.getAttribute(sortAttrName);
				if (sortAttr == null) throw new UnknownAttributeException(sortAttrName);

				String sortField = getSortField(sortAttr);
				SortOrder sortOrder = sortDirection == Direction.ASC ? SortOrder.ASC : SortOrder.DESC;
				FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sortField).order(sortOrder).sortMode("min");
				searchRequestBuilder.addSort(sortBuilder);
			}
		}
	}

	private String getSortField(Attribute attr)
	{
		String sortField;
		AttributeType dataType = attr.getDataType();
		switch (dataType)
		{
			case BOOL:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case INT:
			case LONG:
				// use indexed field for sorting
				sortField = attr.getName();
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				// use raw field for sorting
				sortField = new StringBuilder(attr.getName()).append('.').append(MappingsBuilder.FIELD_NOT_ANALYZED)
						.toString();
				break;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case MREF:
			case XREF:
			case FILE:
				// use nested field for sorting
				String refSortField = getSortField(attr.getRefEntity().getLabelAttribute());
				sortField = new StringBuilder(attr.getName()).append('.').append(refSortField).toString();
				break;
			case COMPOUND:
				throw new UnsupportedOperationException();
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
		return sortField;
	}
}
