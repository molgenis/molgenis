package org.molgenis.compute.ui.model;

import java.util.ArrayList;
import java.util.List;

public enum JobStatus
{
	CREATED, RUNNING, COMPLETED, FAILED;

	public static List<String> names()
	{
		JobStatus[] values = values();
		List<String> names = new ArrayList<String>(values.length);
		for (JobStatus analysisStatus : values)
			names.add(analysisStatus.toString());
		return names;
	}
}
