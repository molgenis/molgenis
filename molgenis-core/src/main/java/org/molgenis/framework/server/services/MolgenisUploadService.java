package org.molgenis.framework.server.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.AuthStatus;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.framework.server.MolgenisServiceAuthenticationHelper;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.util.Entity;

public class MolgenisUploadService implements MolgenisService
{
	private static final Logger logger = Logger.getLogger(MolgenisDownloadService.class);

	/** the name of the datatype input */
	public static final String INPUT_DATATYPE = "data_type_input";
	/** the name of the data input */
	public static final String INPUT_DATA = "data_input";
	/** boolean indicating file upload */
	public static final String INPUT_FILE = "data_file";
	/** the name of the submit button */
	public static final String INPUT_SUBMIT = "submit_input";
	/** the action input */
	public static final String INPUT_ACTION = "data_action";
	/** indicating wether uploads should return added data */
	public static final String INPUT_SILENT = "data_silent";

	public MolgenisUploadService(MolgenisContext mc)
	{
	}

	@Override
	public void handleRequest(MolgenisRequest req, MolgenisResponse res) throws ParseException, DatabaseException,
			IOException
	{
		logger.info("starting upload " + req.getRequest().getPathInfo());
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
				// if no type selected: show data type choice
				if (req.getString(INPUT_DATATYPE) == null)
				{
					out.println("<html><body>");
					authStatus.getPrintMe();
					if (req.getDatabase().getLogin().isAuthenticated())
					{
						out.println(MolgenisServiceAuthenticationHelper.displayLogoutForm());
					}
					showDataTypeChoice(out, req);
					out.println("</body></html>");
				}
				// if no data provided, show csv input form
				else if (req.get(INPUT_DATA) == null && req.get(INPUT_FILE) == null)
				{
					out.println("<html><body>");
					authStatus.getPrintMe();
					if (req.getDatabase().getLogin().isAuthenticated())
					{
						out.println(MolgenisServiceAuthenticationHelper.displayLogoutForm());
					}
					showCsvInputForm(out, req);
					out.println("</body></html>");
				}
				// process request
				else
				{
					processRequest(req, out);
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

		logger.info("servlet took: " + (System.currentTimeMillis() - start_time) + " ms");
		logger.info("------------");

	}

	private void showDataTypeChoice(PrintWriter out, MolgenisRequest req)
	{
		out.println("<form method=\"post\" enctype=\"multipart/form-data\">");
		out.println("<h1>Data upload (step 1)</h1>");
		out.println("Choose your data type.");
		out.println("<table><tr><td><label>Data type:</label></td><td><select name=\"" + INPUT_DATATYPE + "\">");

		for (Class<? extends Entity> c : req.getDatabase().getEntityClasses())
		{
			// write to screen
			out.println("<option value=\"" + c.getName() + "\">" + c.getName() + "</option>");
		}
		out.println("</select></td></tr>");
		out.println("<tr><td>&nbsp;</td><td><input type=\"submit\" name=\"" + INPUT_SUBMIT
				+ "\" value=\"Submit\"></td></tr>");
		out.println("</table></form>");
	}

	private void showCsvInputForm(PrintWriter out, MolgenisRequest req) throws InstantiationException,
			IllegalAccessException
	{
		// String clazzName =
		req.getString(INPUT_DATATYPE);

		out.println("<html><body><form method=\"post\" enctype=\"multipart/form-data\">");
		out.println("<h1>Data upload (step 2)</h1>");
		out.println("Enter your data as CSV.");

	}

	private void processRequest(MolgenisRequest req, PrintWriter out) throws Exception
	{
		NumberFormat formatter = NumberFormat.getInstance(Locale.US);
		logger.info("processing add/update/delete");
		Class<? extends Entity> entityClass = null;

		String clazzName = req.getString(INPUT_DATATYPE);
		String simpleEntityName = clazzName.substring(clazzName.lastIndexOf('.') + 1);
		entityClass = req.getDatabase().getClassForName(simpleEntityName);

		String action = req.getString(INPUT_ACTION);

		// create a database
		Database db = req.getDatabase();

		CsvReader csvReader;
		if (req.get(INPUT_DATA) != null)
		{
			csvReader = new CsvReader(new StringReader(req.getString(INPUT_DATA)));
		}
		else if (req.get(INPUT_FILE) != null)
		{
			csvReader = new CsvReader(req.getFile(INPUT_FILE));
		}
		else
			csvReader = null;

		try
		{
			int nRowsChanged = 0;
			if (action.equals("ADD"))
			{
				File temp = File.createTempFile("molgenis", "tab");
				CsvWriter csvWriter = new CsvWriter(temp);
				try
				{
					if (req.get(INPUT_SILENT) != null && req.getBoolean(INPUT_SILENT) == true)
					{
						csvWriter.close();
						csvWriter = null;
					}

					if (req.get(INPUT_DATA) != null)
					{
						logger.info("processing textarea upload...");
						nRowsChanged = db.add(entityClass, csvReader, csvWriter);
					}
					else if (req.get(INPUT_FILE) != null)
					{
						logger.info("processing file upload...");
						nRowsChanged = db.add(entityClass, csvReader, csvWriter);
					}
					else
					{
						logger.error("no input data or input file provided.");
						out.print("ERROR: no input data or input file provided.");
					}
					out.print("Uploaded " + formatter.format(nRowsChanged) + " rows of "
							+ entityClass.getCanonicalName() + "\n");
				}
				finally
				{
					if (csvWriter != null) csvWriter.close();
				}
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(temp),
						Charset.forName("UTF-8")));
				try
				{
					String line = null;
					while ((line = reader.readLine()) != null)
					{
						out.println(line);
					}
				}
				finally
				{
					IOUtils.closeQuietly(reader);
				}
				boolean ok = temp.delete();
				if (!ok) logger.warn("failed to delete file: " + temp);
			}
			else if (action.equals("UPDATE"))
			{
				if (req.get(INPUT_DATA) != null)
				{
					nRowsChanged = db.update(entityClass, csvReader);
					out.print("Updated " + formatter.format(nRowsChanged) + " rows of "
							+ entityClass.getCanonicalName() + "\n");
				}
				else if (req.get(INPUT_FILE) != null)
				{
					nRowsChanged = db.update(entityClass, csvReader);
					out.print("Updated " + formatter.format(nRowsChanged) + " rows of "
							+ entityClass.getCanonicalName() + "\n");
				}
			}
			else if (action.equals("REMOVE"))
			{
				if (req.get(INPUT_DATA) != null)
				{
					nRowsChanged = db.remove(entityClass, csvReader);
					out.print("Removed " + formatter.format(nRowsChanged) + " rows of "
							+ entityClass.getCanonicalName() + "\n");
				}
				else if (req.get(INPUT_FILE) != null)
				{
					nRowsChanged = db.remove(entityClass, csvReader);
					out.print("Removed " + formatter.format(nRowsChanged) + " rows of "
							+ entityClass.getCanonicalName() + "\n");
				}
			}
			else
			{
				throw new Exception("Unknown action " + action);
			}
		}
		finally
		{
			if (csvReader != null) csvReader.close();
		}
	}
}
