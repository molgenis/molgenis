package org.molgenis.standardsregistry.services;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.standardsregistry.model.*;
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
import static org.junit.Assert.assertEquals;

/**
 * @author sido
 */
@ContextConfiguration(classes = StandardRegistryTestHarnessConfig.class)
public class StandardRegistryServiceTest extends AbstractMolgenisSpringTest {


    @Autowired
    private EntityTestHarness entityTestHarness;
    @Autowired
    private StandardRegistryTestHarness standardRegistryTestHarness;

    @Mock
    private StandardRegistryService standardRegistryService;


    // models to test with
    private final static String testPackageName = "test";
    private List<StandardRegistryEntity> entities;
    private PackageTreeNode packageTreeNode;
    private EntityType emd;
    private Package pkg;

    private MockMvc mockMvc;

    @Before
    public void setup()
    {
        // Process mock annotations
        MockitoAnnotations.initMocks(this);
        // Setup Spring test in standalone mode
        this.mockMvc = MockMvcBuilders.standaloneSetup(standardRegistryService).build();
    }

    @BeforeClass
    public void beforeClass()
    {
        emd = entityTestHarness.createDynamicRefEntityType();
        entities = standardRegistryTestHarness.createStandardRegsitryEntities();
//        pkg = new Package("test", emd);
        packageTreeNode = standardRegistryTestHarness.createPackageTreeNode();
    }

    @BeforeMethod
    public void beforeMethod()
    {
        when(standardRegistryService.getEntitiesInPackage(testPackageName)).thenReturn(entities);
        when(standardRegistryService.createPackageTreeNode(pkg)).thenReturn(packageTreeNode);
    }

    @Test
    public void testGetEntitiesInPackage()
    {
        List<StandardRegistryEntity> standardRegistryEntities = standardRegistryService.getEntitiesInPackage(testPackageName);
        assertEquals(2, standardRegistryEntities.size());
    }

    @Test
    public void testCreatePackageTreeNode()
    {
        PackageTreeNode packageTreeNode = standardRegistryService.createPackageTreeNode(pkg);
        assertEquals(true, packageTreeNode.getTitle().equalsIgnoreCase("testTreeNode"));
    }


    @Test
    public void testSearch()
    {

        PackageSearchRequest request = new PackageSearchRequest();
        request.setQuery("");

        PackageSearchResponse response = standardRegistryService.search(request);

    }

}
