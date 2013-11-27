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
	 * @param catalogId
	 * @param featureIds
	 * @throws MessagingException
	 * @throws IOException
	 */
	void orderStudyData(String studyName, Part requestForm, String catalogId, List<Integer> featureIds)
			throws MessagingException, IOException;

	/**
	 * Returns order for the current user
	 * 
	 * @return
	 */
	List<StudyDataRequest> getOrders();

	/**
	 * Returns the given order for the current user
	 * 
	 * @param orderId
	 * @return
	 */
	StudyDataRequest getOrder(Integer orderId);
}