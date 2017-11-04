package org.molgenis.ui.admin.usermanager;

import com.google.auto.value.AutoValue;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.User;

import java.util.Collection;

@AutoValue
@SuppressWarnings("squid:S1610")
public abstract class UserViewData
{
	public abstract User getUser();

	public abstract Collection<Group> getCurrentGroups();

	public static UserViewData create(User user, Collection<Group> currentGroups)
	{
		return new AutoValue_UserViewData(user, currentGroups);
	}
}
