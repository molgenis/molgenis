package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortMode;
import org.elasticsearch.search.sort.SortOrder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

/**
 * Adds Sort to the SearchRequestBuilder object.
 *
 * @author erwin
 */
public class SortGenerator implements QueryPartGenerator
{
	private final DocumentIdGenerator documentIdGenerator;

	SortGenerator(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

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
				FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sortField).order(sortOrder)
						.sortMode(SortMode.MIN);
				searchRequestBuilder.addSort(sortBuilder);
			}
		}
	}

	private String getSortField(Attribute attr)
	{
		String sortField;
		String fieldName = documentIdGenerator.generateId(attr);
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
				sortField = fieldName;
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				// use raw field for sorting
				sortField = fieldName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED;
				break;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case FILE:
			case MREF:
			case ONE_TO_MANY:
			case XREF:
				// use nested field for sorting
				String refSortField = getSortField(attr.getRefEntity().getLabelAttribute());
				sortField = fieldName + '.' + refSortField;
				break;
			case COMPOUND:
				throw new UnsupportedOperationException();
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
		return sortField;
	}
}
