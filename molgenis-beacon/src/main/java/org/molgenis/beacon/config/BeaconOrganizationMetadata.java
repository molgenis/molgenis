package org.molgenis.beacon.config;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.beacon.config.BeaconPackage.PACKAGE_BEACON;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class BeaconOrganizationMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "BeaconOrganization";
	public static final String BEACON_ORGANIZATION = PACKAGE_BEACON + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ADDRESS = "address";
	public static final String WELCOME_URL = "welcome_url";
	public static final String CONTACT_URL = "contact_url";
	public static final String LOGO_URL = "logo_url";

	private final BeaconPackage beaconPackage;

	public BeaconOrganizationMetadata(BeaconPackage beaconPackage)
	{
		super(SIMPLE_NAME, PACKAGE_BEACON);
		this.beaconPackage = requireNonNull(beaconPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Beacon organization");
		setPackage(beaconPackage);
		setDescription("Organization owning a beacon");
		addAttribute(ID, ROLE_ID).setDataType(STRING)
								 .setNillable(false)
								 .setLabel("Organization identifier")
								 .setDescription("Unique identifier of an organization");
		addAttribute(NAME).setDataType(STRING)
						  .setNillable(false)
						  .setLabel("Organization name")
						  .setDescription("Name of the organization");
		addAttribute(DESCRIPTION).setDataType(TEXT)
								 .setNillable(true)
								 .setLabel("Organization description")
								 .setDescription("Description of the organization");
		addAttribute(ADDRESS).setDataType(STRING)
							 .setNillable(true)
							 .setLabel("Organization address")
							 .setDescription("Address of the organization");
		addAttribute(WELCOME_URL).setDataType(HYPERLINK)
								 .setNillable(true)
								 .setLabel("Organization welcome URL")
								 .setDescription("URL of the website of the organization");
		addAttribute(CONTACT_URL).setDataType(STRING)
								 .setNillable(true)
								 .setLabel("Contact URL")
								 .setDescription("URL with the contact for the beacon operator/maintainer");
		addAttribute(LOGO_URL).setDataType(HYPERLINK)
							  .setNillable(true)
							  .setLabel("Organization logo")
							  .setDescription("URL to the logo (PNG/JPG format) of the organization");
	}
}
