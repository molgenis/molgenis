/**
 * File: invengine.screen.form.FileInput <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-05-14; 1.0.0; MA Swertz Creation.
 * </ul>
 * TODO: make efficient.
 */
package org.molgenis.framework.ui.html;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for upload of files.
 */
public class FileInput extends HtmlInput<Object>
{
	public static final String INPUT_CURRENT_DOWNLOAD = "__filename";
	public static final String ACTION_DOWNLOAD = "download";

	/** Entity name, needed for download */
	private String entityname;

	public FileInput(String name, Object value)
	{
		super(name, value);
	}

	public FileInput(String name)
	{
		this(name, null);
	}

	public FileInput(String name, String label, String value, boolean nillable, boolean readonly)
	{
		this(name, value);
		this.setLabel(label);
		this.setNillable(nillable);
		this.setReadonly(readonly);
	}

	public FileInput(Tuple t) throws HtmlInputException
	{
		set(t);
	}

	protected FileInput()
	{
	}

	@Override
	public String toHtml()
	{
		// FIXME how to check not null file uploads
		this.setNillable(true);
		String readonly = (isReadonly() ? "readonly class=\"readonly\" readonly " : "");

		StringInput hidden = new StringInput(this.getName(), super.getValue());
		hidden.setLabel(this.getLabel());
		hidden.setDescription(this.getDescription());
		hidden.setHidden(true);

		if (this.isHidden())
		{
			return hidden.toHtml();
		}

		if (uiToolkit == UiToolkit.ORIGINAL)
		{
			return hidden.toHtml()
					+ (!isReadonly() ? "<input type=\"file\" " + readonly + "name=\"filefor_" + getName()
							+ "\" size=\"20\">" : "") + getValue();
		}
		else if (uiToolkit == UiToolkit.JQUERY)
		{
			return hidden.toHtml()
					+ (!isReadonly() ? "<input class=\"ui-widget-content ui-corner-all\" type=\"file\" " + readonly
							+ "name=\"filefor_" + getName() + "\" size=\"20\">" : "") + getValue();
		}
		else
		{
			return "ERROR";
		}
	}

	/**
	 * {@inheritDoc}. Extended to show download button.
	 */
	@Override
	public String getValue()
	{
		if (!super.getValue().isEmpty()) return super.getValue()
				+ "<input class=\"manbutton\" type=\"image\" src=\"img/download.png\" alt=\"download\" onclick=\"this.form.__filename.value = '"
				+ super.getValue() + "';this.form.__action.value='" + ACTION_DOWNLOAD + "'; return true;\"/>";
		return super.getValue();
	}

	/**
	 * Retrieve the name of the entity for wich a download has to be started
	 * 
	 * @return entity name
	 */
	public String getEntityname()
	{
		return entityname;
	}

	/**
	 * Set the entity for which this file can be downloaded/.
	 * 
	 * @param entityname
	 */
	public void setEntityname(String entityname)
	{
		this.entityname = entityname;
	}

	@Override
	public String toHtml(Tuple params) throws HtmlInputException
	{
		return new FileInput(params).render();
	}
}
