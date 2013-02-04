package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.List;
import java.util.Vector;

import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.ValueLabel;

/**
 * Input for radio button data.
 */
public class RadioInput extends OptionInput<String>
{
	/**
	 * Construct a radio button input with a name, a label and a description, as
	 * well as one or more options and a selected value.
	 * 
	 * @param name
	 * @param label
	 * @param description
	 * @param options
	 * @param value
	 */
	@Deprecated
	public RadioInput(String name, String label, String description, Vector<ValueLabel> options, String value)
	{
		super(name, value);
		super.setLabel(label);
		super.setDescription(description);
		super.setOptions(options);
		this.setReadonly(false);
	}

	public RadioInput(String name, String label, String value, Boolean nillable, Boolean readonly, String description,
			List<String> options, List<String> option_labels) throws HtmlInputException
	{
		super(name, label, value, nillable, readonly, description, options, option_labels);
	}

	public RadioInput(Tuple t) throws HtmlInputException
	{
		super(t);
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

		String optionString;
		StringBuilder optionStringBuilder = new StringBuilder();
		String readonly = (isReadonly() ? " class=\"readonly\" readonly " : "");
		String checked;
		String selectedValueLabel = this.getValue();

		if (!(getOptions().isEmpty()))
		{
			for (ValueLabel option : getOptions())
			{
				String optionLabel = option.getLabel();
				checked = (selectedValueLabel.equals(optionLabel)) ? " checked " : "";
				optionStringBuilder.append("<input id=\"").append(this.getId()).append("\" type=\"radio\" ");
				optionStringBuilder.append(readonly).append(checked).append(" name=\"").append(this.getName());
				optionStringBuilder.append("\" value=\"").append(option.getValue()).append("\">");
				optionStringBuilder.append(option.getLabel()).append("<br />");
			}
			// remove trailing <br />
			optionString = optionStringBuilder.substring(0, optionStringBuilder.length() - 6);
		}
		else
		{
			checked = this.getValue().equals(this.getName()) ? " checked " : "";
			optionStringBuilder.append("<input id=\"").append(this.getId()).append("\" type=\"radio\" ");
			optionStringBuilder.append(readonly).append(checked).append(" name=\"").append(this.getName());
			optionStringBuilder.append("\">").append(this.getLabel());
			optionString = optionStringBuilder.toString();
		}

		return optionString;
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		return new RadioInput(params).render();
	}

}
