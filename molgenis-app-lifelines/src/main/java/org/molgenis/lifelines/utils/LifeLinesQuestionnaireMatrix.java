package org.molgenis.lifelines.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

public class LifeLinesQuestionnaireMatrix
{
	private static final Logger logger = Logger.getLogger(LifeLinesQuestionnaireMatrix.class);

	private static final String TABLE_CHECKLIST = "checklist";
	private static final String HEADER_GROUP = "Group";
	private static final String HEADER_CODE = "Code";
	private static final String VALUE_CHECKED = "x";

	private final Map<String, Set<CohortTimePair>> questionnaireMap;

	public LifeLinesQuestionnaireMatrix()
	{
		questionnaireMap = new HashMap<String, Set<CohortTimePair>>();
	}

	public void add(String group, String code, Set<CohortTimePair> cohortTypePairs)
	{
		if (group == null) throw new IllegalArgumentException("group is null");
		if (code == null) throw new IllegalArgumentException("code is null");
		questionnaireMap.put(group + code, cohortTypePairs);
	}

	public Set<CohortTimePair> get(String group, String code)
	{
		return questionnaireMap.get(group + code);
	}

	static class CohortTimePair
	{
		private final String cohort;
		private final String time;

		public CohortTimePair(String cohort, String time)
		{
			if (cohort == null) throw new IllegalArgumentException("cohort is null");
			if (time == null) throw new IllegalArgumentException("time is null");
			this.cohort = cohort;
			this.time = time;
		}

		public String getCohort()
		{
			return cohort;
		}

		public String getTime()
		{
			return time;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cohort == null) ? 0 : cohort.hashCode());
			result = prime * result + ((time == null) ? 0 : time.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CohortTimePair other = (CohortTimePair) obj;
			if (cohort == null)
			{
				if (other.cohort != null) return false;
			}
			else if (!cohort.equals(other.cohort)) return false;
			if (time == null)
			{
				if (other.time != null) return false;
			}
			else if (!time.equals(other.time)) return false;
			return true;
		}
	}

	public static LifeLinesQuestionnaireMatrix parse(InputStream in) throws IOException
	{
		LifeLinesQuestionnaireMatrix questionnaireMatrix = new LifeLinesQuestionnaireMatrix();

		ExcelReader excelReader = new ExcelReader(in, false);
		try
		{
			ExcelSheetReader checklistSheet = excelReader.getSheet(TABLE_CHECKLIST);
			try
			{
				Iterator<Tuple> it = checklistSheet.iterator();
				if (!it.hasNext()) throw new IOException("missing header row #1");
				Tuple header1 = it.next();
				if (!it.hasNext()) throw new IOException("missing header row #2");
				Tuple header2 = it.next();
				if (!it.hasNext()) throw new IOException("missing header row #3");
				it.next(); // skip row containing time details

				if (!it.hasNext()) throw new IOException("missing header row #4");
				it.next(); // skip row containing questionnaire names
				if (!it.hasNext()) throw new IOException("missing header row #5");
				Tuple header5 = it.next();

				String groupHeader = header5.getString(0);
				if (groupHeader == null || !groupHeader.equals(HEADER_GROUP)) throw new IOException("missing header: "
						+ HEADER_GROUP);
				String codeHeader = header5.getString(1);
				if (codeHeader == null || !codeHeader.equals(HEADER_CODE)) throw new IOException("missing header: "
						+ HEADER_CODE);

				while (it.hasNext())
				{
					Tuple tuple = it.next();
					String group = tuple.getString(0);
					String code = tuple.getString(1);
					if (group == null || code == null)
					{
						logger.warn("missing group or code");
						continue;
					}

					Set<CohortTimePair> cohortTypeSet = new HashSet<CohortTimePair>();
					final int cols = tuple.getNrCols();
					for (int i = 2; i < cols; ++i)
					{
						String val = tuple.getString(i);
						if (val != null && val.equals(VALUE_CHECKED))
						{
							cohortTypeSet.add(new CohortTimePair(header1.getString(i), header2.getString(i)));
						}
					}
					questionnaireMatrix.add(group, code, cohortTypeSet);
				}
			}
			finally
			{
				checklistSheet.close();
			}
		}
		finally
		{
			excelReader.close();
		}

		return questionnaireMatrix;
	}
}
