/**
 * File: invengine.screen.FormView <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-05-07; 1.0.0; MA Swertz; Creation.
 * <li>2005-12-02; 1.0.0; RA Scheltema; Moved to the new structure, made the
 * method reset abstract and added documentation.
 * <li>2006-5-14; 1.1.0; MA Swertz; refactored to separate controller and view
 * </ul>
 */

package org.molgenis.framework.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntitiesImporter;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.db.paging.DatabasePager;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.security.SimpleLogin;
import org.molgenis.framework.ui.commands.AddBatchCommand;
import org.molgenis.framework.ui.commands.AddCommand;
import org.molgenis.framework.ui.commands.AddCsvFileCommand;
import org.molgenis.framework.ui.commands.ChangeListLimitCommand;
import org.molgenis.framework.ui.commands.CommandMenu;
import org.molgenis.framework.ui.commands.DownloadAllCommand;
import org.molgenis.framework.ui.commands.DownloadAllXlsCommand;
import org.molgenis.framework.ui.commands.DownloadSelectedCommand;
import org.molgenis.framework.ui.commands.DownloadSelectedXlsCommand;
import org.molgenis.framework.ui.commands.DownloadVisibleCommand;
import org.molgenis.framework.ui.commands.DownloadVisibleXlsCommand;
import org.molgenis.framework.ui.commands.EditSelectedCommand;
import org.molgenis.framework.ui.commands.GalaxyCommand;
import org.molgenis.framework.ui.commands.RemoveSelectedCommand;
import org.molgenis.framework.ui.commands.ScreenCommand;
import org.molgenis.framework.ui.commands.ViewEditViewCommand;
import org.molgenis.framework.ui.commands.ViewListViewCommand;
import org.molgenis.framework.ui.html.FileInput;
import org.molgenis.framework.ui.html.HtmlForm;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

public class FormModel<E extends Entity> extends SimpleScreenModel
{
	/**
	 * Parameters to enable links between parent forms and subforms using
	 * foreign key relationships (aka master-detail views)
	 * 
	 * @author Morris Swertz
	 */
	public static class ParentFilter
	{
		/** for filtering based on parentform */
		private String parentForm;
		private String parentId;
		private List<String> parentLabels;
		private String xrefToParent;

		public ParentFilter(String parentForm, String parentId, List<String> parentLabel, String xrefToParent)
		{
			this.parentForm = parentForm;
			this.parentId = parentId;
			this.parentLabels = parentLabel;
			this.xrefToParent = xrefToParent;
		}

		public String getParentForm()
		{
			return parentForm;
		}

		public String getParentId()
		{
			return parentId;
		}

		public String getXrefToParent()
		{
			return xrefToParent;
		}

		public void setParentForm(String parentForm)
		{
			this.parentForm = parentForm;
		}

		public void setParentId(String parentId)
		{
			this.parentId = parentId;
		}

		public void setXrefToParent(String xrefToParent)
		{
			this.xrefToParent = xrefToParent;
		}

		public List<String> getParentLabels()
		{
			return parentLabels;
		}

		public void setParentLabels(List<String> parentLabel)
		{
			this.parentLabels = parentLabel;
		}
	}

	public List<HtmlInput<?>> getInputs()
	{
		return getController().getInputs(create(), true).getInputs();
	}

	public List<HtmlInput<?>> getInputs(E entity)
	{
		return getController().getInputs(entity, false).getInputs();
	}

	/** Alternative view modes */
	public static enum Mode
	{
		LIST_VIEW("listview"), EDIT_VIEW("editview");
		// RECORD_VIEW("recordview")
		// , EMBEDDED_VIEW("embeddedview"),;

		private Mode(String tag)
		{
			this.tag = tag;
		}

		public final String tag;

		@Override
		public String toString()
		{
			return tag;
		}
	}

	// CONSTANTS
	public static final String INPUT_SHOW = "__show";
	public static final String INPUT_COMMAND = "__command";
	public static final String INPUT_OFFSET = "__offset";
	public static final String INPUT_DOWNLOADFILENAME = "__filename";
	public static final String INPUT_BATCHADD = "__batchadd";
	public static final String INPUT_SELECTED = "massUpdate";
	public static final String ACTION_DOWNLOAD = "download";

	// PROPERTIES (default initialization is done in reset!)
	private static final Logger logger = Logger.getLogger(FormModel.class);

	/** List of actions of this screen */
	private Map<String, ScreenCommand> commands = new LinkedHashMap<String, ScreenCommand>();

	/** Optional custom header for the selected form screen */
	private String header;

	/** Optional description for the selected form screen */
	private String description;

	private EntitiesImporter csvEntityImporter;

	private Class<E> entityClass;

	/** currently known offset */
	private int offset;

	/** how many pages to show */
	private int limit;

	/** number of entities */
	private int count;

	/** column to sort by */
	private String sortby = "";

	/** either ASC or DESC */
	private Operator sortMode;

	/** how the form should be shown */
	Mode viewMode;

	/** remember the current record we are editing */
	// private E current = null;

	/** cache of currently viewable records */
	private List<E> records = new ArrayList<E>();

	/** whether this form can be edited */
	private boolean readonly;

	/** query filters set by user */
	private List<QueryRule> userRules;

	/** query filter set by system (invisible to user) */
	List<QueryRule> systemRules;

	/** Active command (in case of a command with dialog) */
	private ScreenCommand currentCommand = null;

	/** columns that are invisible to the user */
	private List<String> systemHiddenColumns = new Vector<String>();
	protected List<String> userHiddenColumns = new Vector<String>();

	/** Here the currently selected is are stored */
	private List<?> selectedIds;
	/** Filter of parent form filtering */
	// TODO make this infer automatically
	private List<ParentFilter> parentFilters = new ArrayList<ParentFilter>();

	/* which fields are shown in compact view */
	private List<String> compactView = new ArrayList<String>();

	/** */
	private static final long serialVersionUID = 8048540994925740038L;

	// constructors
	/**
	 * TODO: make gateway part of inversion of control structure. TODO: make
	 * parent_rules integral part of the system rules of this view.
	 * 
	 * @param parent
	 * @throws DatabaseException
	 */
	public FormModel(FormController<E> controller)
	{
		super(controller);

		// set defaults

		setUserRules(new ArrayList<QueryRule>());
		setSystemRules(new ArrayList<QueryRule>());
		setLimit(5);
		setOffset(0);
		setCount(0);

		// default sort on id
		setSort(this.getIdField());
		setSortMode(Operator.SORTASC);

		setMode(Mode.LIST_VIEW);
		setMessages(new Vector<ScreenMessage>());
		setReadonly(false);

		// add all actions
		// menu FILE

		// TXT file: Download visible
		super.addCommand(new DownloadVisibleCommand("download_txt_visible", this.getController()));

		// TXT file: Download Selected
		super.addCommand(new DownloadSelectedCommand<E>("download_txt_selected", this.getController()));

		// TXT file: Download all
		super.addCommand(new DownloadAllCommand<E>("download_txt_all", this.getController()));

		// XLS file: Download visible
		super.addCommand(new DownloadVisibleXlsCommand("download_xls_visible", this.getController()));

		// XLS file: Download Selected
		super.addCommand(new DownloadSelectedXlsCommand<E>("download_xls_selected", this.getController()));

		// XLS file: Download all
		super.addCommand(new DownloadAllXlsCommand<E>("download_xls_all", this.getController()));

		// File: Add batch
		super.addCommand(new AddBatchCommand<E>("upload_csv", this.getController()));

		// File: Add batch
		super.addCommand(new AddCsvFileCommand<E>("upload_csvfile", this.getController()));

		// Sending data to a Galaxy server.
		// Note: We do not send the actual data.
		// Instead we send a link that Galaxy can GET or POST to fetch the data.

		// Todo: add option to send only selected records/fields to Galaxy.
		// File: Send Selected to Galaxy
		// super.addCommand(new GalaxyCommand("send_selected_to_galaxy",
		// this.getController()));

		// File: Send All to Galaxy
		super.addCommand(new GalaxyCommand("send_all_to_galaxy", this.getController()));

		// EDIT MENU
		// EDIT: Add new record
		super.addCommand(new AddCommand<E>("edit_new", this.getController()));

		// EDIT: Update selected
		super.addCommand(new EditSelectedCommand("edit_update_selected", this.getController()));

		// EDIT: Remove selected
		super.addCommand(new RemoveSelectedCommand("edit_remove_selected", this.getController()));

		// menu VIEW
		// ScreenCommand v2 = new ViewRecordViewCommand("recordview",
		// this.getController());
		// v2.setToolbar(true);
		// super.addCommand("View", v2);

		// v3.setToolbar(true);
		super.addCommand(new ViewEditViewCommand("editview", this.getController()));
		super.addCommand(new ViewListViewCommand<E>("listview", this.getController()));

		ChangeListLimitCommand<E> view_5 = new ChangeListLimitCommand<E>("view_5show5", this.getController());
		view_5.setLimit(5);
		super.addCommand(view_5);

		ChangeListLimitCommand<E> view_10 = new ChangeListLimitCommand<E>("view_6show10", this.getController());
		view_5.setLimit(10);
		super.addCommand(view_10);

		ChangeListLimitCommand<E> view_20 = new ChangeListLimitCommand<E>("view_7show20", this.getController());
		view_20.setLimit(20);
		super.addCommand(view_20);

		ChangeListLimitCommand<E> view_50 = new ChangeListLimitCommand<E>("view_8show50", this.getController());
		view_50.setLimit(50);
		super.addCommand(view_50);

		ChangeListLimitCommand<E> view_100 = new ChangeListLimitCommand<E>("view_9show100", this.getController());
		view_100.setLimit(100);
		super.addCommand(view_100);

		ChangeListLimitCommand<E> view_500 = new ChangeListLimitCommand<E>("view_10show500", this.getController());
		view_500.setLimit(500);
		super.addCommand(view_500);

		// add the plugged-in actions
		for (ScreenCommand command : this.commands.values())
		{
			super.addCommand(command);
		}
	}

	@Override
	public boolean isVisible()
	{
		Login login = this.getController().getApplicationController().getLogin();
		try
		{
			return (login.canRead(this.getController()));
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Create the toolbar
	 * 
	 * @return vector of commands to be shown on the toolbar
	 */
	public Vector<ScreenCommand> getToolbar()
	{
		Vector<ScreenCommand> toolbar = new Vector<ScreenCommand>();

		for (CommandMenu menu : this.getMenus())
		{
			for (ScreenCommand c : menu.getCommands())
			{
				if (c.isToolbar() && c.isVisible())
				{
					toolbar.add(c);
				}
			}
		}

		return toolbar;
	}

	/**
	 * A convenience method that merges system and user rules.
	 * 
	 * @return an array of all rules
	 */
	public QueryRule[] getRules()
	{
		return this.getRules(true);
	}

	/**
	 * A convenience method that merges system and user rules, excluding limit
	 * and offset (i.e., if you want 'all').
	 * 
	 * @return an array of all rules
	 */
	public QueryRule[] getRulesExclLimitOffset()
	{
		return this.getRules(false);
	}

	/**
	 * A convenience method that merges system and user rules.
	 * 
	 * @return an array of all rules
	 */
	public QueryRule[] getRules(boolean includingLimitOffset)
	{
		List<QueryRule> rules = new ArrayList<QueryRule>();
		rules.addAll(this.getSystemRules());
		rules.addAll(this.getUserRules());
		if (includingLimitOffset)
		{
			if (this.limit > 0) rules.add(new QueryRule(QueryRule.Operator.LIMIT, this.limit));
			if (this.offset > 0) rules.add(new QueryRule(QueryRule.Operator.OFFSET, this.offset));
		}
		if (!StringUtils.equals(this.sortby, "")) rules.add(new QueryRule(this.sortMode, this.sortby));
		logger.debug("rules.size: " + rules.size() + "=" + this.getSystemRules().size() + "+"
				+ this.getUserRules().size());
		return rules.toArray(new QueryRule[rules.size()]);
	}

	/**
	 * Create a new instance of the entity
	 */
	public E create()
	{
		try
		{
			E entity = getController().getEntityClass().newInstance();

			// set defaults for xrefs
			for (ParentFilter pf : this.getParentFilters())
			{
				FormModel<?> parent = (FormModel<?>) this.getController().get(pf.getParentForm()).getModel();
				List<?> records = parent.getRecords();
				if (records.size() > 0)
				{
					// xref only
					Object value = ((Entity) records.get(0)).get(pf.getParentId());
					if (!(value instanceof List<?>) && value != null)
					{
						entity.set(pf.getXrefToParent(), value);
					}

					for (String labelName : pf.getParentLabels())
					{
						Object label = ((Entity) records.get(0)).get(labelName);
						entity.set(pf.getXrefToParent() + "_" + labelName, label);
					}
				}
			}

			return entity;
		}
		catch (Exception e)
		{
			// should never happen
			logger.error("failed to create class " + getController().getEntityClass() + ": " + e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Column headers of the form
	 * 
	 * @return vector of column label strings.
	 */
	public Vector<String> getHeaders()
	{
		Vector<String> headers = new Vector<String>();
		for (HtmlInput<?> input : getController().getInputs(this.create(), true).getInputs())
		{
			headers.add(input.getLabel());
		}
		return headers;
	}

	/**
	 * Create the inputs for a new record.
	 * 
	 * @throws DatabaseException
	 */
	public HtmlForm getNewRecordForm() throws DatabaseException
	{
		E entity = this.create();
		// if (current != null) entity = current;
		return getController().getInputs(entity, true);
	}

	/**
	 * Create for each record a form, with each record modeled as a list of
	 * HtmlInput elements.
	 * 
	 * @return a vector of vectors, each vector containing the inputs for one
	 *         row on screen.
	 */
	public Vector<HtmlForm> getRecordInputs()
	{
		Vector<HtmlForm> records = new Vector<HtmlForm>();

		try
		{
			for (E entity : getRecords())
			{
				HtmlForm record = getController().getInputs(entity, false);
				record.setReadonly(!getLogin().canWrite(entity));
				records.add(record);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return records;
	}

	/**
	 * This function is used by the user interface template to show rules on the
	 * screen.
	 * 
	 * @return a list of query rules that can be managed by the user.
	 * @throws DatabaseException
	 */
	public Vector<String> getFilters() throws DatabaseException
	{
		if (this.getUserRules() == null || this.getUserRules().isEmpty()) return new Vector<String>();

		Vector<String> filters = new Vector<String>();
		for (QueryRule rule : this.getUserRules())
		{
			String label = "";
			if (rule.getField() != null)
			{
				label = getField(rule.getField());
			}
			else
			{
				// assume that this rule has nested rule
				QueryRule[] nestedRules = rule.getNestedRules();
				if (nestedRules != null && nestedRules.length > 0)
				{
					label = "Any field";
					rule = nestedRules[0];
				}
			}
			filters.add(label + " " + rule.getOperator().toString() + " " + rule.getValue());
		}
		return filters;
	}

	// SIMPLE GETTERS AND SETTERS BELOW
	/**
	 * Limit the number of retrieved records. 0 means all records.
	 * 
	 * @param limit
	 */
	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	/**
	 * @return Limit the number of retrieved records. 0 means all records.
	 */
	public int getLimit()
	{
		if (this.viewMode.equals(Mode.EDIT_VIEW)) return 1;
		return limit;
	}

	/**
	 * Total number of records available to this form (given filter rules).
	 * 
	 * @param count
	 */
	public void setCount(int count)
	{
		this.count = count;
	}

	/**
	 * @return Total number of records available to this form (given filter
	 *         rules).
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * @return The column name this form is sorted by.
	 */
	public String getSort()
	{
		return sortby;
	}

	/**
	 * @param sort
	 *            the column name this form should be sorted by.
	 */
	public void setSort(String sort)
	{
		this.sortby = sort;
	}

	/**
	 * @return Operator indicating whether this form should be sorted ASC or
	 *         DESC
	 */
	public Operator getSortMode()
	{
		return sortMode;
	}

	/**
	 * 
	 * @param sortMode
	 */
	public void setSortMode(Operator sortMode)
	{
		this.sortMode = sortMode;
	}

	/**
	 * Number of records that should be skipped before retrieving records. 0
	 * means first record.
	 * 
	 * @param offset
	 */
	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	/**
	 * @return Number of records that should be skipped before retrieving
	 *         records. 0 means first record.
	 */
	public int getOffset()
	{
		return offset;
	}

	/**
	 * Switch between alternative view modes.
	 * 
	 * @param viewmode
	 */
	public void setMode(Mode viewmode)
	{
		this.viewMode = viewmode;
	}

	/**
	 * @return currently selected view mode.
	 */
	public Mode getMode()
	{
		return viewMode;
	}

	/**
	 * Cache of the current set of records as viewed by this FormScreen.
	 * 
	 * @param recordlist
	 */
	public void setRecords(List<E> recordlist)
	{
		this.records = recordlist;
	}

	/**
	 * Current list of records.
	 */
	public List<E> getRecords()
	{
		return records;
	}

	/** Getter/setter for optional custom header for the selected form screen */
	public String getHeader()
	{
		return this.header;
	}

	public void setHeader(String header)
	{
		this.header = header;
	}

	/** Getter/setter for optional description for the selected form screen */
	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * User defined filtering rules. For example, filtering by a field such as
	 * date.
	 * 
	 * @see QueryRule
	 * @param rules
	 */
	public void setUserRules(List<QueryRule> rules)
	{
		this.userRules = rules;
	}

	/**
	 * @return User defined filtering rules
	 */
	public List<QueryRule> getUserRules()
	{
		return userRules;
	}

	/**
	 * System defined filtering rules. For example, filtering by a "parent"
	 * field of a related record.
	 */
	public List<QueryRule> getSystemRules()
	{
		List<QueryRule> rules = new ArrayList<QueryRule>();

		// parent filters
		if (this.getParentFilters() != null && this.getParentFilters().size() > 0)
		{
			List<QueryRule> xref_filters = new ArrayList<QueryRule>();

			for (ParentFilter pf : this.getParentFilters())
			{
				FormModel<?> parent = (FormModel<?>) this.getController().get(pf.getParentForm()).getModel();
				List<?> records = parent.getRecords();

				// add filters for xref or mref relationships (if any)
				// if multiple xrefs apply then the filters are union
				// (so xref1 OR xref2 OR etc)
				if (records.size() > 0)
				{

					Object parentValue = ((Entity) records.get(0)).get(pf.getParentId());
					// mref?
					if (parentValue instanceof List<?>)
					{
						List<?> values = (List<?>) parentValue;
						if (values.size() > 0)
						{
							for (int i = 0; i < values.size(); i++)
							{
								QueryRule rule = new QueryRule(pf.getXrefToParent(), QueryRule.Operator.EQUALS,
										values.get(i));
								if (xref_filters.size() > 0) xref_filters.add(new QueryRule(Operator.OR));
								xref_filters.add(rule);
							}
						}
						// if no filters then prohibit to get ALL data by adding
						// impossible condition
						else
						{
							QueryRule rule = new QueryRule(pf.getXrefToParent(), QueryRule.Operator.EQUALS,
									Integer.MIN_VALUE);
							if (xref_filters.size() > 0) xref_filters.add(new QueryRule(Operator.OR));
							xref_filters.add(rule);

						}
					}
					// xref
					else if (parentValue != null)
					{
						QueryRule rule = new QueryRule(pf.getXrefToParent(), QueryRule.Operator.EQUALS, parentValue);
						if (xref_filters.size() > 0) xref_filters.add(new QueryRule(Operator.OR));
						xref_filters.add(rule);

					}
					// else
					// {
					// QueryRule rule = new QueryRule(pf.getXrefToParent(),
					// QueryRule.Operator.EQUALS,
					// Integer.MIN_VALUE);
					// if (xref_filters.size() > 0) xref_filters.add(new
					// QueryRule(Operator.OR));
					// xref_filters.add(rule);
					//
					// }
				}
			}
			// put it all in one clause, e.g. (xref1 = 0 OR xref1 = 1 OR xref2=3
			// ....)
			if (rules.size() > 0) rules.add(new QueryRule(Operator.OR));
			rules.add(new QueryRule(xref_filters));

		}

		return rules;

	}

	/**
	 * @param systemRules
	 *            System defined filtering rules.
	 */
	public void setSystemRules(List<QueryRule> systemRules)
	{
		this.systemRules = systemRules;
	}

	/**
	 * Determine whether the form can handle data manipulation events.
	 * 
	 * @param readonly
	 */
	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	/**
	 * @return whether the form handles data manipulation events.
	 */
	public boolean isReadonly()
	{
		// If no "real" auth is used, return value from xml-ui
		if (this.getLogin() instanceof SimpleLogin) return this.readonly;

		// Otherwise dynamically return whether form is read-only
		try
		{
			return !this.getLogin().canWrite(this.create().getClass());
		}
		catch (DatabaseException e)
		{
			logger.warn(e);
		}
		return true;
	}

	@Deprecated
	public File getDownloadFile(Database db, Tuple requestTuple)
	{
		String file = null;
		try
		{
			file = db.getFilesource() + requestTuple.getString(FileInput.INPUT_CURRENT_DOWNLOAD);
			logger.error("file to download: " + file);
		}
		catch (Exception e)
		{
			logger.warn(e);
			return null;
		}
		return new File(file);
	}

	public void resetUserHiddenColumns()
	{
		this.userHiddenColumns = new Vector<String>();
		this.userHiddenColumns.addAll(this.getSystemHiddenColumns());
	}

	public List<String> getSystemHiddenColumns()
	{
		return systemHiddenColumns;
	}

	public List<String> getUserHiddenColumns()
	{
		return userHiddenColumns;
	}

	public void setUserHiddenColumns(List<String> userHiddenColumns)
	{
		this.userHiddenColumns = userHiddenColumns;
	}

	public Login getLogin()
	{
		return getController().getApplicationController().getLogin();
	}

	public EntitiesImporter getCsvEntityImporter()
	{
		return csvEntityImporter;
	}

	public void setCsvEntityImporter(EntitiesImporter csvReader)
	{
		this.csvEntityImporter = csvReader;
	}

	public Class<E> getEntityClass()
	{
		return entityClass;
	}

	public void setEntityClass(Class<E> entityClass)
	{
		this.entityClass = entityClass;
	}

	public String getIdField()
	{
		E object = this.create();
		return object.getClass().getSimpleName() + "_" + object.getIdField();
	}

	public List<ScreenCommand> getCommands()
	{
		return new ArrayList<ScreenCommand>(commands.values());
	}

	public ScreenCommand getCurrentCommand()
	{
		return currentCommand;
	}

	public void setCurrentCommand(ScreenCommand currentCommand)
	{
		this.currentCommand = currentCommand;
	}

	public DatabasePager<E> getPager()
	{
		return getController().getPager();
	}

	public List<?> getSelectedIds()
	{
		return selectedIds;
	}

	public void setSelectedIds(List<?> selected)
	{
		this.selectedIds = selected;
	}

	public void setParentFilters(List<ParentFilter> parentFilters)
	{
		this.parentFilters = parentFilters;
	}

	public List<ParentFilter> getParentFilters()
	{
		return parentFilters;
	}

	public E getCurrent()
	{
		if (this.getRecords() != null && this.getRecords().size() > 0) return this.getRecords().get(0);
		return null;
	}

	@Override
	public FormController<E> getController()
	{
		return (FormController<E>) super.getController();
	}

	public void setCompactView(List<String> compactView)
	{
		this.compactView = compactView;
	}

	public List<String> getCompactView()
	{
		return compactView;
	}

	public void setSystemHiddenColumns(Vector<String> systemHiddenColumns)
	{
		this.systemHiddenColumns = systemHiddenColumns;
	}

	/**
	 * Helper function that translates xref field name into its label (for
	 * showing that in the UI).
	 */
	public String getSearchField(String fieldName)
	{
		return this.getController().getSearchField(fieldName);
	}

	public String getField(String searchFieldName)
	{
		return this.getController().getField(searchFieldName);
	}
}
