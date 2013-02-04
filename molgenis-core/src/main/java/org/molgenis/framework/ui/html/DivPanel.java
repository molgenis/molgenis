package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.molgenis.util.tuple.Tuple;

/**
 * Provides a panel to order your inputs using html <code>div</code>.
 */
public class DivPanel extends HtmlWidget
{
	LinkedHashMap<String, HtmlInput<?>> inputs = new LinkedHashMap<String, HtmlInput<?>>();
	boolean makeNewDiv = true;

	public DivPanel()
	{
		this(UUID.randomUUID().toString(), null);
	}

	public DivPanel(String name, String label)
	{
		super(name, label);
		this.setLabel(label);
	}

	public DivPanel(String name, String label, boolean makeNewDiv)
	{
		super(name, label);
		this.setLabel(label);
		this.makeNewDiv = makeNewDiv;
	}

	/**
	 * Adds the given inputs to the TablePanel.
	 * 
	 * @param HtmlInput
	 *            ... inputs
	 */
	public void add(HtmlInput<?>... inputs)
	{
		for (HtmlInput<?> input : inputs)
		{
			this.inputs.put(input.getName(), input);
		}
	}

	/**
	 * Removes the given inputs from the TablePanel.
	 * 
	 * @param HtmlInput
	 *            ... inputs
	 */
	public void remove(HtmlInput<?>... inputs)
	{
		for (HtmlInput<?> input : inputs)
		{
			this.inputs.remove(input.getName());
		}
	}

	public HtmlInput<?> get(String name)
	{
		return this.inputs.get(name);
	}

	@Override
	/**
	 * Each input is rendered with a label and in its own div to enable scripting.
	 */
	public String toHtml()
	{
		StringBuilder strBuilder = new StringBuilder("<div>");
		for (HtmlInput<?> i : this.inputs.values())
		{
			if (makeNewDiv)
			{
				strBuilder.append("<div style=\"clear:both;");

				if (i.isHidden())
				{
					strBuilder.append("display:none\"");
				}
				else
				{
					strBuilder.append("display:block\"");
				}
				if (i.getId() != null)
				{
					strBuilder.append(" id=\"div").append(i.getId()).append("\">");
				}

			}
			strBuilder.append("<label style=\"width:16em;float:left;\" for=\"").append(i.getName()).append("\">");
			strBuilder.append(i.getLabel()).append("</label>").append(i.toHtml());
			if (!i.isNillable()) strBuilder.append(" *");

			if (makeNewDiv)
			{
				strBuilder.append("</div>");
			}

		}
		strBuilder.append("</div>");
		return strBuilder.toString();
	}

	/**
	 * Tries to set the values of all the inputs in the DivPanel to the
	 * corresponding ones in the request tuple.
	 * 
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	public void setValuesFromRequest(Tuple request)
	{
		Object object;
		List<HtmlInput<?>> inputList = new ArrayList<HtmlInput<?>>();
		fillList(inputList, this);
		for (HtmlInput input : inputList)
		{
			object = request.get(input.getName());
			if (input instanceof SelectMultipleInput && object instanceof String)
			{
				// avoid messing up multiple select boxes
				List<String> stringList = new ArrayList<String>();
				stringList.add((String) object);
				input.setValue(stringList);
			}
			else
			{
				if (object != null)
				{
					input.setValue(object);
				}
			}
		}
	}

	/**
	 * Add to 'inputList' all the inputs that are part of the 'startInput'
	 * DivPanel. Fully recursive, so nested TablePanels are also taken into
	 * account.
	 * 
	 * @param inputList
	 * @param startInput
	 */
	private void fillList(List<HtmlInput<?>> inputList, DivPanel startInput)
	{
		for (HtmlInput<?> input : startInput.inputs.values())
		{
			if (input instanceof DivPanel || input instanceof RepeatingPanel)
			{
				fillList(inputList, (DivPanel) input);
			}
			else
			{
				inputList.add(input);
			}
		}
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		// TODO this should work with also a 'nested' value.
		throw new UnsupportedOperationException();
	}

}
