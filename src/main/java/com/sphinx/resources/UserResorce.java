package com.sphinx.resources;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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


@Path("/user")
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



	@DELETE
	public static Response deleteUsers(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			@SuppressWarnings("unchecked")
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