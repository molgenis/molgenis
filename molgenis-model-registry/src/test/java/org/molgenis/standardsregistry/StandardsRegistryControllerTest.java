package org.molgenis.standardsregistry;

import org.junit.Before;
import org.junit.Ignore;
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
import org.molgenis.standardsregistry.utils.StandardRegistryTestHarness;
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
//        pkg = packageFactory.create(StandardRegistryTestHarness.TEST_PACKAGE_NAME);
    }

    @BeforeMethod
    public void beforeMethod()
    {
//        when(metaDataService.getPackage(StandardRegistryTestHarness.TEST_PACKAGE_NAME)).thenReturn(pkg);
    }

    @Ignore
    @Test
    public void testDocumentation() throws Exception
    {
        //TODO-sido: create a test for /documentation path
        //TODO-sido: create a test for /documentation/#package# path
    }

    @Ignore
    @Test
    public void testSearch() throws Exception
    {
        //TODO-sido: create a test for /search path (GET)
        //TODO-sido: create a test for /search path (POST)
    }

    @Ignore
    @Test
    public void testDetails() throws Exception
    {
        //TODO-sido: create a test for /details
    }

    @Test
    public void testGetUml() throws Exception
    {
//        this.mockMvc.perform(get(StandardsRegistryController.URI + "/uml").param("package", StandardRegistryTestHarness.TEST_PACKAGE_NAME));
        //TODO-sido: Perform onExpect to test result of controller
    }

    @Ignore
    @Test
    public void testGetPackage() throws Exception
    {
        //TODO-sido: create a test for /getPackage path
    }

    @Ignore
    @Test
    public void testGetTreeData() throws Exception
    {
        //TODO-sido: create a test for /getTreeData path
    }

}
