package org.molgenis.framework.ui.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.util.ValueLabel;
import org.molgenis.util.tuple.Tuple;

/**
 * Common superclass for option based inputs such as xrefs, enums, radio, etc.
 */
public abstract class OptionInput<E> extends HtmlInput<E>
{
	public static final String OPTIONS = "options";
	public static final String OPTION_LABELS = "option_labels";

	private List<ValueLabel> options = new ArrayList<ValueLabel>();

	public OptionInput(String name, E value)
	{
		super(name, value);
	}

	public OptionInput(String name, String label, E value, Boolean nillable, Boolean readonly, String description,
			List<String> options, List<String> option_labels) throws HtmlInputException
	{
		super(name, label, value, nillable, readonly, description);
		this.setOptions(options, option_labels);
	}

	public void setOptions(List<String> options, List<String> optionLabels) throws HtmlInputException
	{
		if (options == null) throw new RuntimeException("parameter options=\"opt1,opt2,...\" is required");

		List<ValueLabel> valueLabels = new ArrayList<ValueLabel>();
		if (optionLabels == null)
		{
			for (String option : options)
				valueLabels.add(new ValueLabel(option, option));
		}
		else
		{
			if (options.size() != optionLabels.size()) throw new HtmlInputException(
					"List(options) and List(option_labels) should be of same size");
			for (int i = 0; i < options.size(); i++)
			{
				valueLabels.add(new ValueLabel(options.get(i), optionLabels.get(i)));
			}
		}
		this.options = valueLabels;

	}

	public OptionInput(Tuple t) throws HtmlInputException
	{
		set(t);
	}

	@Override
	public void set(Tuple t) throws HtmlInputException
	{
		super.set(t);
		this.setOptions(t.getList(OPTIONS), t.getList(OPTION_LABELS));
	}

	protected OptionInput()
	{
	}

	@Deprecated
	public List<ValueLabel> getChoices()
	{
		return getOptions();
	}

	public List<ValueLabel> getOptions()
	{
		return options;
	}

	public void setOptions(ValueLabel... choices)
	{
		this.options = Arrays.asList(choices);
	}

	public void setOptions(List<ValueLabel> choices)
	{
		this.options = choices;
	}

	public void setOptions(String... choices)
	{
		List<ValueLabel> choicePairs = new ArrayList<ValueLabel>();
		for (String choice : choices)
		{
			choicePairs.add(new ValueLabel(choice, choice));
		}
		this.setOptions(choicePairs);
	}

	@Override
	/**
	 * Returns the label of the selected value.
	 */
	public String getValue()
	{
		for (ValueLabel choice : options)
		{
			if (super.getValue().equals(choice.getValue().toString()))
			{
				return choice.getLabel().toString();
			}
		}
		return "";
	}

	/**
	 * Set the options for this selectbox using a list of strings
	 * 
	 * @param choices
	 *            List of Strings representing the options for this selectbox
	 */
	public void setOptionsFromStringList(List<String> choices)
	{
		List<ValueLabel> choicePairs = new ArrayList<ValueLabel>();
		for (String choice : choices)
		{
			choicePairs.add(new ValueLabel(choice, choice));
		}
		this.setOptions(choicePairs);
	}

}
