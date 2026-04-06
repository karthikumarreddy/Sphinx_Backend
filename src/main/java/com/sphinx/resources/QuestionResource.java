package com.sphinx.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
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

	@Context
	HttpServletRequest request;

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

	private String validateQuestionData(Map<String, Object> input) {
		String questionDetail = (String) input.get("questionDetail");
		String optionA = (String) input.get("optionA");
		String optionB = (String) input.get("optionB");
		String optionC = (String) input.get("optionC");
		String optionD = (String) input.get("optionD");
		String answer = (String) input.get("answer");
		String questionType = (String) input.get("questionType");
		String difficultyLevel = (String) input.get("difficultyLevel");
		String answerValue = (String) input.get("answerValue");
		String topicId = (String) input.get("topicId");

		if (UtilValidate.isEmpty(questionDetail)) {
			return "Question detail is required";
		}

		if (UtilValidate.isEmpty(questionType)) {
			return "Question type is required";
		}

		if (UtilValidate.isEmpty(difficultyLevel)) {
			return "Difficulty level is required";
		}

		if (UtilValidate.isEmpty(topicId)) {
			return "Invalid Topic, Choose a Valid one.";
		}


		// here we give the question types is based on the enum type (hard coded).

		// Fill Ups or True/False question type
		if (questionType.equals("FILL_UP") || questionType.equals("TRUE_FALSE") || questionType.equals("DETAILED_ANSWER")) {
			if (UtilValidate.isEmpty(answerValue)) {
				return "Answer value is mandatory for Fill Ups type questions.";
			}
		}

		// Multiple, Single choice questions
		if (questionType.equals("MULTIPLE_CHOICE") || questionType.equals("SINGLE_CHOICE")) {
			if (UtilValidate.isEmpty(optionA)) {
				return "Option A is mandatory";
			}

			if (UtilValidate.isEmpty(optionB)) {
				return "Option B is mandatory";
			}

			if (UtilValidate.isEmpty(optionC)) {
				return "Option C is mandatory";
			}

			if (UtilValidate.isEmpty(optionD)) {
				return "Option D is mandatory";
			}

			if (UtilValidate.isEmpty(answer)) {
				return "Answer is mandatory";
			}

			if (UtilValidate.isEmpty(input.get("numAnswers"))) {
				return "Number of Answer is mandatory";
			}

			int numOfAnswers;
			try {
				numOfAnswers = (Integer) input.get("numAnswers");
			} catch (ClassCastException e) {
				return "Invalid Number of Answers";
			}


			if (questionType.equals("MULTIPLE_CHOICE") && numOfAnswers <= 0) {
				return "Invalid Number of answers.";
			}

			if (questionType.equals("MULTIPLE_CHOICE") && (answer == null || answer.split(",").length != numOfAnswers)) {
				return "Number of answers marked is invalid.";
			}

		}


		return null;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllQuestionByTopic() {
		try {

			String topicIdStr = request.getQueryString();
			String topicId = topicIdStr.split("=")[1];

			if (topicId == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("Invalid topic Id")).build();
			}

			Map<String, Object> result = getDispatcher().runSync("getAllQuestionByTopic", UtilMisc.toMap("topicId", topicId));
			return Response.status(Response.Status.OK).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/questionTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllQuestionTypes() {
		try {
			Map<String, Object> result = getDispatcher().runSync("getAllQuestionTypes", UtilMisc.toMap());
			return Response.status(Response.Status.OK).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateQuestion(Map<String, Object> input) {


		String errorMsg = validateQuestionData(input);

		if (errorMsg != null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError(errorMsg)).build();
		}

		Map<String, Object> result;
		try {
			result = getDispatcher().runSync("updateQuestion", input);
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			}
			return Response.status(Response.Status.CREATED).entity(result).build();
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceUtil.returnError(e.getMessage())).build();
		}

	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteQuestion(Map<String, Object> input) {

		String qId = (String) input.get("questionId");

		if (UtilValidate.isEmpty(qId)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("Question Id is required")).build();
		}

		Map<String, Object> result;
		try {
			result = getDispatcher().runSync("deleteQuestion", input);
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			}
			return Response.status(Response.Status.OK).entity(result).build();
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceUtil.returnError(e.getMessage())).build();
		}

	}

	@POST

	@Produces(MediaType.APPLICATION_JSON)
	public Response addQuestions(@Context HttpServletRequest request) {
		try {
			Map<String, Object> input = UtilMisc.toMap("questionDetail", request.getAttribute("questionDetail"),
			"questionType", request.getAttribute("questionType"),
			"topicId", request.getAttribute("topicId"),
			"optionA", request.getAttribute("optionA"),
			"optionB", request.getAttribute("optionB"),
			"optionC", request.getAttribute("optionC"),
			"optionD", request.getAttribute("optionD"),
							"answer", request.getAttribute("answer"), "numAnswers", request.getAttribute("numAnswers"), "difficultyLevel",
							request.getAttribute("difficultyLevel"), "answerValue", request.getAttribute("answerValue"));
			
			String errorMsg = validateQuestionData(input);

			if (errorMsg != null) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError(errorMsg)).build();
			}

			Map<String, Object> result = getDispatcher().runSync("createQuestion", input);
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			}
			else {
				result.put("successMessage", "Question added successfully!");
			}
			return Response.status(Response.Status.CREATED).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/downloadTemplate")
	@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	public Response downloadTemplate() {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		Map<String, ? extends Object> result;
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

			result = ServiceUtil.returnSuccess();

			Sheet sheet = workbook.createSheet("Questions");

			Row header = sheet.createRow(0);

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

			return Response.ok(bytes)
								.header("Content-Disposition", "attachment; filename=questions_template.xlsx")
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
	// public Response uploadQuestions(@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition
	// fileDetail) {
	public Response uploadQuestions(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		// if (file == null || fileDetail == null) {
		// return Response.status(400).entity(ServiceUtil.returnError("File not received by server")).build();
		// }
		//
		// String fileName = fileDetail.getFileName();
		//
		// // file type check
		// if (!fileName.endsWith(".xlsx")) {
		// Debug.logWarning("Invalid file upload by user.", MODULE);
		// return Response.status(400).entity(ServiceUtil.returnError("Only files with .xlsx are allowed")).build();
		// }

		InputStream file;
		Part filePart;
		ByteBuffer buffer;
		try {
			filePart = request.getPart("file");

			if (filePart == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("File was not found on Request!"))
								.build();
			}

			String fileName = filePart.getSubmittedFileName();

			if (!fileName.endsWith(".xlsx")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ServiceUtil.returnError("Only Excel file are allowed!")).build();
			}

			byte[] bytes = filePart.getInputStream().readAllBytes();

			buffer = ByteBuffer.wrap(bytes);

			System.out.println("Uploaded: " + fileName);

		} catch (IOException | ServletException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.entity(ServiceUtil.returnError("Unexpected error occured, try again after sometime!")).build();
		}



		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		
		try {
			Map<String, Object> result = dispatcher.runSync("uploadBulkQuestion", UtilMisc.toMap("file", buffer));
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			}
			else {
				result.put("successMessage", "Questions uploaded successfully!");
			}

			return Response.status(Response.Status.CREATED).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.entity(ServiceUtil.returnError("Unexpected error occured, try again after sometime!")).build();
		}

	}

}
