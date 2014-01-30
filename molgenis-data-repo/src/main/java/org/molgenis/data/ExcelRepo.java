package org.molgenis.data;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.excel.ExcelEntitySource;
import org.molgenis.data.support.AbstractRepo;

public class ExcelRepo extends AbstractRepo implements Repo
{
	public ExcelRepo(File f, String entityName) throws IOException
	{
		//???? close error?
		repository = new ExcelEntitySource(f).getRepositoryByEntityName(entityName);
	}
}
