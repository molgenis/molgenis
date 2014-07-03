package org.molgenis.gaf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.molgenis.data.AggregateableCrudRepositorySecurityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.gaf.GafListValidator.GafListValidationReport;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.search.SearchService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.gdata.util.ServiceException;

@Service
public class GafListFileImporterService
{
	private static final Logger logger = Logger.getLogger(GafListFileImporterService.class);

	private static final String PROTOCOL_IDENTIFIER_GAF_LIST = "gaf_list_protocol";
	private static final String PREFIX_REPO_NAME = "GAF list ";

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private GafListValidator gafListValidator;

	@Autowired
	private DataSetsIndexer dataSetIndexer;
	
	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private EntityValidator entityValidator;

	private GafListFileRepository gafListFileRepository;
	private GafListValidationReport report = null;

	public void createCsvRepository(MultipartFile csvFile, Character separator)
	{
		try
		{
			File tmpFile = copyDataToTempFile(csvFile);
			this.gafListFileRepository = new GafListFileRepository(tmpFile, null, separator, report);
		}
		catch (Exception e)
		{
			logger.error(e);
		}
	}

	/**
	 * Copy the data of a multipart file to a temporary file.
	 * 
	 * @param multipart
	 * @return the representation of file and directory pathnames
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public File copyDataToTempFile(MultipartFile multipart) throws IllegalStateException, IOException
	{
		File upLoadedfile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator")
				+ multipart.getOriginalFilename());
		upLoadedfile.createNewFile();
		FileOutputStream fos = new FileOutputStream(upLoadedfile);
		fos.write(multipart.getBytes());
		fos.close();

		return upLoadedfile;
	}

	public void createValidationReport()
	{
		try
		{
			this.report = gafListValidator.validate(this.gafListFileRepository);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public Boolean hasValidationError()
	{
		if (null == report) return null;
		return this.report.hasErrors();
	}

	public String getValidationReportHtml()
	{
		if (null == report) return "No validation report available";
		return report.toStringHtml();
	}

	@Transactional(rollbackFor =
	{ IOException.class, ServiceException.class, ValueConverterException.class, MessagingException.class })
	@RunAsSystem
	public String importValidatedGafList()
	{
		String dataSetIdentifier = UUID.randomUUID().toString();
		String dataSetName = generateGafListRepoName();
		Object dataSetId;

		try
		{
			DataSet dataSet = new DataSet();
			dataSet.set(DataSet.NAME, dataSetName);
			dataSet.set(DataSet.IDENTIFIER, dataSetIdentifier);
			dataSet.set(DataSet.PROTOCOLUSED, getGafListProtocolUsed());
			dataService.add(DataSet.ENTITY_NAME, dataSet);
			dataSetId = dataSet.getId();

			OmxRepository omxRepository = new OmxRepository(dataService, searchService, dataSetIdentifier,
					entityValidator);

			System.out.println("dataService.getEntityMetaData(dataSetIdentifier).getName(): " + dataService.getEntityMetaData(dataSetIdentifier).getName());
			System.out.println("dataService.getEntityMetaData(dataSetName).getIdAttribute().getName()"
					+ dataService.getEntityMetaData(dataSetName).getIdAttribute().getName());

			dataService.addRepository(new AggregateableCrudRepositorySecurityDecorator(omxRepository));

			System.out.println("omxRepository.getName(): " + omxRepository.getName());

			dataService.add(dataSetIdentifier, this.gafListFileRepository);
		}
		finally
		{
			try
			{
				this.gafListFileRepository.close();
			}
			catch (IOException e)
			{
				logger.error(e);
			}
		}

		logger.info("start indexing");
		dataSetIndexer.indexDataSets(Arrays.asList(dataSetId));
		logger.info("finished indexing");

		return dataSetName;
	}

	public Protocol getGafListProtocolUsed()
	{
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.IDENTIFIER, PROTOCOL_IDENTIFIER_GAF_LIST), Protocol.class);

		return protocol;
	}

	public final static String generateGafListRepoName()
	{
		return PREFIX_REPO_NAME + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
	}
}
