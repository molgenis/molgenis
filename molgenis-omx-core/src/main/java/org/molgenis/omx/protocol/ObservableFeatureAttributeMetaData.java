package org.molgenis.omx.protocol;

import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.omx.observ.CharacteristicMetaData;
import org.molgenis.omx.observ.ObservableFeature;

public class ObservableFeatureAttributeMetaData implements AttributeMetaData
{
	private final ObservableFeature observableFeature;

	public ObservableFeatureAttributeMetaData(ObservableFeature observableFeature)
	{
		if (observableFeature == null) throw new IllegalArgumentException("ObservableFeature is null");
		this.observableFeature = observableFeature;
	}

	@Override
	public String getName()
	{
		return observableFeature.getIdentifier(); // yes, getIdentifier and not getName
	}

	@Override
	public String getLabel()
	{
		return observableFeature.getName(); // yes, getName
	}

	@Override
	public String getDescription()
	{
		return observableFeature.getDescription();
	}

	@Override
	public FieldType getDataType()
	{
		return MolgenisFieldTypes.getType(observableFeature.getDataType().toLowerCase());
	}

	@Override
	public boolean isNillable()
	{
		return true;
	}

	@Override
	public boolean isReadonly()
	{
		return false;
	}

	@Override
	public boolean isUnique()
	{
		return false;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

	@Override
	public Object getDefaultValue()
	{
		return null;
	}

	@Override
	public boolean isIdAtrribute()
	{
		return false;
	}

	@Override
	public boolean isLabelAttribute()
	{
		return false;
	}

	@Override
	public EntityMetaData getRefEntity()
	{
		FieldTypeEnum dataType = getDataType().getEnumType();
		if (dataType == FieldTypeEnum.XREF || dataType == FieldTypeEnum.MREF)
		{
			return new CharacteristicMetaData();
		}
		else if (dataType == FieldTypeEnum.CATEGORICAL)
		{
			return new OmxLookupTableEntityMetaData(observableFeature);
		}
		return null;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributeParts()
	{
		return null;
	}

	@Override
	public boolean isAuto()
	{
		return false;
	}

	@Override
	public boolean isLookupAttribute()
	{
		return false;
	}

	@Override
	public boolean isAggregateable()
	{
		return observableFeature.getDataType().equalsIgnoreCase("BOOL")
				|| observableFeature.getDataType().equalsIgnoreCase("XREF")
				|| observableFeature.getDataType().equalsIgnoreCase("CATEGORICAL");
	}

	@Override
	public Range getRange()
	{
		if (this.observableFeature.getMinRange() != null && this.observableFeature.getMaxRange() != null)
		{
			return new Range(this.observableFeature.getMinRange(), this.observableFeature.getMaxRange());
		}
		return null;
	}

	@Override
	public List<String> getEnumOptions()
	{
		return null;
	}

	@Override
	public boolean isSameAs(AttributeMetaData attributeMetaData)
	{
		return false;
	}
}
