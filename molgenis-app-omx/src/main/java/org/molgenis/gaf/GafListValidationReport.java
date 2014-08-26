package org.molgenis.gaf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Scope("session")
public class GafListValidationReport
{
	private static final Logger logger = Logger.getLogger(GafListValidationReport.class);
	private final Map<String, List<GafListValidationError>> validationErrors;
	private final List<String> validRunIds = new ArrayList<String>();
	private final List<String> invalidRunIds = new ArrayList<String>();
	private List<String> allRunIds = new ArrayList<String>();
	private String dataSetName = null;
	private String dataSetIdentifier = null;
	private File tempFile;
	private String tempFileName;
	private String tempFileOriginalName;

	@Autowired
	FileStore fileStore;

	public GafListValidationReport()
	{
		validationErrors = new LinkedHashMap<String, List<GafListValidationError>>();
	}

	public void addEntry(String runId, GafListValidationError validationError)
	{
		List<GafListValidationError> runEntries = validationErrors.get(runId);
		if (runEntries == null)
		{
			runEntries = new ArrayList<GafListValidationError>();
			validationErrors.put(runId, runEntries);
		}
		runEntries.add(validationError);
	}

	public Map<String, List<GafListValidationError>> getEntries()
	{
		return Collections.unmodifiableMap(validationErrors);
	}

	public boolean hasErrors()
	{
		return !validationErrors.isEmpty();
	}

	public boolean hasErrors(String runId)
	{
		return validationErrors.containsKey(runId);
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();
		for (Entry<String, List<GafListValidationError>> reportEntry : validationErrors.entrySet())
		{
			String runId = reportEntry.getKey();
			if (runId == null) runId = "<undefined>";
			strBuilder.append("Validation errors for run ").append(runId).append('\n');
			for (GafListValidationError validationError : reportEntry.getValue())
			{
				strBuilder.append('\t').append(validationError).append('\n');
			}
		}
		return strBuilder.toString();
	}

	public String toStringHtml()
	{
		StringBuilder strBuilder = new StringBuilder();
		for (Entry<String, List<GafListValidationError>> reportEntry : validationErrors.entrySet())
		{
			String runId = reportEntry.getKey();
			if (runId == null) runId = "NO RUN ID!";
			strBuilder.append("Run: ").append(runId).append('\n');
			strBuilder.append("<div class=\"molgenis-table-container\" id=\"table-container\">").append(
					'\n');
			strBuilder.append(
"<table class=\"table molgenis-table table-striped table-bordered table-hover table-condensed listtable\">")
					.append('\n');
			strBuilder.append("<thead><tr><th>Row</th><th>Column</th><th>Value</th><th>Message</th></tr></thead>")
					.append('\n')
			.append("<tbody>");
			for (GafListValidationError validationError : reportEntry.getValue())
			{
				strBuilder.append(validationError.toStringHtml()).append('\n');
			}
			strBuilder.append("</tbody>");
			strBuilder.append("</table>").append('\n');
			strBuilder.append("</div>").append('\n');
		}
		return strBuilder.toString();
	}

	/**
	 * @return the validatedRunIds
	 */
	public List<String> getAllRunIds()
	{
		return allRunIds;
	}

	/**
	 * @return the validRunIds
	 */
	public List<String> getValidRunIds()
	{
		return validRunIds;
	}

	/**
	 * @return the invalidRunIds
	 */
	public List<String> getInvalidRunIds()
	{
		return invalidRunIds;
	}

	public void populateStatusImportedRuns()
	{
		for (String runId : allRunIds)
		{
			if (this.hasErrors(runId))
			{
				if (!invalidRunIds.contains(runId)) invalidRunIds.add(runId);
			}
			else
			{
				if (!validRunIds.contains(runId)) validRunIds.add(runId);
			}
		}
	}

	/**
	 * @return the dataSetName
	 */
	public String getDataSetName()
	{
		return dataSetName;
	}

	/**
	 * @param dataSetName
	 *            the dataSetName to set
	 */
	public void setDataSetName(String dataSetName)
	{
		this.dataSetName = dataSetName;
	}

	public String getTempFileName()
	{
		return tempFileName;
	}

	protected void setTempFileName(String tempFileName)
	{
		this.tempFileName = tempFileName;
	}

	public String getTempFileOriginalName()
	{
		return tempFileOriginalName;
	}

	protected void setTempFileOriginalName(String tempFileOriginalName)
	{
		this.tempFileOriginalName = tempFileOriginalName;
	}

	@PreDestroy
	public void cleanUp() throws Exception
	{
		String fileName = this.getTempFileName();
		if (null != fileName)
		{
			boolean deleted = fileStore.delete(fileName);
			if (!deleted)
			{
				logger.error("File " + this.getTempFileName() + " cannot be deleted from filestore!");
			}
		}

		this.validationErrors.clear();
		this.validRunIds.clear();
		this.invalidRunIds.clear();
		this.allRunIds.clear();
		this.dataSetName = null;
		this.dataSetIdentifier = null;
		this.tempFile = null;
		this.tempFileName = null;
		this.tempFileOriginalName = null;
	}

	public File getTempFile()
	{
		return tempFile;
	}

	protected void setTempFile(File tempFile)
	{
		this.tempFile = tempFile;
	}

	public void uploadCsvFile(MultipartFile csvFile) throws Exception
	{
		this.cleanUp();
		String fileName = UUID.randomUUID().toString().toLowerCase() + csvFile.getOriginalFilename();
		File tmpFile = fileStore.store(csvFile.getInputStream(), fileName);
		this.setTempFileName(tmpFile.getName());
		this.setTempFileOriginalName(csvFile.getOriginalFilename());
		this.setTempFile(tmpFile);
	}

	public String getDataSetIdentifier()
	{
		return dataSetIdentifier;
	}

	public void setDataSetIdentifier(String dataSetIdentifier)
	{
		this.dataSetIdentifier = dataSetIdentifier;
	}
}

