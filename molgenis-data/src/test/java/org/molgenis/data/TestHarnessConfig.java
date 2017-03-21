package org.molgenis.data;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityTestHarness.class, EntitySelfXrefTestHarness.class, OneToManyTestHarness.class, TestPackage.class })
@ComponentScan("org.molgenis.data.staticentity")
public class TestHarnessConfig
{
}
