package org.molgenis.script;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class ScriptMetaRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;

	@Autowired
	public ScriptMetaRegistrator(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE - 100;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		dataService.getMeta().addEntityMeta(ScriptParameter.META_DATA);
		dataService.getMeta().addEntityMeta(ScriptType.META_DATA);
		dataService.getMeta().addEntityMeta(Script.META_DATA);
	}

}
