package org.molgenis.data.plugin.model;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.springframework.security.acls.model.MutableAclService;

import java.util.stream.Stream;

/**
 * {@link Plugin} decorator that creates/deletes {@link org.springframework.security.acls.model.Acl ACLs} when creating/deleting plugins.
 */
public class PluginSecurityRepositoryDecorator extends AbstractRepositoryDecorator<Plugin>
{
	private final MutableAclService mutableAclService;

	PluginSecurityRepositoryDecorator(Repository<Plugin> delegateRepository, MutableAclService mutableAclService)
	{
		super(delegateRepository);
		this.mutableAclService = mutableAclService;
	}

	@Override
	public void add(Plugin plugin)
	{
		createAcl(plugin);
		super.add(plugin);
	}

	@Override
	public Integer add(Stream<Plugin> pluginStream)
	{
		return super.add(pluginStream.filter(plugin ->
		{
			createAcl(plugin);
			return true;
		}));
	}

	@Override
	public void delete(Plugin plugin)
	{
		deleteAcl(plugin);
		super.delete(plugin);
	}

	@Override
	public void deleteById(Object pluginId)
	{
		deleteAcl(pluginId.toString());
		super.deleteById(pluginId);
	}

	@Override
	public void deleteAll()
	{
		iterator().forEachRemaining(this::deleteAcl);
		super.deleteAll();
	}

	@Override
	public void delete(Stream<Plugin> pluginStream)
	{
		super.delete(pluginStream.filter(plugin ->
		{
			deleteAcl(plugin);
			return true;
		}));
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		super.deleteAll(ids.filter(pluginId ->
		{
			deleteAcl(pluginId);
			return true;
		}));
	}

	private void createAcl(Plugin plugin)
	{
		mutableAclService.createAcl(new PluginIdentity(plugin.getId()));
	}

	private void deleteAcl(Plugin plugin)
	{
		deleteAcl(new PluginIdentity(plugin.getId()));
	}

	private void deleteAcl(Object pluginId)
	{
		deleteAcl(new PluginIdentity(pluginId.toString()));
	}

	private void deleteAcl(PluginIdentity pluginIdentity)
	{
		mutableAclService.deleteAcl(pluginIdentity, true);
	}
}
