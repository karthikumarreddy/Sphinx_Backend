package com.sphinx.resources;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/topics")
public class TopicResource {
	private static final String MODULE = TopicResource.class.getName();
	@Context
	private HttpServletRequest request;

	@Context
	private ServletContext servletContext;
	

	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		return dispatcher;
	}

	private String validateTopicId(String topicId) {
		if (UtilValidate.isEmpty(topicId))
			return "TopicId is required";
		return null;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllTopics() {
		try {
			Map<String, Object> result = getDispatcher().runSync("getAllTopics", UtilMisc.toMap());
			return Response.ok(result).build();
		} catch (Exception e) {
			Debug.log(MODULE);
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/{topicId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTopicById(@PathParam("topicId") String topicId) {

		String error = validateTopicId(topicId);
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("getTopicById", UtilMisc.toMap("topicId", topicId));
			return Response.ok(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTopic(Map<String, Object> input) {
		try {
			if (input.get("topicName") == null || input.get("topicName").toString().isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("topicname cannot be empty ")).build();
			}
			Map<String, Object> result = getDispatcher().runSync("updateTopic", input);
			return Response.status(200).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteTopic(Map<String, Object> input) {
		try {
			if (input.get("topicId") == null || input.get("topicId").toString().isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("topicId in empty ")).build();
			}
			Map<String, Object> result = getDispatcher().runSync("deleteTopic", input);
			return Response.status(200).entity(ServiceUtil.returnSuccess("Topic deleted sucessfully")).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();			// TODO: handle exception
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTopic(Map<String, Object> input) {
		try {
			if (input.get("topicName") == null || input.get("topicName").toString().isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("Topic name is empty ")).build();
			}
			Map<String, Object> result = getDispatcher().runSync("createTopic", input);
			return Response.status(200).entity(ServiceUtil.returnSuccess("Topic created suceddfully .")).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();		}
	}

}
