package com.sphinx.resources;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/user")
public class UserExameResource {

	public static String validateExamStatus(Map<String, Object> input) {
		String partyId = (String) input.get("partyId");
		String examId = (String) input.get("examId");
		String remainingTime = (String) input.get("remainingTime");
		String totalAnswered = (String) input.get("totalAnswered");
		String totalRemaining = (String) input.get("totalRemaining");
		String isExamActive = (String) input.get("isExamActive");

		if (UtilValidate.isEmpty(partyId)) {
			return "Party Id is required";
		}
		if (UtilValidate.isEmpty(examId)) {
			return "Exam Id  is required";
		}
		if (UtilValidate.isEmpty(remainingTime)) {
			return "Remaining Time is required";
		}
		if (UtilValidate.isEmpty(totalAnswered)) {
			return "Total Answered is required";
		}
		if (UtilValidate.isEmpty(totalRemaining)) {
			return "Total Remaining is required";
		}
		if (UtilValidate.isEmpty(isExamActive)) {
			return "Is Exam Active is required";
		}
		return null;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static Response startExamStatus(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String partyId = (String) request.getAttribute("partyId");
			String examId = (String) request.getAttribute("examId");
			String remainingTime = (String) request.getAttribute("remainingTime");
			String totalAnswered = (String) request.getAttribute("totalAnswered");
			String totalRemaining = (String) request.getAttribute("totalRemaining");
			String isExamActive = (String) request.getAttribute("isExamActive");

			Map<String, Object> input = UtilMisc.toMap("partyId", partyId, "examId", examId, "remainingTime",
					remainingTime, "totalAnswered", totalAnswered, "totalRemaining", totalRemaining, "isExamActive",
					isExamActive);
			String error = validateExamStatus(input);
			if (UtilValidate.isEmpty(error)) {
				return Response.status(400).entity(error).build();
			}

			Map<String, Object> result = dispatcher.runSync("startExam", input);
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity("Something went wrong try again later").build();
		}

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static Response updateExamStatus(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String partyId = (String) request.getAttribute("partyId");
			String examId = (String) request.getAttribute("examId");
			String remainingTime = (String) request.getAttribute("remainingTime");
			String totalAnswered = (String) request.getAttribute("totalAnswered");
			String totalRemaining = (String) request.getAttribute("totalRemaining");
			String isExamActive = (String) request.getAttribute("isExamActive");

			Map<String, Object> input = UtilMisc.toMap("partyId", partyId, "examId", examId, "remainingTime",
					remainingTime, "totalAnswered", totalAnswered, "totalRemaining", totalRemaining, "isExamActive",
					isExamActive);
			String error = validateExamStatus(input);
			if (UtilValidate.isEmpty(error)) {
				return Response.status(400).entity(error).build();
			}
			Map<String, Object> result = dispatcher.runSync("updateExamStatus", input);
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity("Something went wrong try again later").build();
		}

	}

	@POST
	@Path("/submitanswer")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)

	public static Response submitAmswer(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			Long qId = (Long) request.getAttribute("qId");
			String examId = (String) request.getAttribute("examId");
			String partyId = (String) request.getAttribute("partyId");
			String submittedAnswer = (String) request.getAttribute("submittedAnswer");
			Long sNo = (Long) request.getAttribute("sNo");
			Long isFlagged = (Long) request.getAttribute("isFlagged");

			if (UtilValidate.isEmpty(qId)) {
				return Response.status(400).entity(ServiceUtil.returnError("qId is required ")).build();
			}
			if (UtilValidate.isEmpty(examId)) {
				return Response.status(400).entity(ServiceUtil.returnError("Exam Id is required ")).build();
			}
			if (UtilValidate.isEmpty(partyId)) {
				return Response.status(400).entity(ServiceUtil.returnError("Party Id is required ")).build();
			}
			if (UtilValidate.isEmpty(submittedAnswer)) {
				return Response.status(400).entity(ServiceUtil.returnError("Submitted Answer is required ")).build();
			}
			if (UtilValidate.isEmpty(sNo)) {
				return Response.status(400).entity(ServiceUtil.returnError("sNo is required ")).build();
			}
			if (UtilValidate.isEmpty(isFlagged)) {
				return Response.status(400).entity(ServiceUtil.returnError("IsFlagged is required ")).build();
			}
			Map<String, Object> result = dispatcher.runSync("submitAnswer", UtilMisc.toMap("qId", qId, "examId", examId,
					"partyId", partyId, "submittedAnswer", submittedAnswer, "sNo", sNo, "isFlagged", isFlagged));

			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity(ServiceUtil.returnError("Something Went wrong try again later")).build();
		}
	}

	@POST
	@Path("/exam-questions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static Response getAllExamQuestions(@Context HttpServletRequest request) {
		try {
			String examId = (String) request.getAttribute("examId");
			if (UtilValidate.isEmpty(examId)) {
				return Response.status(400).entity(ServiceUtil.returnError("Exam id is required")).build();
			}

			String partyId = (String) request.getAttribute("partyId");
			if (UtilValidate.isEmpty(examId)) {
				return Response.status(400).entity(ServiceUtil.returnError("party id is required")).build();
			}

			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			Map<String, Object> result = dispatcher.runSync("getAllExamQuestions",
					UtilMisc.toMap("examId", examId, "partyId", partyId));
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity(ServiceUtil.returnError("Something Went wrong try again later")).build();
		}
	}

	@POST
	@Path("/get-all-exam")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static Response getAllPartyExam(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String partyId = (String) request.getAttribute("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				return Response.status(400).entity(ServiceUtil.returnError("Party Id is required ")).build();
			}

			Map<String, Object> result = dispatcher.runSync("getAllPartyExam", UtilMisc.toMap("partyId", partyId));
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity(ServiceUtil.returnError("Something Went wrong try again later")).build();

		}
	}

	@POST
	@Path("/submit")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static Response submitExam(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected error occurred! Try again later.")).build();
			}

			String examId = (String) request.getAttribute("examId");
			String partyId = (String) request.getAttribute("partyId");

			if (UtilValidate.isEmpty(examId)) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ServiceUtil.returnError("Exam ID is required.")).build();
			}

			if (UtilValidate.isEmpty(partyId)) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ServiceUtil.returnError("Party ID is required.")).build();
			}

			Map<String, Object> input = UtilMisc.toMap("examId", examId, "partyId", partyId);

			Map<String, Object> result = dispatcher.runSync("submitExam", input);

			if (ServiceUtil.isError(result)) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.getErrorMessage(result)).build();
			}

			return Response.status(Response.Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Something went wrong. Please try again later.")).build();
		}
	}

	@POST
	@Path("/result")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static Response getExamResult(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected error occurred! Try again later.")).build();
			}

			String examId = (String) request.getAttribute("examId");
			String partyId = (String) request.getAttribute("partyId");
			Long performanceId = (Long) request.getAttribute("performanceId");

			if (UtilValidate.isEmpty(examId)) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ServiceUtil.returnError("Exam ID is required.")).build();
			}

			if (UtilValidate.isEmpty(partyId)) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ServiceUtil.returnError("Party ID is required.")).build();
			}

			Map<String, Object> input = UtilMisc.toMap("examId", examId, "partyId", partyId);
			if (performanceId != null) {
				input.put("performanceId", performanceId);
			}

			Map<String, Object> result = dispatcher.runSync("getExamResult", input);

			if (ServiceUtil.isError(result)) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.getErrorMessage(result)).build();
			}

			return Response.status(Response.Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Something went wrong. Please try again later.")).build();
		}
	}

}
