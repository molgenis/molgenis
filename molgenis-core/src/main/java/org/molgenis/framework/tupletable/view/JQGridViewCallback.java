package org.molgenis.framework.tupletable.view;

import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.tupletable.TupleTable;

public interface JQGridViewCallback
{
	void beforeLoadConfig(MolgenisRequest request, TupleTable tupleTable);
}
