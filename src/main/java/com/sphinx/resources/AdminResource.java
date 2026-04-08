package com.sphinx.resources;

import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {
	@Context
	private HttpServletRequest request;
	@Context
	private ServletContext servletContext;

	

	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) servletContext.getAttribute("dispatcher");
		return dispatcher;
	}

	// User approval Services
	@POST
	@Path("/approve")
	public Response approveUser(Map<String, Object> input) {
		try {
			if (input.get("userName") == null)
				return Response.status(400).entity(Map.of("error", "The 'userName' field is required.")).build();

			LocalDispatcher dispatcher = getDispatcher();
			if (dispatcher == null)
				return Response.status(500).entity(Map.of("error", "Dispatcher not available.")).build();

			Map<String, Object> result = dispatcher.runSync("approveUser", input);
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();		}
	}
}