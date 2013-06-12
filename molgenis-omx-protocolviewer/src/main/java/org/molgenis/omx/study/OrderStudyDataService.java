package org.molgenis.omx.study;

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
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.FileStore;
import org.molgenis.util.tuple.KeyValueTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class OrderStudyDataService
{
	private static Logger logger = Logger.getLogger(OrderStudyDataService.class);

	@Autowired
	private Database database;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private FileStore fileStore;

	@Autowired(required = false)
	private StudyDefinitionService studyDefinitionService;

	public void orderStudyData(String studyName, Part requestForm, List<Integer> featureIds, Integer userId)
			throws DatabaseException, MessagingException, IOException
	{
		if (studyName == null) throw new IllegalArgumentException("study name is null");
		if (requestForm == null) throw new IllegalArgumentException("request form is null");
		if (featureIds == null || featureIds.isEmpty()) throw new IllegalArgumentException(
				"feature list is null or empty");

		List<ObservableFeature> features = database.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
				Operator.IN, featureIds));
		if (features == null || features.isEmpty()) throw new DatabaseException("requested features do not exist");

		MolgenisUser molgenisUser = database.findById(MolgenisUser.class, userId);

		String appName = getAppName();

		long timestamp = System.currentTimeMillis();
		String fileName = appName + "-request_" + timestamp + ".doc";
		File orderFile = fileStore.store(requestForm.getInputStream(), fileName);

		StudyDataRequest studyDataRequest = new StudyDataRequest();
		studyDataRequest.setIdentifier(UUID.randomUUID().toString());
		studyDataRequest.setName(studyName);
		studyDataRequest.setFeatures(features);
		studyDataRequest.setMolgenisUser(molgenisUser);
		studyDataRequest.setRequestDate(new Date());
		studyDataRequest.setRequestStatus("pending");
		studyDataRequest.setRequestForm(orderFile.getPath());

		logger.debug("create study data request: " + studyName);
		database.add(studyDataRequest);

		if (studyDefinitionService != null)
		{
			studyDefinitionService.persistStudyDefinition(new OmxStudyDefinition(studyDataRequest));
		}

		// create excel attachment for study data request
		String variablesFileName = appName + "-request_" + timestamp + "-variables.xls";
		InputStream variablesIs = createOrderExcelAttachment(studyDataRequest, features);
		File variablesFile = fileStore.store(variablesIs, variablesFileName);

		// send order confirmation to user and admin
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(molgenisUser.getEmail());
		helper.setBcc(MolgenisUserService.getInstance(database).findAdminEmail());
		helper.setSubject("Order confirmation from " + appName);
		helper.setText(createOrderConfirmationEmailText(studyDataRequest, appName));
		helper.addAttachment(fileName, new FileSystemResource(orderFile));
		helper.addAttachment(variablesFileName, new FileSystemResource(variablesFile));
		mailSender.send(message);
	}

	public List<StudyDataRequest> getOrders() throws DatabaseException
	{
		List<StudyDataRequest> orderList = database.find(StudyDataRequest.class);
		return orderList != null ? orderList : Collections.<StudyDataRequest> emptyList();
	}

	public StudyDataRequest getOrder(Integer orderId) throws DatabaseException
	{
		return StudyDataRequest.findById(database, orderId);
	}

	public List<StudyDataRequest> getOrders(Integer userId) throws DatabaseException
	{
		List<StudyDataRequest> orderList = database.find(StudyDataRequest.class, new QueryRule(
				StudyDataRequest.MOLGENISUSER, Operator.EQUALS, userId));
		return orderList != null ? orderList : Collections.<StudyDataRequest> emptyList();
	}

	private String createOrderConfirmationEmailText(StudyDataRequest studyDataRequest, String appName)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Dear Researcher,\n\n");
		strBuilder.append("Thank you for ordering at ").append(appName)
				.append(", attached are the details of your order.\n");
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
