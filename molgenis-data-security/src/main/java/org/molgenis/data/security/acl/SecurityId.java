package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@AutoGson(autoValueClass = AutoValue_SecurityId.class)
@AutoValue
public abstract class SecurityId
{
	@Nullable
	public abstract String getUsername();

	@Nullable
	public abstract String getAuthority();

	public static SecurityId createForUsername(String newUsername)
	{
		requireNonNull(newUsername);
		return new AutoValue_SecurityId(newUsername, null);
	}

	public static SecurityId createForAuthority(String newAuthority)
	{
		requireNonNull(newAuthority);
		return new AutoValue_SecurityId(null, newAuthority);
	}
}
