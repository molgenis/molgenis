package org.molgenis.file.ingest.execution;

import java.io.File;

/**
 * Downloads a file and stores it in the FileStore
 */
public interface FileStoreDownload
{
	File downloadFile(String url, String folderName, String fileName);
}
