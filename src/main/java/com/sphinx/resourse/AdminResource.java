package com.sphinx.resourse;

import java.util.Map;

import javax.servlet.ServletContext;
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

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {
	@Context
	private ServletContext servletContext;

	private Delegator getDelegator() {
		Delegator delegator = (Delegator) servletContext.getAttribute("delegator");
		if (delegator == null) {
			delegator = DelegatorFactory.getDelegator("default");
		}
		return delegator;
	}

	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) servletContext.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("Sphinx", // must match localDispatcherName in web.xml
					getDelegator());
		}
		return dispatcher;
	}
	
	@POST
	@Path("/approve")
	public Response approveUser(Map<String,Object> input) {
		try {
		if(input.get("userName") == null) 
			 return Response.status(404).entity(Map.of("error", "userName is required")).build();
		
		LocalDispatcher dispatcher=getDispatcher();
		Map<String,Object> result=dispatcher.runSync("approveUser", input);
		return Response.ok(result).build();
		
		}catch (Exception e) {
			return Response.status(500).entity(Map.of("error","something went wromg try later")).build();
		}
		
	}

}
