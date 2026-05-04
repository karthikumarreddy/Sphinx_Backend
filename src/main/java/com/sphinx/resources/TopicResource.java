package com.sphinx.resources;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/topics")
public class TopicResource {
	private static final String MODULE = TopicResource.class.getName();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllTopics(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			HttpSession session = request.getSession(false);
			GenericValue userLogin = null;

			if (UtilValidate.isNotEmpty(session) && UtilValidate.isNotEmpty(session.getAttribute("userLogin"))) {
				userLogin = (GenericValue) session.getAttribute("userLogin");
			}

			String partyId = (String) request.getAttribute("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				partyId = userLogin.getString("partyId");
			}

			Map<String, Object> result = dispatcher.runSync("getAllTopics", UtilMisc.toMap("partyId", partyId));
			return Response.ok().entity(result).build();

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
	public Response getTopicById(@PathParam("topicId") String topicId, @Context HttpServletRequest request) {

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			Map<String, Object> result = dispatcher.runSync("getTopicById", UtilMisc.toMap("topicId", topicId));
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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String topicId = (String) request.getAttribute("topicId");
			String topicName = (String) request.getAttribute("topicName");
			String partyId=(String) request.getAttribute("partyId");
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("topicId", topicId);
			input.put("topicName", topicName);
			input.put("partyId", partyId);

			Map<String, Object> result = dispatcher.runSync("updateTopicWrapper", input);
			if(ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String topicId = (String) request.getAttribute("topicId");
			String partyId=(String) request.getAttribute("partyId");
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("topicId", topicId);
			input.put("partyId", partyId);
			Map<String, Object> result = dispatcher.runSync("deleteTopicWrapper", input);
			if(ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(ServiceUtil.returnSuccess("Topic deleted sucessfully")).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build(); 
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTopic(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
			GenericValue userLogin = null;

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String topicName = (String) request.getAttribute("topicName");
			String topicId=topicName;
			String partyId=(String) request.getAttribute("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				HttpSession session = request.getSession(false);
				if (UtilValidate.isNotEmpty(session) && UtilValidate.isNotEmpty(session.getAttribute("userLogin"))) {
					userLogin = (GenericValue) session.getAttribute("userLogin");
					partyId = userLogin.getString("partyId");
				}
			}

			Map<String, Object> result = dispatcher.runSync("createTopicValidator",
					UtilMisc.toMap("partyId",partyId,"topicId",topicId,"topicName", topicName));

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

	@GET
	@Path("getTopicCount")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTopicCount(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String partyId=(String) request.getAttribute("partyId");
			
			Map<String, Object> result = dispatcher.runSync("getAllTopicsCount", UtilMisc.toMap("partyId",partyId));
			if(ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.ok(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

}
