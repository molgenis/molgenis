package org.molgenis.gaf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
			strBuilder.append("<table class=\"table\">").append('\n');
			strBuilder.append("<tr><th>Row</th><th>Column</th><th>Value</th><th>Message</th></tr>").append('\n');
			for (GafListValidationError validationError : reportEntry.getValue())
			{
				strBuilder.append(validationError.toStringHtml()).append('\n');
			}
			strBuilder.append("</table>").append('\n');
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

	public void setTempFileName(String tempFileName)
	{
		this.tempFileName = tempFileName;
	}

	public String getTempFileOriginalName()
	{
		return tempFileOriginalName;
	}

	public void setTempFileOriginalName(String tempFileOriginalName)
	{
		this.tempFileOriginalName = tempFileOriginalName;
	}

	@PreDestroy
	public void cleanUp() throws Exception
	{
		String fileName = this.getTempFileName();
		if (null != fileName && !fileStore.delete(fileName)) logger.error("File " + this.getTempFileName()
				+ " cannot be deleted from filestore!");

		this.validationErrors.clear();
		this.validRunIds.clear();
		this.invalidRunIds.clear();
		this.allRunIds.clear();
		this.dataSetName = null;
		this.tempFileName = null;
		this.tempFileOriginalName = null;
	}
}
