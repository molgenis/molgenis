package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.data.security.auth.User;

@AutoValue
public abstract class UserResponse
{
	public abstract String getId();
	public abstract String getUsername();

	static UserResponse fromEntity(User user) {
		return new AutoValue_UserResponse(user.getId(), user.getUsername());
	}
}
