package org.molgenis.omx.order;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.Part;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.study.StudyDataRequest;

/**
 * Create and view study data requests
 */
public interface OrderStudyDataService
{
	/**
	 * Place an order for the current user
	 * 
	 * @param studyName
	 * @param requestForm
	 * @param dataSetIdentifier
	 * @param featureIds
	 * @throws DatabaseException
	 * @throws MessagingException
	 * @throws IOException
	 */
	void orderStudyData(String studyName, Part requestForm, String dataSetIdentifier, List<Integer> featureIds)
			throws MessagingException, IOException;

	/**
	 * Returns order for the current user
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	List<StudyDataRequest> getOrders();

	/**
	 * Returns the given order for the current user
	 * 
	 * @param orderId
	 * @return
	 * @throws DatabaseException
	 */
	StudyDataRequest getOrder(Integer orderId);
}