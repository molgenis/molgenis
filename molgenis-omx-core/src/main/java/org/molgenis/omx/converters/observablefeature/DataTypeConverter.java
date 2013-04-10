package org.molgenis.omx.converters.observablefeature;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.fieldtypes.BoolField;
import org.molgenis.fieldtypes.CategoricalType;
import org.molgenis.fieldtypes.DateField;
import org.molgenis.fieldtypes.DatetimeField;
import org.molgenis.fieldtypes.DecimalField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.XrefField;

/**
 * Convert an ObservableFeature datatype to a molgenis FieldType
 * 
 * @author erwin
 * 
 */
public class DataTypeConverter
{
	private static Map<String, FieldType> molgenisFieldTypesByFeatureDataType = new HashMap<String, FieldType>();
	static
	{
		molgenisFieldTypesByFeatureDataType.put("xref", new XrefField());
		molgenisFieldTypesByFeatureDataType.put("string", new StringField());
		molgenisFieldTypesByFeatureDataType.put("categorical", new CategoricalType());
		molgenisFieldTypesByFeatureDataType.put("nominal", new StringField());
		molgenisFieldTypesByFeatureDataType.put("ordinal", new StringField());
		molgenisFieldTypesByFeatureDataType.put("date", new DateField());
		molgenisFieldTypesByFeatureDataType.put("datetime", new DatetimeField());
		molgenisFieldTypesByFeatureDataType.put("int", new IntField());
		molgenisFieldTypesByFeatureDataType.put("code", new StringField());
		molgenisFieldTypesByFeatureDataType.put("image", new StringField());
		molgenisFieldTypesByFeatureDataType.put("decimal", new DecimalField());
		molgenisFieldTypesByFeatureDataType.put("bool", new BoolField());
		molgenisFieldTypesByFeatureDataType.put("file", new StringField());
		molgenisFieldTypesByFeatureDataType.put("log", new StringField());
		molgenisFieldTypesByFeatureDataType.put("data", new StringField());
		molgenisFieldTypesByFeatureDataType.put("exe", new StringField());
	}

	public static FieldType getMolgenisFieldType(String featureDataType)
	{
		FieldType fieldType = molgenisFieldTypesByFeatureDataType.get(featureDataType);
		if (fieldType == null)
		{
			throw new IllegalArgumentException("Unsupported dataType [" + featureDataType + "]");
		}

		return fieldType;
	}
}
