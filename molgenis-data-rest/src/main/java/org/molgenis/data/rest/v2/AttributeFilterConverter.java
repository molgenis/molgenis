package org.molgenis.data.rest.v2;

import org.springframework.core.convert.converter.Converter;

class AttributeFilterConverter implements Converter<String, AttributeFilter>
{
	@Override
	public AttributeFilter convert(String source)
	{
		AttributeFilter attributeFilter = new AttributeFilter();
		parseAttributeFilter(source, 0, attributeFilter);
		return attributeFilter;
	}

	private int parseAttributeFilter(String source, int offset, AttributeFilter attrFilter)
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
						if (!strBuilder.toString().isEmpty())
						{
							addFilter(attrFilter, strBuilder.toString());
							strBuilder.setLength(0);
						}
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
						addFilter(attrFilter, strBuilder.toString(), nestedAttributeFilter);
						strBuilder.setLength(0);
						break;
					case ')':
						if (!strBuilder.toString().isEmpty())
						{
							attrFilter.add(strBuilder.toString());
						}
						return i;
					case '*':
						attrFilter.setIncludeAllAttrs(true);
						break;
					default:
						strBuilder.append(c);
						if (i == nrChars - 1)
						{
							addFilter(attrFilter, strBuilder.toString());
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

	private void addFilter(AttributeFilter attrFilter, String attrName)
	{
		addFilter(attrFilter, attrName, null);
	}

	private void addFilter(AttributeFilter attrFilter, String attrName, AttributeFilter nestedAttrFilter)
	{
		if (attrName.equals("~id"))
		{
			attrFilter.setIncludeIdAttr(true, nestedAttrFilter);
		}
		else if (attrName.equals("~lbl"))
		{
			attrFilter.setIncludeLabelAttr(true, nestedAttrFilter);
		}
		else
		{
			attrFilter.add(attrName, nestedAttrFilter);
		}
	}
}
