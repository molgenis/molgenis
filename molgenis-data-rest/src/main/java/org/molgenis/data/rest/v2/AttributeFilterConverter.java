package org.molgenis.data.rest.v2;

import org.springframework.core.convert.converter.Converter;

class AttributeFilterConverter implements Converter<String, AttributeFilter>
{
	@Override
	public AttributeFilter convert(String source)
	{
		if (source == null || source.isEmpty())
		{
			return null;
		}

		AttributeFilter attributeFilter = new AttributeFilter();
		parseAttributeFilter(source, 0, attributeFilter);
		return attributeFilter;
	}

	private int parseAttributeFilter(String source, int offset, AttributeFilter attributeFilter)
	{
		boolean escaped = false;
		int nrChars = source.length();
		StringBuilder strBuilder = new StringBuilder();
		int i = offset;
		for (; i < nrChars; ++i)
		{
			char c = source.charAt(i);
			if (!escaped)
			{
				switch (c)
				{
					case '\\':
						escaped = true;
						break;
					case ',':
						attributeFilter.add(strBuilder.toString());
						strBuilder.setLength(0);
						break;
					// TODO support slash as alternative for parenthesis with one element
					// case '/':
					// AttributeFilter nestedAttributeFilter = new AttributeFilter();
					// parseAttributeFilter(source, i + 1, nestedAttributeFilter);
					// strBuilder.setLength(0);
					// break;
					case '(':
						AttributeFilter nestedAttributeFilter = new AttributeFilter();
						i = parseAttributeFilter(source, i + 1, nestedAttributeFilter);
						attributeFilter.add(strBuilder.toString(), nestedAttributeFilter);
						strBuilder.setLength(0);
						break;
					case ')':
						attributeFilter.add(strBuilder.toString());
						return i;
					default:
						strBuilder.append(c);
						if (i == nrChars - 1)
						{
							attributeFilter.add(strBuilder.toString());
						}
						break;
				}
			}
			else
			{
				strBuilder.append(c);
				escaped = false;
			}
		}
		return i;
	}

	// private Map<String, AttributeFilter> parse(String attributesStr)
	// {
	// String[] tokens = attributesStr.split(",");
	// Map<String, Attribute> attributeRequests = new LinkedHashMap<>();
	// for (String token : tokens)
	// {
	// AttributeFilter attribute = new AttributeFilter(token);
	// attributeRequests.put(normalize(attribute.getName()), attribute);
	// }
	// return attributeRequests;
	// }
}
