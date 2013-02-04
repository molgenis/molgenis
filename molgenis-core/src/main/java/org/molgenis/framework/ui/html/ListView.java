package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.QueryRule;
import org.molgenis.util.tuple.Tuple;

/**
 * (in progress) The listview shows a list of InputForm (sets of inputs) in an
 * excel like view:
 * <ul>
 * <li>input labels as column headers
 * <li>each row rending the value of the input.
 * <li>each InputForm is assumed to have the same set of inputs.
 * <li>each InputForm may have getActions(); those are shown in first column
 * rendered as icon
 * <li>if this list isSelectable (default), each row will have checkbox before
 * each row.
 * <li>if this list isReadonly (default) then each row will show only values,
 * otherwise inputs TODO make easier to populate with entity instances.
 */
public class ListView extends HtmlInput<List<HtmlForm>>
{
	List<HtmlForm> rows = new ArrayList<HtmlForm>();
	String sortedBy = null;
	QueryRule.Operator sortOrder = QueryRule.Operator.SORTDESC;
	boolean selectable = true;
	int offset = 0;

	public ListView(String name, List<HtmlForm> rows)
	{
		super(name, null);
		this.setRows(rows);
		this.setReadonly(true);
	}

	public ListView(String name)
	{
		super(name, null);
		this.setReadonly(true);
	}

	public void setRows(List<HtmlForm> rows)
	{
		if (rows == null) throw new IllegalArgumentException("GridPanel.setRows cannot be null");
		this.rows = rows;
	}

	public List<HtmlForm> getRows()
	{
		return this.rows;
	}

	@Override
	public ListView setValue(List<HtmlForm> value)
	{
		this.setRows(value);
		return this;
	}

	@Override
	public String getValue()
	{
		return this.toHtml();
	}

	public String getSortedBy()
	{
		return sortedBy;
	}

	public void setSortedBy(String sortedBy)
	{
		this.sortedBy = sortedBy;
	}

	public QueryRule.Operator getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(QueryRule.Operator sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public void addRow(HtmlForm... forms)
	{
		for (HtmlForm f : forms)
		{
			this.getRows().add(f);
		}

	}

	public boolean isSelectable()
	{
		return selectable;
	}

	public void setSelectable(boolean selectable)
	{
		this.selectable = selectable;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	@Override
	public String toHtml()
	{
		// todo: we need to include sortaction in header!!!

		// initial layout based on FormView.ftl@listview\

		// include hidden inputs for the sortaction

		// start table rendering
		StringBuilder strBuilder = new StringBuilder("<table class=\"listtable\">");
		int rowNo = 1;
		for (HtmlForm form : this.getRows())
		{
			if (rowNo == 1)
			{
				// todo: add actions on the headers for sorting

				// render header, only first row
				strBuilder
						.append("<tr><th><label>&nbsp;</label></th><th><label>&nbsp;</label></th><th><label>&nbsp;</label></th>");
				for (HtmlInput<?> input : form.getInputs())
				{

					if (!input.isHidden())
					{
						// each label has a sort action
						ActionInput sortAction = new ActionInput(
								QueryRule.Operator.SORTASC.equals(getSortOrder()) ? "sortDesc" : "sortAsc");
						sortAction.setLabel(input.getLabel());
						sortAction.setDescription(input.getDescription());

						// TODO: use central icon map so we can skin it
						sortAction
								.setIcon(QueryRule.Operator.SORTASC.equals(getSortOrder()) ? "img/sort_desc.gif"
										: "img/sort_asc.gif");

						// TODO: pass which input to sort on as additional
						// parameter
						// sortInput.setParameter("__sortattribute",input.getName());

						strBuilder.append("<th><label class=\"tableheader\">").append(sortAction.toLinkHtml());
						strBuilder.append(input.getName().equals(this.getSortedBy()) ? sortAction.getIconHtml() : "");
						strBuilder.append("</label></th>");
					}
				}
				strBuilder.append("</tr>");
			}

			// render each row, using different class to allow for alternating
			// colour
			strBuilder.append("<tr class=\"form_listrow").append(rowNo % 2).append("\">");

			// offset
			strBuilder.append("<td>").append(getOffset() + rowNo).append(".</td>");

			// checkbox
			OnoffInput checkbox = new OnoffInput("massUpdate", "TODO", false);
			strBuilder.append("<td>").append(isSelectable() ? checkbox.toHtml() : "").append("</td>");

			// render action buttons per row
			strBuilder.append("<td>");
			for (ActionInput action : form.getActions())
			{
				strBuilder.append(action.toIconHtml());
			}
			strBuilder.append("</td>");

			// render other inputs
			for (HtmlInput<?> input : form.getInputs())
			{
				if (!input.isHidden())
				{
					strBuilder.append("<td title=\"").append(input.getDescription()).append("\">");
					strBuilder.append(isReadonly() ? input.getValue() : input.toHtml()).append("</td>");
				}
			}
			strBuilder.append("</tr>");

			rowNo++;

		}

		// render selectall
		strBuilder
				.append("<tr><td></td><td><input title=\"select all visible\" type=\"checkbox\" name=\"checkall\" id=\"checkall\" onclick=\"Javascript:checkAll('TODO','massUpdate')\"/></td></tr>");
		strBuilder.append("</table>");

		return strBuilder.toString();
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		// TODO
		throw new UnsupportedOperationException();
	}

}
