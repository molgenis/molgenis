package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.framework.server.services.MolgenisGuiService;
import org.molgenis.util.tuple.Tuple;

/**
 * The ActionInput defines action buttons. When clicked, it will result in a new
 * request(__action=&lt;name&gt;)
 */
public class ActionInput extends HtmlInput<Object>
{
	public enum Type
	{
		/** save current record */
		SAVE("Save"),
		/** cancel current action (closes popup window, refreshes opener window) */
		CANCEL("Cancel"),
		/** goto next screen */
		NEXT("Next"),
		// close current screen
		CLOSE("Close"),
		/** automatically close current dialogue */
		AUTOCLOSE("Autoclose"),
		/**
		 * Default: a custom action that requires you to set label, tooltip and
		 * javascript yourself
		 */
		CUSTOM("Set Label, Tooltip, and JavaScriptAction yourself");

		private Type(String tag)
		{
			this.tag = tag;
		}

		public final String tag;

		@Override
		public String toString()
		{
			return tag;
		}
	}

	private int width = 0;

	/** Path to an icon image */
	private String icon;
	private int iconHeight = 16;
	private int iconWidth = 16;

	/** Type of submit */
	private Type type;

	/** JavaScript action */
	private String JavaScriptAction;

	/** Text to display on button (normally "value") */
	private String buttonValue;

	/** If false, no label and only icon will be shown */
	private boolean showLabel = true;

	// constructor(s)
	/**
	 * Default constructor, type is submit
	 */
	public ActionInput(String name)
	{
		this(name, Type.CUSTOM);
	}

	/**
	 * Constructor that sets action name and label.
	 * 
	 * @param name
	 * @param label
	 */
	public ActionInput(String name, String label)
	{
		this(name, Type.CUSTOM);
		this.setLabel(label);
		this.setButtonValue(label); // override default button value (name) with
									// label
	}

	/**
	 * Constructor that sets action name, label and button value (text to show
	 * on button).
	 * 
	 * @param name
	 * @param label
	 */
	public ActionInput(String name, String label, String buttonValue)
	{
		this(name, label);
		this.setTooltip(buttonValue);
		this.setButtonValue(buttonValue); // override label as button value with
											// explicit button value
	}

	/**
	 * Create a new instance of ActionInput.
	 * 
	 * @param name
	 *            name of the input.
	 * @param type
	 *            type of the input. @see Type
	 */
	public ActionInput(String name, Type type)
	{
		super(name, type);
		this.setType(type);
		this.setLabel(type.toString());
		this.setTooltip(type.toString());
		this.setButtonValue(name); // specific for action buttons
	}

	public ActionInput(Type select_target)
	{
		this(select_target.toString());
		this.setLabel(select_target.toString().replace("_", " "));
	}

	public ActionInput(Tuple t) throws HtmlInputException
	{
		super(t);
		this.type = Type.CUSTOM;
	}

	public ActionInput()
	{
	}

	// HtmlInput overloads
	@Override
	public String toHtml()
	{
		// Use plain HTML rendering if chosen by user OR if there is only an
		// icon and no text to be shown
		// on the button. Reason for this is that jQuery does not render
		// image-only buttons well, messing up
		// the alignment.
		if (this.uiToolkit == UiToolkit.ORIGINAL || (this.getIcon() != null && this.buttonValue.equals("")))
		{
			return this.renderDefault();
		}
		else if (this.uiToolkit == UiToolkit.JQUERY)
		{
			return this.renderJquery();
		}
		return "ERROR";
	}

	public String renderDefault()
	{
		StringBuffer input = new StringBuffer("");
		if (getIcon() != null)
		{
			input.append("<img class=\"edit_button\" src=\"" + getIcon() + "\" alt=\"" + getLabel() + "\" onclick=\""
					+ getJavaScriptAction() + " $(this).closest('form').submit();\" title=\"" + this.getTooltip()
					+ "\" id=\"" + this.getId() + "\" style=\"" + this.getStyle() + "\" " + tabIndex + " />");
		}
		else
		{
			input.append("<input type=\"submit\" onclick=\"" + getJavaScriptAction() + "\" title=\""
					+ this.getTooltip() + "\" id=\"" + this.getId() + "\"" + "value=\"" + this.getButtonValue()
					+ "\" style=\"" + this.getStyle() + "\" " + tabIndex + " />");
		}

		return input.toString();
	}

	// attribute methods
	/**
	 * Get the icon that is shown on this button.
	 */
	public String getIcon()
	{
		return icon;
	}

	/**
	 * Set the icon that should be shown on this button
	 * 
	 * @param icon
	 *            relative path from WebContent or classpath.
	 */
	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	/**
	 * Javascript action for this button.
	 * 
	 * @return onClick javascript
	 */
	public String getJavaScriptAction()
	{
		if (JavaScriptAction == null)
		{
			if (this.type == Type.SAVE)
			{
				StringBuffer jScript = new StringBuffer();
				if (this.uiToolkit == UiToolkit.ORIGINAL) jScript
						.append("if( validateForm(document.forms[0],molgenis_required) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis'+new Date().getTime();} document.forms[0].target = window.opener.name; document.forms[0].submit(); window.close();} else return false;");
				else
					jScript.append("if( $(this.form).valid() && validateForm(document.forms[0],molgenis_required) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis'+new Date().getTime();} document.forms[0].__show.value = ''; document.forms[0].__action.value ='"
							+ this.getValue()
							+ "'; document.forms[0].target = window.opener.name; document.forms[0].submit(); window.close();} return false;");
				// jScript.append("alert('click');");

				return jScript.toString();
			}
			else if (this.type == Type.NEXT)
			{
				StringBuffer jScript = new StringBuffer();
				jScript.append("if( validateForm(document.forms[0],molgenis_required) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis_"
						+ MolgenisGuiService.getNewWindowId()
						+ "';} document.forms[0].__show.value='popup'; document.forms[0].submit();} else return false;");
				return jScript.toString();
			}
			else if (this.type == Type.CLOSE)
			{
				return "window.close();";
			}
			else if (this.type == Type.CUSTOM)
			{
				return "$(this).closest('form').find('input[name=__action]').val('" + this.getName() + "');";
			}
		}
		return JavaScriptAction;
	}

	/**
	 * Override default javascript 'onClick' action.
	 * 
	 * @param javaScriptAction
	 */
	public void setJavaScriptAction(String javaScriptAction)
	{
		JavaScriptAction = javaScriptAction;
	}

	/**
	 * The Type of this action
	 * 
	 * @return type
	 * @see Type
	 */
	public Type getType()
	{
		return type;
	}

	/** Set the Type of this action, e.g. SAVE. */
	public void setType(Type type)
	{
		this.type = type;
	}

	@Override
	public String getLabel()
	{
		return "&nbsp;";
	}

	public String getButtonValue()
	{
		if (buttonValue == null) return this.getLabel();
		return buttonValue;
	}

	public void setButtonValue(String buttonValue)
	{
		this.buttonValue = buttonValue;
	}

	/** Helper method to produce the html for the icon (&lt;img&gt;) */
	public String getIconHtml()
	{
		return "<img src=\"" + this.getIcon() + "\"/>";
	}

	/** Helper method to produce html for the clickable image */
	public String toIconHtml()
	{
		return "<img class=\"edit_button\" src=\"" + getIcon() + "\" title=\"" + getLabel() + "\" onclick=\""
				+ this.getJavaScriptAction() + "\">";
	}

	/** Helper method to render this button as clickable link */
	public String toLinkHtml()
	{
		return "<a title=\"" + this.getDescription() + "\" onclick=\"" + this.getJavaScriptAction() + "\">"
				+ getLabel() + "</a>";
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		return new ActionInput(params).render();
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "";
	}

	private String renderJquery()
	{
		String icons = "";
		String iconClassCss = "";
		if (getIcon() != null)
		{
			// Make a nice CSS class name for the icon
			String iconClass = getIcon().replace("/", "-").replace(".png", "").replace(".jpg", "");
			// Make the actual CSS for the icon button
			iconClassCss = "<style type=\"text/css\">";
			iconClassCss += "." + iconClass + " { background-image: url(" + getIcon() + ") !important; ";// +
			// "width: " + this.getIconWidth() + "px; height: " +
			// this.getIconHeight() + "px; }";
			iconClassCss += "</style>";
			// Make the jQuery code to be appended to make the button an image
			// button
			icons += "{ icons: {primary:'" + iconClass + "', secondary: null}"
					+ (isShowLabel() ? "}" : ", text: false }");
		}
		String result = iconClassCss + "<input type=\"submit\" id=\"" + this.getId() + "\" onclick=\""
				+ this.getJavaScriptAction() + "\" value=\"" + this.getButtonValue() + "\" />" + "<script>$(\"#"
				+ this.getId() + "\")" + (width > 0 ? ".width(" + width + ")" : "")
				// + (getIcon() != null ? ".height(" + (this.getIconHeight() +
				// 10) + ").width(" + (this.getIconWidth() + 10) + ")" : "")
				+ ".button(" + icons + ");</script>\n";

		return result;
	}

	public boolean showLabel()
	{
		return showLabel;
	}

	public boolean isShowLabel()
	{
		return showLabel;
	}

	public void setShowLabel(boolean showLabel)
	{
		this.showLabel = showLabel;
	}

	public void setIconHeight(int iconHeight)
	{
		this.iconHeight = iconHeight;
	}

	public int getIconHeight()
	{
		return iconHeight;
	}

	public void setIconWidth(int iconWidth)
	{
		this.iconWidth = iconWidth;
	}

	public int getIconWidth()
	{
		return iconWidth;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

}
