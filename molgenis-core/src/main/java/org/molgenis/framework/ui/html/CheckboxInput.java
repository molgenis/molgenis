package org.molgenis.framework.ui.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.molgenis.util.ValueLabel;
import org.molgenis.util.tuple.Tuple;

/**
 * Input for checkbox data.
 */
public class CheckboxInput extends OptionInput<List<String>>
{
	public static final String VALUES = "values";
	private String onchange = null;

	/**
	 * Construct a checkbox input with a name, a label and a description, as
	 * well as one or more options and zero or more selected values.
	 * 
	 * @param name
	 * @param label
	 * @param description
	 * @param options
	 * @param value
	 */
	public CheckboxInput(String name, String label, String description, Vector<ValueLabel> options, List<String> value)
	{
		super(name, value);
		super.setLabel(label);
		super.setDescription(description);
		super.setOptions(options);
		this.setReadonly(false);
	}

	/**
	 * Construct a checkbox based on a list of options.
	 * 
	 * @param name
	 * @param options
	 * @param optionLabels
	 * @param label
	 * @param value
	 * @param nillable
	 * @param readonly
	 */
	public CheckboxInput(String name, List<String> options, List<String> optionLabels, String label,
			List<String> value, boolean nillable, boolean readonly)
	{
		super(name, value);
		if (optionLabels != null && optionLabels.size() > 0 && optionLabels.size() != options.size()) throw new IllegalArgumentException(
				"optionLabels, if set, must be of same size as options");

		List<ValueLabel> result = new ArrayList<ValueLabel>();
		if (optionLabels != null && optionLabels.size() == optionLabels.size())
		{
			for (int i = 0; i < options.size(); i++)
			{
				result.add(new ValueLabel(options.get(i), optionLabels.get(i)));
			}
		}
		else
		{
			for (String option : options)
			{
				result.add(new ValueLabel(option, option));
			}
		}
		this.setLabel(label);
		this.setNillable(nillable);
		this.setReadonly(readonly);
	}

	/**
	 * Construct a new checkbox based on a Tuple with properties.
	 * 
	 * @param properties
	 * @throws HtmlInputException
	 */
	public CheckboxInput(Tuple properties) throws HtmlInputException
	{
		super(properties);
		if (!properties.isNull(VALUE)) this.setValue(properties.getList(VALUE));
		if (!properties.isNull(VALUES)) this.setValue(properties.getList(VALUES));
	}

	/**
	 * Null constructor. Use with caution.
	 */
	protected CheckboxInput()
	{
		super();
	}

	public String getOnchange()
	{
		return onchange;
	}

	public void setOnchange(String onchange)
	{
		this.onchange = onchange;
	}

	@Override
	public String toHtml()
	{
		if (this.isHidden())
		{
			StringInput input = new StringInput(this.getName(), this.getValue());
			input.setHidden(true);
			return input.toHtml();
		}

		StringBuffer optionString = new StringBuffer("");
		String readonly = (isReadonly() ? " class=\"readonly\" readonly " : "");
		String checked = "";
		String onchange = (this.onchange != null) ? " onchange=\"" + this.onchange + "\"" : "";

		if (!(getOptions().isEmpty()))
		{
			for (ValueLabel option : getOptions())
			{
				if (getObject() != null)
				{
					checked = getObject().contains(option.getValue().toString()) ? " checked " : "";
				}
				optionString.append("<input id=\"" + this.getId() + "\" type=\"checkbox\" " + onchange + readonly
						+ checked + " name=\"" + this.getName() + "\" value=\"" + option.getValue() + "\">"
						+ option.getLabel() + "<br />\n");
			}
		}
		else
		{
			if (getObject() != null)
			{
				checked = (((Vector<String>) getObject()).contains(this.getName()) ? " checked " : "");
			}
			optionString.append("<input id=\"" + this.getId() + "\" type=\"checkbox\" " + onchange + readonly + checked
					+ " name=\"" + this.getName() + "\">" + this.getLabel());
		}

		return optionString.toString();
	}

	@Override
	public String getValue()
	{
		StringBuilder valueBuilder = new StringBuilder();
		for (ValueLabel i : getOptions())
		{
			if (((Vector<String>) getObject()).contains(i.getValue()))
			{
				valueBuilder.append(i.getLabel()).append(", ");
			}
		}
		// remove trailing comma
		if (valueBuilder.length() > 2)
		{
			return valueBuilder.substring(0, valueBuilder.length() - 2);
		}
		return valueBuilder.toString();
	}

	@Override
	public String toHtml(Tuple p) throws HtmlInputException
	{
		return new CheckboxInput(p).render();
	}
}
