/**
 * File: invengine.screen.form.SelectInput <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2006-03-07, 1.0.0, DI Matthijssen
 * <li>2006-05-14; 1.1.0; MA Swertz integration into Inveninge (and major
 * rewrite)
 * <li>2006-05-14; 1.2.0; RA Scheltema major rewrite + cleanup
 * </ul>
 */

package org.molgenis.framework.ui.html;

// jdk
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

/**
 * Input for cross-reference (xref) entities in a MOLGENIS database. Data will
 * be shown as selection box. Use xrefEntity to specifiy what entity provides
 * the values for selection. Use xrefField to define which entity field to use
 * for the values. Use xrefLabels to select which field(s) should be shown as
 * labels to the user (optional).
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE", justification = "Always use \n for newlines")
public class XrefInput<E extends Entity> extends AbstractRefInput<E>
{
	protected XrefInput()
	{
		super();
	}

	/** Minimal constructor */
	public XrefInput(String name, Class<? extends Entity> xrefEntityClass, E value)
	{
		super(name, xrefEntityClass, value);
		setXrefEntity(xrefEntityClass);
	}

	/** Alternative minimal constructor using an entity class to configure all. */
	public XrefInput(String name, Class<E> xrefEntityClass)
	{
		super(name, xrefEntityClass, null);
		setXrefEntity(xrefEntity);
	}

	/** Complete constructor */
	public XrefInput(String name, String label, E value, Boolean nillable, Boolean readonly, String description,
			Class<? extends Entity> xrefEntityClass)
	{
		super(name, xrefEntityClass, label, value, nillable, readonly, description);
		setXrefEntity(xrefEntityClass);
	}

	/**
	 * Constructor taking parameters from tuple
	 * 
	 * @throws HtmlInputException
	 */
	public XrefInput(Tuple t) throws HtmlInputException
	{
		super(t);
	}

	@Override
	/**
	 * Returns the label of the selected value.
	 */
	public String getValue()
	{
		if (getObject() != null) return this.getObject().getLabelValue();
		return StringUtils.EMPTY;
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		return new XrefInput<E>(params).render();
	}

	@Override
	public void set(Tuple t) throws HtmlInputException
	{
		super.set(t);
		if (t.isNull(XREF_ENTITY)) throw new HtmlInputException("parameter " + XREF_ENTITY + " cannot be null");
		else
			this.setXrefEntity(t.getString(XREF_ENTITY));
	}

	public XrefInput(String name, String entityClassname) throws HtmlInputException, ClassNotFoundException
	{
		this(name, (Class<E>) Class.forName(entityClassname), null);
	}

	@Override
	protected String renderOptions()
	{
		final StringBuilder options = new StringBuilder();
		if (this.getObject() != null) options.append(String.format("\t<option selected value=\"%s\">%s</option>\n",
				getObject().getIdValue(), this.getValue()));
		return options.toString();
	}

	@Override
	protected String getHtmlRefType()
	{
		return "search";
	}

	@Override
	public String renderHidden()
	{
		Object value = this.getObject() == null ? "" : this.getObject().getIdValue();
		return "<input name=\"" + this.getName() + "\" type=\"hidden\" value=\"" + value + "\"/>";
	}
}
