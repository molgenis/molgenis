package org.molgenis.data.importer.emx;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ParsedMetaData;

/**
 * Parameter object for the import job.
 */
public class EmxImportJob
{
	final DatabaseAction dbAction;

	// TODO: there is some overlap between source and parsedMetaData
	public final RepositoryCollection source;
	final ParsedMetaData parsedMetaData;

	public final EntityImportReport report = new EntityImportReport();
	public final String packageId;

	EmxImportJob(DatabaseAction dbAction, RepositoryCollection source, ParsedMetaData parsedMetaData, String packageId)
	{
		this.dbAction = dbAction;
		this.source = source;
		this.parsedMetaData = parsedMetaData;
		this.packageId = packageId;
	}

	RepositoryCollection getSource()
	{
		return source;
	}

	ParsedMetaData getParsedMetaData()
	{
		return parsedMetaData;
	}
}