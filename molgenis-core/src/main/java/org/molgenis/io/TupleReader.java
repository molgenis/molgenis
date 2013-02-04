package org.molgenis.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.molgenis.io.processor.CellProcessor;
import org.molgenis.util.tuple.Tuple;

/**
 * Interface for reading all rows of a table
 */
public interface TupleReader extends Iterable<Tuple>, Closeable
{
	/**
	 * Returns whether tuples have corresponding column names
	 * 
	 * @return
	 */
	public boolean hasColNames();

	/**
	 * Returns an iterator over the corresponding column names for this tuple
	 * 
	 * @return
	 */
	public Iterator<String> colNamesIterator() throws IOException;

	/**
	 * Add a cell processor to process cell values
	 * 
	 * @param cellProcessor
	 */
	public void addCellProcessor(CellProcessor cellProcessor);
}
