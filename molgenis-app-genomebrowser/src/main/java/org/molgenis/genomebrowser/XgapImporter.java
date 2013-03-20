package org.molgenis.genomebrowser;

import java.io.IOException;
import java.net.MalformedURLException;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.ExampleXgapGeneImporter;

public class XgapImporter
{
	public static void main(String[] args) throws DatabaseException, MalformedURLException, IOException
	{
		//Database db = new JpaDatabase(em, model)
		ExampleXgapGeneImporter importer = new ExampleXgapGeneImporter();
		//importer.importXgap(db, new URL("http://molgenis38.target.rug.nl:8080/xqtl_panacea/api/find/"));
		
	}
}
