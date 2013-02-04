package org.molgenis.framework.ui.html;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for yes/no (boolean) values.
 */
public class BoolInput extends HtmlInput<Boolean>
{
	/** Construct a BoolInput with name and default value */
	public BoolInput(String name)
	{
		super(name, null);
	}

	/** Construct a BoolInput with name and label */
	public BoolInput(String name, String label)
	{
		super(name, label, null);
	}

	/** Construct a BoolInput with name and value */
	public BoolInput(String name, Boolean value)
	{
		super(name, value);
	}

	/** Construct a BoolInput with name, label and value */
	public BoolInput(String name, String label, Boolean value)
	{
		super(name, label, value);
	}

	/**
	 * Complete constructure with name, label, value, nillable and readonly
	 * options
	 * 
	 * @param name
	 * @param label
	 * @param value
	 * @param nillable
	 * @param readonly
	 */
	public BoolInput(String name, String label, Boolean value, boolean nillable, boolean readonly)
	{
		this(name, label, value);
		this.setValue(value);
		this.setNillable(nillable);
		this.setReadonly(readonly);
	}

	/**
	 * Construct BoolInput from a tuple
	 * 
	 * @param properties
	 * @throws HtmlInputException
	 */
	public BoolInput(Tuple properties) throws HtmlInputException
	{
		super(properties);
	}

	/** Null constructor. Use with caution */
	protected BoolInput()
	{
	}

	@Override
	public String getValue()
	{
		if (super.getValue().equals("true")) return "yes";
		if (super.getValue().equals("false")) return "no";
		return "";
	}

	@Override
	public String toHtml()
	{
		if (this.isHidden())
		{
			StringInput input = new StringInput(this.getName(), this.getValue());
			input.setLabel(this.getLabel());
			input.setDescription(this.getDescription());
			input.setHidden(true);
			return input.toHtml();
		}
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
		if (isReadonly()) return "<select class=\"readonly\" id=\"" + this.getId() + "\" name=\"" + this.getName()
				+ "\" readonly=\"readonly\">" + "<option value=\"" + getValue() + "\" selected>" + getValue()
				+ "</option></select>\n";
		else
			return "<select id=\"" + this.getId() + "\" name=\"" + this.getName() + "\">" + "<option value=\"true\""
					+ (getValue().equals("yes") ? "selected" : "") + ">yes</option>" + "<option value=\"false\""
					+ (getValue().equals("no") ? "selected" : "") + ">no</option>" + "<option value=\"\""
					+ (getValue().equals("") ? "selected" : "") + "></option>" + "</select>\n";
	}

	private String toJquery()
	{
		String description = " title=\"" + this.getDescription() + "\"";
		if (isReadonly()) return "<select data-placeholder=\"?\" style=\"width: 100px;\" class=\"readonly chzn-no-search ui-widget-content ui-corner-all\" id=\""
				+ this.getId()
				+ "\" name=\""
				+ this.getName()
				+ "\" readonly=\"readonly\" "
				+ description
				+ ">"
				+ "<option value=\""
				+ getValue()
				+ "\" selected>"
				+ getValue()
				+ "</option></select>\n</select><script>$(\"#"
				+ this.getId()
				+ "\").chosen({placeholder: ''});</script>\n";
		else
			return "<select  data-placeholder=\"?\" style=\"width: 100px;\" class=\"chzn-no-search ui-widget-content ui-corner-all\" id=\""
					+ this.getId()
					+ "\" name=\""
					+ this.getName()
					+ "\" "
					+ description
					+ ">"
					+ "<option value=\"true\""
					+ (getValue().equals("yes") ? "selected" : "")
					+ ">yes</option>"
					+ "<option value=\"false\""
					+ (getValue().equals("no") ? "selected" : "")
					+ ">no</option>"
					+ (isNillable() ? "<option value=\"\"" + (getValue().equals("") ? "selected" : "") + ">?</option>"
							: "")
					+ "</select>\n</select><script>$(\"#"
					+ this.getId()
					+ "\").chosen({placeholder: ''});</script>\n";
	}

	@Override
	public String toHtml(Tuple p) throws HtmlInputException
	{
		return new BoolInput(p).render();
	}
}