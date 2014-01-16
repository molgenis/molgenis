package org.molgenis.gaf;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.gaf.GafListValidator.GafListValidationReport;
import org.molgenis.googlespreadsheet.GoogleSpreadsheetRepository;
import org.molgenis.googlespreadsheet.GoogleSpreadsheetRepository.Visibility;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.importer.DataSetImporterService;
import org.molgenis.omx.importer.DataSetImporterServiceImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.ServiceException;

@Service
public class GafListImporterService
{
	private static final Logger logger = Logger.getLogger(GafListImporterService.class);

	private static final String KEY_GAF_LIST_VALIDATOR_EMAILS = "gafList.validator.emails";
	private static final String PROTOCOL_IDENTIFIER_GAF_LIST = "gaf_list_protocol";
	public static final String GOOGLE_SPREADSHEET_SERVICE_APP_NAME = "gcc-molgenis-1";

	public static final String GAF_SHEET_ID = "0AmTxcrav-hTbdFNhSEZxZVIxQVRwbTZ6SE5lUUIyVEE";
	public static final String GAF_WORKBOOK_ID = "6";

	@Value("${google.account.username}")
	private String googleAccountUsername;

	@Value("${google.account.password}")
	private String googleAccountPassword;

	@Autowired
	private GafListValidator gafListValidator;

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private DataSetImporterService dataSetImporterService;

	@Autowired
	private DataSetsIndexer dataSetIndexer;

	@Autowired
	private JavaMailSender mailSender;

	@Transactional(rollbackFor =
	{ IOException.class, ServiceException.class, ValueConverterException.class, MessagingException.class })
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public void importGafListAsSuperuser() throws IOException, ServiceException, ValueConverterException,
			MessagingException
	{
		importGafList();
	}

	@Transactional(rollbackFor =
	{ IOException.class, ServiceException.class, ValueConverterException.class, MessagingException.class })
	@RunAsSystem
	public void importGafListAsSystemUser() throws IOException, ServiceException, ValueConverterException,
			MessagingException
	{
		importGafList();
	}

	private void importGafList() throws IOException, ServiceException, ValueConverterException, MessagingException
	{
		if (googleAccountUsername == null || googleAccountPassword == null)
		{
			throw new IllegalArgumentException(
					"missing required google.account.username and google.account.password (please specify in molgenis-server.properties");
		}

		logger.info("running scheduled gaf list import task ...");
		String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());

		SpreadsheetService service = new SpreadsheetService(GOOGLE_SPREADSHEET_SERVICE_APP_NAME);
		service.setUserCredentials(googleAccountUsername, googleAccountPassword);

		GafListValidationReport report;
		GoogleSpreadsheetRepository spreadsheetRepository = new GoogleSpreadsheetRepository(service, GAF_SHEET_ID,
				GAF_WORKBOOK_ID, Visibility.PRIVATE);
		try
		{
			logger.debug("validating gaf list ...");
			report = gafListValidator.validate(spreadsheetRepository);
			logger.debug("finished validating gaf list");
		}
		finally
		{
			spreadsheetRepository.close();
		}

		logger.debug(report);
		logger.debug("emailing gaf list validation report ...");
		String subject, text;
		if (report.hasErrors())
		{
			subject = "GAF list validation report - " + dateStr + " [ERRORS]";
			text = report.toString();
		}
		else
		{
			subject = "GAF list validation report - " + dateStr + " [SUCCESS]";
			text = "GAF list validation completed successfully.";
		}
		String emailsStr = molgenisSettings.getProperty(KEY_GAF_LIST_VALIDATOR_EMAILS);
		if (emailsStr == null) throw new IllegalArgumentException("missing required setting "
				+ KEY_GAF_LIST_VALIDATOR_EMAILS);

		// send validation report to admins
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(StringUtils.split(emailsStr, ','));
		helper.setSubject(subject);
		helper.setText(text);
		mailSender.send(message);
		logger.debug("finished emailing gaf list validation report");

		Integer dataSetId;
		GafListRepository gafListRepository = new GafListRepository(service, GAF_SHEET_ID, GAF_WORKBOOK_ID,
				Visibility.PRIVATE, report);

		try
		{
			logger.debug("importing valid gaf list runs ...");
			String dataSetIdentifier = UUID.randomUUID().toString();

			Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME,
					new QueryImpl().eq(Protocol.IDENTIFIER, PROTOCOL_IDENTIFIER_GAF_LIST));
			DataSet dataSet = new DataSet();
			dataSet.setIdentifier(dataSetIdentifier);
			dataSet.setName("GAF list " + dateStr);
			dataSet.setProtocolUsed(protocol);
			dataService.add(DataSet.ENTITY_NAME, dataSet);
			dataSetId = dataSet.getId();

			dataSetImporterService.importSheet(gafListRepository, DataSetImporterServiceImpl.DATASET_SHEET_PREFIX
					+ dataSetIdentifier);
			logger.debug("finished importing valid gaf list runs");
		}
		finally
		{
			spreadsheetRepository.close();
		}
		logger.debug("indexing new gaf list data set ...");
		dataSetIndexer.indexDataSets(Arrays.asList(dataSetId));
		logger.debug("finished indexing new gaf list data set");

		logger.info("finished scheduled gaf list import task");
	}
}
