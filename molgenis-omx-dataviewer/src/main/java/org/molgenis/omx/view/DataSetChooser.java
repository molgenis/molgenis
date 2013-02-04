package org.molgenis.omx.view;

import java.util.List;

import org.molgenis.framework.ui.html.HtmlWidget;
import org.molgenis.omx.observ.DataSet;

public class DataSetChooser extends HtmlWidget
{
	private List<DataSet> dataSets;
	private Integer selectedDataSetId;

	public DataSetChooser(List<DataSet> dataSets, Integer selectedDataSetId)
	{
		super(DataSetChooser.class.getSimpleName(), null);
		this.dataSets = dataSets;
		this.selectedDataSetId = selectedDataSetId;
	}

	public Integer getSelectedDataSetId()
	{
		return selectedDataSetId;
	}

	public void setSelectedDataSetId(Integer selectedDataSetId)
	{
		this.selectedDataSetId = selectedDataSetId;
	}

	public List<DataSet> getDataSets()
	{
		return dataSets;
	}

	public void setDataSets(List<DataSet> dataSets)
	{
		this.dataSets = dataSets;
	}

	@Override
	public String toHtml()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='row-fluid grid'>");
		sb.append("<div class='span2'><label>Choose a dataset:</label></div>");
		sb.append("<div class='btn-group' data-toggle='buttons-radio'>");
		for (DataSet ds : dataSets)
		{
			if ((selectedDataSetId != null) && ds.getId().equals(selectedDataSetId))
			{
				sb.append("<button class='btn active' name='dataSetId' value='");
			}
			else
			{
				sb.append("<button class='btn' name='dataSetId' value='");
			}

			sb.append(ds.getId()).append("'>");
			sb.append(ds.getName());// TODO html encode
			sb.append("</button>");
		}
		sb.append("</div>");
		sb.append("</div>");

		sb.append("<script type='text/javascript'>");

		sb.append("$('button[name=dataSetId]').click(function(){");
		sb.append("$('input[name=__action]').val('selectDataSet');");
		sb.append("$('DataSetViewerPlugin').submit();");
		sb.append("});");
		sb.append("</script>");
		sb.append("</div>");

		return sb.toString();
	}
}
