package org.molgenis.data.rest.v2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;

public class SortConverter implements Converter<String, Sort>
{
	private static final String ORDER_DESC_POSTFIX = ":desc";

	@Override
	public Sort convert(String source)
	{
		Sort.Direction direction;
		if (source.endsWith(ORDER_DESC_POSTFIX))
		{
			direction = Sort.Direction.DESC;
			source = source.substring(0, source.length() - ORDER_DESC_POSTFIX.length());
		}
		else
		{
			direction = Sort.Direction.ASC;
		}

		List<Sort.Order> orders = new ArrayList<Sort.Order>();
		for (String attr : source.split(","))
		{
			orders.add(new Sort.Order(direction, attr));
		}
		return new Sort(orders);
	}
}
