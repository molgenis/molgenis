package org.molgenis.tools.compare;

import java.io.File;
import java.io.IOException;

import org.molgenis.DatabaseConfig;
import org.molgenis.EntitiesImporterImpl;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BenchmarkApp
{
	public static void main(String[] args) throws IOException, DatabaseException
	{
		System.in.read();
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(DatabaseConfig.class);
		ctx.refresh();
		try
		{
			DataService dataService = ctx.getBean(DataService.class);
			EntitiesImporterImpl dataSetImporter = new EntitiesImporterImpl(dataService);
			dataSetImporter.importEntities(new File(
					"/Users/Roan/Work/GIDS_Imported_30_august_2013/Omx_Import_CeliacSprue_metadata.xls"),
					DatabaseAction.ADD);

		}
		finally
		{
			ctx.close();
		}
		System.out.println("benchmarking finished");
	}
}
