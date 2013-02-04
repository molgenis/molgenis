package org.molgenis.framework.ui.html;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.molgenis.util.SimpleTree;

public class JQueryTreeView<E> extends HtmlWidget
{
	private SimpleTree<JQueryTreeViewElement> treeData;

	public JQueryTreeView(String name, SimpleTree<JQueryTreeViewElement> treeData)
	{
		super(name);
		this.treeData = treeData;
	}

	/**
	 * Checks whether the given node contains labels that have been
	 * selected/toggled, entailing the node should be presented expanded
	 * (opened).
	 * 
	 * @param node
	 * @param selected
	 * @return
	 */
	private boolean nodeOpen(JQueryTreeViewElement node, List<String> selectedLabels)
	{
		Vector<JQueryTreeViewElement> children = node.getChildren();
		// iterate through all children of this node
		for (JQueryTreeViewElement child : children)
		{
			// if this child is in the selected list, node should be open
			if (selectedLabels.contains(child.getLabel()))
			{
				return true;
			}
			// if this child has children and one of them is in the selected
			// list, node should be open
			if (child.hasChildren())
			{
				if (nodeOpen(child, selectedLabels) == true)
				{
					return true;
				}
			}
		}
		// no (grand)child selected, so node should be closed
		return false;
	}

	/**
	 * 
	 * No Nodes collapsed explicitly. Node is manually closed then by giving its
	 * LI element a "closed"(/opened) CSS class. Animation enabled, speed is
	 * "normal". "Cookie" persistence enabled, causing the current tree state to
	 * be persisted. Dynamically adding a sub tree to the existing tree
	 * demonstrated.
	 */
	private String renderTree(JQueryTreeViewElement node, List<String> selectedLabels)
	{
		StringBuilder strBuilder = new StringBuilder();

		if (node.hasChildren())
		{

			if (!node.getChildren().get(0).hasChildren() && node.getCheckBox() == true)
			{
				strBuilder.append("<li id = \"").append(node.getName().replaceAll(" ", "_"))
						.append("\" class=\"closed");
				strBuilder.append("\" style=\"display:none;\"><span class=\"folder\"><input type=\"checkbox\" id=\"");
				strBuilder.append(node.getEntityID()).append("\" name=\"")
						.append(node.getEntityID().split("_identifier_")[0]).append('\"');
				strBuilder.append(selectedLabels.contains(node.getLabel()) ? " checked=\"yes\"" : "").append(" />");
				strBuilder.append(node.getLabel()).append("</span>\n").append("<ul>\n");
			}
			else
			{
				strBuilder.append("<li id = \"").append(node.getName().replaceAll(" ", "_"))
						.append("\" class=\"closed");
				strBuilder.append("\" style=\"display:none;\"><span class=\"folder\">").append(node.getLabel())
						.append("</span>\n");
				strBuilder.append("<ul>\n");
			}

			Vector<JQueryTreeViewElement> children = node.getChildren();

			for (JQueryTreeViewElement child : children)
			{
				strBuilder.append(renderTree(child, selectedLabels));
			}
			strBuilder.append("</ul>\n</li>\n");
		}
		else
		{
			strBuilder.append("<li id = \"").append(node.getName().replaceAll(" ", "_"));
			strBuilder.append("\" style=\"display:none;\"><span class=\"point\"><input type=\"checkbox\" id=\"");
			strBuilder.append(node.getEntityID()).append("\" name=\"")
					.append(node.getEntityID().split("_identifier_")[0]).append("\"");
			strBuilder.append(selectedLabels.contains(node.getLabel()) ? " checked=\"yes\"" : "").append(" />")
					.append(node.getLabel());
			strBuilder.append("</span></li>\n");
		}

		return strBuilder.toString();
	}

	public String toHtml(List<String> selected)
	{
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder
				.append("<script src=\"res/jquery-plugins/Treeview/jquery.treeview.js\" language=\"javascript\"></script>\n");
		htmlBuilder.append("<script src=\"res/scripts/catalogue.js\" language=\"javascript\"></script>\n");
		htmlBuilder
				.append("<link rel=\"stylesheet\" href=\"res/jquery-plugins/Treeview/jquery.treeview.css\" type=\"text/css\" media=\"screen\" />\n");
		htmlBuilder
				.append("<link rel=\"stylesheet\" href=\"res/css/catalogue.css\" type=\"text/css\" media=\"screen\" />\n");
		htmlBuilder
				.append("<script src=\"res/jquery-plugins/splitter/splitter.js\" language=\"javascript\"></script>\n");
		htmlBuilder
				.append("<link type=\"text/css\" href=\"jquery/css/smoothness/jquery-ui-1.8.7.custom.css\" rel=\"Stylesheet\"/>");
		htmlBuilder
				.append("<script src=\"jquery/development-bundle/ui/jquery-ui-1.8.7.custom.js\" language=\"javascript\"></script>\n");
		htmlBuilder.append("<ul id=\"browser\" class=\"pointtree\">\n")
				.append(renderTree(treeData.getRoot(), selected)).append("</ul>\n");
		return htmlBuilder.toString();
	}

	@Override
	public String toHtml()
	{
		return toHtml(Collections.<String> emptyList());
	}
}