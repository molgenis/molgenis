package org.molgenis.dataexplorer.controller;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class DataExplorerSettingsMeta extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = DataExplorerController.ID + "_settings";

	public static final String ID = "id";
	public static final String MOD = "mods";
	public static final String MOD_AGGREGATES = "mod_aggregates";
	public static final String MOD_ANNOTATORS = "mod_annotators";
	public static final String MOD_CHARTS = "mod_charts";
	public static final String MOD_DATA = "mod_data";
	public static final String MOD_DISEASEMATCHER = "mod_diseasematcher";

	public DataExplorerSettingsMeta()
	{
		super(ENTITY_NAME);
		setLabel("Data Explorer plugin settings");

		addAttribute(ID).setIdAttribute(true).setDataType(STRING).setNillable(false).setLabel("Id").setVisible(false);

		DefaultAttributeMetaData modAggregatesAttr = new DefaultAttributeMetaData(MOD_AGGREGATES).setDataType(BOOL)
				.setNillable(false).setLabel("Aggregates");
		DefaultAttributeMetaData modAnnotatorsAttr = new DefaultAttributeMetaData(MOD_ANNOTATORS).setDataType(BOOL)
				.setNillable(false).setLabel("Annotators");
		DefaultAttributeMetaData modChartsAttr = new DefaultAttributeMetaData(MOD_CHARTS).setDataType(BOOL)
				.setNillable(false).setLabel("Charts");
		DefaultAttributeMetaData modDataAttr = new DefaultAttributeMetaData(MOD_DATA).setDataType(BOOL)
				.setNillable(false).setLabel("Data");
		DefaultAttributeMetaData modDiseaseMatcherAttr = new DefaultAttributeMetaData(MOD_DISEASEMATCHER)
				.setDataType(BOOL).setNillable(false).setLabel("Disease Matcher");

		DefaultAttributeMetaData modAttr = addAttribute(MOD).setDataType(COMPOUND).setLabel("Modules");
		modAttr.addAttributePart(modAggregatesAttr);
		modAttr.addAttributePart(modAnnotatorsAttr);
		modAttr.addAttributePart(modChartsAttr);
		modAttr.addAttributePart(modDataAttr);
		modAttr.addAttributePart(modDiseaseMatcherAttr);
	}
}
