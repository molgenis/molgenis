/**
 * File: invengine.screen.form.SelectInput <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2006-03-07, 1.0.0, DI Matthijssen
 * <li>2006-05-14; 1.1.0; MA Swertz integration into Inveninge (and major
 * rewrite)
 * <li>2006-05-14; 1.2.0; RA Scheltema major rewrite + cleanup
 * </ul>
 */

package org.molgenis.framework.ui.html;

// jdk
import java.text.ParseException;
import java.util.List;

import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.ValueLabel;

/**
 * Input for choosing from an pre-defined series of options. Each option is of
 * class ValueLabel to define values and labels. The options will be shown as
 * dropdown select box.
 */
public class SelectInput extends OptionInput<Object>
{
	private String targetfield;
	private String onchange;
	private int width = 16;

	public SelectInput(Tuple t) throws HtmlInputException
	{
		super(t);
	}

	public SelectInput(String name)
	{
		super(name, null);
	}

	public SelectInput(String name, Object value)
	{
		super(name, value);
	}

	public SelectInput()
	{
		super();
	}

	@Override
	public String toHtml()
	{

		String readonly = (this.isReadonly()) ? " class=\"readonly\" " : "";

		String onchange = (this.onchange != null) ? " onchange=\"" + this.onchange + "\"" : "";

		if (this.isHidden())
		{
			StringInput input = new StringInput(this.getName(), super.getValue());
			input.setLabel(this.getLabel());
			input.setDescription(this.getDescription());
			input.setHidden(true);
			return input.toHtml();
		}

		StringBuilder optionsHtmlBuilder = new StringBuilder();

		for (ValueLabel choice : getOptions())
		{
			if (super.getObject() != null && super.getObject().toString().equals(choice.getValue().toString()))
			{
				optionsHtmlBuilder.append("\t<option selected value=\"").append(choice.getValue()).append("\">");
				optionsHtmlBuilder.append(choice.getLabel()).append("</option>\n");
			}
			else if (!this.isReadonly())
			{
				optionsHtmlBuilder.append("\t<option value=\"").append(choice.getValue()).append("\">");
				optionsHtmlBuilder.append(choice.getLabel()).append("</option>\n");
			}
		}
		// start with empty option, unless there was already a value selected
		if ((!this.isReadonly() && this.isNillable()) || ("".equals(super.getObject()) && this.isNillable()))
		{
			if (super.getObject() != null && super.getObject().toString().equals("")) optionsHtmlBuilder.insert(0,
					"\t<option value=\"\">&nbsp;</option>\n");
			else
				optionsHtmlBuilder.append("\t<option value=\"\">&nbsp;</option>\n");
		}

		if (this.uiToolkit == UiToolkit.ORIGINAL)
		{
			return "<select class=\"" + this.getClazz() + "\" id=\"" + this.getId() + "\" name=\"" + this.getName()
					+ "\" " + readonly + onchange + ">\n" + optionsHtmlBuilder.toString() + "</select>\n";
		}
		else if (this.uiToolkit == UiToolkit.JQUERY)
		{
			String description = " title=\"" + this.getDescription() + "\"";
			readonly = this.isReadonly() ? "readonly " : "";
			return "<select class=\"" + readonly + " ui-widget-content ui-corner-all\" id=\"" + this.getId()
					+ "\" name=\"" + this.getName() + "\" " + onchange
					+ (this.getWidth() != -1 ? " style=\"width:" + this.getWidth() + "em;\" " : "") + description
					+ ">\n" + optionsHtmlBuilder.toString() + "</select><script>$(\"#" + this.getId()
					+ "\").chosen();</script>\n";
		}
		return "STYLE NOT AVAILABLE";
	}

	public String getTargetfield()
	{
		return targetfield;
	}

	public void setTargetfield(String targetfield)
	{
		this.targetfield = targetfield;
	}

	public String getOnchange()
	{
		return this.onchange;
	}

	public void setOnchange(String onchange)
	{
		this.onchange = onchange;
	}

	public void addOption(Object value, Object label)
	{
		this.getOptions().add(new ValueLabel(value.toString(), label.toString()));
	}

	/**
	 * Set the options for the input
	 * 
	 * @param entities
	 *            list of entities to add as options (values)
	 * @param valueField
	 *            field used for identification
	 * @param labelField
	 *            field used for label (what shows on the screen)
	 */
	public void setOptions(List<? extends Entity> entities, String valueField, String labelField)
	{
		// clear list
		this.getOptions().clear();

		// add new values and labels
		for (Entity e : entities)
		{
			this.addOption(e.get(valueField), e.get(labelField));
		}
	}

	public void addEntityOptions(List<? extends Entity> entities)
	{
		// add new values and labels
		for (Entity e : entities)
		{
			this.addOption(e.getIdValue(), e.getLabelValue());
		}
	}

	public void setEntityOptions(List<? extends Entity> entities)
	{
		// first clear list
		this.getOptions().clear();

		addEntityOptions(entities);
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		return new SelectInput(params).render();
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		if (this.uiToolkit == UiToolkit.JQUERY)
		{
			// return
			// "<link rel=\"stylesheet\" href=\"lib/jquery-plugins/chosen.css\">\n"+
			// "<script src=\"lib/jquery-plugins/chosen.js\" type=\"text/javascript\" language=\"javascript\"></script>\n";
		}
		return "";
	}

	public int getWidth()
	{
		return width;
	}

	/**
	 * Set component width. Default is 16. Set to -1 for auto-size.
	 * 
	 * @param width
	 */
	public void setWidth(int width)
	{
		this.width = width;
	}
}
