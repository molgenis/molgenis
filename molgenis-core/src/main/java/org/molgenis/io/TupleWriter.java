package org.molgenis.io;

import java.io.Closeable;
import java.io.IOException;

import org.molgenis.io.processor.CellProcessor;
import org.molgenis.util.tuple.Tuple;

public interface TupleWriter extends Closeable
{
	/**
	 * write column names header
	 * 
	 * @param tuple
	 * @throws IOException
	 */
	public void writeColNames(Iterable<String> colNames) throws IOException;

	/**
	 * write row of values
	 * 
	 * @param tuple
	 * @throws IOException
	 */
	public void write(Tuple tuple) throws IOException;

	/**
	 * Add a cell processor to process cell values
	 * 
	 * @param cellProcessor
	 */
	public void addCellProcessor(CellProcessor cellProcessor);
}
