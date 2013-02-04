package org.molgenis.framework.tupletable;

import org.molgenis.util.tuple.Tuple;

/** Methods for editing a table */
public interface EditableTupleTable extends TupleTable
{
	public void add(Tuple tuple) throws TableException;

	public void update(Tuple tuple) throws TableException;

	public void remove(Tuple tuple) throws TableException;
}
