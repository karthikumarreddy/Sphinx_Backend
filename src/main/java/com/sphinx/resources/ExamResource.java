package com.sphinx.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/exam")
public class ExamResource {

	private static final String MODULE = ExamResource.class.getName();

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
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(201).entity(result).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/search-exam")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExamByName(@Context HttpServletRequest request) {
	    try {
	        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

	        if (UtilValidate.isEmpty(dispatcher)) {
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                    .entity(ServiceUtil.returnError("Unexpected Error Occurred! Try again after Sometime!")).build();
	        }

	        Map<String, Object> result = dispatcher.runSync("getExamByName",
	                UtilMisc.toMap("examName", request.getParameter("examName")));

	        if (ServiceUtil.isError(result)) {
	            return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
	        }
	        return Response.status(200).entity(result).build();

	    } catch (Exception e) {
	        Debug.logError(e, MODULE);
	        return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
	    }
	}

	// @GET
	// @Path("/{examId}")
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response getExamById(@PathParam("examId") String examId, @Context HttpServletRequest request) {
	//
	// try {
	// LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	//
	// if (UtilValidate.isEmpty(dispatcher)) {
	// return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	// .entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
	// }
	//
	// Map<String, Object> input = new HashMap<>();
	// input.put("examId", examId);
	//
	// Map<String, Object> result = dispatcher.runSync("getExamById", input);
	//
	// return Response.status(200).entity(result).build();
	//
	// } catch (Exception e) {
	// return Response.status(400).entity(ServiceUtil.returnError("Something went wrong")).build();
	// }
	// }

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

			GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
			if (UtilValidate.isNotEmpty(userLogin)) {
				String partyId = (String) userLogin.get("partyId");
				map.put("partyId", partyId);
			}
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

			Map<String, Object> result = dispatcher.runSync("createExamWrapper", map);
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
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

			HttpSession session = request.getSession(false);

			String partyId = request.getParameter("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				if (UtilValidate.isNotEmpty(session)) {
					GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
					if (UtilValidate.isNotEmpty(userLogin)) {
						partyId = userLogin.getString("partyId");
					}
				}
			}

			map.put("partyId", partyId);
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

			HttpSession session = request.getSession(false);
			GenericValue userLogin = null;

			if (UtilValidate.isNotEmpty(session) && UtilValidate.isNotEmpty(session.getAttribute("userLogin"))) {
				userLogin = (GenericValue) session.getAttribute("userLogin");
			}

			// String partyId = userLogin.getString("partyId");

			String examId = (String) request.getAttribute("examId");
			String topicId = (String) request.getAttribute("topicId");
			String topicName = (String) request.getAttribute("topicName");
			String percentage = (String) request.getAttribute("percentage");
			String topicPassPercentage = (String) request.getAttribute("topicPassPercentage");
			Boolean savePermanently = (Boolean) request.getAttribute("savePermanently");
			//
			// if (UtilValidate.isEmail(examId)) {
			// return Response.status(400).entity(ServiceUtil.returnError("Exam id is empty")).build();
			// }
			// if (UtilValidate.isEmpty(topicId)) {
			// return Response.status(400).entity(ServiceUtil.returnError("Topic is Required")).build();
			// }
			// if (UtilValidate.isEmpty(topicName)) {
			// return Response.status(400).entity(ServiceUtil.returnError("Topic name is required")).build();
			// }
			// if (UtilValidate.isEmpty(percentage)) {
			// return Response.status(400).entity(ServiceUtil.returnError("Question percentage is required")).build();
			// }
			// if (UtilValidate.isEmpty(topicPassPercentage)) {
			//
			// return Response.status(400).entity(ServiceUtil.returnError("Topic passpercentage is required")).build();
			// }
			

			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			input.put("topicId", topicId);
			input.put("topicName", topicName);
			input.put("percentage", percentage);
			input.put("topicPassPercentage", topicPassPercentage);
			input.put("savePermanently", savePermanently);
			input.put("userLogin", userLogin);

			// Skip Validation for now.
			/*
			 * Delegator delegator = (Delegator) request.getAttribute("delegator"); GenericValue topic =
			 * EntityQuery.use(delegator).from("ExamTopicDetails").where("examId", examId, "topicId", topicId) .queryFirst(); if
			 * (UtilValidate.isNotEmpty(topic)) { return
			 * Response.status(400).entity(ServiceUtil.returnError("Topic already Assigned to the Assessment")).build(); }
			 */
			// GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId",
			// examId).select("noOfQuestions").queryFirst();
			// long totalQuestions = exam.getLong("noOfQuestions");
			// int totalQuestionsInTopic = (int) (totalQuestions * Integer.valueOf(percentage)) / 100;
			// long questionCount = EntityQuery.use(delegator).from("QuestionMaster").where("topicId",
			// topicId).maxRows(totalQuestionsInTopic)
			// .queryCount();
			// if (totalQuestionsInTopic > questionCount) {
			// return Response.status(400).entity(ServiceUtil.returnError(totalQuestionsInTopic - questionCount
			// + " question needed for the Topic to add in Assessment! Please Add Questions to the Topic!")).build();
			// }

			// if (totalQuestionsInTopic > questionCount) {
			// return Response.status(400).entity(ServiceUtil.returnError(totalQuestionsInTopic - questionCount
			// + " question needed for the Topic to add in Assessment! Please Add Questions to the Topic!")).build();
			// }
			// Map<String, Object> result = dispatcher.runSync("addExamTopics", input);

			Map<String, Object> result = dispatcher.runSync("addExamTopicsWrapper", input);
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(result).build();
			}
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

			String examId = request.getParameter("examId");
			if (UtilValidate.isEmpty(examId))
				return Response.status(400).entity(ServiceUtil.returnError("Invalid Assessment Information!")).build();

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
			long topicPassPercentage = (Integer) request.getAttribute("topicPassPercentage");

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
				return Response.status(400).entity(ServiceUtil.returnError("Exam Id is required")).build();
			}

			if (UtilValidate.isEmpty(topicId)) {
				return Response.status(400).entity(ServiceUtil.returnError("Topic Id is required")).build();
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
	@Path("/examTopics")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExamTopicsByExamId(@Context HttpServletRequest request) {

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String examId = request.getParameter("examId");
			
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
			String partyId=(String) request.getAttribute("partyId");
			if (UtilValidate.isEmpty(examId)) {
				return Response.status(400).entity(ServiceUtil.returnError("exami id is null")).build();
			}
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			input.put("partyId", partyId);
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

	@POST
	@Path("/setupExam")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setupExam(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			String examId = (String) request.getAttribute("examId");
			if (UtilValidate.isEmail(examId)) {
				return Response.status(400).entity(ServiceUtil.returnError("Invalid Exam details!")).build();
			}

			Map<String, Object> result = dispatcher.runSync("setupExam", UtilMisc.toMap("examId", examId));
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
			@SuppressWarnings("unchecked")
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
							.toMap("partyId", request.getAttribute("partyIds"), "examId", request.getAttribute("examId")));

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
	@Path("/getAllExamsByAdmin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllExamsByAdmin(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			HttpSession session = request.getSession(false);
			GenericValue userLogin = null;
			if (UtilValidate.isNotEmpty(session)) {
				userLogin = (GenericValue) session.getAttribute("userLogin");
			}

			String partyId = (String) request.getAttribute("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				if (UtilValidate.isNotEmpty(userLogin)) {
					partyId = (String) userLogin.get("partyId");
				}
			}

			if (UtilValidate.isEmpty(partyId)) {
				Debug.logError("Party Id Doesn't comes from frontend, partyId => " + partyId, MODULE);
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ServiceUtil.returnError("Invalid Admin Details!")).build();
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

			HttpSession session = request.getSession();
			GenericValue userLogin = null;
			if (UtilValidate.isNotEmpty(session)) {
				userLogin = (GenericValue) session.getAttribute("userLogin");
			}

			String partyId = (String) request.getAttribute("partyId");

			if (UtilValidate.isEmpty(partyId)) {
				if (UtilValidate.isNotEmpty(userLogin)) {
					partyId = userLogin.getString("partyId");
				}
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
	@Path("/report")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateReport(@Context HttpServletRequest request) {
	    try {
	        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

	        if (UtilValidate.isEmpty(dispatcher)) {
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                    .entity(ServiceUtil.returnError("Unexpected Error Occurred! Try again after Sometime!")).build();
	        }

	        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
	        String partyId = null;

	        if (UtilValidate.isNotEmpty(userLogin)) {
	            partyId = userLogin.getString("partyId");
	        }

	        if (UtilValidate.isEmpty(partyId)) {
	            return Response.status(400)
	                    .entity(ServiceUtil.returnError("partyId is required")).build();
	        }

	        Map<String, Object> result = dispatcher.runSync("generateReport",
	                UtilMisc.toMap("partyId", partyId, "userLogin", userLogin));

	        if (ServiceUtil.isError(result)) {
	            return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
	        }

	        return Response.status(200).entity(result).build();

	    } catch (Exception e) {
	        Debug.logError(e, MODULE);
	        return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
	    }
	}
	
	
	@GET
	@Path("/getUsersNotAssignedToExam")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response getUsersNotAssignedToExam(@Context HttpServletRequest request){
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			String examId = request.getParameter("examId");

			Map<String, Object> result = dispatcher.runSync("getUsersNotAssignedToExam",
							UtilMisc.toMap("examId", examId));
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}

			return Response.status(200).entity(result).build();
		}catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	} 



}