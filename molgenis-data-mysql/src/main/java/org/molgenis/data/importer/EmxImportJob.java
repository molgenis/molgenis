package org.molgenis.data.importer;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.framework.db.EntityImportReport;

/**
 * Parameter object for the import job.
 * 
 */
public class EmxImportJob
{
	public final DatabaseAction dbAction;

	// TODO: there is some overlap between source and parsedMetaData
	public final RepositoryCollection source;
	public final ParsedMetaData parsedMetaData;
	public final ManageableCrudRepositoryCollection target;

	// TODO: there is high overlap between report and metaDataChanges
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