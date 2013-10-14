package org.molgenis.data;

import java.io.File;
import java.util.List;

import org.molgenis.io.processor.CellProcessor;

/**
 * EntitySourceFactory for tabular file base data like excel and xsc
 */
public interface FileBasedEntitySourceFactory extends EntitySourceFactory
{
	/**
	 * Supported file types
	 */
	List<String> getFileExtensions();

	/**
	 * Creates an EntitySource from a file throws a MolgenisDataException when an unsupported file type is used
	 * 
	 * @param file
	 * @return
	 * @throws MolgenisDataException
	 */
	EntitySource create(File file);

	void addCellProcessor(CellProcessor cellProcessor);
}
