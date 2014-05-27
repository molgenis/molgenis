package org.molgenis.gaf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.gaf.GafListValidator.GafListValidationReport;
import org.molgenis.googlespreadsheet.GoogleSpreadsheetRepository.Visibility;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.search.DataSetsIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GafListFileImporterService
{
	private static final Logger logger = Logger.getLogger(GafListFileImporterService.class);

	private static final String PROTOCOL_IDENTIFIER_GAF_LIST = "gaf_list_protocol";

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private GafListValidator gafListValidator;

	@Autowired
	private DataSetsIndexer dataSetIndexer;
	
	@Autowired
	private DataService dataService;

	private CsvRepository csvRepo;
	private GafListValidationReport report;

	public void createCsvRepo(MultipartFile csvFile, Character separator)
	{
		try
		{
			File tmpFile = multipartToFile(csvFile);
			this.setCSVRepository(tmpFile, separator);
		}
		catch (Exception e)
		{
			logger.error(e);
		}
	}

	public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException
	{
		File upLoadedfile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator")
				+ multipart.getOriginalFilename());
		upLoadedfile.createNewFile();
		FileOutputStream fos = new FileOutputStream(upLoadedfile);
		fos.write(multipart.getBytes());
		fos.close(); // setting the value of fileUploaded variable

		return upLoadedfile;
	}

	public void setCSVRepository(File f, Character separator)
	{
		this.csvRepo = new CsvRepository(f, null, separator);
	}

	public void createValidationReport()
	{
		try
		{
			this.report = gafListValidator.validate(this.csvRepo);
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

	public void importValidatedGafList()
	{
		Object dataSetId;
//		GafListRepository gafListRepository = new GafListRepository(service, GAF_SHEET_ID, GAF_WORKBOOK_ID,
//				Visibility.PRIVATE, report);
		try
		{
			logger.debug("importing valid gaf list runs ...");
			String dataSetIdentifier = UUID.randomUUID().toString();

			Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME,
					new QueryImpl().eq(Protocol.IDENTIFIER, PROTOCOL_IDENTIFIER_GAF_LIST), Protocol.class);
			DataSet dataSet = new DataSet();
			dataSet.setIdentifier(dataSetIdentifier);
			String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
			dataSet.setName("GAF list " + dateStr);
			dataSet.setProtocolUsed(protocol);
			dataService.add(DataSet.ENTITY_NAME, dataSet);
//			dataService.add(dataSetIdentifier, this.csvRepo);
			dataSetId = dataSet.getId();
			logger.debug("finished importing valid gaf list runs");
		}
		finally
		{
			try
			{
				this.csvRepo.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		logger.debug("indexing new gaf list data set ...");
		
		dataSetIndexer.indexDataSets(Arrays.asList(dataSetId));
		logger.debug("finished indexing new gaf list data set");

		logger.info("finished scheduled gaf list import task");
	}
}
