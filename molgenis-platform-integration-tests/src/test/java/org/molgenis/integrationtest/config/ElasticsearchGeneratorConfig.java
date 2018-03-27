package org.molgenis.integrationtest.config;

import org.molgenis.data.elasticsearch.generator.ContentGenerators;
import org.molgenis.data.elasticsearch.generator.DocumentIdGenerator;
import org.molgenis.data.elasticsearch.generator.QueryGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Using componentscans because of class-visibility
 */
@Configuration
@ComponentScan({ "org.molgenis.data.elasticsearch.generator" })
@Import({ DocumentIdGenerator.class, ContentGenerators.class, QueryGenerator.class })
public class ElasticsearchGeneratorConfig
{
}
