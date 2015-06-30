package org.molgenis.vortext;

import java.io.InputStream;

import org.molgenis.file.FileMeta;

public interface PublicationService
{
	/**
	 * Adds a pdf to the FileStore
	 * 
	 * @param baseUri
	 * @param filename
	 * @param size
	 * @param pdf
	 * @return
	 */
	FileMeta savePdf(String baseUri, String filename, long size, InputStream pdf);

	/**
	 * Replace the marginalia of a pdf
	 * 
	 * @param fileMetaId
	 * @param marginalia
	 */
	void saveMarginalia(String fileMetaId, Marginalia marginalia);

	/**
	 * Get all marginalia of a saved pdf
	 * 
	 * @param fileMetaId
	 * @return
	 */
	Marginalia getMarginalia(String fileMetaId);
}
