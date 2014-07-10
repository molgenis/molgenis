package org.molgenis.gaf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.data.AggregateableCrudRepositorySecurityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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


	public GafListValidationReport importGafList(MultipartFile csvFile, Character separator)
			throws IOException,
			ServiceException
	{
		File tmpFile = copyDataToTempFile(csvFile);
		GafListFileRepository gafListFileRepositoryToCreateReport = new GafListFileRepository(tmpFile, null, separator,
				null);
		GafListValidationReport report = gafListValidator.validate(gafListFileRepositoryToCreateReport);

		if (!report.getValidRunIds().isEmpty())
		{
			GafListFileRepository gafListFileRepositoryToImport = new GafListFileRepository(tmpFile, null, separator,
					report);

			String dataSetIdentifier = UUID.randomUUID().toString().toLowerCase();
			String dataSetName = generateGafListRepoName();
			report.setDataSetName(dataSetName);
			Object dataSetId;

			try
			{
				DataSet dataSet = new DataSet();
				dataSet.set(DataSet.NAME, dataSetName);
				dataSet.set(DataSet.IDENTIFIER, dataSetIdentifier);
				dataSet.set(DataSet.PROTOCOLUSED, getGafListProtocolUsed());
				dataService.add(DataSet.ENTITY_NAME, dataSet);
				dataSetId = dataSet.getId();

				AggregateableCrudRepositorySecurityDecorator repository = new AggregateableCrudRepositorySecurityDecorator(
						new OmxRepository(dataService, searchService, dataSetIdentifier, entityValidator));
				dataService.addRepository(repository);

				repository.flush();
				repository.clearCache();

				dataService.add(dataSetIdentifier, gafListFileRepositoryToImport);
			}
			finally
			{
				try
				{
					gafListFileRepositoryToCreateReport.close();
					gafListFileRepositoryToImport.close();
				}
				catch (IOException e)
				{
					logger.error(e);
				}
			}

			logger.debug("start indexing");
			dataSetIndexer.indexDataSets(Arrays.asList(dataSetId));
			logger.debug("finished indexing");
		}

		return report;
	}

	/**
	 * Copy the data of a multipart file to a temporary file.
	 * 
	 * @param multipart
	 * @return the representation of file and directory pathnames
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	protected File copyDataToTempFile(MultipartFile multipart) throws IllegalStateException, IOException
	{
		File upLoadedfile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator")
				+ multipart.getOriginalFilename());
		upLoadedfile.createNewFile();
		FileOutputStream fos = new FileOutputStream(upLoadedfile);
		fos.write(multipart.getBytes());
		fos.close();

		return upLoadedfile;
	}

	protected Protocol getGafListProtocolUsed()
	{
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.IDENTIFIER, PROTOCOL_IDENTIFIER_GAF_LIST), Protocol.class);

		return protocol;
	}

	protected final static String generateGafListRepoName()
	{
		return PREFIX_REPO_NAME + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
	}
}
