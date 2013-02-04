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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.molgenis.framework.ui.JQueryDateFormatMapper;
import org.molgenis.util.tuple.Tuple;

/**
 * Input for date.
 */

public class DateInput extends HtmlInput<Date>
{
	/** String constants for property name 'hidden' */
	public static final String DATEFORMAT = "dateformat";
	/** Description. Defaults to 'name'. */
	private String javaDataFormat = "dd-MM-yyyy";

	/**
	 * Construct date input with name and using default value of today as value
	 * 
	 * @throws ParseException
	 */
	public DateInput(String name)
	{
		this(name, name, Calendar.getInstance().getTime());
	}

	/**
	 * Construct date input with name and label.
	 * 
	 * @param name
	 * @param label
	 */
	public DateInput(String name, String label)
	{
		super(name, null);
		this.setLabel(label);
	}

	/**
	 * Construct date input with name, value.
	 * 
	 * @param name
	 * @param label
	 * @param value
	 */
	public DateInput(String name, Date value)
	{
		super(name, value);
	}

	/**
	 * Construct date input with name, label, value.
	 * 
	 * @param name
	 * @param label
	 * @param value
	 */
	public DateInput(String name, String label, Date value)
	{
		super(name, value);
		this.setLabel(label);
	}

	/**
	 * Complete constructor with name, label, value, nillable and readonly.
	 * 
	 * @param name
	 * @param label
	 * @param value
	 * @param nillable
	 * @param readonly
	 */
	public DateInput(String name, String label, Date value, boolean nillable, boolean readonly)
	{
		super(name, value);
		if (label != null && !label.equals("null")) this.setLabel(label);
		this.setReadonly(readonly);
		this.setNillable(nillable);
	}

	/**
	 * Construct dateinput using a Tuple to set all properties
	 * 
	 * @param properties
	 * @throws HtmlInputException
	 */
	public DateInput(Tuple properties) throws HtmlInputException
	{
		set(properties);
	}

	/**
	 * Construct date input with name, label, value, tuple for script properties
	 * 
	 * @param name
	 * @param label
	 * @param value
	 * @param nillable
	 * @param readonly
	 * @param dateformat
	 * @param Jqueryproperties
	 */
	public DateInput(String name, String label, Date value, boolean nillable, boolean readonly, String dateformat,
			String jqueryproperties) throws HtmlInputException
	{
		super(name, value);
		if (label != null && !label.equals("null")) this.setLabel(label);
		this.setReadonly(readonly);
		this.setNillable(nillable);
		this.setDateFormat(dateformat);
		this.setJqueryproperties(jqueryproperties);

	}

	/** Null constructor. Use with caution. */
	protected DateInput()
	{
	}

	// tohtml
	@Override
	public String toHtml()
	{
		if (uiToolkit == UiToolkit.JQUERY)
		{
			return this.toJquery();
		}
		else
		{

			return this.toDefault();
		}
	}

	private String toDefault()
	{
		String readonly = (isReadonly() ? " class=\"readonly\" readonly " : "onclick=\"showDateInput(this) " + "");

		if (this.isHidden())
		{
			StringInput input = new StringInput(this.getName(), this.getValue());
			input.setHidden(true);
			return input.toHtml();
		}

		return "<input type=\"text\" id=\"" + this.getId() + "\" name=\"" + this.getName() + "\" value=\""
				+ this.getObjectString() + "\" " + readonly + "\" size=\"32\" autocomplete=\"off\"/>";

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
		return this.getValue("MMMM d, yyyy");
	}

	public String toJquery()
	{
		String description = getName().equals(getDescription()) ? "" : " title=\"" + getDescription() + "\"";
		// set defaults:

		String options = "dateFormat: '"
				+ JQueryDateFormatMapper.toJQueryDateFormat(this.getDateFormat(), this.getDateFormat())
				+ "', changeMonth: true, changeYear: true, showButtonPanel: true";

		String datevalue = this.getValue(this.getDateFormat());
		// change defaults to Jquery scriptvalues if set
		if (this.getJqueryproperties() != null)
		{
			options = this.getJqueryproperties();
		}
		// add clear button if nillable
		if (this.isNillable()) options += ", beforeShow: function( input ) {"
				+ "\n	setTimeout( function() {"
				+ "\n		var buttonPane = $( input ).datepicker( \"widget\" ).find( \".ui-datepicker-buttonpane\" );"
				+ "\n		$( \"<button>\", {text: \"Clear\", click: function() {$.datepicker._clearDate( input );}}).addClass(\"ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all\").appendTo( buttonPane );"
				+ "\n	}, 1 );" + "\n}";

		String validate = "";

		if (!this.isNillable()) validate = " required";
		String result = "<input type=\"text\" readonly=\"readonly\" class=\"" + (this.isReadonly() ? "readonly " : "")
				+ "text ui-widget-content ui-corner-all" + validate + "\" id=\"" + this.getName() + "\" value=\""
				+ datevalue + "\" name=\"" + this.getName() + "\" autocomplete=\"off\"" + description + " />";

		// add the dialog unless readonly (input is always readonly, i.e.,
		// cannot be typed in).
		if (!this.isReadonly()) result += "<script>" + "\n	" + "$(\"#" + this.getId() + "\").bt().datepicker({"
				+ options + "}).click(function(){$(this).datepicker('show')});" + "\n</script>";
		return result;
	}

	@Override
	public String toHtml(Tuple p) throws ParseException, HtmlInputException
	{
		return new DateInput(p).render();

	}

	public String getDateFormat()
	{
		return javaDataFormat;
	}

	public void setDateFormat(String dateformat)
	{
		this.javaDataFormat = dateformat;
	}
}
