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

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {
	@Context
	private HttpServletRequest request;
    @Context
    private ServletContext servletContext;

    private Delegator getDelegator() {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        if (delegator == null) {
            delegator = DelegatorFactory.getDelegator("default");
        }
        return delegator;
    }

    private LocalDispatcher getDispatcher() {
        LocalDispatcher dispatcher = (LocalDispatcher) servletContext.getAttribute("dispatcher");
        if (dispatcher == null) {
            dispatcher = ServiceContainer.getLocalDispatcher("Sphinx",
                    getDelegator());
        }
        return dispatcher;
    }

  
    // User approval Services
    @POST
    @Path("/approve")
    public Response approveUser(Map<String, Object> input) {
        try {
            if (input.get("userName") == null)
                return Response.status(400)
                        .entity(Map.of("error", "The 'userName' field is required.")).build();

            LocalDispatcher dispatcher = getDispatcher();
            if (dispatcher == null)
                return Response.status(500)
                        .entity(Map.of("error", "Dispatcher not available.")).build();

            Map<String, Object> result = dispatcher.runSync("approveUser", input);
            return Response.ok(result).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }
}