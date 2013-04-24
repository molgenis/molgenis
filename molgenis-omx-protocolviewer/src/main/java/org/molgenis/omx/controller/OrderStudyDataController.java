package org.molgenis.omx.controller;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.servlet.http.Part;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.service.OrderStudyDataService;
import org.molgenis.util.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/plugin")
public class OrderStudyDataController
{
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
		orderStudyDataService.orderStudyData(name, file, shoppingCart.getCart());
		shoppingCart.emptyCart();
	}

	// TODO default exception handler?
	@ExceptionHandler(DatabaseException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public void handleDatabaseException(DatabaseException e)
	{
	}
}
