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
import org.molgenis.io.excel.ExcelSheetReader.ExcelTuple;
import org.molgenis.util.tuple.Tuple;

public class LifeLinesQuestionnaireMatrix
{
	private static final Logger logger = Logger.getLogger(LifeLinesQuestionnaireMatrix.class);

	private static final String TABLE_PROTOCOL = "Protocol";
	private static final String HEADER_PROTOCOL = "Protocol";
	private static final String HEADER_PROTOCOL_ID = "ID";
	private static final String HEADER_PROTOCOL_DESCRIPTION = "Omschrijving";
	private static final String HEADER_VMID = "VMID";
	private static final String HEADER_VMID_ID = "ID";
	private static final String HEADER_VMID_DESCRIPTION = "Time";
	private static final String TABLE_CHECKLIST = "checklist";
	private static final String VALUE_CHECKED = "x";

	private final Map<String, Set<CohortTimePair>> questionnaireMap;
	private final Map<Integer, String> protocolMap;
	private final Map<Integer, String> vmidMap;

	public LifeLinesQuestionnaireMatrix()
	{
		questionnaireMap = new HashMap<String, Set<CohortTimePair>>();
		protocolMap = new HashMap<Integer, String>();
		vmidMap = new HashMap<Integer, String>();
	}

	public void add(String group, String code, Set<CohortTimePair> cohortTypePairs)
	{
		if (group == null) throw new IllegalArgumentException("group is null");
		if (code == null) throw new IllegalArgumentException("code is null");
		String key = (group + code).toLowerCase();
		questionnaireMap.put(key, cohortTypePairs);
	}

	public void addProtocol(Integer id, String description)
	{
		protocolMap.put(id, description.trim());
	}

	public void addVmid(Integer id, String description)
	{
		vmidMap.put(id, description.trim());
	}

	public String getProtocolDescription(Integer id)
	{
		return protocolMap.get(id);
	}

	public String getVmidDescription(Integer id)
	{
		return vmidMap.get(id);
	}

	public Set<CohortTimePair> get(String group, String code)
	{
		String key = (group + code).toLowerCase();
		return questionnaireMap.get(key);
	}

	static class CohortTimePair
	{
		private final int protocolId;
		private final int vmidId;

		public CohortTimePair(int protocolId, int vmidId)
		{
			this.protocolId = protocolId;
			this.vmidId = vmidId;
		}

		public int getProtocolId()
		{
			return protocolId;
		}

		public int getVmidId()
		{
			return vmidId;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + protocolId;
			result = prime * result + vmidId;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CohortTimePair other = (CohortTimePair) obj;
			if (protocolId != other.protocolId) return false;
			if (vmidId != other.vmidId) return false;
			return true;
		}
	}

	public static LifeLinesQuestionnaireMatrix parse(InputStream in) throws IOException
	{
		LifeLinesQuestionnaireMatrix questionnaireMatrix = new LifeLinesQuestionnaireMatrix();

		ExcelReader excelReader = new ExcelReader(in, false);
		try
		{
			parseProtocolSheet(excelReader, questionnaireMatrix);
			parseChecklistSheet(excelReader, questionnaireMatrix);
		}
		finally
		{
			excelReader.close();
		}

		return questionnaireMatrix;
	}

	private static void parseProtocolSheet(ExcelReader excelReader, LifeLinesQuestionnaireMatrix questionnaireMatrix)
			throws IOException
	{
		ExcelSheetReader protocolSheet = excelReader.getSheet(TABLE_PROTOCOL);
		try
		{
			Iterator<Tuple> it = protocolSheet.iterator();
			if (!it.hasNext()) throw new IOException("missing header row #1");
			Tuple header1 = it.next();
			String protocolHeader = header1.getString(0);
			if (protocolHeader == null || !protocolHeader.equals(HEADER_PROTOCOL)) throw new IOException(
					"missing header: " + HEADER_PROTOCOL);
			Tuple header2 = it.next();
			String protocolIdHeader = header2.getString(0);
			if (protocolIdHeader == null || !protocolIdHeader.equals(HEADER_PROTOCOL_ID)) throw new IOException(
					"missing header: " + HEADER_PROTOCOL_ID);
			String protocolDescriptionHeader = header2.getString(1);
			if (protocolDescriptionHeader == null || !protocolDescriptionHeader.equals(HEADER_PROTOCOL_DESCRIPTION)) throw new IOException(
					"missing header: " + HEADER_PROTOCOL_DESCRIPTION);

			if (!it.hasNext()) throw new IOException("missing protocol values in sheet: " + TABLE_PROTOCOL);
			while (it.hasNext())
			{
				Tuple row = it.next();
				Integer protocolId = row.getInt(0);
				String protocolDescription = row.getString(1);
				if (protocolId == null && (protocolDescription == null || protocolDescription.isEmpty())) break;
				if (protocolId == null) throw new IOException("missing protocol id in sheet: " + TABLE_PROTOCOL);
				if (protocolDescription == null) throw new IOException("missing protocol description in sheet: "
						+ TABLE_PROTOCOL);

				questionnaireMatrix.addProtocol(protocolId, protocolDescription);
			}

			if (!it.hasNext()) throw new IOException("missing header row #2");
			Tuple header3 = it.next();
			String vmidHeader = header3.getString(0);
			if (vmidHeader == null || !vmidHeader.equals(HEADER_VMID)) throw new IOException("missing header: "
					+ HEADER_VMID);
			Tuple header4 = it.next();
			String vmidIdHeader = header4.getString(0);
			if (vmidIdHeader == null || !vmidIdHeader.equals(HEADER_VMID_ID)) throw new IOException("missing header: "
					+ HEADER_VMID_ID);
			String vmidDescriptionHeader = header4.getString(1);
			if (vmidDescriptionHeader == null || !vmidDescriptionHeader.equals(HEADER_VMID_DESCRIPTION)) throw new IOException(
					"missing header: " + HEADER_VMID_DESCRIPTION);

			while (it.hasNext())
			{
				Tuple row = it.next();
				Integer vmidId = row.getInt(0);
				String vmidDescription = row.getString(1);
				if (vmidId == null && (vmidDescription == null || vmidDescription.isEmpty())) break;
				if (vmidId == null) throw new IOException("missing VMID id in sheet: " + TABLE_PROTOCOL);
				if (vmidDescription == null) throw new IOException("missing VMID description in sheet: "
						+ TABLE_PROTOCOL);

				questionnaireMatrix.addVmid(vmidId, vmidDescription);
			}
		}
		finally
		{
			protocolSheet.close();
		}
	}

	private static void parseChecklistSheet(ExcelReader excelReader, LifeLinesQuestionnaireMatrix questionnaireMatrix)
			throws IOException
	{
		ExcelSheetReader checklistSheet = excelReader.getSheet(TABLE_CHECKLIST);
		try
		{
			Iterator<Tuple> it = checklistSheet.iterator();
			int rownr = 0;
			// skip first row, retrieve cohorts using protocol id
			if (!it.hasNext()) throw new IOException("expected row, but reached end of sheet: " + TABLE_CHECKLIST);
			it.next();
			++rownr;

			if (!it.hasNext()) throw new IOException("missing header row #1");
			it.next(); // skip formids
			++rownr;
			if (!it.hasNext()) throw new IOException("missing header row #2");
			Tuple header2 = it.next();
			++rownr;
			if (!it.hasNext()) throw new IOException("missing header row #3");
			Tuple header3 = it.next();
			++rownr;

			String currentGroup = null;
			while (it.hasNext())
			{
				++rownr;

				ExcelTuple tuple = (ExcelTuple) it.next();
				if (tuple.isBold(0))
				{
					currentGroup = tuple.getString(0);
					continue;
				}

				String code = tuple.getString(0);
				if (code == null)
				{
					logger.warn("missing code on row " + rownr + " in sheet '" + TABLE_CHECKLIST + "' (group="
							+ currentGroup + ')');
					continue;
				}

				Set<CohortTimePair> cohortTypeSet = new HashSet<CohortTimePair>();
				final int cols = tuple.getNrCols();
				for (int i = 1; i < cols; ++i)
				{
					String val = tuple.getString(i);
					if (val != null && val.equalsIgnoreCase(VALUE_CHECKED))
					{
						String[] protocolIds = header2.getString(i).split(",");
						for (String protocolId : protocolIds)
							cohortTypeSet.add(new CohortTimePair(Integer.valueOf(protocolId), header3.getInt(i)));
					}
				}
				questionnaireMatrix.add(currentGroup, code, cohortTypeSet);
			}
		}
		finally
		{
			checklistSheet.close();
		}
	}
}
