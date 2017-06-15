package org.molgenis.data.elasticsearch.generator;

import org.elasticsearch.search.sort.*;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.elasticsearch.client.FieldConstants.FIELD_NOT_ANALYZED;

@Component
class SortGenerator
{
	private final DocumentIdGenerator documentIdGenerator;

	SortGenerator(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

	public List<SortBuilder> generate(Sort sort, EntityType entityType)
	{
		if (sort == null)
		{
			return emptyList();
		}
		return stream(sort.spliterator(), false).map(order -> this.toSortBuilder(order, entityType)).collect(toList());
	}

	private SortBuilder toSortBuilder(Sort.Order order, EntityType entityType)
	{
		String sortAttrName = order.getAttr();
		if (sortAttrName == null) throw new IllegalArgumentException("Sort property is null");

		Direction sortDirection = order.getDirection();
		if (sortDirection == null) throw new IllegalArgumentException("Missing sort direction");

		Attribute sortAttr = entityType.getAttribute(sortAttrName);
		if (sortAttr == null) throw new UnknownAttributeException(sortAttrName);

		String sortField = getSortField(sortAttr);
		SortOrder sortOrder = sortDirection == Direction.ASC ? SortOrder.ASC : SortOrder.DESC;
		FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sortField).order(sortOrder).sortMode(SortMode.MIN);
		return sortBuilder;
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
				sortField = fieldName + '.' + FIELD_NOT_ANALYZED;
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
