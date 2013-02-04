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

import java.util.List;

import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

/**
 * Input for many-to-many cross-references (xref) to choose data entities from
 * the database. Selectable data items will be shown as selection box and are
 * loaded dynamically via an 'ajax' service.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE", justification = "Always use \n for newlines")
public class MrefInput<E extends Entity> extends AbstractRefInput<List<E>>
{
	/** Minimal constructor */
	public MrefInput(String name, Class<? extends Entity> xrefEntityClass, List<E> dummyList)
	{
		super(name, xrefEntityClass, dummyList);
		setXrefEntity(xrefEntityClass);
	}

	/**
	 * Alternative minimal constructor using an entity object instance to
	 * configure all.
	 */
	public MrefInput(String name, List<E> objects)
	{
		this(name, objects.get(0).getClass(), objects);
		setXrefField(name);
	}

	/** Alternative minimal constructor using an entity class to configure all. */
	public MrefInput(String name, Class<? extends Entity> xrefEntityClass)
	{
		super(name, xrefEntityClass, null);
		setXrefEntity(xrefEntityClass);
	}

	/**
	 * Alternative minimal constructor using entity name
	 * 
	 * @throws HtmlInputException
	 * 
	 * @throws ClassNotFoundException
	 * @throws ClassNotFoundException
	 */
	// @Deprecated
	// public MrefInput(String name, String entityName) throws
	// HtmlInputException
	// {
	// super(name, entityName);
	// }

	/** Complete constructor */
	@Deprecated
	public MrefInput(String name, String label, List<E> values, Boolean nillable, Boolean readonly, String description,
			Class<? extends Entity> xrefEntityClass)
	{
		super(name, xrefEntityClass, label, values, nillable, readonly, description);
		setXrefEntity(xrefEntityClass);
	}

	/**
	 * Alternative complete constructor using String name of entityClass
	 * 
	 * @throws HtmlInputException
	 * @throws ClassNotFoundException
	 */
	@Deprecated
	public MrefInput(String name, String label, List<E> values, Boolean nillable, Boolean readonly, String description,
			String xrefEntityClass) throws HtmlInputException, ClassNotFoundException
	{
		super(name, (Class<? extends Entity>) Class.forName(xrefEntityClass), label, values, nillable, readonly,
				description);
		setXrefEntity(xrefEntityClass);
	}

	/**
	 * Constructor taking parameters from tuple
	 * 
	 * @throws HtmlInputException
	 */
	public MrefInput(Tuple t) throws HtmlInputException
	{
		super(t);
	}

	protected MrefInput()
	{
		super();
	}

	@Override
	/**
	 * Note, this returns the labels of the selected values.
	 */
	public String getValue()
	{
		StringBuilder strBuilder = new StringBuilder();
		for (Entity value : getObject())
		{
			if (strBuilder.length() == 0) strBuilder.append(value.getLabelValue());
			else
				strBuilder.append(", ").append(value.getLabelValue());
		}
		return strBuilder.toString();
	}

	@Override
	public String toHtml(Tuple params) throws HtmlInputException
	{
		return new MrefInput<E>(params).render();
	}

	@Override
	protected String renderOptions()
	{
		final String option = "\t<option selected value=\"%s\">%s</option>\n";

		final StringBuilder result = new StringBuilder();
		for (Entity value : getObject())
		{
			result.append(String.format(option, value.getIdValue(), value.getLabelValue()));
		}

		return result.toString();
	}

	@Override
	protected String getHtmlRefType()
	{
		return "multiple";
	}

	@Override
	public String renderHidden()
	{
		StringBuilder strBuilder = new StringBuilder();

		for (E object : this.getObject())
		{
			strBuilder.append("<input name=\"").append(this.getName()).append("\" type=\"hidden\" value=\"");
			strBuilder.append(object.getIdValue()).append("\"/>");
		}

		return strBuilder.toString();
	}

}
