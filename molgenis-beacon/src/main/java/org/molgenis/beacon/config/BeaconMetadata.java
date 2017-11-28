package org.molgenis.beacon.config;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.beacon.config.BeaconPackage.PACKAGE_BEACON;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class BeaconMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "BeaconResponse";
	public static final String BEACON = PACKAGE_BEACON + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String API_VERSION = "api_version";
	public static final String BEACON_ORGANIZATION = "beacon_organization";
	public static final String DESCRIPTION = "description";
	public static final String VERSION = "version";
	public static final String WELCOME_URL = "welcome_url";
	public static final String DATA_SETS = "data_sets";

	private final BeaconPackage beaconPackage;
	private final BeaconOrganizationMetadata beaconOrganizationMetadata;
	private final EntityTypeMetadata entityTypeMetadata;

	public BeaconMetadata(BeaconPackage beaconPackage, BeaconOrganizationMetadata beaconOrganizationMetadata,
			EntityTypeMetadata entityTypeMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_BEACON);
		this.beaconPackage = requireNonNull(beaconPackage);
		this.beaconOrganizationMetadata = requireNonNull(beaconOrganizationMetadata);
		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
	}

	@Override
	protected void init()
	{
		setLabel("BeaconResponse");
		setPackage(beaconPackage);
		setDescription("A BeaconResponse based on ga4gh beacon API. See https://github.com/ga4gh for more information");
		addAttribute(ID, ROLE_ID).setDataType(STRING)
								 .setNillable(false)
								 .setLabel("BeaconResponse identifier")
								 .setDescription("Unique identifier of the beacon");
		addAttribute(NAME).setDataType(STRING)
						  .setNillable(false)
						  .setLabel("BeaconResponse name")
						  .setDescription("Name of the beacon");
		addAttribute(API_VERSION).setDataType(STRING)
								 .setNillable(false)
								 .setDefaultValue("v0.3.0")
								 .setLabel("API version")
								 .setDescription("Version of the API provided by the beacon");
		addAttribute(BEACON_ORGANIZATION).setDataType(XREF)
										 .setRefEntity(beaconOrganizationMetadata)
										 .setNillable(true)
										 .setLabel("BeaconResponse organization")
										 .setDescription("Organization owning the beacon");
		addAttribute(DESCRIPTION).setDataType(TEXT)
								 .setNillable(true)
								 .setLabel("BeaconResponse description")
								 .setDescription(" Description of the beacon");
		addAttribute(VERSION).setDataType(STRING)
							 .setNillable(true)
							 .setLabel("BeaconResponse version")
							 .setDescription(" Version of the beacon");
		addAttribute(WELCOME_URL).setDataType(STRING)
								 .setNillable(true)
								 .setLabel("Welcome URL")
								 .setDescription("URL to the welcome page for this beacon");
		addAttribute(DATA_SETS).setDataType(MREF)
							   .setRefEntity(entityTypeMetadata)
							   .setNillable(false)
							   .setLabel("BeaconResponse datasets")
							   .setDescription("Data sets served by the beacon");
	}
}
