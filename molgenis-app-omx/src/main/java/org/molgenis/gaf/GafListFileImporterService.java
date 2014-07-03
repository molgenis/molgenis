package org.molgenis.gaf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.gaf.GafListValidator.GafListValidationReport;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

	public String importValidatedGafList()
	{
		String dataSetIdentifier = UUID.randomUUID().toString();
		DataSet dataSet = null;
		String dataSetName = "tODO JJ";

		try
		{
			logger.info("importing gaf list runs ...");
			if (!dataService.hasRepository(dataSetName))
			{
				dataSet = createNewGafListDataSet(dataSetIdentifier);
				dataSetName = dataSet.getName();
				OmxRepository omxRepository = createNewGafListRepo(dataSetIdentifier);

				CrudRepository dataSetRepository = (CrudRepository) dataService
						.getRepositoryByEntityName(DataSet.ENTITY_NAME);
				dataSetRepository.add(dataSet);
				try
				{
					dataSetRepository.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println("lala1");
				DataSet dataSetTEmo = dataService.findOne(DataSet.ENTITY_NAME,
						new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);

				// dataService.addRepository(new AggregateableCrudRepositorySecurityDecorator(omxRepository));

				dataService.add(dataSetIdentifier, this.gafListFileRepository);

				System.out.println("start indexing");
				logger.info("start indexing");
				dataSetIndexer.indexDataSets(Arrays.asList((Object) dataSet.getId()));
				logger.info("finished indexing");
				System.out.println("finished indexing");
			}
			logger.info("finished importing valid gaf list with identifier: " + dataSetIdentifier);
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

		return dataSetName;
	}

	public OmxRepository createNewGafListRepo(String identifier)
	{
		return new OmxRepository(dataService, searchService, identifier, entityValidator);
	}

	public DataSet createNewGafListDataSet(String dataSetIdentifier)
	{
		String name = generateGafListRepoName();
		DataSet dataSet = new DataSet();
		dataSet.setIdentifier(dataSetIdentifier);
		dataSet.setName(dataSetIdentifier);
		dataSet.setProtocolUsed(getGafListProtocolUsed());
		return dataSet;
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
