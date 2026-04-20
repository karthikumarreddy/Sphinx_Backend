package com.sphinx.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
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

import org.apache.http.HttpStatus;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sphinx.util.QuestionColumnConfigUtil;
import com.sphinx.util.QuestionColumnConfigUtil.ColumnConfig;

@Path("/questions")
public class QuestionResource {

	private static final String MODULE = QuestionResource.class.getName();

	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllQuestionByTopic(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String topicIdStr = request.getQueryString();
			String topicId = topicIdStr.split("=")[1];

			if (topicId == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("Invalid topic Id"))
						.build();
			}

			Map<String, Object> result = dispatcher.runSync("getAllQuestionByTopic",
					UtilMisc.toMap("topicId", topicId));
			
			if(ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			
			return Response.status(Response.Status.OK).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/questionTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllQuestionTypes(@Context HttpServletRequest request) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		if (UtilValidate.isEmpty(dispatcher)) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
		}
		try {
			Map<String, Object> result = dispatcher.runSync("getAllQuestionTypes", UtilMisc.toMap());
			if(ServiceUtil.isError(result)) {
				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
			}
			return Response.status(Response.Status.OK).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateQuestion(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			Map<String, Object> input = new HashMap<>();

			input.put("questionDetail", request.getAttribute("questionDetail"));
			input.put("optionA", request.getAttribute("optionA"));
			input.put("optionB", request.getAttribute("optionB"));
			input.put("optionC", request.getAttribute("optionD"));
			input.put("optionD", request.getAttribute("optionD"));
			input.put("answer", request.getAttribute("answer"));
			input.put("questionType", request.getAttribute("questionType"));
			input.put("difficultyLevel", request.getAttribute("difficultyLevel"));
			input.put("numAnswers", request.getAttribute("numAnswers"));
			input.put("answerValue", request.getAttribute("answerValue"));
			input.put("topicId", request.getAttribute("topicId"));
			input.put("questionId", request.getAttribute("questionId"));


			Map<String, Object> result;
			result = dispatcher.runSync("updateQuestionWrapper", input);
			if (ServiceUtil.isError(result)) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			}
			else {
				result.put("successMessage", "Question Updated Successfully!");
			}

			return Response.status(Response.Status.OK).entity(result).build();
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}

	}
	
//	@DELETE
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response deleteQuestion(@Context HttpServletRequest request) {
//
//		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
//
//		if (UtilValidate.isEmpty(dispatcher)) {
//			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
//		}
//
//		String qId = (String) request.getAttribute("questionId");
//
//		if (UtilValidate.isEmpty(qId)) {
//			return Response.status(Response.Status.BAD_REQUEST)
//					.entity(ServiceUtil.returnError("Question Id is required")).build();
//		}
//		Map<String, Object> input = UtilMisc.toMap("qId", qId);
//		Map<String, Object> result;
//		try {
//			result = dispatcher.runSync("deleteQuestion", input);
//			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
//				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
//			}
//			return Response.status(Response.Status.OK).entity(result).build();
//		} catch (GenericServiceException e) {
//			Debug.logError(e, MODULE);
//			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//					.entity(ServiceUtil.returnError(e.getMessage())).build();
//		}
//
//	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response addQuestions(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			Map<String, Object> input = UtilMisc.toMap("questionDetail", request.getAttribute("questionDetail"),
					"questionType", request.getAttribute("questionType"), "topicId", request.getAttribute("topicId"),
					"optionA", request.getAttribute("optionA"), "optionB", request.getAttribute("optionB"), "optionC",
					request.getAttribute("optionC"), "optionD", request.getAttribute("optionD"), "answer",
					request.getAttribute("answer"), "numAnswers", request.getAttribute("numAnswers"), "difficultyLevel",
					request.getAttribute("difficultyLevel"), "answerValue", request.getAttribute("answerValue"));


			HttpSession session = request.getSession(false);
			if (UtilValidate.isEmpty(session)) {
				return Response.status(HttpStatus.SC_UNAUTHORIZED)
						.entity(ServiceUtil.returnError("Sign In Before Adding Question")).build();
			}
			GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");

			input.put("userLogin", userLogin);

			Map<String, Object> result = dispatcher.runSync("createQuestionWrapper", input);

			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			} else {
				result.put("successMessage", "Question added successfully!");
			}
			return Response.status(Response.Status.CREATED).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/downloadTemplate")
	@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	public Response downloadTemplate(@Context HttpServletRequest request) {

		// Map<String, ? extends Object> result;
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

			// result = ServiceUtil.returnSuccess();

			Sheet sheet = workbook.createSheet("Questions");

			Row header = sheet.createRow(0);
			System.out.println("Update");
			List<ColumnConfig> columns = QuestionColumnConfigUtil.getColumnConfigs();

			for (ColumnConfig col : columns) {
				Cell cell = header.createCell(col.index);
				String imp = col.required ? "*" : "";
				cell.setCellValue(col.label + " " + imp);
			}

			// Style the header
			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);
			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			for (Cell cell : header)
				cell.setCellStyle(headerStyle);

			workbook.write(out);

			byte[] bytes = out.toByteArray();

			return Response.ok(bytes).header("Content-Disposition", "attachment; filename=questions_template.xlsx")
					.type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").build();

		} catch (IOException e) {
			Debug.logError(e, MODULE);
			return null;
		}

	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadQuestions(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		// InputStream file;
		Part filePart;
		ByteBuffer buffer;
		try {
			filePart = request.getPart("file");

			if (UtilValidate.isEmpty(filePart)) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ServiceUtil.returnError("File was not found on Request!")).build();
			}

			String fileName = filePart.getSubmittedFileName();

			if (!fileName.endsWith(".xlsx")) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ServiceUtil.returnError("Only Excel file are allowed!")).build();
			}

			byte[] bytes = filePart.getInputStream().readAllBytes();

			buffer = ByteBuffer.wrap(bytes);

			System.out.println("Uploaded: " + fileName);

		} catch (IOException | ServletException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected error occured, try again after sometime!")).build();
		}

		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		if (UtilValidate.isEmpty(dispatcher)) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
		}

		try {
			Map<String, Object> result = dispatcher.runSync("uploadBulkQuestion", UtilMisc.toMap("file", buffer));
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			} else {
				result.put("successMessage", "Questions uploaded successfully!");
			}

			return Response.status(Response.Status.CREATED).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ServiceUtil.returnError("Unexpected error occured, try again after sometime!")).build();
		}

	}

	@POST
	@Path("/getAllQuestions")
	@Produces(MediaType.APPLICATION_JSON)
	public static Response getAllQuestions(@Context HttpServletRequest request) {

		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		try {
			Map<String, Object> serviceResult = dispatcher.runSync("getAllQuestions",
							UtilMisc.toMap("viewIndex", request.getAttribute("viewIndex"), "viewSize", request.getAttribute("viewSize"),
											"topicIds", request.getAttribute("topicIds"), "questionTypes",
											request.getAttribute("questionTypes"), "questionDetailFilter",
											request.getAttribute("questionDetailFilter")));

			if (ServiceUtil.isError(serviceResult)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serviceResult).build();
			}
			return Response.ok().entity(serviceResult).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.serverError().entity(ServiceUtil.returnError("Something went Wrong! Try Again Later!"))
					.build();
		}

	}


//	@PUT
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public static Response updateQuestions(@Context HttpServletRequest request) {
//		try {
//			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
//			if (UtilValidate.isEmpty(dispatcher)) {
//				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
//			}
//
//			Map<String, Object> input = new HashMap();
//			String questionId = (String) request.getAttribute("questionId");
//			String questionType = (String) request.getAttribute("questionType");
//			String questionDetail = (String) request.getAttribute("questionDetail");
//			String answer = (String) request.getAttribute("answer");
//			String difficultyLevel = (String) request.getAttribute("difficultyLevel");
//			String answerValue = (String) request.getAttribute("answerValue");
//
//			if (UtilValidate.isEmpty(questionId)) {
//				return Response.status(400).entity(ServiceUtil.returnError("questionId is required")).build();
//			}
//			if (UtilValidate.isEmpty(questionType)) {
//				return Response.status(400).entity(ServiceUtil.returnError("Question type is required")).build();
//			}
//			if (UtilValidate.isEmpty(questionDetail)) {
//				return Response.status(400).entity(ServiceUtil.returnError(" Question  is required")).build();
//			}
//			if (UtilValidate.isEmpty(answer)) {
//				return Response.status(400).entity(ServiceUtil.returnError(" answer  is required")).build();
//
//			}
//			if (UtilValidate.isEmpty(difficultyLevel)) {
//				return Response.status(400).entity(ServiceUtil.returnError(" Difficulty level  is required")).build();
//
//			}
//			if (UtilValidate.isEmpty(answerValue)) {
//				return Response.status(400).entity(ServiceUtil.returnError(" Answer value  is required")).build();
//
//			}
//			input.put("questionId", questionId);
//			input.put("questionType", questionType);
//			input.put("questionDetail", questionDetail);
//			input.put("answer", answer);
//			input.put("difficultyLevel", difficultyLevel);
//			input.put("answerValue", answerValue);
//
//			if (questionType.equals("MULTIPLE_CHOICE") || questionType.equals("SINGLE_CHOICE")) {
//				String optionA = (String) request.getAttribute("optionA");
//				String optionB = (String) request.getAttribute("optionB");
//				String optionC = (String) request.getAttribute("optionC");
//				String optionD = (String) request.getAttribute("optionD");
//				String numAnswers = (String) request.getAttribute("numAnswers");
//
//				if (UtilValidate.isEmpty(optionA)) {
//					return Response.status(400).entity(ServiceUtil.returnError("Option A is required ")).build();
//				}
//				if (UtilValidate.isEmpty(optionB)) {
//					return Response.status(400).entity(ServiceUtil.returnError("Option A is required ")).build();
//				}
//				if (UtilValidate.isEmpty(optionC)) {
//					return Response.status(400).entity(ServiceUtil.returnError("Option A is required ")).build();
//				}
//				if (UtilValidate.isEmpty(optionD)) {
//					return Response.status(400).entity(ServiceUtil.returnError("Option A is required ")).build();
//				}
//				if (UtilValidate.isEmpty(numAnswers)) {
//					return Response.status(400).entity(ServiceUtil.returnError("Number of answers A is required "))
//							.build();
//				}
//				input.put("optionA", optionA);
//				input.put("optionB", optionB);
//				input.put("optionC", optionC);
//				input.put("optionD", optionD);
//				input.put("numAnswers", numAnswers);
//
//				Map<String, Object> result = dispatcher.runSync("updateQuestion", input);
//				if(ServiceUtil.isError(result)) {
//					return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
//				}
//				return Response.status(200).entity(result).build();
//			}
//
//			Map<String, Object> result = dispatcher.runSync("updateQuestion", input);
//			if(ServiceUtil.isError(result)) {
//				return Response.status(400).entity(ServiceUtil.getErrorMessage(result)).build();
//			}
//			return Response.status(200).entity(result).build();
//
//		} catch (Exception e) {
//			Debug.logError(e, MODULE);
//			return Response.serverError().entity(ServiceUtil.returnError("Something went Wrong! Try Again Later!"))
//					.build();
//		}
//	}


	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static Response deleteQuestions(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			@SuppressWarnings("unchecked")
			List<String> questionIds = (List<String>) request.getAttribute("questionIds");
			if (UtilValidate.isEmpty(questionIds)) {
				return Response.status(400).entity(ServiceUtil.returnError("Question Id is required")).build();
			}

			Map<String, Object> result = dispatcher.runSync("deleteQuestionsWrapper", UtilMisc.toMap("questionIds", questionIds));
			if(ServiceUtil.isError(result)) {
				return Response.status(400).entity(result).build();
			}
			return Response.status(200).entity(result).build();

		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return Response.serverError().entity(ServiceUtil.returnError("Invalid Input for the Action!")).build();
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.serverError().entity(ServiceUtil.returnError("Something went Wrong! Try Again Later!"))
					.build();
		}

	}
}
