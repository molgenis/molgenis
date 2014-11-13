package org.molgenis.data.importer;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.framework.db.EntityImportReport;

public class EmxImportJob
{
	public DatabaseAction dbAction;
	public RepositoryCollection source;
	public ParsedMetaData parsedMetaData;
	public ManageableCrudRepositoryCollection target;
	public final EntityImportReport report = new EntityImportReport();
	public final MetaDataChanges metaDataChanges = new MetaDataChanges();

	public EmxImportJob(DatabaseAction dbAction, RepositoryCollection source, ParsedMetaData parsedMetaData,
			ManageableCrudRepositoryCollection target)
	{
		this.dbAction = dbAction;
		this.source = source;
		this.parsedMetaData = parsedMetaData;
		this.target = target;
	}
}