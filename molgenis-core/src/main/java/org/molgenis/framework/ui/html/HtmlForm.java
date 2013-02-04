package org.molgenis.framework.ui.html;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * (incomplete) Helper for creating forms for entities.
 */
public class HtmlForm extends HtmlWidget
{
	/** Some columns can be hidden from view, using the name */
	private List<String> hiddenColumns = new ArrayList<String>();
	/**
	 * Some columns may be collapsed by default, but can be unhidden by pushing
	 * a button
	 */
	private List<String> compactView = new ArrayList<String>();
	/** A form may be readonly */
	private boolean readonly = false;
	/**
	 * A form may be used to add a new record. This may result in readonly
	 * fields, e.g. for autoid.
	 */
	private boolean newRecord = false;
	/** Inputs */
	private List<HtmlInput<?>> inputs = new ArrayList<HtmlInput<?>>();
	/** Actions */
	private List<ActionInput> actions = new ArrayList<ActionInput>();

	public HtmlForm()
	{
		super(UUID.randomUUID().toString());
	}

	public boolean isNewRecord()
	{
		return newRecord;
	}

	public void setNewRecord(boolean newRecord)
	{
		this.newRecord = newRecord;
	}

	public List<String> getHiddenColumns()
	{
		return hiddenColumns;
	}

	public void setHiddenColumns(List<String> hiddenColumns)
	{
		if (hiddenColumns != null) this.hiddenColumns = hiddenColumns;
	}

	/** Create the inputs to be shown */
	public List<HtmlInput<?>> getInputs()
	{
		return this.inputs;
	}

	public void setInputs(List<HtmlInput<?>> inputs)
	{
		this.inputs = inputs;
	}

	@Override
	public boolean isReadonly()
	{
		return readonly;
	}

	@Override
	public HtmlInput<String> setReadonly(boolean readonly)
	{
		this.readonly = readonly;
		return this;
	}

	public void addInput(HtmlInput<?>... inputs)
	{
		for (HtmlInput<?> input : inputs)
		{
			this.inputs.add(input);
		}

	}

	public List<ActionInput> getActions()
	{
		return actions;
	}

	public void setActions(List<ActionInput> actions)
	{
		this.actions = actions;
	}

	public void addAction(ActionInput... actions)
	{
		for (ActionInput action : actions)
		{
			this.getActions().add(action);
		}
	}

	public List<String> getCompactView()
	{
		return compactView;
	}

	/**
	 * All fields that are not in this list will be marked as 'collapsed' so
	 * they are not visible unless the button 'details' is pushed
	 * 
	 * @param compactView
	 *            list of field names
	 */
	public void setCompactView(List<String> compactView)
	{
		this.compactView = compactView;
	}

	/**
	 * A simple method to create a edited view for one entity.
	 */
	@Override
	public String toHtml()
	{
		StringBuilder htmlBuilder = new StringBuilder("<table>");
		for (HtmlInput<?> i : this.getInputs())
		{
			htmlBuilder.append("<tr><td name=\"").append(i.getLabel()).append("\">");
			htmlBuilder.append(i.getLabel()).append("</td><td>").append(i.getHtml()).append("</td></tr>");
		}
		htmlBuilder.append("</table>");
		return htmlBuilder.toString();
	}

}
