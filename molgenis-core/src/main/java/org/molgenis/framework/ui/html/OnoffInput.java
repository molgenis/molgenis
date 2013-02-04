package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for checkbox data (yes/no).
 */
public class OnoffInput extends HtmlInput<Boolean>
{
	private String onValue = "on";

	// constructor(s)
	/**
	 * ...
	 * 
	 * @param name
	 * @param value
	 */
	public OnoffInput(String name, Boolean value)
	{
		super(name, value);
		this.setReadonly(false);
	}

	public OnoffInput(String name, String label, Boolean value)
	{
		super(name, value);
		this.setLabel(label);
		this.setReadonly(false);
	}

	public OnoffInput(String name, String label, Boolean value, boolean nillable, boolean readonly)
	{
		super(name, value);
		this.setLabel(label);
		this.setNillable(nillable);
		this.setReadonly(readonly);
	}

	public OnoffInput(Tuple t)
	{
		this(t.getString(NAME), t.getString(LABEL), t.getBoolean(VALUE), t.getBoolean(NILLABLE), t.getBoolean(READONLY));
	}

	// HtmlInput overloads
	/**
	 * 
	 */
	@Override
	public String toHtml()
	{
		if (this.isHidden())
		{
			StringInput input = new StringInput(this.getName(), this.getValue());
			input.setHidden(true);
			return input.toHtml();
		}
		String readonly = (isReadonly() ? "readonly " : "");
		String checked = (getObject() != null && Boolean.valueOf(getObject().toString())) ? "checked" : "";

		String html = "<input id=\"" + this.getId() + "\" type=\"checkbox\" " + readonly + checked + " name=\""
				+ this.getName() + " \" value=\"" + this.onValue + "\"/>";

		return html;
	}

	@Override
	public String getHtmlValue()
	{
		String value = (getObject() != null && getObject().equals(true)) ? "yes" : "no";
		return value;
	}

	public void setOnValue(String value)
	{
		this.onValue = value;
	}

	@Override
	public String toHtml(Tuple params) throws ParseException
	{
		return new OnoffInput(params).render();
	}
}
