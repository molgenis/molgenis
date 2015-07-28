package org.molgenis.omx.datasetdeleter;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.DataSet;

public interface DataSetDeleterService
{
	DataSet delete(String dataSetIdentifier, boolean deleteMetaData) throws DatabaseException;
}