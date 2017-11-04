package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.time.LocalDate;
import java.util.Optional;

@AutoValue
@AutoGson(autoValueClass = AutoValue_UpdateUserMembership.class)
@SuppressWarnings("squid:S1610")
public abstract class UpdateUserMembership
{
	public abstract String getUserId();

	public abstract String getGroupId();

	public abstract LocalDate getStart();

	public abstract Optional<LocalDate> getStop();

}
