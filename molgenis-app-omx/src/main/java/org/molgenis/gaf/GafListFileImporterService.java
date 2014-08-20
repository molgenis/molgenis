package org.molgenis.gaf;

import java.io.File;
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
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gdata.util.ServiceException;

@Service
public class GafListFileImporterService
{
	private static final Logger logger = Logger.getLogger(GafListFileImporterService.class);
	private static final String PREFIX_REPO_NAME = "GAF ";

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

	@Autowired
	FileStore fileStore;

	public GafListValidationReport validateGAFList(GafListValidationReport report, MultipartFile csvFile)
			throws IOException, ServiceException, Exception
	{
		report.uploadCsvFile(csvFile);
		GafListFileRepository repo = new GafListFileRepository(report.getTempFile(), null, null, null);
		gafListValidator.validate(report, repo);
		repo.close();
		return report;
	}
	
	public void importGAFList(GafListValidationReport report,
 String key_gaf_list_protocol_name) throws IOException,
			ServiceException
	{
		File tmpFile = fileStore.getFile(report.getTempFileName());

		if (!report.getValidRunIds().isEmpty())
		{
			GafListFileRepository gafListFileRepositoryToImport = new GafListFileRepository(tmpFile, null, null,
					report);

			String dataSetIdentifier = UUID.randomUUID().toString().toLowerCase();
			String dataSetName = generateGafListRepoName();
			report.setDataSetName(dataSetName);
			report.setDataSetIdentifier(dataSetIdentifier);
			Object dataSetId;

			try
			{
				DataSet dataSet = new DataSet();
				dataSet.set(DataSet.NAME, dataSetName);
				dataSet.set(DataSet.IDENTIFIER, dataSetIdentifier);
				dataSet.set(DataSet.PROTOCOLUSED,
						getGafListProtocolUsed(molgenisSettings.getProperty(key_gaf_list_protocol_name)));
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
	}

	protected Protocol getGafListProtocolUsed(String protocolName)
	{
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.IDENTIFIER, protocolName), Protocol.class);

		return protocol;
	}

	protected final static String generateGafListRepoName()
	{
		return PREFIX_REPO_NAME + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
	}
}
