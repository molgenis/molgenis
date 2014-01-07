package org.molgenis.omx.datasetdeleter;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.DataSet;

public interface DataSetDeleterService
{
	String deleteData(String dataSetIdentifier);
	boolean deleteMetadata(String dataSetIdentifier);
}