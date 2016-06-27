package org.molgenis.data.bootstrap;

import org.molgenis.data.annotation.entity.AnnotatorConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AnnotatorBootstrapper
{
	public void bootstrap(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, AnnotatorConfig> annotatorMap = ctx.getBeansOfType(AnnotatorConfig.class);
		annotatorMap.values().forEach(AnnotatorConfig::init);

	}
}
