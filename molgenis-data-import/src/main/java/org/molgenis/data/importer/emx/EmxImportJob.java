package org.molgenis.data.importer.emx;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.ParsedMetaData;
import org.molgenis.framework.db.EntityImportReport;

/**
 * Parameter object for the import job.
 */
public class EmxImportJob
{
	public final DatabaseAction dbAction;

	// TODO: there is some overlap between source and parsedMetaData
	public final RepositoryCollection source;
	public final ParsedMetaData parsedMetaData;

	// TODO: there is high overlap between report and metaDataChanges
	public final EntityImportReport report = new EntityImportReport();
	public final MetaDataChanges metaDataChanges = new MetaDataChanges();
	public String defaultPackage;

	public EmxImportJob(DatabaseAction dbAction, RepositoryCollection source, ParsedMetaData parsedMetaData,
			String defaultPackage)
	{
		this.dbAction = dbAction;
		this.source = source;
		this.parsedMetaData = parsedMetaData;
		this.defaultPackage = defaultPackage;
	}
}