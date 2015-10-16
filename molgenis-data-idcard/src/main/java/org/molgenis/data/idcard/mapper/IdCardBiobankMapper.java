package org.molgenis.data.idcard.mapper;

import java.io.IOException;

import org.molgenis.data.Entity;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.idcard.model.IdCardOrganization;

import com.google.gson.stream.JsonReader;

public interface IdCardBiobankMapper
{
	IdCardBiobank toIdCardBiobank(JsonReader jsonReader) throws IOException;

	// FIXME Iterable<IdCardBiobank> instead of Iterable<Entity>
	Iterable<Entity> toIdCardBiobanks(JsonReader jsonReader) throws IOException;

	IdCardOrganization toIdCardOrganization(JsonReader jsonReader) throws IOException;

	Iterable<IdCardOrganization> toIdCardOrganizations(JsonReader jsonReader) throws IOException;
}
