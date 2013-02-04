package org.molgenis.framework.server;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.ui.html.SelectInput;
import org.molgenis.framework.ui.html.StringInput;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.HttpServletRequestTuple;
import org.molgenis.util.tuple.Tuple;

/**
 * Implementation of the REST interface
 * 
 * @author Morris Swertz
 */
public class RestInterface
{
	private static final Logger logger = Logger.getLogger(RestInterface.class);

	/**
	 * Handle use of the REST api URL pattern /rest/find/[select]?[filter] and
	 * /rest/count/[select]?[filter]<br>
	 * 
	 * [select] should be either an entity name like '/rest/find/sample?...' or
	 * an enumeration of field names like 'sample.id,sample.name' (not yet
	 * implemented!)<br>
	 * 
	 * [filter] should be of form field1>=value1&field2=4&LIMIT=1&SORTASC=field1<br>
	 * 
	 * field names are need to be fully unique within the selection or fully
	 * qualified<br>
	 * 
	 * if fields are selected from multiple entities then a join path is
	 * automatically calculated unless suitable join rules are defined like
	 * 'entity1.field1JOINentity2.field2'
	 * 
	 * Do we need enhancements: Aggregate functions and group by?
	 */
	public static void handleRequest(HttpServletRequest request, HttpServletResponse response, Database db)
	{
		// setup the output-stream
		response.setBufferSize(10000);
		response.setContentType("text/html; charset=UTF-8");

		logger.info("starting REST request " + request.getPathInfo());
		long start_time = System.currentTimeMillis();

		if (request.getPathInfo().startsWith("/find") || request.getPathInfo().startsWith("/count")) handleRetrievalRequest(
				request, response, db);

		logger.info("servlet took: " + (System.currentTimeMillis() - start_time));
		logger.info("------------");
	}

	private static void handleRetrievalRequest(HttpServletRequest request, HttpServletResponse response, Database db)
	{
		try
		{
			PrintWriter out = response.getWriter();

			try
			{
				// check whether the url already includes the entity parameter
				// /rest/find/<selectparameters>
				if (request.getPathInfo().equals("/find") || request.getPathInfo().equals("/count"))
				{
					showSelectEntityDialog(out, db);
					return;
				}

				// retrieve entity name from request (if any)
				String entityName = request.getPathInfo().substring("/find/".length());
				if (request.getPathInfo().startsWith("/count/")) entityName = request.getPathInfo().substring(
						"/count/".length());
				Entity entity = (Entity) Class.forName(entityName).newInstance();
				// TODO this may be an enumeration of fields...

				// check whether a dialog for the filters has to be shown
				if (request.getQueryString() != null && request.getQueryString().equals("__showQueryDialogue=true"))
				{
					showSelectFilterDialogForEntity(entity, out, db);
					return;
				}

				// create query rules
				List<QueryRule> rulesList = createQueryRulesFromRequest(request);

				// execute query
				if (request.getPathInfo().startsWith("/count/"))
				{
					if (rulesList != null) db.count(getClassForName(entityName),
							rulesList.toArray(new QueryRule[rulesList.size()]));
					else
					{
						out.println(db.count(getClassForName(entityName)));
					}
				}
				else
				{
					TupleWriter csvWriter = new CsvWriter(out);
					try
					{
						if (rulesList != null) db.find(getClassForName(entityName), csvWriter,
								rulesList.toArray(new QueryRule[rulesList.size()]));
						else
						{
							db.find(getClassForName(entityName), csvWriter);
						}
					}
					finally
					{
						csvWriter.close();
					}
				}
			}
			catch (Exception e)
			{
				out.println(e + "<br>");
				e.printStackTrace();
				throw e;
			}
			finally
			{
				db.close();
			}

			out.close();
		}
		catch (Exception e)
		{
			logger.error(e);
		}
	}

	@SuppressWarnings("unchecked")
	/*
	 * No way to do this without warnings.
	 */
	private static Class<? extends Entity> getClassForName(String entityName) throws ClassNotFoundException
	{
		return (Class<? extends Entity>) Class.forName(entityName);
	}

	private static List<QueryRule> createQueryRulesFromRequest(HttpServletRequest request) throws Exception
	{
		List<QueryRule> rulesList;
		// use 'get' protocol
		if (request.getQueryString() != null)
		{
			logger.debug("handle find query via http-get: " + request.getQueryString());
			rulesList = QueryRuleUtil.fromRESTstring(URLDecoder.decode(request.getQueryString(), "UTF-8"));
		}
		// use 'post'
		else
		{
			Tuple requestTuple = new HttpServletRequestTuple(request);
			StringBuilder queryStringBuilder = new StringBuilder();
			for (String name : requestTuple.getColNames())
			{
				queryStringBuilder.append(URLDecoder.decode(name, "UTF-8")).append('=');
				queryStringBuilder.append(URLDecoder.decode(requestTuple.getString(name), "UTF-8"));
			}
			String queryString = queryStringBuilder.toString();
			logger.debug("handle find query via http-post with parameters: " + queryString);
			rulesList = QueryRuleUtil.fromRESTstring(queryString);
		}

		return rulesList;
	}

	private static void showSelectFilterDialogForEntity(Entity entity, PrintWriter out, Database db)
	{
		logger.debug("show 'set filters' dialogue");
		out.println("<html><body>");
		out.println("<head><script src=\"../../../res/scripts/rest.js\" language=\"javascript\"></script></head>");
		out.println("<h1>REST url wizard:</h1>");
		out.println("Step 2: choose filters<br>");
		out.println("<form>");
		out.println("You choose to use the REST interface for retrieval of '" + entity.getClass().getName()
				+ "' data. (<a href=\"../find\">back</a>)<br><br> Here you can add filters:<br>");

		SelectInput fieldInput = new SelectInput("field", null);
		fieldInput.setOptions(entity.getFields().toArray(new String[entity.getFields().size()]));

		SelectInput operatorInput = new SelectInput("operator", null);
		List<String> operators = new ArrayList<String>();
		operators.add(QueryRule.Operator.EQUALS.toString());
		operators.add(QueryRule.Operator.GREATER_EQUAL.toString());
		operators.add(QueryRule.Operator.NOT.toString());
		operators.add(QueryRule.Operator.LESS.toString());
		operators.add(QueryRule.Operator.LESS_EQUAL.toString());
		operators.add(QueryRule.Operator.LIKE.toString());
		operatorInput.setOptions(operators.toArray(new String[operators.size()]));

		StringInput valueInput = new StringInput("value", null);

		out.println(fieldInput.getHtml() + operatorInput.getHtml() + valueInput.getHtml());
		out.println("<br>");

		out.println(fieldInput.getHtml() + operatorInput.getHtml() + valueInput.getHtml());
		out.println("<br>");

		out.println("<input type=\"submit\" value=\"generate url\" onclick=\"generateRestUrl(this.form.elements); return false\">");

		out.println("TIP: notice how the url is bookmarkeable for future downloads!");
		out.println("TIP: click 'save as...' and name it as '.txt' file.");
		out.println("</form></body></html>");
	}

	private static void showSelectEntityDialog(PrintWriter out, Database db)
	{
		logger.debug("show 'choose entity' dialogue");
		out.println("<html><body>");
		out.println("<h1>REST url wizard:</h1>");
		out.println("Step 1: choose entity<br>");

		for (String className : db.getEntityNames())
		{

			out.println(className + ": <a href=\"find/" + className
					+ "?__showQueryDialogue=true\">find</a> or <a href=\"count/" + className
					+ "?__showQueryDialogue=true\">count</a><br>");
		}

		// TODO enable a dialog for the selection of fields too

		out.println("</body></html>");

		logger.debug("done");

	}
}
