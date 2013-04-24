package org.molgenis.omx.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.ObservableFeature;
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
	private Login login;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MolgenisSettings molgenisSettings;

	public void orderStudyData(String studyName, Part requestForm, List<Integer> featureIds) throws DatabaseException,
			MessagingException, IOException
	{
		if (studyName == null) throw new IllegalArgumentException("study name is null");
		if (requestForm == null) throw new IllegalArgumentException("request form is null");
		if (featureIds == null || featureIds.isEmpty()) throw new IllegalArgumentException(
				"feature list is null or empty");

		List<ObservableFeature> features = database.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
				Operator.IN, featureIds));
		if (features == null || features.isEmpty()) throw new DatabaseException("requested features do not exist");

		MolgenisUser molgenisUser = database.findById(MolgenisUser.class, login.getUserId());

		File file = storeRequestForm(requestForm);

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

		// send order confirmation to user
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(molgenisUser.getEmail());
		helper.setSubject("Order confirmation from " + getAppName());
		helper.setText(createOrderConfirmationEmailText(studyDataRequest));
		helper.addAttachment(getAppName() + "-request_" + System.currentTimeMillis() + ".doc", new FileSystemResource(
				file));
		mailSender.send(message);
	}

	private File storeRequestForm(Part requestForm) throws FileNotFoundException, IOException
	{
		// TODO put file (meta-)data in database
		// note: use requestForm.getHeader("content-disposition") to get file name)
		File file = new File(System.getProperty("user.home") + "/requestform-" + System.currentTimeMillis() + ".doc");
		FileOutputStream fos = new FileOutputStream(file);
		try
		{
			IOUtils.copy(requestForm.getInputStream(), fos);
		}
		finally
		{
			fos.close();
		}
		return file;
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
