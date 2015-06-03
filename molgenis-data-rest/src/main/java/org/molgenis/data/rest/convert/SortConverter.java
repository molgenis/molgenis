package org.molgenis.data.rest.convert;

import org.molgenis.data.Sort;
import org.springframework.core.convert.converter.Converter;

public class SortConverter implements Converter<String, Sort>
{
	private static final String ORDER_ASC_POSTFIX = ":asc";
	private static final String ORDER_DESC_POSTFIX = ":desc";

	@Override
	public Sort convert(String source)
	{
		Sort sort = new Sort();
		for (String attr : source.split(","))
		{
			Sort.Direction direction;
			if (attr.endsWith(ORDER_DESC_POSTFIX))
			{
				direction = Sort.Direction.DESC;
				attr = attr.substring(0, attr.length() - ORDER_DESC_POSTFIX.length());
			}
			else if (attr.endsWith(ORDER_ASC_POSTFIX))
			{
				direction = Sort.Direction.ASC;
				attr = attr.substring(0, attr.length() - ORDER_ASC_POSTFIX.length());
			}
			else
			{
				direction = Sort.Direction.ASC;
			}
			sort.on(attr, direction);
		}
		return sort;
	}
}
