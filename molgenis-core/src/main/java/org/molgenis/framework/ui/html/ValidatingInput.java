package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for string data that checks within the UI if required data is filled in
 * and if the values are conform validationString. Subclasses use this
 * validationString to restrict data entries. If maxHeight > 1 then a textarea
 * is used to render.
 * 
 * Note: validation is only enabled when using jQuery. This is based on
 * http://docs.jquery.com/Plugins/Validation
 */
public class ValidatingInput<E> extends HtmlInput<E>
{

	public int height;
	private int minHeight = 1;
	private int maxHeight = 1;
	public int width = 32;
	public int maxWidth = 255;

	private int fontsize = 16;

	protected String validationString = "";

	/**
	 * Construct validating input using a Tuple of properties
	 * 
	 * @param properties
	 * @throws HtmlInputException
	 */
	public ValidatingInput(Tuple properties) throws HtmlInputException
	{
		super(properties);
	}

	/**
	 * Construct validating input using name.
	 * 
	 * @param name
	 */
	public ValidatingInput(String name)
	{
		this(name, null);
	}

	/**
	 * Construct validating input using name, value
	 * 
	 * @param name
	 * @param value
	 */
	public ValidatingInput(String name, E value)
	{
		super(name, value);
	}

	/**
	 * Construct validating input using name, label value
	 * 
	 * @param name
	 * @param label
	 * @param value
	 */
	public ValidatingInput(String name, String label, E value)
	{
		super(name, label, value);
	}

	/**
	 * Construct validating input using name, label, value, nillable, readonly.
	 * 
	 * @param name
	 * @param label
	 * @param value
	 * @param nillable
	 * @param readonly
	 * @param description
	 */
	public ValidatingInput(String name, String label, E value, boolean nillable, boolean readonly, String description)
	{
		this(name, value);
		this.setLabel(label);
		this.setNillable(nillable);
		this.setReadonly(readonly);
		this.setDescription(description);
	}

	/** Null constructor. Use with caution */
	protected ValidatingInput()
	{
		super();
	}

	@Override
	public String toHtml()
	{
		String readonly = (this.isReadonly()) ? "readonly " : "";

		if (this.isHidden())
		{
			if (this.uiToolkit == UiToolkit.ORIGINAL || this.uiToolkit == UiToolkit.JQUERY)
			{
				return "<input name=\"" + this.getName() + "\"type=\"hidden\" value=\"" + this.getObjectString()
						+ "\"/>";
			}
		}
		String validate = " " + this.validationString;
		if (!this.isNillable() && !this.isReadonly()) validate += " required";

		String cssClass = this.uiToolkit == UiToolkit.JQUERY ? " class=\"text ui-widget-content ui-corner-all"
				+ validate + " " + readonly + "\"" : "";

		String description = " title=\"" + this.getDescription() + "\"";

		String descriptionJS = HtmlSettings.showDescription ? ".bt()" : "";

		if (this.maxHeight > 1)
		{
			String result = "<textarea "
					+ description
					+ cssClass
					+ " id=\""
					+ this.getId()
					+ "\" name=\""
					+ this.getName()
					+ "\"  "
					+ (this.getSize() != null && this.getSize() > 0 ? "onfocus=\"startcounter(this, " + getSize()
							+ ")\" onblur=\"endcounter()\"" : "") + " cols=\"" + this.getWidth() + "\" rows=\""
					+ this.getHeight() + "\" " + readonly + " >" + this.getObjectString() + "</textarea>";

			result += "<script>$('#" + getId() + "')" + descriptionJS + "; showTextInput(document.getElementById('"
					+ this.getId() + "')," + this.getMinHeight() + "," + this.getMaxHeight() + ");</script>";

			return result;
		}
		else
		{
			String result = "<input "
					+ description
					+ cssClass
					+ " id=\""
					+ this.getId()
					+ "\" name=\""
					+ this.getName()
					+ "\"  "
					+ (this.getSize() != null && this.getSize() > 0 ? "onfocus=\"startcounter(this, " + getSize()
							+ ")\" onblur=\"endcounter()\"" : "") + readonly + " value=\"" + this.getObjectString()
					+ "\">";

			result += "<script>$('#" + this.getId() + "')" + descriptionJS
					+ ".autoGrowInput({comfortZone: 16, minWidth:" + this.getWidth() * this.fontsize + ", maxWidth: "
					+ this.getMaxWidth() * this.fontsize + "});</script>";

			return result;
		}
	}

	@Override
	public String toHtml(Tuple params) throws HtmlInputException, ParseException
	{
		return new StringInput(params).render();
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "";
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public ValidatingInput<E> setHeight(int height)
	{
		this.height = height;
		return this;
	}

	public int getMinHeight()
	{
		return minHeight;
	}

	public void setMinHeight(int minHeight)
	{
		this.minHeight = minHeight;
	}

	public int getMaxHeight()
	{
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight)
	{
		this.maxHeight = maxHeight;
	}

	public int getMaxWidth()
	{
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth)
	{
		this.maxWidth = maxWidth;
	}

	public int getFontsize()
	{
		return fontsize;
	}

	public void setFontsize(int fontsize)
	{
		this.fontsize = fontsize;
	}

}
