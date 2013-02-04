package org.molgenis.omx.view;

import org.molgenis.framework.ui.html.HtmlWidget;

public class DataSetDownloader extends HtmlWidget
{
	public DataSetDownloader()
	{
		super(DataSetDownloader.class.getSimpleName());
	}

	@Override
	public String toHtml()
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<div class=\"row-fluid grid\" style=\"text-align:center;\">");
		strBuilder.append("<button class=\"btn download-btn\">Download</button>");
		strBuilder.append("<script type=\"text/javascript\">");
		strBuilder.append("$('.download-btn').click(function(){");
		strBuilder.append("$('input[name=__action]').val('download_xls');");
		strBuilder.append("$('DataSetViewerPlugin').submit();");
		strBuilder.append("});");
		strBuilder.append("</script>");
		strBuilder.append("</div>");
		return strBuilder.toString();
	}
}
