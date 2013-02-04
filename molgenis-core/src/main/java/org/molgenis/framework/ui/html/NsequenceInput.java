package org.molgenis.framework.ui.html;

/**
 * (Incomplete) Input for formatted sequences.
 */
public class NsequenceInput extends TextInput
{

	public NsequenceInput(String name, String value)
	{
		super(name, value);
		super.setWidth(100);
		super.setHeight(6);
	}

	public NsequenceInput(String name)
	{
		this(name, null);
	}

	@Override
	public String getValue()
	{
		String value = "";
		if (getObject() == null) return value;

		value = getObject().toString();
		StringBuilder newvalueBuilder = new StringBuilder();

		for (int i = 0; i < value.length(); i += 80)
		{
			String line = (i + 80 < value.length()) ? value.substring(i, i + 80) : value.substring(i, value.length());
			StringBuilder newlineBuilder = new StringBuilder();
			if (i < 10) newlineBuilder.append("&nbsp;");
			if (i < 100) newlineBuilder.append("&nbsp;");
			if (i < 1000) newlineBuilder.append("&nbsp;");
			newlineBuilder.append(i + 1);

			for (int j = 0; j < line.length(); j += 10)
			{
				String part = (j + 10 < line.length()) ? line.substring(j, j + 10) : line.substring(j, line.length());
				newlineBuilder.append("&nbsp;").append(part);
			}
			newvalueBuilder.append(newlineBuilder).append('\n');
		}

		return newvalueBuilder.toString();
	}

	@Override
	public String getHtmlValue()
	{
		return "<span class=\"seqQual\">" + this.getValue().replace("\n", "<br>") + "</span>";
	}

}
