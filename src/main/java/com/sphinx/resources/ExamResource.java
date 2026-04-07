package com.sphinx.resources;

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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/exam")
public class ExamResource {

	private static final String MODULE = ExamResource.class.getName();

	@Context
	private HttpServletRequest request;

	private Delegator getDelegator() {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		return delegator;
	}

	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		return dispatcher;

	}

	private String validateExam(Map<String, String> map) {
		if (UtilValidate.isEmpty(map.get("partyId")))
			return "Admin Details are Invalid";
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

	@GET
	@Path("/{examId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExamById(@PathParam("examId") String examId) {

		try {
			if (examId == null || examId.isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("Input is empty")).build();
			}

			Map<String, Object> input = new HashMap<>();
			input.put("examId", examId);

			Map<String, Object> result = getDispatcher().runSync("getExamById", input);

			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(400).entity(ServiceUtil.returnError("Something went wrong")).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createExam(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		Map<String, String> map = new HashMap<>();

		map.put("partyId", (String) request.getAttribute("partyId"));
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
			Map<String, Object> result = getDispatcher().runSync("createExamWrapper", map);
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
	public Response updateExam(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		Map<String, String> map = new HashMap<>();

		map.put("partyId", (String) request.getAttribute("partyId"));
		map.put("examId", (String) request.getAttribute("examId"));
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
	public Response deleteExam(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		String error = validateExamId((String) request.getAttribute("examId"));
		Map<String, Object> examMap = new HashMap<String, Object>();
		examMap.put("examId", (String) request.getAttribute("examId"));

		if (error != null) {
			return Response.status(400).entity(ServiceUtil.returnError(error)).build();
		}

		try {
			Map<String, Object> result = getDispatcher().runSync("deleteExamWrapper", examMap);
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
	public Response addExamTopics(@Context HttpServletRequest request) {

		if (request.getAttribute("examId").toString().isEmpty()
				|| request.getAttribute("topicId").toString().isEmpty()) {
			return Response.status(400).entity(ServiceUtil.returnError("ExamId and TopicId required")).build();
		}
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("examId", request.getAttribute("examId"));
		input.put("topicId", request.getAttribute("topicId"));
		input.put("topicName", (String) request.getAttribute("topicName"));
		input.put("percentage", request.getAttribute("percentage"));

		try {
			Map<String, Object> result = getDispatcher().runSync("addExamTopics", input);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/topics")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExamTopics(@Context HttpServletRequest request) {
		try {
			String examId = request.getParameter("examId");
			if (examId == null)
				return Response.status(400).entity(ServiceUtil.returnError("Exam is null ")).build();

			Map<String, Object> result = getDispatcher().runSync("getAllExamTopics", UtilMisc.toMap("examId", examId));

			return Response.status(200).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@PUT
	@Path("/topics")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateExamTopic(@Context HttpServletRequest request) {
		try {
			String examId = (String) request.getAttribute("examId");
			String percentage = (String) request.getAttribute("percentage");
			String questionsPerExam = (String) request.getAttribute("questionsPerExam");
			String topicId = (String) request.getAttribute("topicId");
			String topicName = (String) request.getAttribute("topicName");

			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			input.put("percentage", percentage);
			input.put("topicId", topicId);
			input.put("topicName", topicName);

			LocalDispatcher dispatcher = getDispatcher();
			Map<String, Object> result = dispatcher.runSync("updateExamTopics", input);
			return Response.status(201).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity("Something went wrong try later").build();
		}
	}

	@DELETE
	@Path("/topics")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response DeleteEamTopic(@Context HttpServletRequest request) {
		try {
			String examId = (String) request.getAttribute("examId");
			String topicId = (String) request.getAttribute("topicId");
			LocalDispatcher dispatcher = getDispatcher();
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			input.put("topicId", topicId);

			Map<String, Object> result = dispatcher.runSync("deleteExamTopics", input);
			return Response.status(201).entity(result).build();

		} catch (Exception e) {
			return Response.status(5000).entity(ServiceUtil.returnError("Something went wrong try later")).build();
		}
	}

	@GET
	@Path("/topics/{examId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExamTopicsByExamId(@PathParam("examId") String examId) {

		try {
			if (examId == null || examId.isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("ExamId is required")).build();
			}

			Map<String, Object> input = new HashMap<>();
			input.put("examId", examId);

			Map<String, Object> result = getDispatcher().runSync("getExamTopicsByExamId", input);

			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity(ServiceUtil.returnError("Something went wrong")).build();
		}
	}

	// generate questions to the topic
	@POST
	@Path("/generate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateQuestions(@Context HttpServletRequest request) {
		try {
			String examId = (String) request.getAttribute("examId");
			if (examId == null) {
				return Response.status(400).entity(ServiceUtil.returnError("exami id is null")).build();
			}
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
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
	public Response launchExam(@Context HttpServletRequest request) {
		try {
			String examId = (String) request.getAttribute("examId");
			if (examId == null) {
				return Response.status(400).entity(ServiceUtil.returnError("exam id is null")).build();
			}
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			Map<String, Object> result = getDispatcher().runSync("launchExam", input);
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	// launch the exam
	@POST
	@Path("/assignUser")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignUser(@Context HttpServletRequest request) {
		try {

			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (dispatcher == null) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			// Map<String, Object> result =
			// dispatcher.runSync("createPartyExamRelationshipWrapper",
			// UtilMisc.toMap("partyId",
			// request.getAttribute("partyId"),
			// "examId", request.getAttribute("examId"), "allowedAttempts",
			// request.getAttribute("allowedAttempts"), "noOfAttempts",
			// request.getAttribute("noOfAttempts"), "timeoutDays",
			// request.getAttribute("timeoutDays"), "fromDate",
			// request.getAttribute("fromDate"), "thruDate",
			// request.getAttribute("thruDate")));

			List<Map<String, Object>> listOfUsers = (List<Map<String, Object>>) request.getAttribute("users");

			Map<String, Object> result = dispatcher.runSync("createPartyExamRelationshipWrapper",
					UtilMisc.toMap("users", listOfUsers));

			if (result.containsKey("responseMessage") && result.get("responseMessage").equals("success")) {
				result.put("successMessage", "User Assigned to the Exam!");
				return Response.status(Response.Status.CREATED).entity(result).build();
			} else {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			}

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/assignedExams/{partyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserAssignedExams(@PathParam("partyId") String partyId) {
		try {
			if (partyId == null || partyId.isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("PartyId is required")).build();
			}

			Map<String, Object> input = new HashMap<>();
			input.put("partyId", partyId);

			Map<String, Object> result = getDispatcher().runSync("getUserAssignedExams", input);

			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError("Something went wrong")).build();
		}
	}

	@POST
	@Path("/removeAssignedUserFromExam")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeAssignedUser(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (dispatcher == null) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			Map<String, Object> result = dispatcher.runSync("removeAssignedUserFromExamWrapper", UtilMisc
					.toMap("partyId", request.getAttribute("partyId"), "examId", request.getAttribute("examId")));

			return Response.ok().entity(result).build();

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@POST
	@Path("/getAssignedUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssignedUsers(@Context HttpServletRequest request) {

		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		if (dispatcher == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
		}

		try {
			Map<String, Object> result = dispatcher.runSync("getAllAssignedUsersForExam",
					UtilMisc.toMap("examId", request.getAttribute("examId")));

			return Response.status(Response.Status.OK).entity(result).build();
		} catch (GenericServiceException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
		}

	}

	@POST
	@Path("/getAllExamAssignedForUser")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllExamAssignedForUser(@Context HttpServletRequest request) {

		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		if (dispatcher == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
		}

		try {
			Map<String, Object> result = dispatcher.runSync("getAllExamAssignedForUser",
					UtilMisc.toMap("partyId", request.getAttribute("partyId")));

			return Response.status(Response.Status.OK).entity(result).build();
		} catch (GenericServiceException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
		}

	}

	@POST
	@Path("/getAllExamsByAdmin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllExamsByAdmin(@Context HttpServletRequest request) {
		try {
			String partyId = (String) request.getAttribute("partyId");
			if (partyId == null || partyId.isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("Login to proceed"))
						.build();
			}
			Map<String, Object> result = getDispatcher().runSync("getAllExamsByAdmin",
					UtilMisc.toMap("partyId", partyId));

			return Response.status(Response.Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Something went wrong try later")).build();
		}
	}

	@GET
	@Path("/getAllExamQuestions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllExamQuestions(@Context HttpServletRequest request) {
		try {
			String examId = request.getParameter("examId");
			if (examId == null || examId.isEmpty()) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("examId is required")).build();
			}

			Map<String, Object> result = getDispatcher().runSync("getAllExamQuestions",
					UtilMisc.toMap("examId", examId));

			if (result == null || result.isEmpty()) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Exam id is undefined ")).build();
			}
			return Response.status(Response.Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Something went wrong try again later")).build();
		}
	}

}