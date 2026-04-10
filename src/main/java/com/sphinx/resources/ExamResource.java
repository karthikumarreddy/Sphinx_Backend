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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/exam")
public class ExamResource {

	private static final String MODULE = ExamResource.class.getName();

	private String validateExam(Map<String, String> map) {
		if (UtilValidate.isEmpty(map.get("partyId")))
			return "Admin details are invalid";
		if (UtilValidate.isEmpty(map.get("examName")))
			return "Exam name is required";
		if (UtilValidate.isEmpty(map.get("description")))
			return "Exam description is required";
		if (UtilValidate.isEmpty(map.get("noOfQuestions")))
			return "Number of questions is required";
		if (UtilValidate.isEmpty(map.get("duration")))
			return "Duration is required";
		if (UtilValidate.isEmpty(map.get("passPercentage")))
			return "Pass percentage is required";
		if (UtilValidate.isEmpty(map.get("questionsRandomized")))
			return "Questions randomized preference is required";
		if (UtilValidate.isEmpty(map.get("answersMust")))
			return "Answers must preference is required";
		if (UtilValidate.isEmpty(map.get("allowNegativeMarks")))
			return "Allow negative marks preference is required";
		if ("Y".equalsIgnoreCase(map.get("allowNegativeMarks")) && UtilValidate.isEmpty(map.get("negativeMarkValue")))
			return "Negative mark value is required when negative marks are allowed";
		return null;
	}

	// exam crud
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExam(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			Map<String, Object> result = dispatcher.runSync("getExam", UtilMisc.toMap());
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/{examId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExamById(@PathParam("examId") String examId, @Context HttpServletRequest request) {

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			if (examId == null || examId.isEmpty()) {
				return Response.status(400).entity(ServiceUtil.returnError("Input is empty")).build();
			}

			Map<String, Object> input = new HashMap<>();
			input.put("examId", examId);

			Map<String, Object> result = dispatcher.runSync("getExamById", input);

			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(400).entity(ServiceUtil.returnError("Something went wrong")).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createExam(@Context HttpServletRequest request) {

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
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

			Map<String, Object> result = dispatcher.runSync("createExamWrapper", map);
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

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
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

			Map<String, Object> result = dispatcher.runSync("updateExam", map);
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
		try {

			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String examId = (String) request.getAttribute("examId");
			if (UtilValidate.isEmpty(examId)) {
				return Response.status(400).entity("ExamId is required").build();
			}

			Map<String, Object> examMap = new HashMap<String, Object>();

			examMap.put("examId", examId);

			Map<String, Object> result = dispatcher.runSync("deleteExamWrapper", examMap);
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

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String examId = (String) request.getAttribute("examId");
			String topicId = (String) request.getAttribute("topicId");
			String topicName = (String) request.getAttribute("topicName");
			String percentage = (String) request.getAttribute("percentage");
			String topicPassPercentage = (String) request.getAttribute("topicPassPercentage");

			if (UtilValidate.isEmail(examId)) {
				return Response.status(400).entity("Exam id is empty ").build();
			}
			if (UtilValidate.isEmpty(topicId)) {
				return Response.status(400).entity("TopicId is Required").build();
			}
			if (UtilValidate.isEmpty(topicName)) {
				return Response.status(400).entity("Topic name is required ").build();
			}
			if (UtilValidate.isEmpty(percentage)) {
				return Response.status(400).entity("Question percentage is required ").build();
			}
			if (UtilValidate.isEmpty(topicPassPercentage)) {
				return Response.status(400).entity("Topic passpercentage is required ").build();
			}

			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			input.put("topicId", topicId);
			input.put("topicName", topicName);
			input.put("percentage", percentage);
			input.put("topicPassPercentage", topicPassPercentage);
			Map<String, Object> result = dispatcher.runSync("addExamTopics", input);
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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String examId = request.getParameter("examId");
			if (UtilValidate.isEmpty(examId))
				return Response.status(400).entity(ServiceUtil.returnError("Exam is null ")).build();

			Map<String, Object> result = dispatcher.runSync("getAllExamTopics", UtilMisc.toMap("examId", examId));

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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String examId = (String) request.getAttribute("examId");
			String percentage = (String) request.getAttribute("percentage");
			String topicId = (String) request.getAttribute("topicId");
			String topicName = (String) request.getAttribute("topicName");
			String topicPassPercentage = (String) request.getAttribute("topicPassPercentage");

			if (UtilValidate.isEmail(examId)) {
				return Response.status(400).entity("Exam id is empty ").build();
			}
			if (UtilValidate.isEmpty(topicId)) {
				return Response.status(400).entity("TopicId is Required").build();
			}
			if (UtilValidate.isEmpty(topicName)) {
				return Response.status(400).entity("Topic name is required ").build();
			}
			if (UtilValidate.isEmpty(percentage)) {
				return Response.status(400).entity("Question percentage is required ").build();
			}
			if (UtilValidate.isEmpty(topicPassPercentage)) {
				return Response.status(400).entity("Topic passpercentage is required ").build();
			}

			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			input.put("percentage", percentage);
			input.put("topicId", topicId);
			input.put("topicName", topicName);
			input.put("topicPassPercentage", topicPassPercentage);

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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String examId = (String) request.getAttribute("examId");
			String topicId = (String) request.getAttribute("topicId");

			if (UtilValidate.isEmpty(examId)) {
				return Response.status(400).entity("Exam Id is required ").build();
			}

			if (UtilValidate.isEmpty(topicId)) {
				return Response.status(400).entity("Topic Id is required ").build();
			}

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
	public Response getExamTopicsByExamId(@PathParam("examId") String examId, @Context HttpServletRequest request) {

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			if (UtilValidate.isEmpty(examId)) {
				return Response.status(400).entity(ServiceUtil.returnError("ExamId is required")).build();
			}

			Map<String, Object> input = new HashMap<>();
			input.put("examId", examId);

			Map<String, Object> result = dispatcher.runSync("getExamTopicsByExamId", input);

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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String examId = (String) request.getAttribute("examId");
			if (UtilValidate.isEmpty(examId)) {
				return Response.status(400).entity(ServiceUtil.returnError("exami id is null")).build();
			}
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			Map<String, Object> result = dispatcher.runSync("generateExamQuestions", input);
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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String examId = (String) request.getAttribute("examId");
			if (UtilValidate.isEmail(examId)) {
				return Response.status(400).entity(ServiceUtil.returnError("exam id is null")).build();
			}
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			Map<String, Object> result = dispatcher.runSync("launchExam", input);
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

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
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
	public Response getUserAssignedExams(@PathParam("partyId") String partyId, @Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			if (UtilValidate.isEmpty(partyId)) {
				return Response.status(400).entity(ServiceUtil.returnError("PartyId is required")).build();
			}

			Map<String, Object> input = new HashMap<>();
			input.put("partyId", partyId);

			Map<String, Object> result = dispatcher.runSync("getUserAssignedExams", input);

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

			if (UtilValidate.isEmpty(dispatcher)) {
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
	@Path("/updateAssignedUser")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateAssignedUser(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			Map<String, Object> result = dispatcher.runSync("updateAssignedUserWrapper",
					UtilMisc.toMap("partyId", request.getAttribute("partyId"), "examId", request.getAttribute("examId"),
							"allowedAttempts", request.getAttribute("allowedAttempts"), "timeoutDays",
							request.getAttribute("timeoutDays")));

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

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

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

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String partyId = (String) request.getAttribute("partyId");
			if (partyId == null || partyId.isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("Login to proceed"))
						.build();
			}
			Map<String, Object> result = dispatcher.runSync("getAllExamsByAdmin", UtilMisc.toMap("partyId", partyId));

			return Response.status(Response.Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Something went wrong try later")).build();
		}
	}

	@POST
	@Path("/examCount")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAdminExamCount(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String partyId = (String) request.getAttribute("partyId");
			if (UtilValidate.isEmail(partyId)) {
				return Response.status(400).entity(ServiceUtil.returnError("Party id is required")).build();
			}
			Map<String, Object> result = dispatcher.runSync("adminExamListCount", UtilMisc.toMap("partyId", partyId));
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(result).build();

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
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String examId = request.getParameter("examId");
			if (UtilValidate.isEmpty(examId)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("examId is required")).build();
			}

			Map<String, Object> result = dispatcher.runSync("getAllExamQuestions", UtilMisc.toMap("examId", examId));

			if (UtilValidate.isEmpty(result)) {
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