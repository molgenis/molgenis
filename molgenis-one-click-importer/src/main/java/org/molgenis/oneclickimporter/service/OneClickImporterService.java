package org.molgenis.oneclickimporter.service;

import com.sun.prism.impl.QueuedPixelSource;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.oneclickimporter.model.DataCollection;

import java.util.List;

public interface OneClickImporterService
{
	/**
	 * Generate {@link DataCollection} with sheet contents
	 *
	 * @param dataCollectionName
	 * @param sheet
	 */
	DataCollection buildDataCollection(String dataCollectionName, Sheet sheet);

	/**
	 * Gues the datatype of the given list.
	 *
	 * The data type is based on the lowest commmen datatype of the given list of values.
	 *
	 * Of example is the list is composed of a set of Date's and a single String the data type will be String
	 *
	 * The type hierarchy use is:  DateTime, Date, Boolean, Int, Long, Decimal, Hyperlink, String, Text
	 *
	 * @param
	 * @return
	 */
	AttributeType guessAttributeType(List<Object> dataValues);
}
