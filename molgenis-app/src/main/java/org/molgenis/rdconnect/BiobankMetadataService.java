package org.molgenis.rdconnect;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.molgenis.data.MolgenisDataException;
import org.springframework.stereotype.Service;

import com.google.api.client.util.DateTime;
import com.google.gson.annotations.SerializedName;

@Service
public class BiobankMetadataService
{

	// public static void main(String args[])
	// {
	// BiobankMetadataService biobankMetadataService = new BiobankMetadataService();
	// Map<String, Object> biobankMetadata = biobankMetadataService
	// .getBiobankMetadata("http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi/regbb/organization-id/10779");
	// System.out.println(biobankMetadata.toString());
	// EntityMetaData emd =
	// MapEntity regbbMapEntity = new MapEntity();
	// }

	public Map<String, Object> getBiobankMetadata(String url)
	{
		try
		{
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("content-type", "application/json");
			HttpResponse result = httpClient.execute(request);
			String json = EntityUtils.toString(result.getEntity(), "UTF-8");

			com.google.gson.Gson gson = new com.google.gson.Gson();
			Map<String, Object> javaRootMapObject = gson.fromJson(json, Map.class);
			return javaRootMapObject;
			//return gson.fromJson(json, BiobankMetadata.class);
		}
		catch (IOException ex)
		{
			throw new MolgenisDataException("Hackathon error message");
		}
	}

	public class BiobankMetadata
	{
		/**
		 * @return the collections
		 */
		public List<Collection> getCollections()
		{
			return Collections;
		}

		/**
		 * @return the organizationId
		 */
		public String getOrganizationId()
		{
			return organizationId;
		}

		/**
		 * @return the type
		 */
		public String getType()
		{
			return type;
		}

		/**
		 * @return the alsoListedIn
		 */
		public String[] getAlsoListedIn()
		{
			return alsoListedIn;
		}

		/**
		 * @return the url
		 */
		public String[] getUrl()
		{
			return url;
		}

		/**
		 * @return the mainContact
		 */
		public MainContact getMainContact()
		{
			return mainContact;
		}

		/**
		 * @return the lastActivities
		 */
		public DateTime getLastActivities()
		{
			return lastActivities;
		}

		/**
		 * @return the dateOfInclusion
		 */
		public DateTime getDateOfInclusion()
		{
			return dateOfInclusion;
		}

		/**
		 * @return the address
		 */
		public Address getAddress()
		{
			return address;
		}

		/**
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * @return the id
		 */
		public String getId()
		{
			return id;
		}

		/**
		 * @return the typeOfHostInstitutionS
		 */
		public String getTypeOfHostInstitutionS()
		{
			return typeOfHostInstitutionS;
		}

		/**
		 * @return the targetPopulation
		 */
		public String getTargetPopulation()
		{
			return targetPopulation;
		}

		/**
		 * @param collections
		 *            the collections to set
		 */
		public void setCollections(List<Collection> collections)
		{
			Collections = collections;
		}

		/**
		 * @param organizationId
		 *            the organizationId to set
		 */
		public void setOrganizationId(String organizationId)
		{
			this.organizationId = organizationId;
		}

		/**
		 * @param type
		 *            the type to set
		 */
		public void setType(String type)
		{
			this.type = type;
		}

		/**
		 * @param alsoListedIn
		 *            the alsoListedIn to set
		 */
		public void setAlsoListedIn(String[] alsoListedIn)
		{
			this.alsoListedIn = alsoListedIn;
		}

		/**
		 * @param url
		 *            the url to set
		 */
		public void setUrl(String[] url)
		{
			this.url = url;
		}

		/**
		 * @param mainContact
		 *            the mainContact to set
		 */
		public void setMainContact(MainContact mainContact)
		{
			this.mainContact = mainContact;
		}

		/**
		 * @param lastActivities
		 *            the lastActivities to set
		 */
		public void setLastActivities(DateTime lastActivities)
		{
			this.lastActivities = lastActivities;
		}

		/**
		 * @param dateOfInclusion
		 *            the dateOfInclusion to set
		 */
		public void setDateOfInclusion(DateTime dateOfInclusion)
		{
			this.dateOfInclusion = dateOfInclusion;
		}

		/**
		 * @param address
		 *            the address to set
		 */
		public void setAddress(Address address)
		{
			this.address = address;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name)
		{
			this.name = name;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(String id)
		{
			this.id = id;
		}

		/**
		 * @param typeOfHostInstitutionS
		 *            the typeOfHostInstitutionS to set
		 */
		public void setTypeOfHostInstitutionS(String typeOfHostInstitutionS)
		{
			this.typeOfHostInstitutionS = typeOfHostInstitutionS;
		}

		/**
		 * @param targetPopulation
		 *            the targetPopulation to set
		 */
		public void setTargetPopulation(String targetPopulation)
		{
			this.targetPopulation = targetPopulation;
		}

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
			/**
			 * @return the collectionName
			 */
			public String getCollectionName()
			{
				return CollectionName;
			}

			/**
			 * @return the collectionID
			 */
			public String getCollectionID()
			{
				return CollectionID;
			}

			/**
			 * @param collectionName
			 *            the collectionName to set
			 */
			public void setCollectionName(String collectionName)
			{
				CollectionName = collectionName;
			}

			/**
			 * @param collectionID
			 *            the collectionID to set
			 */
			public void setCollectionID(String collectionID)
			{
				CollectionID = collectionID;
			}
			private String CollectionName;
			private String CollectionID;
		}

		class MainContact
		{
			/**
			 * @return the title
			 */
			public String getTitle()
			{
				return title;
			}

			/**
			 * @return the first_name
			 */
			public String getFirst_name()
			{
				return first_name;
			}

			/**
			 * @return the email
			 */
			public String getEmail()
			{
				return email;
			}

			/**
			 * @return the last_name
			 */
			public String getLast_name()
			{
				return last_name;
			}

			/**
			 * @param title
			 *            the title to set
			 */
			public void setTitle(String title)
			{
				this.title = title;
			}

			/**
			 * @param first_name
			 *            the first_name to set
			 */
			public void setFirst_name(String first_name)
			{
				this.first_name = first_name;
			}

			/**
			 * @param email
			 *            the email to set
			 */
			public void setEmail(String email)
			{
				this.email = email;
			}

			/**
			 * @param last_name
			 *            the last_name to set
			 */
			public void setLast_name(String last_name)
			{
				this.last_name = last_name;
			}

			private String title;

			@SerializedName("first name")
			private String first_name;

			private String email;

			@SerializedName("last name")
			private String last_name;
		}
		
		class Address
		{
			/**
			 * @return the street2
			 */
			public String getStreet2()
			{
				return street2;
			}

			/**
			 * @return the nameOfHostInstitution
			 */
			public String getNameOfHostInstitution()
			{
				return nameOfHostInstitution;
			}

			/**
			 * @return the zip
			 */
			public String getZip()
			{
				return zip;
			}

			/**
			 * @return the street1
			 */
			public String getStreet1()
			{
				return street1;
			}

			/**
			 * @return the country
			 */
			public String getCountry()
			{
				return country;
			}

			/**
			 * @return the city
			 */
			public String getCity()
			{
				return city;
			}

			/**
			 * @param street2
			 *            the street2 to set
			 */
			public void setStreet2(String street2)
			{
				this.street2 = street2;
			}

			/**
			 * @param nameOfHostInstitution
			 *            the nameOfHostInstitution to set
			 */
			public void setNameOfHostInstitution(String nameOfHostInstitution)
			{
				this.nameOfHostInstitution = nameOfHostInstitution;
			}

			/**
			 * @param zip
			 *            the zip to set
			 */
			public void setZip(String zip)
			{
				this.zip = zip;
			}

			/**
			 * @param street1
			 *            the street1 to set
			 */
			public void setStreet1(String street1)
			{
				this.street1 = street1;
			}

			/**
			 * @param country
			 *            the country to set
			 */
			public void setCountry(String country)
			{
				this.country = country;
			}

			/**
			 * @param city
			 *            the city to set
			 */
			public void setCity(String city)
			{
				this.city = city;
			}

			private String street2;
			
			@SerializedName("name of host institution")
			private String nameOfHostInstitution;
			
			private String zip;

			private String street1;
			
			private String country;
			
			private String city;
		}
	}
}
