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

/** Service to serve entities for datatable */
public class MolgenisDataTableService implements MolgenisService
{
	Logger logger = Logger.getLogger(MolgenisDataTableService.class);

	// private MolgenisContext mc;

	public MolgenisDataTableService(MolgenisContext mc)
	{
		// this.mc = mc;
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
			response.setHeader("Cache-Control", "max-age=0"); // allow no client
			response.setContentType("application/json");

			if (req.isNull("entity"))
			{
				throw new Exception("required parameter 'entity' is missing");
			}

			// get parameters
			Class<? extends Entity> entityClass = req.getDatabase().getClassForName(req.getString("entity"));

			// iDisplayLenght = limit
			int iDisplayLength = 10;
			if (!req.isNull("iDisplayLength"))
			{
				iDisplayLength = req.getInt("iDisplayLength");
			}

			// iDisplayStart = offset
			int iDisplayStart = 0;
			if (!req.isNull("iDisplayStart"))
			{
				iDisplayStart = req.getInt("iDisplayStart");
			}

			// create a query on entity
			Database db = req.getDatabase();
			Query<?> q = db.query(entityClass);

			// sorting
			// iSortCol_0 = sort column number
			// resolve using mDataProp_1='name'
			String sortField = req.getString("mDataProp_" + req.getString("iSortCol_0"));
			boolean asc = "asc".equals(req.getString("sSortDir_0")) ? true : false;
			if (asc) q.sortASC(sortField);
			else q.sortDESC(sortField);

			// iTotalRecords is unfiltered count!
			int count = q.count();

			// sSearch = filtering string
			if (!"".equals(req.getString("sSearch")))
			{
				q.search(req.getString("sSearch"));
			}

			// filtered count
			int filteredCount = q.count();

			// iTotalDisplayRecords is filtered count
			// TODO implement filters
			q.offset(iDisplayStart);
			q.limit(iDisplayLength);
			List<? extends Entity> result = q.find();

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("sEcho", req.getString("sEcho"));
			jsonObject.addProperty("iTotalRecords", count);
			jsonObject.addProperty("iTotalDisplayRecords", filteredCount);

			JsonArray jsonArray = new JsonArray();
			for (Entity e : result)
			{
				JsonObject jsonValues = new JsonObject();
				for (String field : e.getFields())
				{
					if (e.get(field) != null) jsonValues.addProperty(field, e.get(field).toString());
					else jsonValues.addProperty(field, "");
				}
				jsonArray.add(jsonValues);
			}

			jsonObject.add("aaData", jsonArray);
			String json = jsonObject.toString();
			logger.debug(json);

			// write out
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
