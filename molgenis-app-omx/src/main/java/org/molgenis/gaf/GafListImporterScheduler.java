package org.molgenis.gaf;

import java.io.IOException;

import javax.mail.MessagingException;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.converters.ValueConverterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gdata.util.ServiceException;

@Component
public class GafListImporterScheduler
{
	private static final String KEY_GAF_LIST_IMPORTER_ENABLE = "gafList.importer.enable";

	private final GafListImporterService gafListImporterService;
	private final MolgenisSettings molgenisSettings;

	@Autowired
	public GafListImporterScheduler(GafListImporterService gafListImporterService, MolgenisSettings molgenisSettings)
	{
		if (gafListImporterService == null) throw new IllegalArgumentException("GafListImporterService is null");
		if (molgenisSettings == null) throw new IllegalArgumentException("MolgenisSettings is null");
		this.gafListImporterService = gafListImporterService;
		this.molgenisSettings = molgenisSettings;
	}

	@Scheduled(cron = "0 0 4 * * ?")
	public void importGafList() throws IOException, ServiceException, ValueConverterException, MessagingException
	{
		// flag that enables/disables gaf list importer
		String enableStr = molgenisSettings.getProperty(KEY_GAF_LIST_IMPORTER_ENABLE);
		if (enableStr != null && enableStr.equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			gafListImporterService.importGafListAsSystemUser();
		}
	}
}
