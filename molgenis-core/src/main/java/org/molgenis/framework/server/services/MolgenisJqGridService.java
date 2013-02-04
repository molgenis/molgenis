package org.molgenis.framework.server.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.util.Entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/** Service to serve entities for jqGrid */
public class MolgenisJqGridService implements MolgenisService
{
	Logger logger = Logger.getLogger(MolgenisJqGridService.class);

	public MolgenisJqGridService(MolgenisContext mc)
	{
	}

	/**
	 * Handle use of the XREF API.
	 * 
	 * 
	 * @param request
	 * @param response
	 */
	@Override
	public void handleRequest(MolgenisRequest req, MolgenisResponse res) throws ParseException, DatabaseException,
			IOException
	{
		HttpServletResponse response = res.getResponse();

		try
		{
			/****** GET PARAMETERS ******/

			// Get the requested page. By default grid sets this to 1.
			Integer page = req.getInt("page");
			if (page == null || page < 1) page = 1;

			// get how many rows we want to have into the grid - rowNum
			// parameter in the grid
			Integer limit = req.getInt("rows");
			if (limit == null || limit < 0) limit = 10;

			// get index row - i.e. user click to sort. At first time sortname
			// parameter -
			// after that the index from colModel
			String sortIndex = req.getString("sidx");

			boolean sortAsc = "asc".equals(req.getString("sord")) ? true : false;

			// sorting order - at first time sortorder
			// $sord = $_GET['sord'];

			// get entityClass, e.g. 'org.molgenis.pheno.Measurement'
			if (req.isNull("entity"))
			{
				throw new Exception("required parameter 'entity' is missing");
			}
			Class<? extends Entity> entityClass = req.getDatabase().getClassForName(req.getString("entity"));

			/****** RETRIEVE DATA ******/

			// create a query on entity
			Database db = req.getDatabase();
			Query<?> q = db.query(entityClass);

			// search?
			if (!req.isNull("filter") && !"".equals(req.getString("filter")))
			{
				q.search(req.getString("filter").trim());
			}

			// filtered count
			int recordCount = q.count();

			// calculate the total pages for the query
			int total_pages = 1;
			if (recordCount > 0 && limit > 0)
			{
				total_pages = ((int) (recordCount / (double) limit)) + 1;
			}

			// if for some reasons the requested page is greater than the total
			// set the requested page to total page
			if (page > total_pages) page = total_pages;

			// todo: implement filters
			q.limit(limit);
			q.offset(page * limit - limit);

			// sorting
			if (sortAsc) q.sortASC(sortIndex);
			else q.sortDESC(sortIndex);

			List<? extends Entity> result = q.find();

			// {
			// "total": "xxx",
			// "page": "yyy",
			// "records": "zzz",
			// "rows" : [
			// {"id" :"1", "cell" :["cell11", "cell12", "cell13"]},
			// {"id" :"2", "cell":["cell21", "cell22", "cell23"]},
			// ...
			// ]
			// }

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("page", page);
			jsonObject.addProperty("total", total_pages);
			jsonObject.addProperty("records", recordCount);

			JsonArray jsonRows = new JsonArray();
			for (Entity e : result)
			{
				JsonObject jsonValues = new JsonObject();
				for (String field : e.getFields())
				{
					if (e.get(field) != null) jsonValues.addProperty(field, e.get(field).toString());
					else jsonValues.addProperty(field, "");
				}
				jsonRows.add(jsonValues);
			}

			jsonObject.add("rows", jsonRows);
			String json = jsonObject.toString();
			logger.debug(json);

			// write out
			response.setHeader("Cache-Control", "max-age=0"); // allow no client
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			System.out.println("json\n" + json);
			out.print(json);
			out.close();
		}
		catch (Exception e)
		{
			PrintWriter out = response.getWriter();
			response.getWriter().print("{exception: '" + e.getMessage() + "'}");
			out.close();
			e.printStackTrace();
			throw new DatabaseException(e);
		}
	}
}
