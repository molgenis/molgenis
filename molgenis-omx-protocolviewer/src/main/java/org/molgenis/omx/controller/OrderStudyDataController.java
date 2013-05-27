package org.molgenis.omx.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.servlet.http.Part;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.service.OrderStudyDataService;
import org.molgenis.util.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping("/plugin")
public class OrderStudyDataController
{
	@Autowired
	private Login login;

	@Autowired
	private OrderStudyDataService orderStudyDataService;

	@Autowired
	private ShoppingCart shoppingCart;

	@RequestMapping(value = "/order", method = RequestMethod.GET)
	public String getOrderDataForm() throws DatabaseException
	{
		return "orderdata-modal";
	}

	// Spring's StandardServletMultipartResolver can't bind a RequestBody or ModelAttribute
	@RequestMapping(value = "/order", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void orderData(@RequestParam String name, @RequestParam Part file) throws DatabaseException, IOException,
			MessagingException
	{
		orderStudyDataService.orderStudyData(name, file, shoppingCart.getCart(), login.getUserId());
		shoppingCart.emptyCart();
	}

	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	@ResponseBody
	public OrdersResponse getOrders() throws DatabaseException
	{
		Iterable<OrderResponse> ordersIterable = Iterables.transform(
				orderStudyDataService.getOrders(login.getUserId()), new Function<StudyDataRequest, OrderResponse>()
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

	private static class OrdersResponse
	{
		private List<OrderResponse> orders;

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
		private Integer id;
		private String name;
		private String orderDate;
		private String orderStatus;

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

	// TODO default exception handler?
	@ExceptionHandler(DatabaseException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public void handleDatabaseException(DatabaseException e)
	{
	}
}
