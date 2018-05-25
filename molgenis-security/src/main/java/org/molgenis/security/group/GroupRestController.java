package org.molgenis.security.group;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.security.auth.GroupService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.model.GroupValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.PermissionSet.*;

@RestController
public class GroupRestController
{
	private final GroupValueFactory groupValueFactory;
	private final GroupService groupService;
	private static final Map<String, PermissionSet> DEFAULT_ROLES = ImmutableMap.of("Manager", WRITEMETA, "Editor",
			WRITE, "Viewer", READ);

	public GroupRestController(GroupValueFactory groupValueFactory, GroupService groupService)
	{
		this.groupValueFactory = requireNonNull(groupValueFactory);
		this.groupService = requireNonNull(groupService);
	}

	@PostMapping("api/plugin/group")
	public String createGroup(@RequestParam(required = false) @Nullable String name, @RequestParam String label,
			@RequestParam(required = false) @Nullable String description,
			@RequestParam(required = false, defaultValue = "true") boolean publiclyAvailable)
	{
		GroupValue groupValue = groupValueFactory.createGroup(name, label, description, publiclyAvailable,
				DEFAULT_ROLES.keySet());
		groupService.persist(groupValue);
		return groupValue.getName();
	}
}
