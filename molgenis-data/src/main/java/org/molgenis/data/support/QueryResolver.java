package org.molgenis.data.support;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL_MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handle a bit of lagacy, handle query like 'SELECT FROM Category WHERE observableFeature_Identifier=xxx' Resolve xref
 * ids. TODO Do this in a cleaner way and support more operators, remove this completely?
 */
@Component
public class QueryResolver
{
	private final DataService dataService;

	@Autowired
	public QueryResolver(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	public List<QueryRule> resolveRefIdentifiers(List<QueryRule> rules, EntityMetaData meta)
	{
		for (QueryRule r : rules)
		{
			if (r.getOperator() == Operator.NESTED)
			{
				resolveRefIdentifiers(r.getNestedRules(), meta);
			}

			if (r.getField() != null)
			{
				AttributeMetaData attr = meta.getAttribute(r.getField());
				if (attr != null)
				{
					if (r.getField().endsWith("_Identifier"))
					{
						String entityName = StringUtils
								.capitalize(r.getField().substring(0, r.getField().length() - "_Identifier".length()));
						r.setField(entityName);

						Object value = dataService.findOne(entityName, new QueryImpl().eq("Identifier", r.getValue()));
						r.setValue(value);
					}
					else
					{
						FieldTypeEnum dataType = attr.getDataType().getEnumType();
						if ((dataType == XREF || dataType == MREF || dataType == CATEGORICAL
								|| dataType == CATEGORICAL_MREF))
						{
							// Find referencing entity if a ref attribute is given and not the ref entity itself
							if (r.getOperator() == Operator.IN)
							{
								Iterable<?> iterable = (Iterable<?>) r.getValue();
								Iterator<?> it = iterable.iterator();
								if (it.hasNext() && !(it.next() instanceof Entity))
								{
									List<?> values = dataService
											.findAll(attr.getRefEntity().getName(), new QueryImpl()
													.in(attr.getRefEntity().getLabelAttribute().getName(), iterable))
											.collect(Collectors.toList());

									r.setValue(values);
								}
							}
							else if (!(r.getValue() instanceof Entity))
							{
								Object value = dataService.findOne(attr.getRefEntity().getName(), new QueryImpl()
										.eq(attr.getRefEntity().getLabelAttribute().getName(), r.getValue()));

								r.setValue(value);
							}
						}
					}
				}
			}

		}

		return rules;
	}
}
