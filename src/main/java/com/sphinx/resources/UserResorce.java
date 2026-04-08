package com.sphinx.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResorce {

	@GET
	@Path("/getAllUsers")
	public Response getAllUsers(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		if (UtilValidate.isEmpty(dispatcher)) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
		} else
			try {

				Map<String, Object> result = dispatcher.runSync("getAllUsers", Collections.emptyMap());
				return Response.status(Response.Status.OK).entity(result).build();
			} catch (GenericServiceException e) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after sometime!")).build();
			}
	}

	@POST
	@Path("/login")
	public Response loginUser(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String userName = (String) request.getAttribute("userName");
			String password = (String) request.getAttribute("password");
			if (UtilValidate.isEmpty(userName)) {
				return Response.status(400).entity("UserName is required ").build();
			}
			if (UtilValidate.isEmpty(password)) {
				return Response.status(400).entity("Password is required ").build();
			}
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("userName", userName);
			input.put("password", password);

			if (!Pattern.matches("^[a-zA-Z0-9]{4,20}$", userName))
				return Response.status(400).entity(Map.of("error",
						"Invalid username. It must be 4–20 characters long and contain only letters and numbers."))
						.build();

			if (!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", password))
				return Response.status(400).entity(Map.of("error",
						"Invalid password format. Password must be at least 8 characters  and include uppercase, lowercase, a number, and a special character."))
						.build();

			Map<String, Object> result = dispatcher.runSync("loginUser", input);
			return Response.status(200).entity(result).build();

		} catch (Exception e) {

			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@POST
	@Path("/signup")
	public Response signupUser(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String userName = (String) request.getAttribute("userName");
			String firstName = (String) request.getAttribute("firstName");
			String lastName = (String) request.getAttribute("lastName");
			String email = (String) request.getAttribute("email");
			String password = (String) request.getAttribute("password");
			String role = (String) request.getAttribute("role");

			if (UtilValidate.isEmpty(userName)) {
				return Response.status(400).entity("UserName is required").build();
			}
			if (UtilValidate.isEmpty(firstName)) {
				return Response.status(400).entity("FirstName is required").build();
			}
			if (UtilValidate.isEmpty(lastName)) {
				return Response.status(400).entity("LastName is required").build();
			}
			if (UtilValidate.isEmpty(email)) {
				return Response.status(400).entity("Email is required").build();
			}
			if (UtilValidate.isEmpty(password)) {
				return Response.status(400).entity("Password is required").build();
			}
			if (UtilValidate.isEmpty(role)) {
				return Response.status(400).entity("Role is required").build();
			}

			Map<String, Object> result = dispatcher.runSync("signupUser", UtilMisc.toMap("userName", userName,
					"firstName", firstName, "lastName", lastName, "email", email, "password", password, "role", role));
			if (result.containsKey("responseMessage")
					&& "error".equalsIgnoreCase((String) result.get("responseMessage"))) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			}
			return Response.status(Response.Status.CREATED).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}
}