package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.util.tuple.Tuple;

/**
 * Html Element defines any element that can be rendered as Html view.
 * <ul>
 * <li>getCustomHtmlHeaders() defines css and javascript dependencies
 * <li>render() defines the tranformation into html.
 * <li>render(tuple) is a shorthand to configure an html element using a tuple
 * (used for Freemarker macros).
 * <li>getId() is an optional unique id for this element that can be used for
 * scripting.
 * </ul>
 * 
 */
public interface HtmlElement
{
	public static final String ID = "id";
	public static final String CLASS = "class";

	/**
	 * Libraries that indicate the styling library used. Currently DEFAULT
	 * (molgenis original standard) and JQUERY (pimped using jQuery).
	 */
	public enum UiToolkit
	{
		/** jquery-ui */
		JQUERY,
		/** old layout */
		ORIGINAL,
		/** twitter bootstrap */
		BOOTSTRAP
	};

	/** Get any html headers needed for this element */
	public String getCustomHtmlHeaders();

	/** Render the contents as HTML string */
	public String render();

	/** Initialize and render based on a tuple */
	public String render(Tuple params) throws ParseException, HtmlInputException;

	/** Get the unique id of this html element, for scripting purposes */
	public String getId();

	/** Set the unique id of this html element, for scripting purposes */
	public void setId(String id);

	/** Set the css class(es) for this element */
	public String getClazz();

	/** Set the css class(es) for this element */
	public void setClazz(String clazz);

	/** Set the properties of this HtmlElement using a tuple */
	public void set(Tuple properties) throws HtmlInputException;
}
