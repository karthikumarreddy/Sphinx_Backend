package com.sphinx.resources;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
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

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/exam")
public class ExamResource {

	private static final String MODULE = ExamResource.class.getName();

	@Context
	private HttpServletRequest request;

	@Context
	private ServletContext servletContext;

	private Delegator getDelegator() {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		return delegator;
	}

	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) servletContext.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("Sphinx", getDelegator());
		}
		return dispatcher;
	}

	private String validateExam(Map<String, String> map) {
		if (UtilValidate.isEmpty(map.get("examName")))
			return "Exam name is required";
		if (UtilValidate.isEmpty(map.get("noOfQuestions")))
			return "Number of questions required";
		if (UtilValidate.isEmpty(map.get("duration")))
			return "Duration required";
		if (UtilValidate.isEmpty(map.get("passPercentage")))
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
	public Response createExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		
		Map<String, String> map = new HashMap<>();

		map.put("examName", (String) request.getAttribute("examName"));
		map.put("description", (String) request.getAttribute("description"));
		map.put("noOfQuestions", (String) request.getAttribute("noOfQuestions"));
		map.put("duration", (String) request.getAttribute("duration"));
		map.put("passPercentage", (String) request.getAttribute("passPercentage"));
		map.put("questionsRandomized", (String) request.getAttribute("questionsRandomized"));
		map.put("answersMust", (String) request.getAttribute("answersMust"));
		map.put("allowNegativeMarks", (String) request.getAttribute("allowNegativeMarks"));
		map.put("negativeMarkValue", (String) request.getAttribute("negativeMarkValue"));
		map.put("userLoginId", (String) request.getAttribute("userLoginId"));

		String error = validateExam(map);
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}
		try {
			Map<String, Object> result = getDispatcher().runSync("createExam", map);
			 request.getAttribute("");
			return Response.status(201).entity(result).build();
			
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {
		
		Map<String, String> map = new HashMap<>();
		
		map.put("examId",(String) request.getAttribute("examId"));
		map.put("examName", (String) request.getAttribute("examName"));
		map.put("description", (String) request.getAttribute("description"));
		map.put("noOfQuestions", (String) request.getAttribute("noOfQuestions"));
		map.put("duration", (String) request.getAttribute("duration"));
		map.put("passPercentage", (String) request.getAttribute("passPercentage"));
		map.put("questionsRandomized", (String) request.getAttribute("questionsRandomized"));
		map.put("answersMust", (String) request.getAttribute("answersMust"));
		map.put("allowNegativeMarks", (String) request.getAttribute("allowNegativeMarks"));
		map.put("negativeMarkValue", (String) request.getAttribute("negativeMarkValue"));
		map.put("userLoginId", (String) request.getAttribute("userLoginId"));

		String error = validateExam(map);
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("updateExam", map);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteExam(@Context HttpServletRequest request,@Context HttpServletResponse response) {

		String error = validateExamId((String) request.getAttribute("examId"));
		Map<String,Object> examMap=new HashMap<String, Object>();
		examMap.put("examId",(String) request.getAttribute("examId"));
		
		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("deleteExam",examMap );
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