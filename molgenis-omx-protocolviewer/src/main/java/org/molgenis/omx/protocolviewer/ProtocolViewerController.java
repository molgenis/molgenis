package org.molgenis.omx.protocolviewer;

import static org.molgenis.omx.protocolviewer.ProtocolViewerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.utils.I18nTools;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.StudyDefinition.Status;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.molgenis.util.ErrorMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class ProtocolViewerController extends MolgenisPluginController
{
	public static final String ID = "protocolviewer";
	public static final String URI = PLUGIN_URI_PREFIX + ID;
	public static final String KEY_ACTION_DOWNLOAD = "plugin.catalogue.action.download";
	public static final String KEY_ACTION_ORDER = "plugin.catalogue.action.order";
	private static final Logger logger = Logger.getLogger(ProtocolViewerController.class);
	private static final boolean DEFAULT_KEY_ACTION_DOWNLOAD = true;
	private static final boolean DEFAULT_KEY_ACTION_ORDER = true;
	private final ProtocolViewerService protocolViewerService;
	private final MolgenisSettings molgenisSettings;

	@Autowired
	public ProtocolViewerController(ProtocolViewerService protocolViewerService, MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (protocolViewerService == null) throw new IllegalArgumentException("ProtocolViewerService is null");
		if (molgenisSettings == null) throw new IllegalArgumentException("MolgenisSettings is null");
		this.protocolViewerService = protocolViewerService;
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("catalogs", Lists.newArrayList(protocolViewerService.getCatalogs()));
		model.addAttribute("enableDownloadAction", getEnableDownloadAction());
		model.addAttribute("enableOrderAction", getEnableOrderAction());
		if (!SecurityUtils.currentUserIsAuthenticated())
		{
			model.addAttribute("infoMessage", "You need to sign in to order catalog items");
		}
		return "view-protocolviewer";
	}

	// TODO change to catalogId/selection
	@RequestMapping(value = "/selection/{catalogId}", method = GET)
	@ResponseBody
	public SelectedItemsResponse getSelection(@PathVariable Integer catalogId,
			@RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end,
			@RequestParam(value = "excludes[]", required = false) String[] excludedItems)
			throws UnknownStudyDefinitionException, UnknownCatalogException
	{
		Integer total;
		List<SelectedItemResponse> selectedFeatureUris;
		if (SecurityUtils.currentUserIsAuthenticated())
		{
			StudyDefinition studyDefinition = protocolViewerService.getStudyDefinitionDraftForCurrentUser(catalogId
					.toString());

			if (studyDefinition != null)
			{
				Iterable<CatalogItem> catalogItems = studyDefinition.getItems();

				// exclude specific items
				if (excludedItems != null)
				{
					final Set<String> excludedItemsSet = new HashSet<String>(Arrays.asList(excludedItems));
					catalogItems = Lists.newArrayList(Iterables.filter(catalogItems, new Predicate<CatalogItem>()
					{
						@Override
						public boolean apply(CatalogItem catalogItem)
						{
							return !excludedItemsSet.contains(catalogItem.getId());
						}
					}));
				}

				// convert to feature uris
				selectedFeatureUris = Lists.newArrayList(Iterables.transform(catalogItems,
						new Function<CatalogItem, SelectedItemResponse>()
						{
							@Override
							public SelectedItemResponse apply(CatalogItem catalogItem)
							{
								String featureUri = "/api/v1/observablefeature/" + catalogItem.getId();
								List<String> protocolUris = Lists.newArrayList(Iterables.transform(
										catalogItem.getPath(), new Function<String, String>()
										{
											@Override
											public String apply(String pathElement)
											{
												return "/api/v1/protocol/" + pathElement;
											}

										}));

								return new SelectedItemResponse(featureUri, protocolUris);
							}
						}));

				if (start != null && end != null)
				{
					end = Math.min(selectedFeatureUris.size(), end);
					total = selectedFeatureUris.size();
					selectedFeatureUris = selectedFeatureUris.subList(start, end);
				}
				else
				{
					start = 0;
					end = selectedFeatureUris.size();
					total = selectedFeatureUris.size();
				}
			}
			else
			{
				start = 0;
				end = 0;
				total = 0;
				selectedFeatureUris = Collections.emptyList();
			}
		}
		else
		{
			start = 0;
			end = 0;
			total = 0;
			selectedFeatureUris = Collections.emptyList();
		}
		return new SelectedItemsResponse(start, end, total, selectedFeatureUris);
	}

	@RequestMapping(value = "/download/{catalogId}", method = GET)
	public void downloadSelection(HttpServletResponse response, @PathVariable Integer catalogId) throws IOException,
			UnknownCatalogException
	{
		if (!getEnableDownloadAction()) throw new MolgenisDataAccessException("Action not allowed");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm");
		String fileName = "variables_" + dateFormat.format(new Date()) + ".xls";

		// write response headers
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		protocolViewerService.createStudyDefinitionDraftXlsForCurrentUser(response.getOutputStream(),
				catalogId.toString());
	}

	@RequestMapping(value = "/cart/add/{catalogId}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void addToCart(@Valid @RequestBody CartUpdateRequest cartUpdateRequest, @PathVariable Integer catalogId)
			throws UnknownCatalogException, UnknownStudyDefinitionException
	{
		if (!getEnableOrderAction()) throw new MolgenisDataAccessException("Action not allowed");
		protocolViewerService
				.addToStudyDefinitionDraftForCurrentUser(cartUpdateRequest.getHref(), catalogId.toString());
	}

	@RequestMapping(value = "/cart/remove/{catalogId}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void removeFromCart(@Valid @RequestBody CartUpdateRequest cartUpdateRequest, @PathVariable Integer catalogId)
			throws UnknownCatalogException, UnknownStudyDefinitionException
	{
		if (!getEnableOrderAction()) throw new MolgenisDataAccessException("Action not allowed");
		logger.info("remove from cart: " + cartUpdateRequest.getHref());
		protocolViewerService.removeFromStudyDefinitionDraftForCurrentUser(cartUpdateRequest.getHref(),
				catalogId.toString());
	}

	@RequestMapping(value = "/order", method = RequestMethod.GET)
	public String getOrderDataForm()
	{
		if (!getEnableOrderAction()) throw new MolgenisDataAccessException("Action not allowed");
		return "orderdata-modal";
	}

	// Spring's StandardServletMultipartResolver can't bind a RequestBody or
	// ModelAttribute
	@RequestMapping(value = "/order", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void orderData(@RequestParam Integer catalogId, @RequestParam String name, @RequestParam Part file)
			throws IOException, MessagingException, UnknownCatalogException, UnknownStudyDefinitionException
	{
		if (!getEnableOrderAction()) throw new MolgenisDataAccessException("Action not allowed");
		protocolViewerService.submitStudyDefinitionDraftForCurrentUser(name, file, catalogId.toString());
	}

	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	@ResponseBody
	public StudyDefinitionsResponse getOrders()
	{
		if (!getEnableOrderAction()) throw new MolgenisDataAccessException("Action not allowed");

		Iterable<StudyDefinitionResponse> ordersIterable = Iterables.transform(
				protocolViewerService.getStudyDefinitionsForCurrentUser(),
				new Function<StudyDefinition, StudyDefinitionResponse>()
				{
					@Override
					@Nullable
					public StudyDefinitionResponse apply(@Nullable StudyDefinition studyDefinition)
					{
						return studyDefinition != null ? (studyDefinition.getStatus() != Status.DRAFT ? new StudyDefinitionResponse(
								studyDefinition) : null) : null;
					}
				});

		return new StudyDefinitionsResponse(Lists.newArrayList(ordersIterable));
	}

	@RequestMapping(value = "/orders/view", method = RequestMethod.GET)
	public String getOrdersForm()
	{
		if (!getEnableOrderAction()) throw new MolgenisDataAccessException("Action not allowed");
		return "orderlist-modal";
	}

	@RequestMapping(value = "/orders/{orderId}/view", method = RequestMethod.GET)
	public ModelAndView getOrderDetailsForm(@Valid @NotNull @PathVariable Integer orderId)
			throws UnknownStudyDefinitionException
	{
		if (!getEnableOrderAction()) throw new MolgenisDataAccessException("Action not allowed");
		StudyDefinition studyDefinition = protocolViewerService.getStudyDefinitionForCurrentUser(orderId);
		if (studyDefinition == null) throw new MolgenisDataException("invalid order id");

		ModelAndView model = new ModelAndView("orderdetails-modal");
		model.addObject("order", studyDefinition);
		model.addObject("i18n", new I18nTools());
		return model;
	}

	@ExceptionHandler(
	{ UnknownCatalogException.class, UnknownStudyDefinitionException.class, IOException.class,
			MessagingException.class, RuntimeException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleException(Exception e)
	{
		e.printStackTrace();
		logger.error("Error", e);
		return new ErrorMessageResponse(
				Collections.singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

	private boolean getEnableDownloadAction()
	{
		return molgenisSettings.getBooleanProperty(KEY_ACTION_DOWNLOAD, DEFAULT_KEY_ACTION_DOWNLOAD);
	}

	private boolean getEnableOrderAction()
	{
		return molgenisSettings.getBooleanProperty(KEY_ACTION_ORDER, DEFAULT_KEY_ACTION_ORDER);
	}

	private static class StudyDefinitionsResponse
	{
		private final List<StudyDefinitionResponse> orders;

		public StudyDefinitionsResponse(List<StudyDefinitionResponse> orders)
		{
			this.orders = orders;
		}

		@SuppressWarnings("unused")
		public List<StudyDefinitionResponse> getOrders()
		{
			return orders;
		}
	}

	private static class StudyDefinitionResponse
	{
		private final Integer id;
		private final String name;
		private String orderDate;
		private final String orderStatus;

		public StudyDefinitionResponse(StudyDefinition studyDefinition)
		{
			this.id = Integer.valueOf(studyDefinition.getId());
			this.name = studyDefinition.getName();
			if (studyDefinition.getDateCreated() != null)
			{
				this.orderDate = new SimpleDateFormat("yyyy-MM-dd").format(studyDefinition.getDateCreated());
			}
			this.orderStatus = studyDefinition.getStatus().toString().toLowerCase();
		}

		@SuppressWarnings("unused")
		public Integer getId()
		{
			return id;
		}

		@SuppressWarnings("unused")
		public String getName()
		{
			return name;
		}

		@SuppressWarnings("unused")
		public String getOrderDate()
		{
			return orderDate;
		}

		@SuppressWarnings("unused")
		public String getOrderStatus()
		{
			return orderStatus;
		}
	}

	private static class SelectedItemsResponse
	{
		private final Integer start;
		private final Integer end;
		private final Integer total;
		private final List<SelectedItemResponse> items;

		public SelectedItemsResponse(Integer start, Integer end, Integer total, List<SelectedItemResponse> items)
		{
			this.start = start;
			this.end = end;
			this.total = total;
			this.items = items;
		}

		@SuppressWarnings("unused")
		public Integer getStart()
		{
			return start;
		}

		@SuppressWarnings("unused")
		public Integer getEnd()
		{
			return end;
		}

		@SuppressWarnings("unused")
		public Integer getTotal()
		{
			return total;
		}

		@SuppressWarnings("unused")
		public List<SelectedItemResponse> getItems()
		{
			return items;
		}
	}

	private static class SelectedItemResponse
	{
		private final String feature;
		private final List<String> path;

		public SelectedItemResponse(String feature, List<String> path)
		{
			this.feature = feature;
			this.path = path;
		}

		@SuppressWarnings("unused")
		public String getFeature()
		{
			return feature;
		}

		@SuppressWarnings("unused")
		public List<String> getPath()
		{
			return path;
		}
	}

	private static final class CartUpdateRequest
	{
		@NotNull
		private String href;

		public String getHref()
		{
			return href;
		}

		@SuppressWarnings("unused")
		public void setHref(String href)
		{
			this.href = href;
		}
	}
}
