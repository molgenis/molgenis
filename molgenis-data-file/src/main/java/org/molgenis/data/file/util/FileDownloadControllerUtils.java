package org.molgenis.data.file.util;

/**
 * Workaround to replace molgenis-data-rest dependency on molgenis-core-ui with molgenis-data-file. Remove once file
 * upload/download has its own API and REST API only deal with file metadata references.
 *
 * @deprecated use FileDownloadController.URI in molgenis-core-ui
 */
@Deprecated
public class FileDownloadControllerUtils
{
	public static final String URI = "/files";

	private FileDownloadControllerUtils()
	{
	}
}
