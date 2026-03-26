package com.sphinx.resources;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.*;

@Path("/exam")
public class ExamResource {

	private static final String MODULE = ExamResource.class.getName();

	@Context
	private HttpServletRequest request;

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
			dispatcher = ServiceContainer.getLocalDispatcher("Sphinx", getDelegator());
		}
		return dispatcher;
	}

	private String validateExam(Map<String, Object> input) {
		if (UtilValidate.isEmpty(input.get("examName")))
			return "Exam name is required";
		if (UtilValidate.isEmpty(input.get("noOfQuestions")))
			return "Number of questions required";
		if (UtilValidate.isEmpty(input.get("duration")))
			return "Duration required";
		if (UtilValidate.isEmpty(input.get("passPercentage")))
			return "Pass percentage required";
		return null;
	}

	private String validateExamId(String examId) {
		if (UtilValidate.isEmpty(examId))
			return "ExamId is required";
		return null;
	}

	// exam crud
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExam() {
		try {
			Map<String, Object> result = getDispatcher().runSync("getExam", UtilMisc.toMap());
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createExam(Map<String, Object> input) {

		String error = validateExam(input);
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}
		try {
			Map<String, Object> result = getDispatcher().runSync("createExamWrapper", input);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateExam(Map<String, Object> input) {

		String error = validateExamId((String) input.get("examId"));
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("updateExam", input);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteExam(Map<String, Object> input) {

		String error = validateExamId((String) input.get("examId"));
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("deleteExam", input);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	// exam specific topic crud

	@POST
	@Path("/topics")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addExamTopics(Map<String, Object> input) {

		if (input.get("examId").toString().isEmpty() || input.get("topicId").toString().isEmpty()) {
			return Response.status(400).entity(ServiceUtil.returnError("ExamId and TopicId required")).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("addExamTopics", input);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	// generate questions to the topic
	@POST
	@Path("/generate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateQuestions(Map<String, Object> input) {

		String error = validateExamId((String) input.get("examId"));
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("generateExamQuestions", input);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	// launch th exam
	@POST
	@Path("/launch")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response launchExam(Map<String, Object> input) {

		String error = validateExamId((String) input.get("examId"));
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("launchExam", input);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}
}