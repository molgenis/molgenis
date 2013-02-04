package org.molgenis.framework.tupletable.view;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.EditableTupleTable;
import org.molgenis.framework.tupletable.FilterableTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.framework.tupletable.view.JQGridJSObjects.JQGridConfiguration;
import org.molgenis.framework.tupletable.view.JQGridJSObjects.JQGridFilter;
import org.molgenis.framework.tupletable.view.JQGridJSObjects.JQGridPostData;
import org.molgenis.framework.tupletable.view.JQGridJSObjects.JQGridResult;
import org.molgenis.framework.tupletable.view.JQGridJSObjects.JQGridRule;
import org.molgenis.framework.tupletable.view.JQGridJSObjects.JQGridSearchOptions;
import org.molgenis.framework.tupletable.view.renderers.Renderers;
import org.molgenis.framework.tupletable.view.renderers.Renderers.JQGridRenderer;
import org.molgenis.framework.tupletable.view.renderers.Renderers.Renderer;
import org.molgenis.framework.ui.FreemarkerView;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.html.HtmlWidget;
import org.molgenis.util.HandleRequestDelegationException;
import org.molgenis.util.tuple.Tuple;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * View class which provides a JQGrid view on a {@link TupleTable}
 */
public class JQGridView extends HtmlWidget
{
	public static final String OPERATION = "Operation";
	private static final int DEFAULT_MAX_VISIBLE_COLUMN_COUNT = 5;
	private JQGridSearchOptions searchOptions;
	private final int maxVisibleColumnCount = DEFAULT_MAX_VISIBLE_COLUMN_COUNT;
	private JQGridViewCallback callback;

	/**
	 * Operations that the GridView can handle. LOAD_CONFIG, RENDER_DATA,
	 * LOAD_TREE
	 */
	private enum Operation
	{
		LOAD_CONFIG, RENDER_DATA, EDIT_RECORD, ADD_RECORD, DELETE_RECORD, NEXT_COLUMNS, PREVIOUS_COLUMNS, SET_COLUMN_PAGE, HIDE_COLUMN, SHOW_COLUMN
	}

	/**
	 * Interface for builder classes that allow easy reconstruction of the
	 * view's inner {@link TupleTable} in the {@link JQGridView#handleRequest}
	 * function
	 */
	public interface TupleTableBuilder
	{
		public TupleTable create(Tuple request) throws TableException;

		public String getUrl();
	}

	private final TupleTableBuilder tupleTableBuilder;

	private JQGridView(String name, TupleTableBuilder tupleTableBuilder)
	{
		super(name);
		this.tupleTableBuilder = tupleTableBuilder;
	}

	/**
	 * Default construction with an anonymous inner
	 * {@link JQGridView.TupleTableBuilder}
	 */
	public JQGridView(final String name, final ScreenController<?> hostController, final TupleTable table)
	{

		this(name, new TupleTableBuilder()
		{
			@Override
			public String getUrl()
			{
				return "molgenis.do?__target=" + hostController.getName() + "&__action=download_json_dataset";
			}

			@Override
			public TupleTable create(Tuple request) throws TableException
			{
				return table;
			}
		});

		if (hostController instanceof JQGridViewCallback)
		{
			callback = (JQGridViewCallback) hostController;
		}
	}

	public JQGridView(final String name, final ScreenController<?> hostController, final TupleTable table,
			JQGridSearchOptions searchOptions)
	{
		this(name, hostController, table);
		this.searchOptions = searchOptions;
	}

	/**
	 * Handle a particular {@link MolgenisRequest}, and render into an
	 * {@link OutputStream}. Particulars handled:
	 * <ul>
	 * <li>Wrap the desired data source in the appropriate instantiation of
	 * {@link TupleTable}.</li>
	 * <li>Determine which {@link Operation} the request is asking to handle.
	 * <li>Apply proper sorting and filter rules.</li>
	 * <li>Select the appropriate view towards which to export/render.</li>
	 * <li>Select and render the data.</li>
	 * </ul>
	 * 
	 * @param db
	 *            The database to connect to
	 * @param request
	 *            The {@link MolgenisRequest} tuple that encodes the request to
	 *            handle
	 * @param out
	 *            The {@link OutputStream} to render to.
	 */
	public void handleRequest(Database db, MolgenisRequest request, OutputStream out)
			throws HandleRequestDelegationException
	{
		try
		{
			if (db.getConnection().isClosed())
			{
				throw new HandleRequestDelegationException(new Exception("handleRequest: Connection is closed!"));
			}

			final TupleTable tupleTable = tupleTableBuilder.create(request);
			tupleTable.setColLimit(maxVisibleColumnCount);

			if (tupleTable instanceof DatabaseTupleTable)
			{
				((DatabaseTupleTable) tupleTable).setDb(db);
			}

			final Operation operation = StringUtils.isNotEmpty(request.getString(OPERATION)) ? Operation
					.valueOf(request.getString(OPERATION)) : Operation.RENDER_DATA;

			switch (operation)
			{
				case LOAD_CONFIG:
					if (callback != null)
					{
						callback.beforeLoadConfig(request, tupleTable);
					}
					loadTupleTableConfig(db, request, tupleTable);
					break;
				case HIDE_COLUMN:
					String columnToRemove = request.getString("column");
					tupleTable.hideColumn(columnToRemove);
					loadTupleTableConfig(db, request, tupleTable);
					break;
				case SHOW_COLUMN:
					String columnToShow = request.getString("column");
					tupleTable.showColumn(columnToShow);
					loadTupleTableConfig(db, request, tupleTable);
					break;
				case SET_COLUMN_PAGE:

					// TODO put this in a util class (default value for
					// requestparams)
					int colPage;
					try
					{
						colPage = request.getInt("colPage");
					}
					catch (Exception e)
					{
						colPage = 1;
					}

					// TODO put maxColPage function in util class
					int maxColPage = (int) Math.floor(tupleTable.getColCount() / (double) tupleTable.getColLimit());
					if ((tupleTable.getColCount() % tupleTable.getColLimit()) > 0)
					{
						maxColPage++;
					}
					colPage = Math.min(colPage, maxColPage);

					int colOffset = (colPage - 1) * tupleTable.getColLimit();
					colOffset = Math.max(colOffset, 0);

					tupleTable.setColOffset(colOffset);
					loadTupleTableConfig(db, request, tupleTable);
					break;
				case NEXT_COLUMNS:
					tupleTable.setColOffset(tupleTable.getColOffset() + maxVisibleColumnCount);
					loadTupleTableConfig(db, request, tupleTable);
					break;
				case PREVIOUS_COLUMNS:
					tupleTable.setColOffset(tupleTable.getColOffset() - maxVisibleColumnCount);
					loadTupleTableConfig(db, request, tupleTable);
					break;
				case RENDER_DATA:
					final List<QueryRule> rules = new ArrayList<QueryRule>();

					// parse the request into post data
					final JQGridPostData postData = new JQGridPostData(request);

					// convert any filters to query rules
					final List<QueryRule> filterRules = createQueryRulesFromJQGridRequest(postData.getFilters());

					if (!Iterables.isEmpty(filterRules))
					{
						rules.addAll(filterRules);
					}

					int residue = 0;
					final int rowCount = tupleTable.getCount();
					if (rowCount % postData.getRows() != 0)
					{
						residue = 1;
					}
					final int totalPages = (int) Math.floor(rowCount / (double) postData.getRows()) + residue;

					// update page
					postData.setPage(Math.min(postData.getPage(), totalPages));
					final int offset = Math.max((postData.getPage() - 1) * postData.getRows(), 0);

					final String exportSelection = request.getString("exportSelection");
					if (!StringUtils.equalsIgnoreCase(exportSelection, "ALL"))
					{
						// data.rows == limit
						tupleTable.setLimit(postData.getRows());
						// data.rows * data.page
						tupleTable.setOffset(offset);
					}

					if (StringUtils.isNotEmpty(postData.getSidx()))
					{
						final Operator sortOperator = StringUtils.equals(postData.getSord(), "asc") ? QueryRule.Operator.SORTASC : QueryRule.Operator.SORTDESC;
						rules.add(new QueryRule(sortOperator, postData.getSidx()));
					}

					if ((tupleTable instanceof FilterableTupleTable) && !request.isNull("_search")
							&& request.getBoolean("_search"))
					{
						((FilterableTupleTable) tupleTable).setFilters(rules);
					}

					renderData(request, postData, totalPages, tupleTable);
					break;

				case EDIT_RECORD:

					if (!(tupleTable instanceof EditableTupleTable))
					{
						throw new UnsupportedOperationException("TupleTable is not editable");
					}

					// create a json object to take the message and success
					// variables.
					JsonObject result = new JsonObject();

					try
					{
						((EditableTupleTable) tupleTable).update(request);

						result.addProperty("message", "Record updated");
						result.addProperty("success", true);
					}
					catch (TableException e)
					{
						result.addProperty("message", e.getMessage());
						result.addProperty("success", false);
					}
					// Send this json string back the html.
					request.getResponse().getOutputStream().println(result.toString());
					break;

				case ADD_RECORD:

					if (!(tupleTable instanceof EditableTupleTable))
					{
						throw new UnsupportedOperationException("TupleTable is not editable");
					}

					// create a json object to take the message and success
					// variables.
					result = new JsonObject();

					try
					{
						((EditableTupleTable) tupleTable).add(request);
						result.addProperty("message", "Record added");
						result.addProperty("success", true);
					}
					catch (TableException e)
					{
						result.addProperty("message", e.getMessage());
						result.addProperty("success", false);
					}

					// Send this json string back the html.
					request.getResponse().getOutputStream().println(result.toString());
					break;

				case DELETE_RECORD:
					if (!(tupleTable instanceof EditableTupleTable))
					{
						throw new UnsupportedOperationException("TupleTable is not editable");
					}

					// create a json object to take the message and success
					// variables.
					result = new JsonObject();

					try
					{
						((EditableTupleTable) tupleTable).remove(request);

						result.addProperty("message", "Record deleted");
						result.addProperty("success", true);
					}
					catch (TableException e)
					{
						result.addProperty("message", e.getMessage());
						result.addProperty("success", false);
					}

					// Send this json string back the html.
					request.getResponse().getOutputStream().println(result.toString());
					break;
				default:
					break;
			}
		}
		catch (IOException e)
		{
			throw new HandleRequestDelegationException(e);
		}
		catch (TableException e)
		{
			throw new HandleRequestDelegationException(e);
		}
		catch (SQLException e)
		{
			throw new HandleRequestDelegationException(e);
		}
		catch (DatabaseException e)
		{
			throw new HandleRequestDelegationException(e);
		}
	}

	/**
	 * Render a particular subset of data from a {@link TupleTable} to a
	 * particular {@link Renderer}.
	 * 
	 * @param request
	 *            The request encoding the particulars of the rendering to be
	 *            done.
	 * @param postData
	 *            The selected page (only relevant for {@link JQGridRenderer}
	 *            rendering)
	 * @param totalPages
	 *            The total number of pages (only relevant for
	 *            {@link JQGridRenderer} rendering)
	 * @param tupleTable
	 *            The table from which to render the data.
	 */
	private void renderData(MolgenisRequest request, JQGridPostData postData, int totalPages,
			final TupleTable tupleTable) throws TableException
	{

		String strViewType = request.getString("viewType");
		if (StringUtils.isEmpty(strViewType))
		{
			strViewType = "JQ_GRID";
		}

		try
		{
			final ViewFactory viewFactory = new ViewFactoryImpl();
			final Renderers.Renderer view = viewFactory.createView(strViewType);
			view.export(request, request.getString("caption"), tupleTable, totalPages, postData.getPage());
		}
		catch (final Exception e)
		{
			throw new TableException(e);
		}
	}

	/**
	 * Extract the filter rules from the sent jquery request, and convert them
	 * into Molgenis Query rules.
	 * 
	 * @param filters
	 *            A request containing filter rules
	 * @return A list of QueryRules that represent the filter rules from the
	 *         request.
	 */
	private static List<QueryRule> createQueryRulesFromJQGridRequest(JQGridFilter filters)
	{
		final List<QueryRule> rules = new ArrayList<QueryRule>();
		if (filters != null)
		{
			final String groupOp = filters.getGroupOp();

			int ruleIdx = 0;
			for (final JQGridRule rule : filters.getRules())
			{
				final QueryRule queryRule = convertOperator(rule);
				rules.add(queryRule);

				final boolean notLast = filters.getRules().size() - 1 != ruleIdx++;
				if (groupOp.equals("OR") && notLast)
				{
					rules.add(new QueryRule(QueryRule.Operator.OR));
				}
			}
		}
		return rules;
	}

	/**
	 * Create a {@link QueryRule} based on a jquery operator string, from the
	 * filter popup/dropdown in the {@link JQGridRenderer} UI. Example:
	 * Supplying the arguments 'name', 'ne', 'Asia' creates a QueryRule that
	 * filters for rows where the 'name' column does not equal 'Asia'.
	 * 
	 * @param field
	 *            The field to which to apply the operator
	 * @param op
	 *            The operator string (jquery syntax)
	 * @param value
	 *            The value (if any) for the right-hand side of the operator
	 *            expression.
	 * @return A new QueryRule that represents the supplied jquery expression.
	 */

	private static QueryRule convertOperator(JQGridRule jqGridRule)
	{
		// ['eq','ne','lt','le','gt','ge','bw','bn','in','ni','ew','en','cn','nc']
		QueryRule rule = new QueryRule(jqGridRule.getField(), QueryRule.Operator.EQUALS, jqGridRule.getData());
		switch (jqGridRule.getOp())
		{
			case eq:
				rule.setOperator(QueryRule.Operator.EQUALS);
				return rule;
			case ne:
				rule.setOperator(QueryRule.Operator.EQUALS);
				return toNotRule(rule);
			case lt:
				rule.setOperator(QueryRule.Operator.LESS);
				return rule;
			case le:
				rule.setOperator(QueryRule.Operator.LESS_EQUAL);
				return rule;
			case gt:
				rule.setOperator(QueryRule.Operator.GREATER);
				return rule;
			case ge:
				rule.setOperator(QueryRule.Operator.GREATER_EQUAL);
				return rule;
			case bw:
				rule.setValue(jqGridRule.getData() + "%");
				rule.setOperator(QueryRule.Operator.LIKE);
				return rule;
			case bn:
				// NOT
				rule.setValue(jqGridRule.getData() + "%");
				rule.setOperator(QueryRule.Operator.LIKE);
				rule = toNotRule(rule);
				return rule;
			case in:
				rule.setOperator(QueryRule.Operator.IN);
				return rule;
			case ni:
				// NOT
				rule.setOperator(QueryRule.Operator.IN);
				rule = toNotRule(rule);
				return rule;
			case ew:
				rule.setValue("%" + jqGridRule.getData());
				rule.setOperator(QueryRule.Operator.LIKE);
				return rule;
			case en:
				// NOT
				rule.setValue("%" + jqGridRule.getData());
				rule.setOperator(QueryRule.Operator.LIKE);
				return toNotRule(rule);
			case cn:
				rule.setValue("%" + jqGridRule.getData() + "%");
				rule.setOperator(QueryRule.Operator.LIKE);
				return rule;
			case nc:
				// NOT
				rule.setValue("%" + jqGridRule.getData() + "%");
				rule.setOperator(QueryRule.Operator.LIKE);
				return toNotRule(rule);
			default:
				throw new IllegalArgumentException(String.format("Unkown Operator: %s", jqGridRule.getOp()));
		}
	}

	/**
	 * Add a 'NOT' operator to a particular rule.
	 * 
	 * @param rule
	 *            The rule to negate.
	 * @return A new {@link QueryRule} which is the negation of the supplied
	 *         rule.
	 */
	private static QueryRule toNotRule(QueryRule rule)
	{
		return new QueryRule(QueryRule.Operator.NOT, rule);
	}

	/**
	 * Create the HTML that is sent to the browser. Based on a Freemarker
	 * template file.
	 */
	@Override
	public String toHtml()
	{
		final Map<String, Object> args = new HashMap<String, Object>();

		args.put("tableId", super.getId());
		args.put("url", tupleTableBuilder.getUrl());

		return new FreemarkerView(JQGridView.class, args).render();
	}

	/**
	 * Create a properly-configured grid with default settings, on first load.
	 */
	public void loadTupleTableConfig(Database db, MolgenisRequest request, TupleTable tupleTable)
			throws TableException, IOException
	{

		final JQGridConfiguration config = new JQGridConfiguration(db, getId(), "Name", tupleTableBuilder.getUrl(),
				getLabel(), tupleTable);

		if (searchOptions != null)
		{
			config.setSearchOptions(searchOptions);
		}

		Writer writer = new OutputStreamWriter(request.getResponse().getOutputStream(), Charset.forName("UTF-8"));
		try
		{
			new Gson().toJson(config, writer);
		}
		finally
		{
			writer.close();
		}
	}

	/**
	 * Function to build a datastructure filled with rows from a
	 * {@link TupleTable}, to be serialised by Gson and displayed from there by
	 * a jqGrid.
	 * 
	 * @param rowCount
	 *            The number of rows to select.
	 * @param totalPages
	 *            The total number of pages of data (ie. dependent on size of
	 *            dataset and nr. of rows per page)
	 * @param page
	 *            The selected page.
	 * @param table
	 *            The Tupletable from which to read the data.
	 * @return
	 */

	public static JQGridResult buildJQGridResults(final int rowCount, final int totalPages, final int page,
			final TupleTable table) throws TableException
	{
		final JQGridResult result = new JQGridResult(page, totalPages, rowCount);

		for (Iterator<Tuple> it = table.iterator(); it.hasNext();)
		{
			Tuple row = it.next();
			final LinkedHashMap<String, String> rowMap = new LinkedHashMap<String, String>();

			for (String fieldName : row.getColNames())
			{
				String rowValue = !row.isNull(fieldName) ? row.getString(fieldName) : "null";
				rowMap.put(fieldName, rowValue); // TODO encode to HTML
			}
			result.addRow(rowMap);
		}

		return result;
	}
}
