package org.molgenis.lifelines.hl7;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.hl7.ANY;
import org.molgenis.hl7.BL;
import org.molgenis.hl7.CD;
import org.molgenis.hl7.CO;
import org.molgenis.hl7.INT;
import org.molgenis.hl7.PQ;
import org.molgenis.hl7.REAL;
import org.molgenis.hl7.ST;
import org.molgenis.hl7.TS;

/**
 * Maps HL7v3 data types to OMX data types
 */
public class HL7DataTypeMapper
{
	private static final Map<Class<? extends ANY>, String> dataTypeMap;

	static
	{
		dataTypeMap = new HashMap<Class<? extends ANY>, String>();
		dataTypeMap.put(INT.class, "int");
		dataTypeMap.put(ST.class, "string");
		dataTypeMap.put(CO.class, "code"); // or categorical?
		dataTypeMap.put(CD.class, "code"); // or categorical?
		dataTypeMap.put(PQ.class, "decimal");
		dataTypeMap.put(TS.class, "datetime");
		dataTypeMap.put(REAL.class, "decimal");
		dataTypeMap.put(BL.class, "bool");
	}

	public static String get(Class<? extends ANY> clazz)
	{
		return dataTypeMap.get(clazz);
	}

	public static String get(ANY any)
	{
		return get(any.getClass());
	}
}
