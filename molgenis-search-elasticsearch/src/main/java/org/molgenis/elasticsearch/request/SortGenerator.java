package org.molgenis.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.elasticsearch.index.MappingsBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Adds Sort to the SearchRequestBuilder object.
 * 
 * @author erwin
 * 
 */
public class SortGenerator implements QueryPartGenerator
{

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query, EntityMetaData entityMetaData)
	{
		if (query.getSort() != null)
		{
			for (Sort.Order sort : query.getSort())
			{
				String sortAttrName = sort.getProperty();
				if (sortAttrName == null) throw new IllegalArgumentException("Sort property is null");

				Direction sortDirection = sort.getDirection();
				if (sortDirection == null) throw new IllegalArgumentException("Missing sort direction");

				AttributeMetaData sortAttr = entityMetaData.getAttribute(sortAttrName);
				if (sortAttr == null) throw new UnknownAttributeException(sortAttrName);

				String sortField;
				FieldTypeEnum dataType = sortAttr.getDataType().getEnumType();
				switch (dataType)
				{
					case BOOL:
						sortField = sortAttrName;
						break;
					case DATE:
					case DATE_TIME:
					case DECIMAL:
					case EMAIL:
					case ENUM:
					case HTML:
					case HYPERLINK:
					case INT:
					case LONG:
					case SCRIPT:
					case STRING:
					case TEXT:
						// <attr_name>.<not_analyzed_field>
						sortField = new StringBuilder(sortAttrName).append('.')
								.append(MappingsBuilder.FIELD_NOT_ANALYZED).toString();
						break;
					case CATEGORICAL:
					case MREF:
					case XREF:
						// <nested_type>.<attr_name>.<not_analyzed_field>
						String refLabelAttrName = sortAttr.getRefEntity().getLabelAttribute().getName();
						sortField = new StringBuilder(sortAttrName).append('.').append(refLabelAttrName).append('.')
								.append(MappingsBuilder.FIELD_NOT_ANALYZED).toString();
						break;
					case COMPOUND:
					case FILE:
					case IMAGE:
						throw new UnsupportedOperationException();
					default:
						throw new RuntimeException("Unknown data type [" + dataType + "]");
				}
				SortOrder sortOrder = sortDirection == Direction.ASC ? SortOrder.ASC : SortOrder.DESC;
				FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sortField).order(sortOrder).sortMode("min");
				searchRequestBuilder.addSort(sortBuilder);
			}
		}
	}
}
