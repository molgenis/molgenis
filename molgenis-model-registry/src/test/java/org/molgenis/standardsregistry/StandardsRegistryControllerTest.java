package org.molgenis.standardsregistry;

import org.junit.Ignore;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.standardsregistry.utils.StandardRegistryTestHarnessConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    private MetaDataService metaDataService;

    @Autowired
    private PackageFactory packageFactory;

    @Mock
    private StandardsRegistryController standardRegistryController;

    private MockMvc mockMvc;

    @BeforeClass
    public void beforeClass()
    {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(standardRegistryController).build();
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
        String TEST_PACKAGE = "test-package";
        Package pkg = packageFactory.create(TEST_PACKAGE);
        when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(pkg);
        this.mockMvc.perform(get(StandardsRegistryController.URI + "/uml").param("package", TEST_PACKAGE)).andExpect(mvcResult -> {
            if(mvcResult != null) {
                System.out.println(mvcResult);
//                mvcResult.getModelAndView().getView().getContentType();
            } else {
                System.out.println("no view is resolved");
            }

        });
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
