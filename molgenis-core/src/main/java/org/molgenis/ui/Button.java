package org.molgenis.ui;

/**
 * Buttons to fire a MOLGENIS action.
 * 
 * TODO: also support other events such as hyperlinks, free javascript?
 */
public class Button extends MolgenisComponent<Button>
{
	private Icon icon;
	private String action;
	private String label;
	private String onClick;

	public Button(String id)
	{
		super(id);
	}

	public Button(String id, String label)
	{
		super(id);
		this.label = label;
	}

	public String getAction()
	{
		if (this.action == null) return this.getId();
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public Icon getIcon()
	{
		return icon;
	}

	public Button setIcon(Icon icon)
	{
		this.icon = icon;
		return this;
	}

	public String getLabel()
	{
		if (label == null) return this.getId();
		return label;
	}

	public Button setLabel(String label)
	{
		this.label = label;
		return this;
	}

	public String getOnClick()
	{
		return onClick;
	}

	public void setOnClick(String onClick)
	{
		this.onClick = onClick;
	}
}
