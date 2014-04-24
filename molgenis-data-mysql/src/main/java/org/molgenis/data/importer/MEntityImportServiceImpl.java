package org.molgenis.data.importer;

import java.io.IOException;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MEntityImportServiceImpl implements MEntityImportService
{
	MysqlRepositoryCollection store;

	public MEntityImportServiceImpl()
	{
		System.out.println("MEntityImportServiceImpl created");
	}

	@Autowired
	public void setRepositoryCollection(MysqlRepositoryCollection coll)
	{
		this.store = coll;
		System.out.println("MEntityImportServiceImpl created with coll=" + coll);
	}

	public EntityImportReport doImport(RepositoryCollection repositories, DatabaseAction databaseAction) throws IOException
	{
        System.out.println("MEntityImportServiceImpl.doImport");
		return null;
	}

}
