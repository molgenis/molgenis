package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.elasticsearch.generator.model.Sort;
import org.molgenis.data.elasticsearch.generator.model.SortDirection;
import org.molgenis.data.elasticsearch.generator.model.SortOrder;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.elasticsearch.FieldConstants.FIELD_NOT_ANALYZED;

/**
 * Generates Elasticsearch sorts from MOLGENIS sorts.
 */
@Component
class SortGenerator
{
	private final DocumentIdGenerator documentIdGenerator;

	SortGenerator(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

	Sort generateSort(org.molgenis.data.Sort sort, EntityType entityType)
	{
		Stream<org.molgenis.data.Sort.Order> orderStream = stream(sort.spliterator(), false);
		List<SortOrder> sortOrders = orderStream.map(order -> this.toSortOrder(order, entityType)).collect(toList());
		return Sort.create(sortOrders);
	}

	private SortOrder toSortOrder(org.molgenis.data.Sort.Order order, EntityType entityType)
	{
		String attributeName = order.getAttr();
		if (attributeName == null)
		{
			throw new IllegalArgumentException("Sort property is null");
		}
		Attribute sortAttribute = entityType.getAttribute(attributeName);
		if (sortAttribute == null)
		{
			throw new UnknownAttributeException(entityType, attributeName);
		}
		String sortField = getSortField(sortAttribute);
		SortDirection sortDirection = getSortDirection(order.getDirection());
		return SortOrder.create(sortField, sortDirection);
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
				throw new UnexpectedEnumException(dataType);
		}
		return sortField;
	}

	private SortDirection getSortDirection(org.molgenis.data.Sort.Direction direction)
	{
		switch (direction)
		{
			case ASC:
				return SortDirection.ASC;
			case DESC:
				return SortDirection.DESC;
			default:
				throw new UnexpectedEnumException(direction);
		}
	}
}
