package org.molgenis.standardsregistry;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.standardsregistry.utils.StandardRegistryTestHarnessConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;



/**
 *
 * <p>Test for <code>StandardRegistryTest</code>.</p>
 *
 * @author sido
 */
@ContextConfiguration(classes = StandardRegistryTestHarnessConfig.class)
public class StandardsRegistryControllerTest extends AbstractMolgenisSpringTest {

    @Autowired
    private EntityTestHarness entityTestHarness;
    @Autowired
    private PackageFactory packageFactory;
    @Autowired
    private MetaDataService metaDataService;

    @InjectMocks
    private StandardsRegistryController standardRegistryController;

    private List<Entity> entities;
    private EntityType emd;
    private Package pkg;

    private MockMvc mockMvc;

    @BeforeClass
    public void beforeClass()
    {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(standardRegistryController).build();
        emd = entityTestHarness.createDynamicRefEntityType();
        pkg = packageFactory.create("testPackage");
    }

    @BeforeMethod
    public void beforeMethod()
    {
        when(metaDataService.getPackage("testPackage")).thenReturn(pkg);
    }

    @Test
    public void testGetUml() throws Exception {
        this.mockMvc.perform(get(StandardsRegistryController.URI + "/uml").param("package", "testPackage"));

    }


}
