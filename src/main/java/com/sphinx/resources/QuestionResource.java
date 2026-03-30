package com.sphinx.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
				return Response.status(400).entity(ServiceUtil.returnError("Invalid topic Id")).build();
			}

			Map<String, Object> result = getDispatcher().runSync("getAllQuestionByTopic", UtilMisc.toMap("topicId", topicId));
			return Response.status(200).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.serverError().entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/questionTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllQuestionTypes() {
		try {
			Map<String, Object> result = getDispatcher().runSync("getAllQuestionTypes", UtilMisc.toMap());
			return Response.status(200).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateQuestion(Map<String, Object> input) {


		String errorMsg = validateQuestionData(input);

		if (errorMsg != null) {
			return Response.status(400).entity(ServiceUtil.returnError(errorMsg)).build();
		}

		Map<String, Object> result;
		try {
			result = getDispatcher().runSync("updateQuestion", input);
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(400).entity(result).build();
			}
			return Response.status(201).entity(result).build();
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.serverError().entity(ServiceUtil.returnError(e.getMessage())).build();
		}

	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteQuestion(Map<String, Object> input) {

		String qId = (String) input.get("questionId");

		if (UtilValidate.isEmpty(qId)) {
			return Response.status(400).entity(ServiceUtil.returnError("Question Id is required")).build();
		}

		Map<String, Object> result;
		try {
			result = getDispatcher().runSync("deleteQuestion", input);
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(400).entity(result).build();
			}
			return Response.status(200).entity(result).build();
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.serverError().entity(ServiceUtil.returnError(e.getMessage())).build();
		}

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addQuestions(Map<String, Object> input) {
		try {

			String errorMsg = validateQuestionData(input);

			if (errorMsg != null) {
				return Response.status(400).entity(ServiceUtil.returnError(errorMsg)).build();
			}

			Map<String, Object> result = getDispatcher().runSync("createQuestion", input);
			if (result.get("responseMessage") != null && result.get("responseMessage").equals("error")) {
				return Response.status(400).entity(result).build();
			}
			else {
				result.put("successMessage", "Question added successfully!");
			}
			return Response.status(201).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/downloadTemplate")
	@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	public Response downloadTemplate() {
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Questions");

			Row row = sheet.createRow(0);

			List<ColumnConfig> columns = QuestionColumnConfigUtil.getColumnConfigs();

			for (ColumnConfig col : columns) {
				Cell cell = row.createCell(col.index);
				String imp = col.required ? "*" : "";
				cell.setCellValue(col.label + " " + imp);
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			workbook.write(out);

			byte[] bytes = out.toByteArray();

			out.close();

			workbook.close();

			return Response.ok(bytes).header("Content-Disposition", "attachment; filename=questions_template.xlsx")
							.type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").build();

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.serverError().build();
		}

	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadQuestions(@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileDetail) {

		String fileName = fileDetail.getFileName();

		// file type check
		if (!fileName.endsWith(".xlsx")) {
			Debug.logWarning("Invalid file upload by user.", MODULE);
			return Response.status(400).entity(ServiceUtil.returnError("Only files with .xlsx are allowed"))
							.build();
		}

		// process the excel file

		try {
			Workbook workbook = WorkbookFactory.create(file);
			Sheet sheet = workbook.getSheetAt(0);
			List<Map<String, Object>> questions = new ArrayList<>();

			int totalRows = sheet.getLastRowNum();
			if (totalRows <= 1) {
				return Response.status(400).entity(ServiceUtil.returnError("Please fill the details and upload the file")).build();
			}

			for (int i = 1; i <= sheet.getLastRowNum(); i++) { // <= to include last row
				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				Map<String, Object> question = new HashMap<>();
				List<ColumnConfig> columns = QuestionColumnConfigUtil.getColumnConfigs();


				for (ColumnConfig col : columns) {
					Cell cell = row.getCell(col.index);


					if (col.required && (cell == null || cell.getCellType() == CellType.BLANK)) {
						return Response.status(400)
										.entity(ServiceUtil.returnError(
														"Row " + i + ", Column " + col.index + " " + col.label + " is required", null))
										.build();
					}

					if (cell == null) {
						question.put(col.field, null);
						continue;
					}


					switch (cell.getCellType()) {

						case NUMERIC:
							double numVal = cell.getNumericCellValue();
							question.put(col.field, numVal);
							break;

						case STRING:
							String strVal = cell.getStringCellValue();
							question.put(col.field, strVal != null ? strVal.trim() : null);
							break;

						case BOOLEAN:
							question.put(col.field, cell.getBooleanCellValue());
							break;

						case BLANK:
							question.put(col.field, null);
							break;
						default:
							question.put(col.field, null);
							break;
					}
				}
				questions.add(question);
			}


			for (Map<String, ? extends Object> question : questions) {
				getDispatcher().runSync("createQuestion", question);
			}

			return Response.status(201).entity(ServiceUtil.returnSuccess("Question uploaded successfully")).build();

		} catch (EncryptedDocumentException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		} catch (IOException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}

	}

}
