package org.molgenis.omicsconnect.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;

public class UploadFile implements MolgenisService
{
	/**
	 * File upload service. Callable by Curl, RCurl, and other post services.
	 * Currently used to images, expandable to all other files.
	 */

	private MolgenisContext mc;

	public UploadFile(MolgenisContext mc)
	{
		this.mc = mc;
	}

	@Override
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws ParseException,
			DatabaseException, IOException
	{

		PrintWriter out = response.getResponse().getWriter();
		response.getResponse().setContentType("text/plain");
		Database db = null;

		try
		{
			db = request.getDatabase();

			String fileName = request.getString("name"); // the 'real' file name
			String fileType = request.getString("type"); // file type, must
															// correspond to a
															// subclass of
															// MolgenisFile
			File fileContent = request.getFile("file"); // has a tmp name, so
														// content only

			HashMap<String, String> extraFields = new HashMap<String, String>();

			for (String colName : request.getColNames())
			{
				if (!colName.equals("name") && !colName.equals("type") && !colName.equals("file"))
				{
					extraFields.put(colName, request.getString(colName));
				}
			}

			PerformUpload.doUpload(db, true, fileName, fileType, fileContent, extraFields, false);

			out.println("upload successful");

		}
		catch (Exception e)
		{
			out.println("Upload failed.");
			out.println("Usage: see https://stat.ethz.ch/pipermail/bioconductor/2010-March/032550.html");
			out.println("With minimal arguments: 'name', 'type', 'file', where:");
			out.println("\tname = The file name plus extension to be put in the database.");
			out.println("\ttype = The type of file, use 'MolgenisFile' for minimal upload. Corresponds to a subclass of MolgenisFile. Other examples: 'RScript', 'InvestigationFile', 'BinaryDataMatrix'.");
			out.println("\tfile = The file (location) you wish to upload the contents of.");
			out.println();
			out.println("Example for RCurl when uploading an 'InvestigationFile':");
			out.println("library(\"bitops\", lib.loc=\"/Users/joerivandervelde/libs\")");
			out.println("library(\"RCurl\", lib.loc=\"/Users/joerivandervelde/libs\")");
			out.println("uri <- \"http://mydomain.org/xqtl/uploadfile\");");
			out.println("postForm(uri,");
			out.println("\tname = \"harry.png\",");
			out.println("\tInvestigation_name = \"ClusterDemo\",");
			out.println("\ttype = \"InvestigationFile\",");
			out.println("\tfile = fileUpload(filename = \"usr/home/danny/harry.png\"),");
			out.println("\tstyle = \"HTTPPOST\"");
			out.println(")");
			out.println();
			out.println("Example for commandline Curl when uploading an 'InvestigationFile':");
			out.println("curl -F \"file=@/pictures/mypicture.jpg\" -F \"name=mypicture.jpg\" -F \"Investigation_name=ClusterDemo\" -F \"type=InvestigationFile\" http://mydomain.org/xqtl/uploadfile");
			out.println();
			e.printStackTrace(out);
		}
		finally
		{
			out.close();
		}
	}
}
