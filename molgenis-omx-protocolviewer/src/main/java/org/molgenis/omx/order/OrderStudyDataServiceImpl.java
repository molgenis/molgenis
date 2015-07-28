package org.molgenis.omx.order;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.omx.studymanager.OmxStudyDefinition;
import org.molgenis.study.StudyDefinition;
import org.molgenis.studymanager.StudyManagerService;
import org.molgenis.util.FileStore;
import org.molgenis.util.tuple.KeyValueTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderStudyDataServiceImpl implements OrderStudyDataService
{
	private static final Logger logger = Logger.getLogger(OrderStudyDataServiceImpl.class);

	@Autowired
	private Database database;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private StudyManagerService studyManagerService;

	@Autowired
	private org.molgenis.security.user.MolgenisUserService molgenisUserService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.order.OrderStudyDataService#orderStudyData(java.lang.String, javax.servlet.http.Part,
	 * java.lang.String, java.util.List)
	 */
	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_PROTOCOLVIEWER')")
	@Transactional(rollbackFor =
	{ DatabaseException.class, MessagingException.class, IOException.class })
	public void orderStudyData(String studyName, Part requestForm, String dataSetIdentifier, List<Integer> featureIds)
			throws DatabaseException, MessagingException, IOException
	{
		if (studyName == null) throw new IllegalArgumentException("study name is null");
		if (requestForm == null) throw new IllegalArgumentException("request form is null");
		if (featureIds == null || featureIds.isEmpty()) throw new IllegalArgumentException(
				"feature list is null or empty");

		List<ObservableFeature> features = database.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
				Operator.IN, featureIds));
		if (features == null || features.isEmpty()) throw new DatabaseException("requested features do not exist");

		DataSet dataSet = DataSet.findByIdentifier(database, dataSetIdentifier);
		MolgenisUser molgenisUser = molgenisUserService.getCurrentUser();

		String appName = getAppName();

		long timestamp = System.currentTimeMillis();
		String fileName = appName + "-request_" + timestamp + ".doc";
		File orderFile = fileStore.store(requestForm.getInputStream(), fileName);

		logger.debug("creating study data request: " + studyName);
		StudyDataRequest studyDataRequest = new StudyDataRequest();
		studyDataRequest.setIdentifier(UUID.randomUUID().toString());
		studyDataRequest.setName(studyName);
		studyDataRequest.setDataSet(dataSet);
		studyDataRequest.setFeatures(features);
		studyDataRequest.setMolgenisUser(molgenisUser);
		studyDataRequest.setRequestDate(new Date());
		studyDataRequest.setRequestStatus("pending");
		studyDataRequest.setRequestForm(orderFile.getPath());

		StudyDefinition studyDefinition = studyManagerService.persistStudyDefinition(new OmxStudyDefinition(
				studyDataRequest));
		studyDataRequest.setIdentifier(studyDefinition.getId());

		database.add(studyDataRequest);
		logger.debug("created study data request: " + studyName);

		// create excel attachment for study data request
		String variablesFileName = appName + "-request_" + timestamp + "-variables.xls";
		InputStream variablesIs = createOrderExcelAttachment(studyDataRequest, features);
		File variablesFile = fileStore.store(variablesIs, variablesFileName);

		// send order confirmation to user and admin
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(molgenisUser.getEmail());
		helper.setBcc(molgenisUserService.getSuEmailAddresses().toArray(new String[]
		{}));
		helper.setSubject("Submission confirmation from " + appName);
		helper.setText(createOrderConfirmationEmailText(studyDataRequest, appName));
		helper.addAttachment(fileName, new FileSystemResource(orderFile));
		helper.addAttachment(variablesFileName, new FileSystemResource(variablesFile));
		mailSender.send(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.order.OrderStudyDataService#getOrders()
	 */
	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_PROTOCOLVIEWER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public List<StudyDataRequest> getOrders() throws DatabaseException
	{
		List<StudyDataRequest> orderList = database.find(StudyDataRequest.class);
		return orderList != null ? orderList : Collections.<StudyDataRequest> emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.order.OrderStudyDataService#getOrder(java.lang.Integer)
	 */
	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_PROTOCOLVIEWER')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public StudyDataRequest getOrder(Integer orderId) throws DatabaseException
	{
		return StudyDataRequest.findById(database, orderId);
	}

	private String createOrderConfirmationEmailText(StudyDataRequest studyDataRequest, String appName)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Dear Researcher,\n\n");
		strBuilder.append("Thank you for submitting to ").append(appName)
				.append(", attached are the details of your submission.\n");
		strBuilder.append("The ").append(appName)
				.append(" Research Office will contact you upon receiving your application.\n\n");
		strBuilder.append("Sincerely,\n");
		strBuilder.append(appName);
		return strBuilder.toString();
	}

	private InputStream createOrderExcelAttachment(StudyDataRequest studyDataRequest, List<ObservableFeature> features)
			throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] bytes = null;
		try
		{
			List<String> header = Arrays.asList("Id", "Variable", "Description");
			ExcelWriter excelWriter = new ExcelWriter(bos);
			try
			{
				TupleWriter sheetWriter = excelWriter.createTupleWriter(studyDataRequest.getName());
				try
				{
					sheetWriter.writeColNames(header);

					for (ObservableFeature feature : features)
					{
						KeyValueTuple tuple = new KeyValueTuple();
						tuple.set(header.get(0), feature.getIdentifier());
						tuple.set(header.get(1), feature.getName());
						tuple.set(header.get(2), feature.getDescription());
						sheetWriter.write(tuple);
					}
				}
				finally
				{
					sheetWriter.close();
				}
			}
			finally
			{
				excelWriter.close();
			}
		}
		finally
		{
			bytes = bos.toByteArray();
			bos.close();
		}
		return new ByteArrayInputStream(bytes);
	}

	// TODO move to utility class
	private String getAppName()
	{
		String keyAppName = "app.name";
		String defaultKeyAppName = "MOLGENIS";
		return molgenisSettings.getProperty(keyAppName, defaultKeyAppName);
	}
}
