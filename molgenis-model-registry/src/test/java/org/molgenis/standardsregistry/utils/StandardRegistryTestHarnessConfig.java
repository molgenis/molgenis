package org.molgenis.standardsregistry.utils;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.MetaDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author sido
 */
@Configuration
@Import({TestHarnessConfig.class, StandardRegistryTestHarness.class})
public class StandardRegistryTestHarnessConfig {

    @Mock
    private MetaDataService metaDataService;

    public StandardRegistryTestHarnessConfig() {
        MockitoAnnotations.initMocks(this);
    }

    @Bean
    public MetaDataService metaDataService() {
        return metaDataService;
    };


}
