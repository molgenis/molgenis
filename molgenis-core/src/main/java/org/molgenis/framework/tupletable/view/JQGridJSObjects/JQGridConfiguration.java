package org.molgenis.framework.tupletable.view.JQGridJSObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.EditableTupleTable;
import org.molgenis.framework.tupletable.FilterableTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.model.elements.Field;

@SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
{ "URF_UNREAD_FIELD", "SS_SHOULD_BE_STATIC" }, justification = "Gson reads private fields")
public class JQGridConfiguration
{
	private String id;

	/** ajax url */
	private final String url;

	private final String editurl;

	/** formatting of the ajax service data */
	private final String datatype = "json";

	/** id of pager diff (== toolbar at bottom) */
	private final String pager;

	/** definitions of the columns */
	private List<JQGridColModel> colModel = new ArrayList<JQGridColModel>();

	/** current limit = number of rows to show */
	private int rowNum = 20;

	/** choices of alternative rowNum values */
	private Integer[] rowList = new Integer[]
	{ 20, 30, 40 };

	/** indicates whether we want to show total records from query in page bar */
	private boolean viewrecords = true;

	/** the caption of this table */
	private String caption = "jqGrid View";

	private boolean autowidth = true;

	/** whether this grid is sortable */
	private String sortname = "";

	/** default sorting order */
	private String sortorder = "desc";

	/** default height */
	private String height = "466px";

	/** the total column count **/
	private int totalColumnCount;

	/** the current column offset **/
	private int colOffset;

	/** the current coliumn limit, so the nr of visible columns **/
	private int colLimit;

	/** list of hidden columns **/
	private List<JQGridColModel> hiddenColumns = new ArrayList<JQGridColModel>();

	/** Wether the first column is 'fixed', must always be visible **/
	private boolean firstColumnFixed;

	/** virtual scrolling */
	// private int scroll = 1;

	/** preload filter settings */
	// note: this is a string value
	// private String postData =
	// "{filters : '{\"groupOp\":\"AND\",\"rules\":[{\"field\":\"Country.Code\",\"op\":\"eq\",\"data\":\"AGO\"}]}'}";
	private JQGridPostData postData = new JQGridPostData();

	/** ???? */
	private HashMap<String, Object> jsonReader = new HashMap<String, Object>();

	// private String postData = "viewType : JQ_GRID";

	private JQGridSettings settings = new JQGridSettings();

	private JQGridSearchOptions searchOptions = new JQGridSearchOptions();

	@SuppressWarnings("unchecked")
	private Object[] toolbar = Arrays.asList(true, "top").toArray();

	public JQGridConfiguration(Database db, String id, String idField, String url, String caption, TupleTable tupleTable)
			throws TableException
	{
		if (tupleTable instanceof DatabaseTupleTable)
		{
			((DatabaseTupleTable) tupleTable).setDb(db);
		}

		this.id = id;
		this.pager = "#" + id + "_pager";
		this.url = url;
		this.editurl = url;
		this.caption = caption;
		this.totalColumnCount = tupleTable.getColCount();
		this.colOffset = tupleTable.getColOffset();
		this.colLimit = tupleTable.getColLimit();
		this.firstColumnFixed = tupleTable.isFirstColumnFixed();

		// "{repeatitems: false, id: \"Code\"}"
		jsonReader.put("repeatitems", false);
		jsonReader.put("id", idField);

		if (tupleTable instanceof FilterableTupleTable)
		{
			// sortable = true;
			settings.setSearch(true);
		}

		if (tupleTable instanceof EditableTupleTable)
		{
			settings.setAdd(true);
			settings.setEdit(true);
			settings.setDel(true);
		}

		// set col names
		for (final Field f : tupleTable.getColumns())
		{
			colModel.add(new JQGridColModel(f));
		}

		// Set hidden columns, for in dropdown
		for (Field f : tupleTable.getHiddenColumns())
		{
			hiddenColumns.add(new JQGridColModel(f));
		}
	}

	public JQGridConfiguration(String id, String url, String caption)
	{
		this.id = id;
		this.pager = "#" + id + "Pager";
		this.url = url;
		this.editurl = url;
		this.caption = caption;
	}

	public JQGridSearchOptions getSearchOptions()
	{
		return searchOptions;
	}

	public void setSearchOptions(JQGridSearchOptions searchOptions)
	{
		this.searchOptions = searchOptions;
	}
}
