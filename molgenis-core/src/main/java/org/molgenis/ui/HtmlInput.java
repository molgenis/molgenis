package org.molgenis.ui;

import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.framework.ui.html.HtmlSettings;
import org.molgenis.framework.ui.html.render.RenderDecorator;
import org.molgenis.util.tuple.Tuple;

/**
 * An HtmlInput allows a user to enter data. Thus, HtmlInput is the base-class
 * for html inputs, such as button, textareas, calendars.
 * 
 * 
 */
public abstract class HtmlInput<T extends HtmlInput<T, E>, E> extends MolgenisComponent<HtmlInput<T, E>>
{
	// STRING CONSTANTS
	/** String constants for property name 'name' */
	public static final String NAME = "name";

	// /** String constants for property name 'label' */
	// public static final String LABEL = "label";

	/** String constants for property name 'name' */
	public static final String VALUE = "value";

	/** String constants for property name 'value' */
	public static final String NILLABLE = "nillable";

	/** String constants for property name 'readonly' */
	public static final String READONLY = "readonly";

	/** String constants for property name 'description' */
	public static final String DESCRIPTION = "decription";

	/** String constants for property name 'hidden' */
	public static final String HIDDEN = "hidden";

	public static final String JQUERYPROPERTIES = "Jqueryproperties";

	// PROPERTIES
	/** The ID of this input. Defaults to 'name'. */
	private String id;

	/** The name of the input */
	private String name;

	/** The value of the input */
	private E value;

	/** The label of the input. Defaults to 'name'. */
	private String label;

	/** Flag indicating whether this input is readonly ( default: false) */
	private boolean readonly;

	/** Flag indicating whether this input is hidden ( default: false ) */
	protected boolean hidden;

	/** indicate if this is required form field */
	private boolean nillable = true;

	/** indicate if this input should be hidden in 'compact' view */
	private boolean collapse = false;

	/** String with a one-line description of the input ( optional ) */
	private String tooltip;

	/** variable for make-up */
	private String style;

	/** variable to validate size */
	private Integer size;

	/** for hyperlinks...??? */
	private String target = "";

	/** Description. Defaults to 'name'. */
	private String description = "";

	/** to pass jquery script properties. */
	private String Jqueryproperties;

	/** Tab index of this input (optionl) */
	protected String tabIndex = "";

	protected RenderDecorator renderDecorator = HtmlSettings.defaultRenderDecorator;

	/**
	 * Standard constructor, which sets the name and value of the input
	 * 
	 * @param name
	 *            The name of the html-input.
	 * @param value
	 *            The value of the html-input.
	 */
	public HtmlInput(String name, E value)
	{
		if (name == null) name = UUID.randomUUID().toString().replace("-", "");
		this.id(name.replace(" ", ""));
		this.setName(name.replace(" ", ""));
		// this.setLabel(name);
		// this.setDescription(name);
		this.id(name);
		this.setValue(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(Tuple t) throws HtmlInputException
	{
		this.id = t.getString(NAME);
		this.name = t.getString(NAME);
		// this.label = t.getString(LABEL);
		this.value = (E) t.get(VALUE);
		if (t.getBoolean(NILLABLE) != null) this.nillable = t.getBoolean(NILLABLE);
		if (t.getBoolean(READONLY) != null) this.readonly = t.getBoolean(READONLY);
		this.description = t.getString(DESCRIPTION);
		if (t.getBoolean(HIDDEN) != null) this.hidden = t.getBoolean(HIDDEN);
		this.Jqueryproperties = t.getString(JQUERYPROPERTIES);

	}

	/** No arguments constructor. Use with caution */
	protected HtmlInput()
	{
		this(UUID.randomUUID().toString(), null);
	}

	public String getLabel()
	{
		return label;
	}

	@SuppressWarnings("unchecked")
	public T label(String label)
	{
		// assert (label != null); fails web tests due to label -> null
		// constructors, so allow it
		this.label = label;
		return (T) this;
	}

	public String getName()
	{
		if (name == null) return this.getId();
		return name;
	}

	@SuppressWarnings("unchecked")
	public T setName(String name)
	{
		this.name = name;
		return (T) this;
	}

	// TODO: This *needs* to be renamed to getValue()
	public E getObject()
	{
		return value;
	}

	public String getObjectString()
	{
		if (this.value == null) return "";
		else
			return value.toString();
	}

	// TODO: This *needs* to be renamed to getValueToString() or removed!!!
	public String getValue()
	{
		return getValue(false);
	}

	/**
	 * Get the value of the input as a String, optionally replacing special
	 * characters like \\n and &gt;
	 * 
	 * @param replaceSpecialChars
	 * @return
	 */
	public String getValue(boolean replaceSpecialChars)
	{
		if (getObject() == null)
		{
			return "";
		}

		// todo: why different from getHtmlValue()??
		// if (replaceSpechialChars)
		// {
		// return
		// this.renderDecorator.render(getObject().toString().replace("\n",
		// "<br>")
		// .replace("\r", "").replace(">", "&gt;")
		// .replace("<", "&lt;"));
		// }
		// else
		// {
		return getObject().toString();
		// }
	}

	public String getHtmlValue(int maxLength)
	{
		// we render all tags, but we stop rendering text outside tags after
		// maxLength
		StringBuilder strBuilder = new StringBuilder();
		boolean inTag = false;
		int count = 0;
		for (char c : this.getHtmlValue().toCharArray())
		{
			// check if we go into tag
			if ('<' == c)
			{
				inTag = true;

			}

			if (inTag || count < maxLength)
			{
				strBuilder.append(c);
			}

			if ('>' == c)
			{
				inTag = false;
			}

			if (!inTag) count++;
		}

		return strBuilder.toString();
	}

	public String getHtmlValue()
	{
		String value = null;
		value = this.getValue();
		// .replace("\n", "<br>").replace("\r", "")
		// .replace(">", "&gt;").replace("<", "&lt;");
		return this.renderDecorator.render(value);
	}

	public String getJavaScriptValue()
	{
		String value = StringEscapeUtils.escapeXml(StringEscapeUtils.escapeJavaScript(this.getValue()));
		return value;
	}

	// BORING PROPERTIES
	public HtmlInput<T, E> setValue(E value)
	{
		this.value = value;
		return this;
	}

	public boolean isReadonly()
	{
		return readonly;
	}

	public HtmlInput<T, E> setReadonly(boolean readonly)
	{
		this.readonly = readonly;
		return this;
	}

	public boolean isHidden()
	{
		return hidden;
	}

	public HtmlInput<T, E> setHidden(boolean hidden)
	{
		this.hidden = hidden;
		return this;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T id(String id)
	{
		this.id = id;
		return (T) this;
	}

	public String getStyle()
	{
		return style;
	}

	public HtmlInput<T, E> setStyle(String style)
	{
		this.style = style;
		return this;
	}

	public String getTooltip()
	{
		return tooltip;
	}

	public HtmlInput<T, E> setTooltip(String tooltip)
	{
		this.tooltip = tooltip;
		return this;
	}

	public String getTarget()
	{
		return target.replace(".", "_");
	}

	public HtmlInput<T, E> setTarget(String target)
	{
		this.target = target.replace(".", "_");
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public HtmlInput<T, E> setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public boolean isNillable()
	{
		return nillable;
	}

	public HtmlInput<T, E> setNillable(boolean required)
	{
		this.nillable = required;
		return this;
	}

	public String getJqueryproperties()
	{
		return Jqueryproperties;
	}

	public void setJqueryproperties(String jqueryproperties)
	{
		Jqueryproperties = jqueryproperties;
	}

	public boolean isCollapse()
	{
		return collapse;
	}

	public void setCollapse(boolean collapse)
	{
		this.collapse = collapse;
	}

	public synchronized Integer getSize()
	{
		return size;
	}

	public synchronized HtmlInput<T, E> setSize(Integer size)
	{
		this.size = size;
		return this;
	}

	public HtmlInput<T, E> setTabIndex(int tabidx)
	{
		tabIndex = " tabindex=" + Integer.toString(tabidx);
		return this;
	}
}
