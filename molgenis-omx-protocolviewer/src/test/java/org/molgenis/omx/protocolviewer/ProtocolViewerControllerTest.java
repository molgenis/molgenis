package org.molgenis.omx.protocolviewer;

//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Date;
//
//import org.molgenis.data.DataService;
//import org.molgenis.data.Entity;
//import org.molgenis.data.Query;
//import org.molgenis.framework.db.DatabaseException;
//import org.molgenis.framework.server.MolgenisSettings;
//import org.molgenis.omx.observ.ObservableFeature;
//import org.molgenis.omx.protocolviewer.ProtocolViewerService;
//import org.molgenis.omx.protocolviewer.ProtocolViewerServiceImpl;
//import org.molgenis.omx.protocolviewer.ProtocolViewerController;
//import org.molgenis.omx.study.StudyDataRequest;
//import org.molgenis.security.user.MolgenisUserService;
//import org.molgenis.studymanager.StudyManagerService;
//import org.molgenis.util.FileStore;
//import org.molgenis.util.GsonHttpMessageConverter;
//import org.molgenis.util.HandleRequestDelegationException;
//import org.molgenis.util.ShoppingCart;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.converter.FormHttpMessageConverter;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;

//import org.molgenis.data.DataService;
//import org.molgenis.omx.cart.ShoppingCartController;
//import org.molgenis.omx.observ.ObservableFeature;
//import org.molgenis.util.GsonHttpMessageConverter;
//import org.molgenis.util.HandleRequestDelegationException;
//import org.molgenis.util.ShoppingCart;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.FormHttpMessageConverter;

import java.util.Arrays;

//FIXME
public class ProtocolViewerControllerTest
{
//    @Autowired
//    private ProtocolViewerController protocolViewerController;
//
//    @Autowired
//    private ProtocolViewerService protocolViewerService;
//
//    private MockMvc mockMvc;
//    private Authentication authentication;
//
//    @BeforeMethod
//    public void setUp() throws HandleRequestDelegationException, Exception
//    {
//        mockMvc = MockMvcBuilders.standaloneSetup(protocolViewerController)
//                .setMessageConverters(new GsonHttpMessageConverter(), new FormHttpMessageConverter()).build();
//
//        authentication = mock(Authentication.class);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//
//    @Test
//    public void getOrderDataForm() throws Exception
//    {
//        this.mockMvc.perform(get(ProtocolViewerController.URI + "/order")).andExpect(status().isOk())
//                .andExpect(view().name("orderdata-modal"));
//    }
//
//    @Test
//    public void getOrdersForm() throws Exception
//    {
//        this.mockMvc.perform(get(ProtocolViewerController.URI + "/orders/view")).andExpect(status().isOk())
//                .andExpect(view().name("orderlist-modal"));
//    }
//
//    @Test
//    public void getOrders() throws Exception
//    {
//        StudyDataRequest request0 = mock(StudyDataRequest.class);
//        when(request0.getId()).thenReturn(0);
//        when(request0.getRequestDate()).thenReturn(new Date(1350000000000l));
//        when(request0.getRequestStatus()).thenReturn("pending");
//        when(request0.getName()).thenReturn("request #0");
//        when(protocolViewerService.getStudyDefinitionsForCurrentUser()).thenReturn(Arrays.asList(request0));
//
//        when(authentication.isAuthenticated()).thenReturn(true);
//        UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn("user0").getMock();
//        when(authentication.getPrincipal()).thenReturn(userDetails);
//
//        this.mockMvc
//                .perform(get(ProtocolViewerController.URI + "/orders"))
//                .andExpect(status().isOk())
//                .andExpect(
//                        content()
//                                .string("{\"orders\":[{\"id\":0,\"name\":\"request #0\",\"orderDate\":\"2012-10-12\",\"orderStatus\":\"pending\"}]}"));
//    }
//
//    // TODO how to test multipart/form-data using fileUpload() and post()?
//    // @Test
//    // public void orderData() throws Exception
//    // {
//    //
//    // }
//
//    @Configuration
//    public static class Config
//    {
//        @Bean
//        public ProtocolViewerController protocolViewerController()
//        {
//            return new ProtocolViewerController();
//        }
//
//        @Bean
//        public ProtocolViewerService orderStudyDataService() throws DatabaseException
//        {
//            StudyDataRequest request0 = mock(StudyDataRequest.class);
//            when(request0.getId()).thenReturn(0);
//            when(request0.getRequestDate()).thenReturn(new Date(1350000000000l));
//            when(request0.getRequestStatus()).thenReturn("pending");
//            when(request0.getName()).thenReturn("request #0");
//            StudyDataRequest request1 = mock(StudyDataRequest.class);
//            when(request1.getId()).thenReturn(1);
//            when(request1.getRequestDate()).thenReturn(new Date(1360000000000l));
//            when(request1.getRequestStatus()).thenReturn("rejected");
//            when(request1.getName()).thenReturn("request #1");
//            ProtocolViewerService protocolViewerService = mock(ProtocolViewerServiceImpl.class);
//            when(protocolViewerService.getStudyDefinitionsForCurrentUser()).thenReturn(Arrays.asList(request0));
//            when(protocolViewerService.getStudyDefinitionsForCurrentUser()).thenReturn(Arrays.asList(request1));
//            return protocolViewerService;
//        }
//
//        @Bean
//        public StudyManagerService studyManagerService()
//        {
//            return mock(StudyManagerService.class);
//        }
//
//        @Bean
//        public DataService dataService()
//        {
//            return mock(DataService.class);
//        }
//
//        @Bean
//        public MolgenisSettings molgenisSettings()
//        {
//            return mock(MolgenisSettings.class);
//        }
//
//        @Bean
//        public JavaMailSender mailSender()
//        {
//            return mock(JavaMailSender.class);
//        }
//
//        @Bean
//        public FileStore fileStore()
//        {
//            return mock(FileStore.class);
//        }
//
//        @Bean
//        public ShoppingCart shoppingCart()
//        {
//            ShoppingCart shoppingCart = mock(ShoppingCart.class);
//            when(shoppingCart.getCart()).thenReturn(Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
//            return shoppingCart;
//        }
//
//        @Bean
//        public MolgenisUserService molgenisUserService()
//        {
//            return mock(MolgenisUserService.class);
//        }
//    }
//
//	@Test
//	public void handleRequest_download_xls() throws Exception
//	{
//		// mock db
//		DataService dataService = mock(DataService.class);
//		MolgenisSettings settings = mock(MolgenisSettings.class);
//		ShoppingCart shoppingCart = mock(ShoppingCart.class);
//
//		ObservableFeature feature1 = new ObservableFeature();
//		feature1.setId(1);
//		feature1.setName("feature1");
//		feature1.setIdentifier("featureid1");
//		feature1.setDescription("this is feature1");
//
//		ObservableFeature feature2 = new ObservableFeature();
//		feature2.setId(2);
//		feature2.setName("feature2");
//		feature2.setIdentifier("featureid2");
//		feature2.setDescription("this is feature2");
//
//		ObservableFeature feature3 = new ObservableFeature();
//		feature3.setId(3);
//		feature3.setName("feature3");
//		feature3.setIdentifier("featureid3");
//		feature3.setDescription("this is feature3");
//
//		Query q = mock(Query.class);
//		when(q.in(ObservableFeature.ID, Arrays.asList(1, 2, 3))).thenReturn(q);
//		Iterable<Entity> entities = Arrays.<Entity> asList(feature1, feature2, feature3);
//		when(dataService.findAll(ObservableFeature.ENTITY_NAME, q)).thenReturn(entities);
//		ProtocolViewerController controller = new ProtocolViewerController(dataService, settings, shoppingCart);
//
//		// mock request
//		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
//		httpRequest.setMethod("GET");
//
//		when(shoppingCart.getCart()).thenReturn(Arrays.asList(1, 2, 3));
//		MockHttpServletResponse response = new MockHttpServletResponse();
//
//		controller.download(response);
//	}
//
//	@Test
//	public void handleRequest_download_xls_noFeatures() throws Exception
//	{
//		// mock db
//		DataService dataService = mock(DataService.class);
//		MolgenisSettings settings = mock(MolgenisSettings.class);
//		ShoppingCart shoppingCart = mock(ShoppingCart.class);
//
//		Query q = mock(Query.class);
//		when(q.in(ObservableFeature.ID, Arrays.asList(1, 2, 3))).thenReturn(q);
//		Iterable<Entity> it = Collections.emptyList();
//		when(dataService.findAll(ObservableFeature.ENTITY_NAME, q)).thenReturn(it);
//		ProtocolViewerController controller = new ProtocolViewerController(dataService, settings, shoppingCart);
//
//		// mock request
//		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
//		httpRequest.setMethod("GET");
//
//		when(shoppingCart.getCart()).thenReturn(Arrays.asList(1, 2, 3));
//		MockHttpServletResponse response = new MockHttpServletResponse();
//
//		controller.download(response);
//	}

//    @Autowired
//    private ShoppingCartController shoppingCartController;
//
//    @Autowired
//    private ShoppingCart shoppingCart;
//
//    private MockMvc mockMvc;
//
//    @BeforeMethod
//    public void setUp() throws HandleRequestDelegationException, Exception
//    {
//        mockMvc = MockMvcBuilders.standaloneSetup(shoppingCartController)
//                .setMessageConverters(new GsonHttpMessageConverter(), new FormHttpMessageConverter()).build();
//    }
//
//    @Test
//    public void getCart() throws Exception
//    {
//        this.mockMvc
//                .perform(get("/cart").accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(
//                        content()
//                                .string("{\"features\":[{\"id\":0,\"name\":\"feature #0\",\"i18nDescription\":{\"en\":\"feature #0 description\"}},{\"id\":1,\"name\":\"feature #1\",\"i18nDescription\":{\"en\":\"feature #1 description\"}}]}"));
//    }
//
//    @Test
//    public void addToCart() throws Exception
//    {
//        this.mockMvc.perform(
//                post("/cart/add").content("{features:[{feature:3},{feature:4}]}")
//                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(
//                status().isOk());
//        verify(shoppingCart).addToCart(Arrays.asList(Integer.valueOf(3), Integer.valueOf(4)));
//    }
//
//    @Test
//    public void addToCart_invalidBody() throws Exception
//    {
//        this.mockMvc.perform(
//                post("/cart/add").content("[{feature:3},{feature:4}]").contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void emptyCart() throws Exception
//    {
//        this.mockMvc.perform(post("/cart/empty")).andExpect(status().isOk());
//        verify(shoppingCart).emptyCart();
//    }
//
//    @Test
//    public void removeFromCart() throws Exception
//    {
//        this.mockMvc.perform(
//                post("/cart/remove").content("{features:[{feature:3},{feature:4}]}")
//                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(
//                status().isOk());
//        verify(shoppingCart).removeFromCart(Arrays.asList(Integer.valueOf(3), Integer.valueOf(4)));
//    }
//
//    @Test
//    public void removeFromCart_invalidBody() throws Exception
//    {
//        this.mockMvc.perform(
//                post("/cart/remove").content("[{feature:3},{feature:4}]").contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void emptyAndAddToCart() throws Exception
//    {
//        this.mockMvc.perform(
//                post("/cart/replace").content("{features:[{feature:3},{feature:4}]}")
//                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(
//                status().isOk());
//        verify(shoppingCart).emptyAndAddToCart(Arrays.asList(Integer.valueOf(3), Integer.valueOf(4)));
//    }
//
//    @Test
//    public void emptyAndAddToCart_invalidBody() throws Exception
//    {
//        this.mockMvc.perform(
//                post("/cart/replace").content("[{feature:3},{feature:4}]").contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
//    }
//
//    @Configuration
//    public static class Config
//    {
//        @Bean
//        public ShoppingCartController shoppingCartController()
//        {
//            return new ShoppingCartController();
//        }
//
//        @Bean
//        public ShoppingCart shoppingCart()
//        {
//            ShoppingCart shoppingCart = mock(ShoppingCart.class);
//            when(shoppingCart.getCart()).thenReturn(Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
//            return shoppingCart;
//        }
//
//        @Bean
//        public DataService dataService()
//        {
//            ObservableFeature feature0 = mock(ObservableFeature.class);
//            when(feature0.getId()).thenReturn(0);
//            when(feature0.getName()).thenReturn("feature #0");
//            when(feature0.getDescription()).thenReturn("feature #0 description");
//
//            ObservableFeature feature1 = mock(ObservableFeature.class);
//            when(feature1.getId()).thenReturn(1);
//            when(feature1.getName()).thenReturn("feature #1");
//            when(feature1.getDescription()).thenReturn("feature #1 description");
//
//            DataService dataService = mock(DataService.class);
//            when(dataService.findOne(ObservableFeature.ENTITY_NAME, 0)).thenReturn(feature0);
//            when(dataService.findOne(ObservableFeature.ENTITY_NAME, 1)).thenReturn(feature1);
//            return dataService;
//        }
//    }
}
