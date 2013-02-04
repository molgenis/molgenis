package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.molgenis.framework.ui.html.render.RenderDecorator;
import org.molgenis.util.tuple.Tuple;

/**
 * An HtmlInput allows a user to enter data. Thus, HtmlInput is the base-class
 * for html inputs, such as button, textareas, calendars.
 * 
 * 
 */
public abstract class HtmlInput<E> extends AbstractHtmlElement implements Input<E>, HtmlElement
{
	// STRING CONSTANTS
	/** String constants for property name 'name' */
	public static final String NAME = "name";

	/** String constants for property name 'label' */
	public static final String LABEL = "label";

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

	/** Style to render in */
	protected UiToolkit uiToolkit = HtmlSettings.uiToolkit;

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
		this.setId(name.replace(" ", ""));
		this.setName(name.replace(" ", ""));
		this.setLabel(name);
		// this.setDescription(name);
		this.setId(name);
		this.setValue(value);
	}

	public HtmlInput(String name, String label, E value)
	{
		if (name == null) throw new IllegalArgumentException("name is null");
		this.setId(name.replace(" ", ""));
		this.setName(name.replace(" ", ""));
		this.setLabel(label);
		// this.setDescription(name);
		this.setId(name);
		this.setValue(value);
	}

	/**
	 * Complete constructor
	 * 
	 * @param name
	 * @param label
	 * @param value
	 * @param nillable
	 * @param readonly
	 * @param description
	 */
	public HtmlInput(String name, String label, E value, boolean nillable, boolean readonly, String description)
	{
		this(name, value);
		this.setLabel(label);
		this.setNillable(nillable);
		this.setReadonly(readonly);
		this.setDescription(description);
	}

	/**
	 * Constructor using a tuple. Valid keys for the tuple are listed as
	 * constant, e.g. HtmlInput.NAME
	 * 
	 * @param properties
	 * @throws HtmlInputException
	 */
	public HtmlInput(Tuple properties) throws HtmlInputException
	{
		this.set(properties);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(Tuple t) throws HtmlInputException
	{
		this.id = t.getString(NAME);
		this.name = t.getString(NAME);
		this.label = t.getString(LABEL);
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

	/**
	 * Implement this for generating the specific html-code for the html-input.
	 * All inheriting classes need to override this method in order to work in
	 * the same fashion.
	 * 
	 * @return The html-code for the html-input.
	 */
	public abstract String toHtml();

	public String getLabel()
	{
		if (label != null) return label;
		return name;
	}

	public HtmlInput<E> setLabel(String label)
	{
		this.label = label;
		return this;
	}

	public String getName()
	{
		if (name == null) return this.getId();
		return name;
	}

	public HtmlInput<E> setName(String name)
	{
		this.name = name;
		return this;
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

	/**
	 * Generic method to produce the inputs html by setting its parameters from
	 * tuple. This is used to create Freermarker macros like <@string name="id"
	 * /> Needs to be overriden to work.
	 * 
	 * @throws ParseException
	 * @throws HtmlInputException
	 */
	@Deprecated
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		throw new UnsupportedOperationException();
	}

	/** Synonym to toHtml */
	@Override
	public String render(Tuple params) throws ParseException, HtmlInputException
	{
		return this.toHtml(params);
	}

	@Override
	public String render()
	{
		return this.toHtml();
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return null;
	}

	// BORING PROPERTIES
	public HtmlInput<E> setValue(E value)
	{
		this.value = value;
		return this;
	}

	public boolean isReadonly()
	{
		return readonly;
	}

	public HtmlInput<E> setReadonly(boolean readonly)
	{
		this.readonly = readonly;
		return this;
	}

	public boolean isHidden()
	{
		return hidden;
	}

	public HtmlInput<E> setHidden(boolean hidden)
	{
		this.hidden = hidden;
		return this;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public void setId(String id)
	{
		this.id = id;
	}

	public String getStyle()
	{
		return style;
	}

	public HtmlInput<E> setStyle(String style)
	{
		this.style = style;
		return this;
	}

	public String getTooltip()
	{
		return tooltip;
	}

	public HtmlInput<E> setTooltip(String tooltip)
	{
		this.tooltip = tooltip;
		return this;
	}

	public String getTarget()
	{
		return target.replace(".", "_");
	}

	public HtmlInput<E> setTarget(String target)
	{
		this.target = target.replace(".", "_");
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public HtmlInput<E> setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public boolean isNillable()
	{
		return nillable;
	}

	public HtmlInput<E> setNillable(boolean required)
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

	@Override
	public String toString()
	{
		return this.toHtml();
	}

	public String getHtml()
	{
		return toHtml();
	}

	public synchronized Integer getSize()
	{
		return size;
	}

	public synchronized HtmlInput<E> setSize(Integer size)
	{
		this.size = size;
		return this;
	}

	public HtmlInput<E> setTabIndex(int tabidx)
	{
		tabIndex = " tabindex=" + Integer.toString(tabidx);
		return this;
	}

	public UiToolkit getUiToolkit()
	{
		return this.uiToolkit;
	}

}
