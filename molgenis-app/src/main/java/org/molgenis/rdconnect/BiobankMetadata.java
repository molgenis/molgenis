package org.molgenis.rdconnect;

import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.gson.annotations.SerializedName;

public class BiobankMetadata
{
	private List<Collection> Collections;

	@SerializedName("OrganizationID")
	private String organizationId;

	private String type;

	@SerializedName("also listed in")
	private String[] alsoListedIn;

	private String[] url;

	@SerializedName("main contact")
	private MainContact mainContact;

	@SerializedName("last activities")
	private DateTime lastActivities;

	@SerializedName("date of inclusion")
	private DateTime dateOfInclusion;

	private Address address;
	private String name;

	@SerializedName("ID")
	private String id;

	@SerializedName("type of host institution")
	private String typeOfHostInstitutionS;

	@SerializedName("target population")
	private String targetPopulation;

	class Collection
	{
		private String CollectionName;
		private String CollectionID;
	}

	class MainContact
	{
		private String title;

		@SerializedName("first name")
		private String first_name;

		private String email;

		@SerializedName("last name")
		private String last_name;
	}

	class Address
	{
		private String street2;

		@SerializedName("name of host institution")
		private String nameOfHostInstitution;

		private String zip;

		private String street1;

		private String country;

		private String city;
	}
}
