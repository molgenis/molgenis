package org.molgenis.beacon.config;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.beacon.config.BeaconPackage.PACKAGE_BEACON;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class BeaconDatasetMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "BeaconDataset";
	public static final String BEACON_DATASET = PACKAGE_BEACON + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String DATA_SET_ENTITY_TYPE = "data_set_entity_type";
	public static final String GENOME_BROWSER_ATTRIBUTES = "genome_browser_attributes";

	private final BeaconPackage beaconPackage;
	private final EntityTypeMetadata entityTypeMetadata;
	private final GenomeBrowserAttributesMetadata genomeBrowserAttributesMetadata;

	public BeaconDatasetMetadata(BeaconPackage beaconPackage, EntityTypeMetadata entityTypeMetadata,
			GenomeBrowserAttributesMetadata genomeBrowserAttributesMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_BEACON);
		this.beaconPackage = requireNonNull(beaconPackage);
		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
		this.genomeBrowserAttributesMetadata = requireNonNull(genomeBrowserAttributesMetadata);
	}

	@Override
	protected void init()
	{
		setLabel("Beacon dataset");
		setPackage(beaconPackage);
		setDescription("Dataset for a beacon");
		addAttribute(ID, ROLE_ID).setDataType(STRING)
								 .setNillable(false)
								 .setLabel("Beacon dataset identifier")
								 .setDescription("Unique identifier of an beacon dataset");
		addAttribute(LABEL).setDataType(STRING)
						   .setNillable(false)
						   .setLabel("Beacon dataset label")
						   .setDescription("Label of beacon dataset");
		addAttribute(DESCRIPTION).setDataType(STRING)
								 .setNillable(true)
								 .setLabel("Beacon dataset description")
								 .setDescription("Description of beacon dataset");
		addAttribute(DATA_SET_ENTITY_TYPE).setDataType(XREF)
										  .setNillable(false)
										  .setLabel("Beacon dataset entityType")
										  .setRefEntity(entityTypeMetadata)
										  .setDescription("Beacon dataset entityType");
		addAttribute(GENOME_BROWSER_ATTRIBUTES).setDataType(XREF)
											   .setNillable(false)
											   .setRefEntity(genomeBrowserAttributesMetadata)
											   .setLabel("GenomeBrowser attributes")
											   .setDescription("GenomeBrowser attributes for the beacon dataset");
	}

}
