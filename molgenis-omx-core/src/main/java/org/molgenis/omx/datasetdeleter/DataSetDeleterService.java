package org.molgenis.omx.datasetdeleter;

public interface DataSetDeleterService
{
	/**
	 * Deletes data set data (DataSet, ObervationSets, ObservedValues, Values) and optionally data set meta data
	 * (Protocol, Category, ObservableFeature). Data set meta data is only deleted is no references exist to the meta
	 * data from other data sets.
	 * 
	 * @param dataSetIdentifier
	 * @param deleteMetadata
	 *            whether or not to try to delete data set meta data
	 * @return name of the deleted data set
	 */
	String deleteData(String dataSetIdentifier, boolean deleteMetadata);
}