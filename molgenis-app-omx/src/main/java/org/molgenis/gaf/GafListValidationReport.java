package org.molgenis.gaf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GafListValidationReport
{
	private final Map<String, List<GafListValidationError>> validationErrors;
	private final List<String> validRunIds = new ArrayList<String>();
	private final List<String> invalidRunIds = new ArrayList<String>();
	private List<String> allRunIds = new ArrayList<String>();
	private String dataSetName = null;

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
}
