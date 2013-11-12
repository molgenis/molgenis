package org.molgenis.omx.order;

import static org.molgenis.omx.order.OrderStudyDataController.URI;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.servlet.http.Part;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.omx.utils.I18nTools;
import org.molgenis.security.SecurityUtils;
import org.molgenis.ui.util.ErrorMessageResponse;
import org.molgenis.ui.util.ErrorMessageResponse.ErrorMessage;
import org.molgenis.util.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class OrderStudyDataController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(OrderStudyDataController.class);

	public static final String URI = "/plugin/study";

	@Autowired
	private OrderStudyDataService orderStudyDataService;

	@Autowired
	private ShoppingCart shoppingCart;

	public OrderStudyDataController()
	{
		super(URI);
	}

	@RequestMapping(value = "/order", method = RequestMethod.GET)
	public String getOrderDataForm() throws DatabaseException
	{
		return "orderdata-modal";
	}

	// Spring's StandardServletMultipartResolver can't bind a RequestBody or
	// ModelAttribute
	@RequestMapping(value = "/order", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void orderData(@RequestParam String dataSetIdentifier, @RequestParam String name, @RequestParam Part file)
			throws DatabaseException, IOException, MessagingException
	{
		orderStudyDataService.orderStudyData(name, file, dataSetIdentifier, shoppingCart.getCart(),
				SecurityUtils.getCurrentUsername());
		shoppingCart.emptyCart();
	}

	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	@ResponseBody
	public OrdersResponse getOrders() throws DatabaseException
	{
		Iterable<OrderResponse> ordersIterable = Iterables.transform(
				orderStudyDataService.getOrders(SecurityUtils.getCurrentUsername()),
				new Function<StudyDataRequest, OrderResponse>()
				{
					@Override
					@Nullable
					public OrderResponse apply(@Nullable StudyDataRequest studyDataRequest)
					{
						return studyDataRequest != null ? new OrderResponse(studyDataRequest) : null;
					}
				});
		return new OrdersResponse(Lists.newArrayList(ordersIterable));
	}

	@RequestMapping(value = "/orders/view", method = RequestMethod.GET)
	public String getOrdersForm() throws DatabaseException
	{
		return "orderlist-modal";
	}

	@RequestMapping(value = "/orders/{orderId}/view", method = RequestMethod.GET)
	public ModelAndView getOrderDetailsForm(@Valid @NotNull @PathVariable Integer orderId) throws DatabaseException
	{
		StudyDataRequest studyDataRequest = orderStudyDataService.getOrder(orderId);
		if (studyDataRequest == null) throw new DatabaseException("invalid order id");

		ModelAndView model = new ModelAndView("orderdetails-modal");
		model.addObject("order", studyDataRequest);
		model.addObject("i18n", new I18nTools());
		return model;
	}

	private static class OrdersResponse
	{
		private final List<OrderResponse> orders;

		public OrdersResponse(List<OrderResponse> orders)
		{
			this.orders = orders;
		}

		@SuppressWarnings("unused")
		public List<OrderResponse> getOrders()
		{
			return orders;
		}
	}

	private static class OrderResponse
	{
		private final Integer id;
		private final String name;
		private final String orderDate;
		private final String orderStatus;

		public OrderResponse(StudyDataRequest studyDataRequest)
		{
			this.id = studyDataRequest.getId();
			this.name = studyDataRequest.getName();
			this.orderDate = new SimpleDateFormat("yyyy-MM-dd").format(studyDataRequest.getRequestDate());
			this.orderStatus = studyDataRequest.getRequestStatus();
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

	@ExceptionHandler(DatabaseException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleDatabaseException(DatabaseException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(IOException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleIOException(IOException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(MessagingException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleMessagingException(MessagingException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}
}
