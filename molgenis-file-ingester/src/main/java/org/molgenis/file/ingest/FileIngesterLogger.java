package org.molgenis.file.ingest;

import java.io.File;

import org.molgenis.data.Entity;
import org.molgenis.framework.db.EntityImportReport;

/**
 * Create and update FileIngestJobMetaData entries
 */
public interface FileIngesterLogger
{
	String start(Entity fileIngest);

	void downloadFinished(File file, String contentType);

	void success(EntityImportReport report);

	void failure(Exception e);
}
