package org.molgenis.framework.ui.html;

import java.util.List;

import org.molgenis.util.tuple.Tuple;

public class TupleTable extends HtmlWidget
{
	List<Tuple> tuples;

	public TupleTable(String name, List<Tuple> tuples)
	{
		super(name);
		assert (tuples != null);
		this.tuples = tuples;
	}

	@Override
	public String toHtml()
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<div><table id=\"").append(getName()).append("\"><thead><tr>");

		// header
		if (!tuples.isEmpty()) for (String name : tuples.get(0).getColNames())
		{
			strBuilder.append("<th>").append(name).append("</th>");
		}
		strBuilder.append("</tr></thead><tbody>");

		// body
		for (Tuple t : tuples)
		{
			strBuilder.append("<tr>");

			for (String name : t.getColNames())
				strBuilder.append("<td>").append(t.isNull(name) ? "" : t.getString(name)).append("</td>");

			strBuilder.append("</tr>");
		}
		strBuilder.append("</tbody></table><script>$('#");
		strBuilder.append(getName());
		strBuilder
				.append("').dataTable({'bJQueryUI': true,'sPaginationType': 'full_numbers','sScrollX': '100%','bScrollCollapse': true});</script></div>");

		return strBuilder.toString();
	}
}
