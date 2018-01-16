package org.molgenis.integrationtest.data.aggregation;

import org.molgenis.data.security.aggregation.AggregateAnonymizerImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AggregateAnonymizerImpl.class)
public class AggregationTestConfig
{
}
