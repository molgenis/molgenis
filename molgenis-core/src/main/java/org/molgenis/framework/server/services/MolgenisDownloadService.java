package org.molgenis.framework.server.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.server.AuthStatus;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.framework.server.MolgenisServiceAuthenticationHelper;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.util.Entity;

public class MolgenisDownloadService implements MolgenisService
{
	Logger logger = Logger.getLogger(MolgenisDownloadService.class);

	private MolgenisContext mc;

	public MolgenisDownloadService(MolgenisContext mc)
	{
		this.mc = mc;
	}

	/**
	 * Handle use of the download API.
	 * 
	 * TODO: this method is horrible and should be properly refactored,
	 * documented and tested!
	 * 
	 * @param request
	 * @param response
	 */
	@Override
	public void handleRequest(MolgenisRequest req, MolgenisResponse res) throws ParseException, DatabaseException,
			IOException
	{
		logger.info("starting download " + req.getRequest().getPathInfo());
		long start_time = System.currentTimeMillis();

		res.getResponse().setBufferSize(10000);
		res.getResponse().setContentType("text/html; charset=UTF-8");

		PrintWriter out = res.getResponse().getWriter();
		Database db = req.getDatabase();

		try
		{

			AuthStatus authStatus = MolgenisServiceAuthenticationHelper.handleAuthentication(req, out);

			if (!authStatus.isShowApi())
			{
				out.println("<html><body>");
				out.println(authStatus.getPrintMe());
				out.println("</body></html>");
			}
			else
			{

				String entityName = req.getRequest().getPathInfo().substring(req.getServicePath().length());

				if (entityName.startsWith("/"))
				{
					entityName = entityName.substring(1);
				}

				if (entityName.equals(""))
				{
					out.println("<html><body>");
					out.println(authStatus.getPrintMe());
					if (req.getDatabase().getLogin().isAuthenticated())
					{
						out.println(MolgenisServiceAuthenticationHelper.displayLogoutForm());
					}
					showAvailableDownloads(out, db, req);
					out.println("</body></html>");
				}
				else
				{
					// Check if this entity exists and is downloadable
					List<org.molgenis.model.elements.Entity> downloadableEntities = getDownloadableEntities(db);
					boolean found = false;
					for (int i = 0; i < downloadableEntities.size() && !found; i++)
					{
						String name = downloadableEntities.get(i).getName();
						if ((name != null) && name.equals(entityName))
						{
							found = true;
						}
					}

					if (!found)
					{
						res.getResponse().sendError(404);// NOT FOUND
						return;
					}

					if (req.getRequest().getQueryString() != null
							&& req.getRequest().getQueryString().equals("__showQueryDialogue=true"))
					{
						out.println("<html><body>");
						out.println(authStatus.getPrintMe());
						if (req.getDatabase().getLogin().isAuthenticated())
						{
							out.println(MolgenisServiceAuthenticationHelper.displayLogoutForm());
						}
						showFilterableDownload(out, entityName, db);
						out.println("</body></html>");
					}
					else
					{
						executeQuery(out, req, db, entityName);
					}
				}
			}
		}
		catch (Exception e)
		{
			out.println(e.getMessage());
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		finally
		{
			out.flush();
			out.close();
			db.close();
		}

		logger.info("servlet took: " + (System.currentTimeMillis() - start_time));
		logger.info("------------");

	}

	private void showFilterableDownload(PrintWriter out, String entityName, Database db) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException
	{
		// System.out.println("show 'set filters' dialogue");
		out.println("<form>");
		out.println("You choose to download '" + entityName // FIXME: bad,
															// hardcoded
															// location!
				+ "' data. (<a href=\"../find\">back</a>)<br><br> Here you can set filters before downloading:<br>");
		out.println("<table>");

		String simpleEntityName = entityName.substring(entityName.lastIndexOf('.') + 1);
		Class<? extends Entity> klazz = db.getClassForName(simpleEntityName);

		for (String field : ((Entity) klazz.newInstance()).getFields())
		{
			out.println("<tr><td>" + field + "</td><td>=</td><td><input name=\"" + field
					+ "\" type=\"text\"></td><tr/>");
		}
		out.println("</table>");
		out.println("<SCRIPT>" + "function createFilterURL(fields)" + "{	" + "	var query = '';" + "	var count = 0;"
				+ "	for (i = 0; i < fields.length; i++) " + "	{"
				+ "		if (fields[i].value != '' && fields[i].name != '__submitbutton')" + "		{" + "			if(count > 0)"
				+ "				query +='&';" + "			query += fields[i].name + '=' + fields[i].value;" + "			count++;" + "		}"
				+ "	}" + "	return query" + "}" + "</SCRIPT>");

		out.println("<input name=\"__submitbutton\" type=\"submit\" value=\"Download tab delimited file\" onclick=\""
				+ "window.location.href = 'http://' + window.location.host + window.location.pathname + '?'+createFilterURL(this.form.elements);\"><br><br>");
		out.println("TIP: notice how the url is bookmarkeable for future downloads!<br>");
		out.println("TIP: click 'save as...' and name it as '.txt' file.");
		out.println("</form>");
	}

	private void showAvailableDownloads(PrintWriter out, Database db, MolgenisRequest req) throws DatabaseException
	{
		// print message to indicate your are anonymous
		if (!db.getLogin().isAuthenticated())
		{
			out.println("You are currently browsing as anonymous.<br>");
		}

		out.println("You can download these data:<br>");
		out.println("<table>");

		for (org.molgenis.model.elements.Entity eClass : getDownloadableEntities(db))
		{
			String name = eClass.getName();
			Class<? extends Entity> klazz = db.getClassForName(name);

			// hide entities without read permission
			if (db.getLogin().canRead(klazz))
			{
				out.println("<tr>");
				out.println("<td><a href=\""
						+ (req.getRequest().getPathInfo().endsWith("/") ? "" : "/" + mc.getVariant()
								+ req.getServicePath() + "/") + name + "\">" + name + "</a></td>");
				out.println("<td><a href=\""
						+ (req.getRequest().getPathInfo().endsWith("/") ? "" : "/" + mc.getVariant()
								+ req.getServicePath() + "/") + name + "?__showQueryDialogue=true\">" + "filter"
						+ "</a></td>");
				out.println("</tr>");
			}
		}
		out.println("</table>");
	}

	private List<org.molgenis.model.elements.Entity> getDownloadableEntities(Database db) throws DatabaseException
	{
		return db.getMetaData().getEntities(false, false);
	}

	private List<QueryRule> createQueryRules(MolgenisRequest req, Class<? extends Entity> klazz) throws Exception
	{
		List<QueryRule> rulesList = new ArrayList<QueryRule>();

		// use get
		if (req.getRequest().getQueryString() != null)
		{
			// System.out.println("handle find query via http-get: " +
			// request.getQueryString());
			String[] ruleStrings = req.getRequest().getQueryString().split("&");

			for (String rule : ruleStrings)
			{
				String[] ruleElements = rule.split("=");

				if (ruleElements.length != 2)
				{
					// this is OK: just a not-filled-in queryrule
					// throw new Exception("cannot understand querystring " +
					// rule + ", does not have 2 elements");
				}
				else if (ruleElements[1].startsWith("["))
				{
					ruleElements[1] = ruleElements[1].replace("%20", " ");
					String[] values = ruleElements[1].substring(1, ruleElements[1].indexOf("]")).split(",");
					rulesList.add(new QueryRule(ruleElements[0], QueryRule.Operator.IN, values));
				}
				else
				{
					if (ruleElements[1] != "" && !"__submitbutton".equals(ruleElements[0])) rulesList
							.add(new QueryRule(ruleElements[0], QueryRule.Operator.EQUALS, ruleElements[1]));
				}
			}
		}
		// use post
		else
		{
			for (String name : req.getColNames())
			{
				if (klazz.newInstance().getFields().contains(name))
				{
					if (req.getString(name).startsWith("["))
					{
						String[] values = req.getString(name).substring(1, req.getString(name).indexOf("]")).split(",");
						rulesList.add(new QueryRule(name, QueryRule.Operator.IN, values));
					}
					else
					{
						rulesList.add(new QueryRule(name, QueryRule.Operator.EQUALS, req.getString(name)));
					}
				}
			}
		}
		return rulesList;
	}

	private void executeQuery(PrintWriter out, MolgenisRequest req, Database db, String entityName) throws Exception
	{

		String simpleEntityName = entityName.substring(entityName.lastIndexOf('.') + 1);
		Class<? extends Entity> klazz = db.getClassForName(simpleEntityName);

		// create query rules
		List<QueryRule> rulesList = createQueryRules(req, klazz);

		// execute query
		TupleWriter csvWriter = new CsvWriter(out);
		try
		{
			db.find(klazz, csvWriter, rulesList.toArray(new QueryRule[rulesList.size()]));
		}
		finally
		{
			csvWriter.close();
		}
	}

}
