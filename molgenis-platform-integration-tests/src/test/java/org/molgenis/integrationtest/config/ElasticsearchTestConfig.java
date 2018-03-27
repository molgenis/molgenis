package org.molgenis.integrationtest.config;

import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.client.ElasticsearchConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ElasticsearchConfig.class, ElasticsearchGeneratorConfig.class, ElasticsearchService.class })
public class ElasticsearchTestConfig
{
}
