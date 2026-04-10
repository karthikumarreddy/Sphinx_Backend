package com.sphinx.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import clojure.repl__init;

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
		} 
			try {

				Map<String, Object> result = dispatcher.runSync("getAllUsers", Collections.emptyMap());
				return Response.status(Response.Status.OK).entity(result).build();
			} catch (GenericServiceException e) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after sometime!")).build();
			}
	}
	
	@GET
	@Path("/getAllUsersCount")
	public Response getAllUsersCount(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		if (UtilValidate.isEmpty(dispatcher)) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
		} 
			try {

				Map<String, Object> result = dispatcher.runSync("getAllUsersCount", Collections.emptyMap());
				return Response.status(Response.Status.OK).entity(result).build();
			} catch (GenericServiceException e) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after sometime!")).build();
			}
	}

	@POST
	@Path("/login")
	public Response loginUser(@Context HttpServletRequest request, @Context HttpServletResponse response) {
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
			String confirmPassword = (String) request.getAttribute("confirmPassword");
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

			Map<String, Object> result = dispatcher.runSync("signupUser",
					UtilMisc.toMap("userName", userName, "firstName", firstName, "lastName", lastName, "email", email,
							"password", password, "confirmPassword", confirmPassword, "role", role));
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

	@DELETE
	public static Response deleteUsers(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			List<String> partyIds = (List<String>) request.getAttribute("partyIds");

			if (UtilValidate.isEmpty(partyIds)) {
				return Response.status(400).entity(ServiceUtil.returnError("Part id is required")).build();
			}
			Map<String, Object> result = dispatcher.runSync("deleteUser", UtilMisc.toMap("partyIds", partyIds));
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@PUT
	public static Response updateUser(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String partyId = (String) request.getAttribute("partyId");
			String firstName = (String) request.getAttribute("firstName");
			String lastName = (String) request.getAttribute("lastName");

			if (UtilValidate.isEmpty(partyId)) {
				return Response.status(400).entity(ServiceUtil.returnError("Party Id is required")).build();
			}
			if (UtilValidate.isEmpty(firstName)) {
				return Response.status(400).entity(ServiceUtil.returnError("First Name is required")).build();
			}
			if (UtilValidate.isEmpty(lastName)) {
				return Response.status(400).entity(ServiceUtil.returnError("Last Name is required")).build();
			}
			Map<String, Object> result = dispatcher.runSync("updateUser",
					UtilMisc.toMap("partyId", partyId, "firstName", firstName, "lastName", lastName));

			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}
	
	

}