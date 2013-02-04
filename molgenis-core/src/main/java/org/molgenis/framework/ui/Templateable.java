package org.molgenis.framework.ui;

/**
 * Contains the parameters needed for freemarker template application.
 */
@Deprecated
public interface Templateable
{
	/**
	 * Retrieve the view name of the screen. This allows flexibility of views as
	 * the Screen can now indicate how it should be presented. In the current
	 * implementation, this must be the name of a macro in the template. It is
	 * good practice to let this macro having the same name as the class.
	 * 
	 * @see #getTemplate()
	 * 
	 * @return a generic name of the layout to be used
	 */
	@Deprecated
	public String getMacro();

	/**
	 * Retrieve the path of the template file containing the view(s) of the
	 * screens.
	 * 
	 * This path must be relative to the root of the classpath as that will be
	 * used for loading. If null, it is ignored by the template loading
	 * mechanism.
	 * 
	 * {@link #getMacro()}
	 * 
	 * @return path of the template file.
	 */
	public String getTemplate();

	/**
	 * This enables plugin developers to add their custom css or javascript (or
	 * whatever) headers.
	 * 
	 * @return custom html elements for the header, e.g. for script or css
	 */
	public String getCustomHtmlHeaders();

	/**
	 * This enables plugin developers to add additional 'onload' javascript
	 * statements to the page's body tag.
	 * 
	 * @return string for 'body onload="[default];[custom]"'
	 */
	public String getCustomHtmlBodyOnLoad();
}
