package org.molgenis.io;

import java.io.Closeable;
import java.io.IOException;

public interface TableWriter extends Closeable
{
	public TupleWriter createTupleWriter(String tableName) throws IOException;
}
