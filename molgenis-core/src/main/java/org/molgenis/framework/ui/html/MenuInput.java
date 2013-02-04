package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.util.tuple.Tuple;

/**
 * Menu can contain a bunch of action inputs. Optionally, MenuInput can be
 * nested in a submenu.
 */
public class MenuInput extends AbstractHtmlElement implements HtmlElement
{
	private List<HtmlElement> menusAndButtons = new ArrayList<HtmlElement>();
	private String label;

	public MenuInput(String name, String label)
	{
		super(name);
		this.label = label;
	}

	public void AddAction(ActionInput action)
	{
		menusAndButtons.add(action);
	}

	public void AddMenu(MenuInput menu)
	{
		menusAndButtons.add(menu);
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public String render()
	{
		return render(this);
	}

	protected String render(MenuInput root)
	{
		StringBuilder itemsBuilder = new StringBuilder();
		if (root == null) root = this;

		for (HtmlElement item : menusAndButtons)
		{
			if (item instanceof ActionInput)
			{
				ActionInput action = (ActionInput) item;
				if (this.equals(root))
				{
					itemsBuilder.append(action.render()).append("<br />");
				}
				else
				{
					itemsBuilder.append("<li><a href=\"#\" onclick=\"").append(action.getJavaScriptAction())
							.append("\">");
					itemsBuilder.append(action.getButtonValue()).append("</a></li>");
				}
			}
			else
			{
				MenuInput menu = (MenuInput) item;
				if (this.equals(root))
				{
					itemsBuilder.append("<button onclick=\"return false;\">").append(menu.getLabel())
							.append("</button>").append(menu.render(root));
				}
				else
				{
					itemsBuilder.append("<li><a href=\"#\">").append(menu.getLabel()).append("</a>")
							.append(menu.render(root)).append("</li>");
				}
			}
		}

		if (this.equals(root))
		{
			StringBuilder strBuilder = new StringBuilder(
					"<div style=\"vertical-align:middle\"><input id=\"downloadButton\" type=\"button\" value=\"Download\" ");
			strBuilder.append("onclick=\"if (document.getElementById('");
			strBuilder.append(getId());
			strBuilder.append("').style.display=='none') {document.getElementById('");
			strBuilder.append(getId());
			strBuilder.append("').style.display='block';} else {document.getElementById('");
			strBuilder.append(getId());
			strBuilder.append("').style.display='none';} \" ");
			strBuilder.append("/>");
			strBuilder.append("<script>$(\"#downloadButton\").button();</script>");
			strBuilder.append("</div>");
			strBuilder.append("<div id=\"");
			strBuilder.append(getId());
			strBuilder
					.append("\" style=\"position:absolute; z-index:1; background-color:white; padding:2px; display:none\">");
			strBuilder.append(itemsBuilder).append("</div>");

			return strBuilder.toString();
		}
		else
		{
			return itemsBuilder.insert(0, "<ul>").append("</ul>").toString();
		}
	}

	@Override
	public String render(Tuple params) throws ParseException, HtmlInputException
	{
		MenuInput root = null;
		return render(root);
	}

}
