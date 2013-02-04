/**
 * File: TextInput.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2006-03-08, 1.0.0, DI Matthijssen; Creation
 * <li>2006-05-14, 1.1.0, MA Swertz; Refectoring into Invengine.
 * </ul>
 * TODO look at the depreciated functions.
 */

package org.molgenis.framework.ui.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for datetime data..
 */
public class DatetimeInput extends HtmlInput<Date>
{
	/** Construct DatetimeInput with name */
	public DatetimeInput(String name)
	{
		super(name, null);
	}

	/** Construct DatetimeInput with name, value */
	public DatetimeInput(String name, Date value)
	{
		super(name, value);
	}

	/** Construct DatetimeInput with name, label, value */
	public DatetimeInput(String name, String label, Date value)
	{
		super(name, label, value);
	}

	/** Constructe Datetimeinput with name, label, value, nillable, and readonly */
	public DatetimeInput(String name, String label, Date value, boolean nillable, boolean readonly)
	{
		super(name, value);
		if (label != null && !label.equals("null")) this.setLabel(label);
		this.setReadonly(readonly);
		this.setNillable(nillable);
	}

	/** Construct Datetimeinput using a Tuple with properties */
	public DatetimeInput(Tuple properties) throws HtmlInputException
	{
		super(properties);
	}

	// tohtml
	@Override
	public String toHtml()
	{
		if (this.isHidden())
		{
			StringInput input = new StringInput(this.getName(), this.getValue());
			input.setHidden(true);
			return input.toHtml();
		}

		if (uiToolkit == UiToolkit.ORIGINAL)
		{
			return this.toDefault();
		}
		if (uiToolkit == UiToolkit.JQUERY)
		{
			return this.toJquery();
		}

		return "NOT IMPLEMENTED FOR LIBRARY " + uiToolkit;
	}

	private String toDefault()
	{
		String readonly = isReadonly() ? " class=\"readonly\" readonly=\"readonly\" "
				: "onclick=\"showDateInput(this,true) " + "";

		return "<input type=\"text\" id=\"" + this.getId() + "\" name=\"" + getName() + "\"  size=\"32\" value=\""
				+ getValue() + "\" " + readonly + "\" autocomplete=\"off\"/>";
	}

	public String toJquery()
	{
		String description = getName().equals(getDescription()) ? "" : " title=\"" + getDescription() + "\"";
		String options = "dateFormat: 'yy-mm-dd', timeFormat: 'hh:mm:ss', changeMonth: true, changeYear: true, showButtonPanel: true";
		// String options =
		// "dateFormat: 'dd-mm-yy', changeMonth: true, changeYear: true, showButtonPanel: true";
		// add clear button if nillable
		String createScript = "function( input ) {setTimeout(function() {var buttonPane = $( input ).datetimepicker( \"widget\" ).find( \".ui-datepicker-buttonpane\" );"
				+ "$( \"<button>\", {text: \"Clear\", click: function() { $(input).datetimepicker( 'setDate', null );}}).addClass(\"ui-datepicker-close ui-state-default ui-priority-secondary ui-corner-all\").appendTo( buttonPane );}, 1 );}";

		if (this.isNillable()) options += ", beforeShow: " + createScript;

		String validate = "";

		if (!this.isNillable()) validate = " required";
		String result = "<input type=\"text\" readonly=\"readonly\" class=\"" + (this.isReadonly() ? "readonly " : "")
				+ "text ui-widget-content ui-corner-all" + validate + "\" id=\"" + this.getName() + "\" value=\""
				+ this.getValue("yyyy-MM-dd HH:mm:ss") + "\" name=\"" + this.getName() + "\" autocomplete=\"off\" "
				// + this.getValue("dd-MM-yyyy HH:mm") + "\" name=\"" +
				// this.getName() + "\" autocomplete=\"off\" "
				+ description + "/>";

		// add the dialog unless readonly (input is always readonly, i.e.,
		// cannot be typed in).
		if (!this.isReadonly()) result += "<script>" + "$(\"#" + this.getName() + "\").bt().datetimepicker({" + options
				+ "}).click(function(){$(this).datetimepicker('show')});</script>";
		return result;
	}

	public String getValue(String format)
	{
		DateFormat formatter = new SimpleDateFormat(format, Locale.US);

		Object dateObject = getObject();
		if (dateObject == null)
		{
			return "";
		}
		if (dateObject.equals(""))
		{
			return "";
		}
		// If it's already a string, return it
		if (dateObject instanceof String)
		{
			return dateObject.toString();
		}

		// If it's a Date object, first format and then return
		String result;
		try
		{
			result = formatter.format(dateObject);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
		result = result.substring(0, 1).toUpperCase() + result.substring(1);
		return result;
	}

	@Override
	public String getValue()
	{
		return getValue("MMMM d, yyyy, HH:mm:ss");
	}

	@Override
	public String toHtml(Tuple p) throws HtmlInputException
	{
		return new DatetimeInput(p).render();
	}
}
