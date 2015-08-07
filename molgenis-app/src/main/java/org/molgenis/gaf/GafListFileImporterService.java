package org.molgenis.gaf;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.file.FileStore;
import org.molgenis.gaf.settings.GafListSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GafListFileImporterService
{
	private static final Logger LOG = LoggerFactory.getLogger(GafListFileImporterService.class);

	private final GafListSettings gafListSettings;
	private final GafListValidator gafListValidator;
	private final DataService dataService;
	private final FileStore fileStore;

	@Autowired
	public GafListFileImporterService(GafListSettings gafListSettings, GafListValidator gafListValidator,
			DataService dataService, FileStore fileStore)
	{
		this.gafListSettings = checkNotNull(gafListSettings);
		this.gafListValidator = checkNotNull(gafListValidator);
		this.dataService = checkNotNull(dataService);
		this.fileStore = checkNotNull(fileStore);
	}

	public GafListValidationReport validateGAFList(GafListValidationReport report, MultipartFile csvFile)
			throws IOException, Exception
	{
		report.uploadCsvFile(csvFile);
		GafListFileRepository repo = new GafListFileRepository(report.getTempFile(), null, null, null);
		gafListValidator.validate(report, repo, GafListValidator.COLUMNS);
		repo.close();
		return report;
	}

	public void importGAFList(GafListValidationReport report, String key_gaf_list_protocol_name) throws IOException
	{
		String gaflistEntityName = gafListSettings.getEntityName();
		if (gaflistEntityName == null)
		{
			throw new MolgenisDataException("Please configure target entity");
		}
		File tmpFile = fileStore.getFile(report.getTempFileName());

		if (!report.getValidRunIds().isEmpty())
		{

			GafListFileRepository gafListFileRepositoryToImport = new GafListFileRepository(tmpFile, null, null,
					report);
			report.setDataSetName(gaflistEntityName);
			report.setDataSetIdentifier(gaflistEntityName);

			try
			{
				Repository writableRepository = dataService.getRepository(gaflistEntityName);
				for (Entity entity : gafListFileRepositoryToImport)
				{
					writableRepository.add(entity);
				}
			}
			finally
			{
				try
				{
					gafListFileRepositoryToImport.close();
				}
				catch (IOException e)
				{
					LOG.error("Error while importing " + key_gaf_list_protocol_name, e);
				}
			}
		}
	}
}
