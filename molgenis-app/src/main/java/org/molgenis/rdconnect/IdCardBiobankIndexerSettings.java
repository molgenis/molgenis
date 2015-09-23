package org.molgenis.rdconnect;

public interface IdCardBiobankIndexerSettings
{
	String getIdCardApiBaseUri();

	void setIdCardApiBaseUri(String idCardApiBaseUri);

	String getIdCardBiobankResourceName();

	void setIdCardBiobankResourceName(String idCardBiobankResourceName);
}