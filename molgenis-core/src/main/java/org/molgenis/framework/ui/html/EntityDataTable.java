package org.molgenis.framework.ui.html;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.Entity;

/**
 * DataTable for listing entity via an AJAX list view, incl search.
 * 
 * To get this to work you also need to install the related molgenisservice. In
 * your properties say:<br/>
 * org.molgenis.framework.server.services.MolgenisDataTableService@/datatable
 */
public class EntityDataTable extends HtmlWidget
{
	String klazzName;
	org.molgenis.model.elements.Entity entityModel;

	/**
	 * Constructor
	 * 
	 * @param unique
	 *            name
	 * @param class of the entity to be shown
	 * @throws DatabaseException
	 */
	public EntityDataTable(String name, Class<? extends Entity> entityClass, Database db) throws DatabaseException
	{
		super(name);
		entityModel = db.getMetaData().getEntity(entityClass.getSimpleName());
		klazzName = entityClass.getName();
	}

	@Override
	public String toHtml()
	{
		List<String> labels = new ArrayList<String>();

		try
		{
			for (Field field : entityModel.getAllFields())
			{
				// in this case use label
				if (field.getType() instanceof XrefField || field.getType() instanceof MrefField)
				{
					for (String label : field.getXrefLabelNames())
					{
						labels.add(field.getName() + "_" + label);
					}
				}
				else
				{
					labels.add(field.getName());
				}
			}
		}
		catch (Exception e)
		{

		}

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<table id=\"").append(getName()).append("\"><thead>");
		for (String label : labels)
			strBuilder.append("<td>").append(label).append("</td>");
		strBuilder.append("</thead></table>");
		strBuilder.append("\n<script>");
		strBuilder.append("var oTable = $('#").append(getName()).append("').dataTable({");
		strBuilder.append("\n	'bJQueryUI' : true,");
		strBuilder.append("\n	'bServerSide' : true,");
		strBuilder.append("\n	'sAjaxSource' : 'datatable',");
		strBuilder.append("\n	'bPagination' : true,");
		strBuilder.append("\n	'sScrollX': '100%',");
		strBuilder.append("\n	'bScrollCollapse': true,");
		strBuilder.append("\n	'bProcessing' : true,");
		strBuilder.append("\n	'aoColumns' : [");
		for (String label : labels)
			strBuilder.append("{ 'mDataProp' : '").append(label).append("'}").append(',');
		if (!labels.isEmpty()) strBuilder.deleteCharAt(strBuilder.length() - 1);
		strBuilder.append("	],");
		strBuilder.append("	'fnServerParams' : function(aoData) {");
		strBuilder.append("\n	aoData.push({");
		strBuilder.append("\n		'name' : 'entity',");
		strBuilder.append("\n		'value' : '").append(klazzName).append('\'');
		strBuilder.append("\n	});");
		strBuilder.append("\n}");
		strBuilder.append("});");
		strBuilder.append("\nnew FixedColumns(oTable, {'iLeftWidth' : 200});");
		strBuilder.append("</script>");
		return strBuilder.toString();
	}
}
