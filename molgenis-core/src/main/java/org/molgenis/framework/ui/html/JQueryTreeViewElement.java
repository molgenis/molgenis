package org.molgenis.framework.ui.html;

import org.molgenis.util.SimpleTree;

public class JQueryTreeViewElement extends SimpleTree<JQueryTreeViewElement>
{
	private static final long serialVersionUID = 1L;

	/** Label of the tree that can be made also linkable **/
	String label = null;

	String nodeName = null;

	String htmlValue;

	private boolean isbottom = false;

	private String category;

	private boolean checked = false;

	private String entityID;

	public JQueryTreeViewElement(String name, String entityID, JQueryTreeViewElement parent)
	{
		super(name, parent);
		this.setLabel(name);
		this.setEntityID(entityID);
	}

	public JQueryTreeViewElement(String name, String label, String entityID, JQueryTreeViewElement parent)
	{
		super(name, parent);
		this.setLabel(label);
		this.setEntityID(entityID);

	}

	public JQueryTreeViewElement(String name, JQueryTreeViewElement parent, String htmlValue)
	{
		super(name, parent);
		this.setLabel(name);
		this.htmlValue = htmlValue;
	}

	public JQueryTreeViewElement(String name, String label, JQueryTreeViewElement parent, String htmlValue)
	{
		super(name, parent);
		this.setLabel(label);
		this.htmlValue = htmlValue;
	}

	public void setCheckBox(boolean checked)
	{
		this.checked = checked;
	}

	public boolean getCheckBox()
	{
		return checked;
	}

	public String getNodeName()
	{
		return nodeName;
	}

	// whether the element is ticked/selected
	private boolean isSelected = false;

	// whether the element is collapsed
	private boolean isCollapsed = true;

	public boolean isSelected()
	{
		return isSelected;
	}

	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
	}

	public boolean isCollapsed()
	{
		return isCollapsed;
	}

	public void setCollapsed(boolean isCollapsed)
	{
		this.isCollapsed = isCollapsed;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	private void setEntityID(String entityID)
	{
		this.entityID = entityID;

	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public String getCategory()
	{
		return category;
	}

	public void setHtmlValue(String htmlValue)
	{
		this.htmlValue = htmlValue;
	}

	public String getHtmlValue()
	{
		return htmlValue;
	}

	public String getEntityID()
	{
		return entityID;
	}

	public boolean isIsbottom()
	{
		return isbottom;
	}

	public void setIsbottom(boolean isbottom)
	{
		this.isbottom = isbottom;
	}

	public void toggleNode()
	{
		if (isCollapsed == true)
		{
			isCollapsed = false;
		}
		else
		{
			isCollapsed = true;
		}
	}

	public String toHtml()
	{
		StringBuilder nodeBuilder = new StringBuilder();

		if (!this.isIsbottom())
		{
			StringBuilder childrenNodeBuilder = new StringBuilder();

			if (!this.isCollapsed() && this.hasChildren())
			{

				for (JQueryTreeViewElement childNode : getChildren())
				{
					childrenNodeBuilder.append(childNode.toHtml());
				}

			}

			nodeBuilder.append("<li id = \"").append(getName().replaceAll(" ", "_")).append("\" class=\"");
			nodeBuilder.append(isCollapsed ? "closed" : "open").append("\"><span class=\"folder\">");
			nodeBuilder.append(getLabel() == null ? getName() : getLabel()).append("</span><ul style=\"display:");
			nodeBuilder.append(isCollapsed ? "none" : "block").append("\">").append(childrenNodeBuilder);
			nodeBuilder.append("</ul></li>");
		}
		else
		{
			nodeBuilder.append("<li id = \"").append(getName().replaceAll(" ", "_"));
			nodeBuilder.append("\"><span class=\"point\">");
			nodeBuilder.append(getLabel() == null ? getName() : getLabel()).append("</span></li>");
		}

		return nodeBuilder.toString();

	}

	public String toHtml(String childNode)
	{
		StringBuilder nodeBuilder = new StringBuilder();

		if (!this.isIsbottom())
		{
			nodeBuilder.append("<li id = \"").append(getName().replaceAll(" ", "_")).append("\" class=\"");
			nodeBuilder.append(isCollapsed ? "closed" : "open").append("\"><span class=\"folder\">");
			nodeBuilder.append(getLabel() == null ? getName() : getLabel()).append("</span><ul style=\"display:");
			nodeBuilder.append(isCollapsed ? "none" : "block").append("\">");
			nodeBuilder.append(childNode == null ? "" : childNode).append("</ul></li>");
		}
		else
		{
			nodeBuilder.append("<li id = \"").append(getName().replaceAll(" ", "_"));
			nodeBuilder.append("\"><span class=\"point\">");
			nodeBuilder.append(getLabel() == null ? getName() : getLabel()).append("</span></li>");
		}

		return nodeBuilder.toString();
	}
}