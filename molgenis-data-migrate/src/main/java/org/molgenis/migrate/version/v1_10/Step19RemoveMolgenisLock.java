package org.molgenis.migrate.version.v1_10;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class Step19RemoveMolgenisLock extends MolgenisUpgrade implements ApplicationListener<ContextRefreshedEvent>
{
	private final MetaDataService metaDataService;
	private boolean enabled = false;

	@Autowired
	public Step19RemoveMolgenisLock(MetaDataService metaDataService)
	{
		super(18, 19);
		this.metaDataService = metaDataService;
	}

	@Override
	public void upgrade()
	{
		enabled = true;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (enabled)
		{
			RunAsSystemProxy.runAsSystem(() -> {
				if (metaDataService.getEntityMetaData("MolgenisLock") != null)
				{
					metaDataService.deleteEntityMeta("MolgenisLock");
				}
			});
		}
	}
}
