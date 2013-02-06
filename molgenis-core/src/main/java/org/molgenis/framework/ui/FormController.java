/**
 * File: invengine.screen.Controller <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-05-07; 1.0.0; MA Swertz; Creation.
 * <li>2005-12-02; 1.0.0; RA Scheltema; Moved to the new structure, made the
 * method reset abstract and added documentation.
 * <li>2007-05-15; 1.1.0; MA Swertz; refactored to use pluggable pager.
 * </ul>
 */

package org.molgenis.framework.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.db.paging.DatabasePager;
import org.molgenis.framework.db.paging.LimitOffsetPager;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormModel.Mode;
import org.molgenis.framework.ui.ScreenModel.Show;
import org.molgenis.framework.ui.commands.ScreenCommand;
import org.molgenis.framework.ui.commands.SimpleCommand;
import org.molgenis.framework.ui.html.HtmlForm;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/**
 * @param <E>
 */
public abstract class FormController<E extends Entity> extends SimpleScreenController<FormModel<E>>
{
	private static final Logger logger = Logger.getLogger(FormController.class);

	private static final long serialVersionUID = 7813540700458832850L;

	private static final int MAX_FILTERS = 100;
	private static final String FILTER_ATTRIBUTE_ALL = "all";

	/** Helper object that takes care of database paging */
	protected DatabasePager<E> pager;

	/**
	 * @param model
	 * @throws DatabaseException
	 */
	public FormController(String name, ScreenController<?> parent)
	{
		super(name, null, parent);
		this.setModel(new FormModel<E>(this));

		FormModel<E> model = getModel();
		resetSystemHiddenColumns();
		model.resetUserHiddenColumns();

		// FIXME: this assumes first column is sortable...
		try
		{
			this.pager = new LimitOffsetPager<E>(getEntityClass(), model.create().getFields().firstElement());

			// copy default sort from view
			pager.setOrderByField(model.getSort());
			pager.setOrderByOperator(model.getSortMode());
			pager.setLimit(model.getLimit());

		}
		catch (DatabaseException e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public Show handleRequest(Database db, MolgenisRequest request, OutputStream out)
	{
		logger.debug("handleRequest(" + request + ")");

		// clear the old messages
		FormModel<E> model = getModel();
		model.setMessages(new Vector<ScreenMessage>()); // clear messsages

		try
		{
			String action = request.getString(FormModel.INPUT_ACTION);
			if (request.getString(FormModel.INPUT_COMMAND) != null)
			{
				action = request.getString(FormModel.INPUT_COMMAND);
			}

			// get the selected ids into the screen list (if any)
			model.setSelectedIds(request.getList(FormModel.INPUT_SELECTED));

			// if none selected, make empty list
			if (model.getSelectedIds() == null) model.setSelectedIds(new ArrayList<Object>());

			// get the current command if any
			ScreenCommand command = model.getCommand(action);

			if (action == null || action.isEmpty())
			{
				logger.debug("action or command does not exist");
				return Show.SHOW_MAIN;
			}
			// delegate to a command
			else if (command != null && command instanceof SimpleCommand)
			{
				logger.debug("delegating to PluginCommand");
				model.setCurrentCommand(command);
				return command.handleRequest(db, request, out);
			}
			else if (action.equals("filter_add"))
			{
				this.addFilters(pager, db, request);
			}
			else if (action.equals("filter_remove"))
			{
				// remove filter
				int index = request.getInt("filter_id");
				model.getUserRules().remove(index);

				// update pager
				List<QueryRule> rules = new ArrayList<QueryRule>();
				rules.addAll(model.getUserRules());
				rules.addAll(model.getSystemRules());
				pager.resetFilters(rules);
			}
			else if (action.equals("filter_set"))
			{
				// remove all existing filters and than add this as a new one.
				model.setUserRules(new ArrayList<QueryRule>());
				this.addFilters(pager, db, request);
			}
			else if (action.equals("update"))
			{
				this.doUpdate(db, request);
			}
			else if (action.equals("remove"))
			{
				this.doRemove(db, request);
			}
			else if (action.equals("add"))
			{
				this.doAdd(db, request);
			}
			else if (action.equals("prev"))
			{
				pager.prev(db);
			}
			else if (action.equals("next"))
			{
				pager.next(db);
			}
			else if (action.equals("first"))
			{
				pager.first(db);
			}
			else if (action.equals("last"))
			{
				pager.last(db);
			}
			else if (action.equals("sort"))
			{
				String attribute = getSearchField(request.getString("__sortattribute"));

				if (pager.getOrderByField().equals(attribute))
				{
					if (pager.getOrderByOperator().equals(Operator.SORTASC))
					{
						pager.setOrderByOperator(Operator.SORTDESC);
					}
					else
					{
						pager.setOrderByOperator(Operator.SORTASC);
					}
				}
				else
				{
					pager.setOrderByField(attribute);
					pager.setOrderByOperator(Operator.SORTASC);
				}
			}
			else if (action.equals("xref_select")) // this is used to link from
			// one
			// form to another based on an xref
			{
				this.doXrefselect(request);
			}
			else if (action.equals("hideColumn"))
			{
				List<String> UserHiddencols = model.getUserHiddenColumns();
				String attribute = request.getString("attribute");

				if (!UserHiddencols.contains(attribute)) UserHiddencols.add(attribute);
				model.setUserHiddenColumns(UserHiddencols);

			}
			else if (action.equals("showColumn"))
			{
				List<String> UserHiddencols = model.getUserHiddenColumns();
				String attribute = request.getString("attribute");

				if (UserHiddencols.contains(attribute)) UserHiddencols.remove(attribute);
				model.setUserHiddenColumns(UserHiddencols);
			}
			else
			{
				logger.debug("action '" + action + "' unknown");
			}

			logger.debug("handleRequest finished.");
		}
		catch (DatabaseException e)
		{
			logger.warn(e);
		}
		catch (MolgenisModelException e)
		{
			logger.warn(e);
		}
		catch (ParseException e)
		{
			logger.warn(e);
		}
		catch (IOException e)
		{
			logger.warn(e);
		}
		catch (Exception e)
		{
			logger.warn(e);
		}
		return Show.SHOW_MAIN;
	}

	private Show addFilters(DatabasePager<E> pager, Database db, MolgenisRequest request) throws DatabaseException,
			MolgenisModelException
	{
		List<QueryRule> userRules = new ArrayList<QueryRule>();
		for (int i = 0; i < MAX_FILTERS; ++i)
		{
			// suffix: __filter_value, __filter_value1, __filter_value2 etc.
			String suffix = i > 0 ? i + "" : "";

			String filterAttr = request.getString("__filter_attribute" + suffix);
			if (filterAttr == null) break;

			String operatorStr = request.getString("__filter_operator" + suffix);
			Operator operator = QueryRule.Operator.valueOf(operatorStr);
			String filterValue = request.getString("__filter_value" + suffix);

			QueryRule filterRule;
			if (filterAttr.equals(FILTER_ATTRIBUTE_ALL))
			{
				filterRule = createFilterRule(db, operator, filterValue);
			}
			else
			{
				String fieldName = toFieldName(filterAttr);
				filterRule = createFilterRule(db, fieldName, operator, filterValue);
			}
			userRules.add(filterRule);
		}

		FormModel<E> model = getModel();
		model.getUserRules().addAll(userRules);

		// reset pager
		List<QueryRule> rules = new ArrayList<QueryRule>();
		rules.addAll(model.getUserRules());
		rules.addAll(model.getSystemRules());
		pager.resetFilters(rules);
		pager.first(db);

		return Show.SHOW_MAIN;
	}

	QueryRule createFilterRule(Database db, String fieldName, Operator operator, String value)
			throws DatabaseException, MolgenisModelException
	{
		if (StringUtils.isEmpty(fieldName) || operator == null || StringUtils.isEmpty(value)) return null;

		QueryRule queryRule = null;
		for (Field field : getAllFields(db))
		{
			if (field.getName().equals(fieldName))
			{
				fieldName = getSearchField(fieldName);
				queryRule = new QueryRule(fieldName, operator, value);
				break;
			}
		}
		return queryRule;
	}

	QueryRule createFilterRule(Database db, Operator operator, String value) throws DatabaseException,
			MolgenisModelException
	{
		if (operator == null || StringUtils.isEmpty(value)) return null;

		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		for (Field field : getAllFields(db))
		{
			String fieldName = getSearchField(field.getName());
			if (!queryRules.isEmpty()) queryRules.add(new QueryRule(Operator.OR));
			queryRules.add(new QueryRule(fieldName, operator, value));
		}
		return !queryRules.isEmpty() ? new QueryRule(queryRules) : null;
	}

	/**
	 * returns all non-system, non-hidden fields for this entity
	 * 
	 * @param db
	 * @return
	 * @throws DatabaseException
	 * @throws MolgenisModelException
	 */
	private Vector<Field> getAllFields(Database db) throws DatabaseException, MolgenisModelException
	{
		String simpleName = this.getEntityClass().getSimpleName();
		org.molgenis.model.elements.Entity entity = db.getMetaData().getEntity(simpleName);
		Vector<Field> allFields = entity.getAllFields();
		for (Iterator<Field> it = entity.getAllFields().iterator(); it.hasNext();)
		{
			Field field = it.next();
			if (field.isSystem() || field.isHidden()) it.remove();
		}
		return allFields;
	}

	private String toFieldName(String entityUnderscoreFieldName)
	{
		String simpleName = this.getEntityClass().getSimpleName();
		return entityUnderscoreFieldName.substring(simpleName.length() + 1);
	}

	@Override
	public void reload(Database db)
	{
		logger.info("reloading...");
		FormModel<E> model = getModel();

		try
		{
			pager.setDirty(true);

			// reset pager
			List<QueryRule> rules = new ArrayList<QueryRule>();
			rules.addAll(model.getUserRules());
			rules.addAll(model.getSystemRules());
			pager.resetFilters(rules);

			// check view and set limit accordingly
			if (model.getMode().equals(Mode.EDIT_VIEW)) pager.setLimit(1);
			else pager.setLimit(model.getLimit());

			// refresh pager and options
			if (model.isReadonly()) pager.refresh(db);

			// update view

			// set readonly records
			// view.setRecords( pager.getPage() );
			model.setRecords(this.getData(db));
			model.setCount(pager.getCount(db));

			model.setOffset(pager.getOffset());
			model.setSort(pager.getOrderByField());
			model.setSortMode(pager.getOrderByOperator());

			// update child views
			if (model.getMode().equals(Mode.EDIT_VIEW))
			{
				for (ScreenController<?> c : this.getChildren())
				{
					// only the real screens, not the commands
					if (c instanceof SimpleScreenController<?>)
					{
						c.reload(db);
					}
				}
			}
			logger.debug("reload finished.");
		}
		catch (Exception e)
		{
			logger.error("reload() failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * This function is actually responsible for querying the database.
	 * 
	 * @return list of entity objects currently in view
	 * @throws DatabaseException
	 */
	private List<E> getData(Database db) throws DatabaseException
	{
		// TODO: move the row level security to the entitymapper...
		FormModel<E> model = getModel();

		// set form level rights
		boolean formReadonly = model.isReadonly() || !model.getLogin().canWrite(model.create().getClass());
		model.setReadonly(formReadonly);

		// load the rows
		List<E> visibleRecords = new ArrayList<E>();

		// load all records and select rows that can be visible
		// List allRecords = view.getDatabase().find( view.getEntityClass(),
		// allRules );
		List<E> allRecords = pager.getPage(db);

		for (E record : allRecords)
		{
			boolean rowReadonly = formReadonly || !model.getLogin().canWrite(record.getClass());

			if (rowReadonly) record.setReadonly(true);
			// else
			// recordreadonly = false;

			visibleRecords.add(record);
		}

		return visibleRecords;
	}

	/**
	 * Helper function to add a record from UI
	 * 
	 * @param request
	 * @return true if add was successfull, false if it wassen't
	 * @throws ParseException
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public boolean doAdd(Database db, MolgenisRequest request) throws ParseException, DatabaseException, IOException
	{
		ScreenMessage msg = null;
		Entity entity = getModel().create();
		boolean result = false;

		try
		{
			db.beginTx();
			entity.set(request, false);
			int updatedRows = 0;
			if (request.get(FormModel.INPUT_BATCHADD) != null && request.getInt(FormModel.INPUT_BATCHADD) > 1)
			{
				// batch
				int i;
				for (i = 0; i < request.getInt(FormModel.INPUT_BATCHADD); i++)
				{
					updatedRows += db.add(entity);
				}
			}
			else
			{
				updatedRows = db.add(entity);

			}
			db.commitTx();
			msg = new ScreenMessage("ADD SUCCESS: affected " + updatedRows, null, true);
			result = true;
			// navigate to newly added record
			pager.last(db);

		}
		catch (Exception e)
		{
			db.rollbackTx();
			msg = new ScreenMessage("ADD FAILED: " + e.getMessage(), null, false);
			result = false;
		}
		getModel().getMessages().add(msg);

		/* make sure the user sees the newly added record(s) */
		// view.setMode(FormScreen.Mode.RECORD_VIEW);
		// pager.setLimit(1);
		pager.resetOrderBy();
		pager.last(db);
		// should reset to an order that shows the record on the end

		return result;
	}

	// helper method
	protected void doUpdate(Database db, MolgenisRequest request) throws DatabaseException, IOException, ParseException
	{
		Entity entity = getModel().create();
		ScreenMessage msg = null;
		try
		{
			entity.set(request, false);
			int updatedRows = db.update(entity);
			msg = new ScreenMessage("UPDATE SUCCESS: affected " + updatedRows, null, true);
		}
		catch (Exception e)
		{
			logger.error("doUpdate(): " + e);
			e.printStackTrace();
			msg = new ScreenMessage("UPDATE FAILED: " + e.getMessage(), null, false);
		}
		getModel().getMessages().add(msg);
		if (msg.isSuccess())
		{
			pager.setDirty(true);
			// resetChildren();
		}
	}

	// helper method
	protected void doRemove(Database db, MolgenisRequest request) throws DatabaseException, ParseException, IOException
	{
		Entity entity = getModel().create();
		ScreenMessage msg = null;
		try
		{
			entity.set(request);
			int updatedRows = db.remove(entity);
			if (updatedRows > 0) msg = new ScreenMessage("REMOVE SUCCESS: affected " + updatedRows, null, true);
			else msg = new ScreenMessage("REMOVE FAILED: call system administrator", null, false);
		}
		catch (Exception e)
		{
			msg = new ScreenMessage("REMOVE FAILED: " + e.getMessage(), null, false);
		}
		getModel().getMessages().add(msg);

		// **make sure the user sees a record**/
		if (msg.isSuccess())
		{
			pager.prev(db);
			// resetChildren();
		}
	}

	/**
	 * Needed for hyperlink form switches...
	 * 
	 * @param request
	 * @throws DatabaseException
	 */
	private void doXrefselect(Tuple request) throws DatabaseException
	{
		// also set the parent menu
		if (getParent() != null && getParent() instanceof MenuController)
		{
			// set the filter to select the xref-ed entity
			pager.resetFilters();
			getModel().setUserRules(new ArrayList<QueryRule>());
			QueryRule rule = new QueryRule(request.getString("attribute"), QueryRule.Operator.valueOf(request
					.getString("operator")), request.getString("value"));
			pager.addFilter(rule);

			// tell "my" menu to select me
			WritableTuple tuple = new KeyValueTuple();
			String aChildName = getModel().getName();
			ScreenController<?> aParent = getParent();
			while (aParent != null)
			{
				if (aParent instanceof MenuController)
				{
					tuple.set("select", aChildName);
					MenuController c = (MenuController) aParent;
					c.doSelect(tuple);
				}
				aChildName = aParent.getName();
				aParent = aParent.getParent();
			}
		}
	}

	public DatabasePager<E> getPager()
	{
		return pager;
	}

	/**
	 * Calculates visible columns. Needed for downloads.
	 */
	public List<String> getVisibleColumnNames()
	{
		// FIXME temporary fix because model not properly used
		// we should instead use getModel().getHiddenColumns().

		List<String> showColumns = new ArrayList<String>();
		try
		{
			HtmlForm f = getInputs(this.getEntityClass().newInstance(), false);

			for (HtmlInput<?> i : f.getInputs())
			{
				if (!i.isHidden())
				{
					// strip prefix = this.getEntityClass() + "_"
					String name = i.getName();
					name = name.substring(this.getEntityClass().getSimpleName().length() + 1);

					// then add _label using getSearchField() where needed
					showColumns.add(getSearchField(name));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return showColumns;
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		// stylesheet and scripts for all inputs
		StringBuilder s = new StringBuilder();
		s.append("<link rel=\"stylesheet\" href=\"css/molgenis-dateinput.css\" type=\"text/css\" />");
		s.append("<link rel=\"stylesheet\" href=\"css/molgenis-jquery_icons.css\" type=\"text/css\" />");
		s.append("<link rel=\"stylesheet\" href=\"css/molgenis-xrefinput.css\" type=\"text/css\" />");
		s.append("<link rel=\"stylesheet\" href=\"css/jquery.bt.css\" type=\"text/css\" />");
		s.append("<link rel=\"stylesheet\" href=\"css/jquery.chosen.css\" type=\"text/css\" />");
		s.append("<script type=\"text/javascript\" src=\"js/molgenis-textinput.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/molgenis-datetimeinput.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/molgenis-xrefinput.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/molgenis-mrefinput.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.ajax-chosen.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.autogrowinput.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.bt.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.chosen.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.timepicker.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.validate.min.js\"></script>");
		return s.toString();
	}

	@Override
	public ScreenView getView()
	{
		return new FreemarkerView("FormView.ftl", getModel());
	}

	/**
	 * Provides the class of the entitites managed by this form. Note: Java
	 * erases the specific type of E, therefore we cannot say E.newInstance();
	 */
	public abstract Class<E> getEntityClass();

	/**
	 * Default settings for hidden columns
	 */
	public abstract void resetSystemHiddenColumns();

	/**
	 * Default settings for compact columns
	 */
	public abstract void resetCompactView();

	// ABSTRACT METHODS, varied per entity
	/**
	 * Abstract method to build the inputs for each row. The result will be a
	 * set of inputs that can be put on the form screen.
	 * 
	 * @param entity
	 * @param newrecord
	 * @throws ParseException
	 */
	public abstract HtmlForm getInputs(E entity, boolean newrecord);

	public abstract Vector<String> getHeaders();

	/**
	 * Helper function that translates xref field name into its label (for
	 * showing that in the UI).
	 */
	public abstract String getSearchField(String fieldName);

	/**
	 * Returns fields name for the given search field name
	 * 
	 * @param searchFieldName
	 * @return
	 */
	public abstract String getField(String searchFieldName);
}
