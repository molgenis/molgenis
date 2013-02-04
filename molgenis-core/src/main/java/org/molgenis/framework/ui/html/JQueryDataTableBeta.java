package org.molgenis.framework.ui.html;

/**
 * Apparently DataTable doesn't support rowspanning and colspanning. So we
 * should look out to another method. Probably best to css ourselves
 */
public class JQueryDataTableBeta extends TableBeta
{
	public JQueryDataTableBeta(String name)
	{
		super(name);
	}

	@Override
	public String toHtml()
	{
		String result = super.toHtml();
		result += "<script>$('#" + getId() + "')" + ".css('min-height','100px')" + ".dataTable({"
				+ "\n\"bPaginate\": false," + "\n\"bLengthChange\": true," + "\n\"bFilter\": false,"
				+ "\n\"bSort\": false," + "\n\"bInfo\": false," + "\n\"bJQueryUI\": true})" + "</script>";
		return result;
	}

}
