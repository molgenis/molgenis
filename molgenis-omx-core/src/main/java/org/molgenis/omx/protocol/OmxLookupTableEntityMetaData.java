package org.molgenis.omx.protocol;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.INT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.support.AbstractEntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;

import com.google.common.collect.Lists;

public class OmxLookupTableEntityMetaData extends AbstractEntityMetaData
{
	private final ObservableFeature categoricalFeature;

	public OmxLookupTableEntityMetaData(ObservableFeature categoricalFeature)
	{
		if (categoricalFeature == null) throw new IllegalArgumentException("categoricalFeature is null");
		this.categoricalFeature = categoricalFeature;
	}

	@Override
	public String getName()
	{
		return categoricalFeature.getIdentifier() + "-LUT"; // yes, Identifier
	}

	@Override
	public String getLabel()
	{
		return categoricalFeature.getName() + " lookup table"; // yes, Name
	}

	@Override
	public String getDescription()
	{
		return "Lookup table for: " + categoricalFeature.getDescription();
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		List<AttributeMetaData> attributes = Lists.newArrayList();

		DefaultAttributeMetaData id = new DefaultAttributeMetaData("id", INT);
		id.setLabel("id");
		id.setDescription("automatically generated internal id, only for internal use.");
		id.setIdAttribute(true);
		id.setNillable(false);
		id.setReadOnly(true);
		id.setAuto(true);
		id.setVisible(false);
		attributes.add(id);

		DefaultAttributeMetaData identifier = new DefaultAttributeMetaData("Identifier", STRING);
		identifier.setLabel("Identifier");
		identifier
				.setDescription("user supplied or automatically assigned (using a decorator) unique and short identifier, e.g. MA1234");
		identifier.setNillable(false);
		identifier.setUnique(true);
		identifier.setLookupAttribute(true);
		attributes.add(identifier);

		DefaultAttributeMetaData name = new DefaultAttributeMetaData("Name", STRING);
		name.setLabel("Name");
		name.setDescription("human readible name, not necessary unique.");
		name.setNillable(false);
		name.setLookupAttribute(true);
		name.setLabelAttribute(true);
		attributes.add(name);

		DefaultAttributeMetaData description = new DefaultAttributeMetaData("description", TEXT);
		description.setLabel("description");
		description
				.setDescription("(Optional) Rudimentary meta data about the observable feature. Use of ontology       terms references to establish unambigious descriptions is recommended");
		description.setLookupAttribute(true);
		attributes.add(description);

		DefaultAttributeMetaData observableFeature = new DefaultAttributeMetaData("observableFeature", XREF);
		observableFeature.setLabel("observableFeature");
		observableFeature.setDescription("The Measurement these permitted values are part of.");
		observableFeature.setNillable(false);
		observableFeature.setRefEntity(new org.molgenis.omx.observ.ObservableFeatureMetaData());
		attributes.add(observableFeature);

		DefaultAttributeMetaData valueCode = new DefaultAttributeMetaData("valueCode", STRING);
		valueCode.setLabel("valueCode");
		attributes.add(valueCode);

		DefaultAttributeMetaData definition = new DefaultAttributeMetaData("definition", XREF);
		definition.setLabel("definition");
		definition.setDescription("The category that is being measured in a specific way.");
		definition.setRefEntity(new org.molgenis.omx.observ.target.OntologyTermMetaData());
		attributes.add(definition);

		DefaultAttributeMetaData isMissing = new DefaultAttributeMetaData("isMissing", BOOL);
		isMissing.setDefaultValue(false);
		isMissing.setLabel("isMissing");
		isMissing.setDescription("whether this value should be treated as missing value.");
		isMissing.setNillable(false);
		attributes.add(isMissing);

		return attributes;
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return Category.class;
	}
}
