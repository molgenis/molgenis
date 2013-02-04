package org.molgenis.framework.tupletable.view.renderers;

import java.io.IOException;
import java.io.OutputStream;

import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;

public abstract class AbstractExporter
{
	protected final TupleTable tupleTable;

	public AbstractExporter(TupleTable tupleTable)
	{
		if (tupleTable == null) throw new IllegalArgumentException();
		this.tupleTable = tupleTable;
	}

	public abstract void export(OutputStream os) throws IOException, TableException;
}
