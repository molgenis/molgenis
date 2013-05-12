package org.molgenis.omx.service;

import java.io.File;
import java.io.IOException;
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
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = WebApplicationContext.SCOPE_REQUEST)
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

		String fileName = getAppName() + "-request_" + System.currentTimeMillis() + ".doc";
		File file = fileStore.store(requestForm.getInputStream(), fileName);

		StudyDataRequest studyDataRequest = new StudyDataRequest();
		studyDataRequest.setIdentifier(UUID.randomUUID().toString());
		studyDataRequest.setName(studyName);
		studyDataRequest.setFeatures(features);
		studyDataRequest.setMolgenisUser(molgenisUser);
		studyDataRequest.setRequestDate(new Date());
		studyDataRequest.setRequestStatus("pending");
		studyDataRequest.setRequestForm(file.getPath());

		logger.debug("create study data request: " + studyName);
		database.add(studyDataRequest);

		// send order confirmation to user and admin
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(molgenisUser.getEmail());
		helper.setBcc(MolgenisUserService.getInstance(database).findAdminEmail());
		helper.setSubject("Order confirmation from " + getAppName());
		helper.setText(createOrderConfirmationEmailText(studyDataRequest));
		helper.addAttachment(getAppName() + "-request_" + System.currentTimeMillis() + ".doc", new FileSystemResource(
				file));
		mailSender.send(message);
	}

	public List<StudyDataRequest> getOrders() throws DatabaseException
	{
		List<StudyDataRequest> orderList = database.find(StudyDataRequest.class);
		return orderList != null ? orderList : Collections.<StudyDataRequest> emptyList();
	}

	public List<StudyDataRequest> getOrders(Integer userId) throws DatabaseException
	{
		List<StudyDataRequest> orderList = database.find(StudyDataRequest.class, new QueryRule(
				StudyDataRequest.MOLGENISUSER, Operator.EQUALS, userId));
		return orderList != null ? orderList : Collections.<StudyDataRequest> emptyList();
	}

	private String createOrderConfirmationEmailText(StudyDataRequest studyDataRequest)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("TODO: ORDER CONFIRMATION EMAIL HERE");
		return strBuilder.toString();
	}

	// TODO move to utility class
	private String getAppName()
	{
		String keyAppName = "app.name";
		String defaultKeyAppName = "MOLGENIS";
		return molgenisSettings.getProperty(keyAppName, defaultKeyAppName);
	}
}
