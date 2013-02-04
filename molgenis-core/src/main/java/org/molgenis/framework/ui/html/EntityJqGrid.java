package org.molgenis.framework.ui.html;

import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.Entity;

/**
 * jqGrid for listing entity via an AJAX list view, incl search.
 * 
 * To get this to work you also need to install the related molgenisservice. In
 * your properties say:<br/>
 * org.molgenis.framework.server.services.MolgenisJqGridService@/jqgrid
 */
public class EntityJqGrid extends HtmlWidget
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
	public EntityJqGrid(String name, Class<? extends Entity> entityClass, Database db) throws DatabaseException
	{
		super(name);
		entityModel = db.getMetaData().getEntity(entityClass.getSimpleName());
		klazzName = entityClass.getName();
	}

	@Override
	public String toHtml()
	{
		// csv of colNames like 'label1','label2'
		String colNames = "";

		// list of colDefs like \n\t{name:'field1',
		// index:'field1',width:55},\n\t{...}
		String colDefs = "";

		try
		{
			for (Field field : entityModel.getAllFields())
			{
				// in this case use label
				if (field.getType() instanceof XrefField || field.getType() instanceof MrefField)
				{
					for (String label : field.getXrefLabelNames())
					{
						colNames += "'" + field.getName() + "',";
						colDefs += String.format("{name: '%s', index: '%s', width: 55},",
								field.getName() + "_" + label, field.getName());
					}
				}
				else
				{
					colNames += "'" + field.getLabel() + "',";
					colDefs += String.format("{name: '%s', index: '%s', width: 55},", field.getName(), field.getName());
				}
			}
			colDefs = colDefs.substring(0, colDefs.length() - 2);
			colNames = colNames.substring(0, colNames.length() - 2);

			// configure the jqGrid
			String result = "";
			result += "<div id=\"" + getName() + "_pager\"></div>";
			result += "<table id=\"" + getName() + "\"><tr><td/></tr></table>";
			result += "<script>$('#" + getName() + "').jqGrid({";
			result += "\n	url:'jqgrid',";
			result += "\n   datatype: 'json',";
			result += "\n   mtype: 'POST',";
			result += "\n	colNames:[" + colNames + "],";
			result += "\n	colModel :[" + colDefs + "],";
			result += "\n	pager: '#" + getName() + "_pager',";
			result += "\n	rowNum:10,";
			result += "\n	sortname: '" + entityModel.getPrimaryKey().getName() + "',";
			result += "\n	sortorder: 'desc',";
			result += "\n	rowList:[10,20,30,50],";
			result += "\n	viewrecords: true,";
			result += "\n	gridview: true,";
			result += "\n	jsonReader : {root: 'rows',  page: 'page', total: 'total', records: 'records',repeatitems: false, id: '0'},";
			result += "\n	caption: '" + getLabel() + "',";
			// automatically size to parent
			result += "\n	autowidth: true,";
			result += "\n	postData: { entity : '" + klazzName + "'}";
			result += "\n});";

			// create search box. OnChange, update postData with filter and
			// reload
			result += "\n$('#"
					+ getName()
					+ "_pager_left').append('<label>Search: <input type=\"text\" onkeyup=\"$(\\\'#"
					+ getName()
					+ "\\\').setGridParam({postData : { filter : this.value} }).trigger(\\\'reloadGrid\\\');\"></label>');";

			result += "\n</script>";

			return result;

		}
		catch (Exception e)
		{
			return "EntityJqGrid configuration error: " + e.getMessage();
		}
	}
}
