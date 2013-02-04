package org.molgenis.framework.ui.html;

/**
 * An extension of Table that renders as a jQuery DataTable instead of a plain
 * HTML table.
 * 
 * @author erikroos
 * 
 */
public class JQueryDataTable extends Table
{

	private boolean bSort = false;
	private boolean bFilter = false;
	private boolean bPaginate = false;

	public JQueryDataTable(String name)
	{
		this(name, null);
	}

	public JQueryDataTable(String name, String label)
	{
		super(name, label);
		this.setLabel(label);
		super.setDefaultCellStyle("");
		super.setHeaderCellStyle("");
	}

	@Override
	public String toHtml()
	{
		StringBuilder strBuilder = new StringBuilder(super.toHtml());
		strBuilder.append("<script>$('#").append(getId()).append("')").append(".css('min-height','10px')")
				.append(".dataTable({");
		strBuilder.append("\n\"bLengthChange\": true,");
		strBuilder.append("\n\"bInfo\": false,").append("\n\"bAutoWidth\": false,").append("\n\"bJQueryUI\": true,");

		if (bSort)
		{
			strBuilder.append("\n\"bSort\": true,");
		}
		else
		{
			strBuilder.append("\n\"bSort\": false,");
		}
		if (bFilter)
		{
			strBuilder.append("\n\"bFilter\": true,");
		}
		else
		{
			strBuilder.append("\n\"bFilter\": false,");
		}
		if (bPaginate)
		{
			strBuilder.append("\n\"bPaginate\": true,");
		}
		else
		{
			strBuilder.append("\n\"bPaginate\": false,");
		}

		strBuilder.append("\n\"aoColumns\": [");
		// Prevent fancy auto-detected sorting types by hard-setting to 'string'
		// for every column
		for (int i = 0; i < super.cols.size() + 1; i++)
		{
			strBuilder.append("\n{ \"sType\": \"string\" },");
		}
		strBuilder.setLength(strBuilder.length() - 1); // chop off last ,
		strBuilder.append("\n]");
		strBuilder.append("\n})</script>");
		return strBuilder.toString();
	}

	public boolean isbSort()
	{
		return bSort;
	}

	public void setbSort(boolean bSort)
	{
		this.bSort = bSort;
	}

	public boolean isbFilter()
	{
		return bFilter;
	}

	public void setbFilter(boolean bFilter)
	{
		this.bFilter = bFilter;
	}

	public boolean isbPaginate()
	{
		return bPaginate;
	}

	public void setbPaginate(boolean bPaginate)
	{
		this.bPaginate = bPaginate;
	}

}
