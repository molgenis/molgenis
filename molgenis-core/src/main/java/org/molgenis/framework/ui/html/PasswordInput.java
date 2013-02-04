package org.molgenis.framework.ui.html;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for passwords. The password will be made unreadible. TODO: sent
 * encoded.
 */
public class PasswordInput extends ValidatingInput<String>
{
	public PasswordInput(String name)
	{
		this(name, null);
	}

	public PasswordInput(String name, String value)
	{
		super(name, value);
	}

	public PasswordInput(String name, String label, String value, Boolean readonly, Boolean nillable, String description)
	{
		super(name, label, value, readonly, nillable, description);
	}

	public PasswordInput(Tuple t) throws HtmlInputException
	{
		super(t);
	}

	@Override
	public String toHtml()
	{
		String readonly = (this.isReadonly()) ? "readonly class=\"readonly\" " : "";

		if (this.isHidden())
		{
			StringInput input = new StringInput(this.getName(), this.getValue());
			input.setHidden(true);
			return input.toHtml();
		}

		if (this.uiToolkit.equals(UiToolkit.ORIGINAL))
		{
			return "<input type=\"password\" id=\"" + getId() + "\" name=\"" + getName() + "\" value=\"" + getValue()
					+ "\" " + readonly + tabIndex + " />";
		}
		else
		{

			String validate = this.isNillable() || this.isReadonly() ? "" : " required";
			String cssClass = this.uiToolkit == UiToolkit.JQUERY ? " class=\"text ui-widget-content ui-corner-all"
					+ validate + " " + readonly + "\"" : "";
			String result = "<input type=\"password\""
					+ cssClass
					+ " id=\""
					+ this.getId()
					+ "\" name=\""
					+ this.getName()
					+ "\"  "
					+ (this.getSize() != null && this.getSize() > 0 ? "onfocus=\"startcounter(this, " + getSize()
							+ ")\" onblur=\"endcounter()\"" : "") + readonly + " value=\"" + this.getValue() + "\">";

			result += "<script>$('#" + this.getId() + "').autoGrowInput({comfortZone: 16, minWidth:" + this.getWidth()
					* this.getFontsize() + ", maxWidth: " + this.getMaxWidth() * this.getFontsize() + "});</script>";

			return result;
		}

	}

	@Override
	public String toHtml(Tuple params) throws HtmlInputException
	{
		return new PasswordInput(params).render();
	}

}
