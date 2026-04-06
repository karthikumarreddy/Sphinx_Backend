package com.sphinx.resources;

import java.util.HashMap;
import java.util.Map;

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
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/topics")
public class TopicResource {
	private static final String MODULE = TopicResource.class.getName();
	@Context
	private HttpServletRequest request;

	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		return dispatcher;
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

		if (topicId == null) {
			return Response.status(400).entity(ServiceUtil.returnError("Smething went wrong while selecting the topic ")).build();
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
	public Response updateTopic(@Context HttpServletRequest request) {
		try {
			String topicId = (String) request.getAttribute("topicId");
			String topicName = (String) request.getAttribute("topicName");
			if (topicId == null || topicId.isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("Something went wrong wile fetching the topics try later "))
								.build();
			}
			if (topicName == null || topicName.isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("topicname cannot be empty ")).build();
			}
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("topicId", topicId);
			input.put("topicName", topicName);

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
	public Response deleteTopic(@Context HttpServletRequest request) {
		try {
			String topicId = (String) request.getAttribute("topicId");
			if (topicId == null || topicId.isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("topicId in empty ")).build();
			}
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("topicId", topicId);
			Map<String, Object> result = getDispatcher().runSync("deleteTopic", input);
			return Response.status(200).entity(ServiceUtil.returnSuccess("Topic deleted sucessfully")).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build(); // TODO: handle
																									// exception
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTopic(@Context HttpServletRequest request) {
		try {
			String topicName = (String) request.getAttribute("topicName");
			if (topicName == null || topicName.isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("Topic name is empty ")).build();
			}

			Map<String, Object> result = getDispatcher().runSync("createTopicValidator", UtilMisc.toMap("topicName", topicName));

			if (result.get("responseMessage") != null && result.get("responseMessage").equals("success")) {
				result.put("successMessage", "Topic created successfully!");
				return Response.status(201).entity(result).build();
			} else {
				return Response.status(400).entity(result).build();
			}

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

}
