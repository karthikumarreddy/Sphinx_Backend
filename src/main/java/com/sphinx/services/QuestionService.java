package com.sphinx.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.sphinx.util.QuestionColumnConfigUtil;
import com.sphinx.util.QuestionColumnConfigUtil.ColumnConfig;

public class QuestionService {

	private static final String MODULE = QuestionService.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";
	
	private String validateQuestionData(Map<String, ? extends Object> context) {

		String questionDetail = (String) context.get("questionDetail");
		String optionA = (String) context.get("optionA");
		String optionB = (String) context.get("optionB");
		String optionC = (String) context.get("optionC");
		String optionD = (String) context.get("optionD");
		String answer = (String) context.get("answer");
		String questionType = (String) context.get("questionType");
		String difficultyLevel = (String) context.get("difficultyLevel");
		String answerValue = (String) context.get("answerValue");
		String topicId = (String) context.get("topicId");

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
		if (questionType.equals("FILL_UP") || questionType.equals("TRUE_FALSE")
				|| questionType.equals("DETAILED_ANSWER")) {
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

			if (UtilValidate.isEmpty(context.get("numAnswers"))) {
				return "Number of Answer is mandatory";
			}

			int numOfAnswers;
			try {
				// numOfAnswers = (Integer) context.get("numAnswers");
				numOfAnswers = Integer.valueOf((String) context.get("numAnswers"));
			} catch (ClassCastException | NumberFormatException e) {
				return "Invalid Number of Answers";
			}

			if (questionType.equals("MULTIPLE_CHOICE") && numOfAnswers <= 0) {
				return "Invalid Number of answers.";
			}

			if (questionType.equals("MULTIPLE_CHOICE")
					&& (answer == null || answer.split(",").length != numOfAnswers)) {
				return "Number of answers marked is invalid.";
			}

		}

		return null;
	}

	// QUESTION SERVICE

	public Map<String, ? extends Object> getAllQuestionByTopic(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		Delegator delegator = dctx.getDelegator();

		if (UtilValidate.isEmpty(delegator)) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {

			String topicId = (String) context.get("topicId");
			if (UtilValidate.isEmpty(topicId)) {
				return ServiceUtil.returnError("Invalid topic id.");
			}

			List<GenericValue> questionsByCategory = EntityQuery.use(delegator).from("QuestionMaster")
					.where("topicId", topicId).queryList();
			result.put("questions", questionsByCategory);
			return result;

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}

	}

	public Map<String, ? extends Object> getAllQuestionTypes(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			Map<String, Object> result = ServiceUtil.returnSuccess();
			List<GenericValue> questionTypes = EntityQuery.use(delegator).from("Enumeration")
					.where(EntityCondition.makeCondition("enumTypeId", EntityOperator.LIKE, "%SPHINX_Q_TYPE%"))
					.queryList();

			result.put("data", questionTypes);
			return result;
		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	public Map<String, ? extends Object> uploadBulkQuestion(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		// process the excel file

		try {

			ByteBuffer buffer = (ByteBuffer) context.get("file");

			byte[] bytes = new byte[buffer.remaining()];

			buffer.get(bytes);

			InputStream is = new ByteArrayInputStream(bytes);

			// InputStream file = (InputStream) context.get("file");

			Map<String, Object> result = ServiceUtil.returnSuccess();

			Workbook workbook = WorkbookFactory.create(is);
			Sheet sheet = workbook.getSheetAt(0);

			// list of questions map
			List<Map<String, Object>> questions = new ArrayList<>();

			int totalRows = sheet.getLastRowNum();

			// first row considered as Header
			if (totalRows <= 1) {
				return ServiceUtil.returnError("Please fill the details and upload the file");
			}

			for (int i = 1; i <= sheet.getPhysicalNumberOfRows(); i++) {

				Row row = sheet.getRow(i);

				if (row == null)
					continue;

				Map<String, Object> question = new HashMap<>();
				List<ColumnConfig> columns = QuestionColumnConfigUtil.getColumnConfigs();

				for (ColumnConfig col : columns) {
					Cell cell = row.getCell(col.index);

					if (col.required && (cell == null || cell.getCellType() == CellType.BLANK)) {
						return ServiceUtil
								.returnError("Row " + i + ", Column " + col.index + " " + col.label + " is required");
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

			// Transaction BEGIN
			TransactionUtil.begin();

			for (Map<String, ? extends Object> question : questions) {
				Map<String, Object> serviceResult = dctx.getDispatcher().runSync("createQuestion", question);
				if (serviceResult.get("responseMessage") != null
						&& serviceResult.get("responseMessage").equals("error")) {
					Map<String, Object> errorResult = ServiceUtil
							.returnError((String) serviceResult.get("errorMessage"));

					// Transaction ROLL BACK

					TransactionUtil.rollback(); // Here we rolled back, because the service returns error;
					return errorResult;

				}
			}

			// Transaction COMMIT
			TransactionUtil.commit();

			result.put("successMessage", "Questions uploaded successfully");

			return result;

		} catch (EncryptedDocumentException | IOException | GenericServiceException | GenericTransactionException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Unexpected error occured, try again after sometime!");
		}

	}

	public Map<String, ? extends Object> getAllQuestions(DispatchContext dctx, Map<String, ? extends Object> context) {

		Delegator delegator = dctx.getDelegator();

		try {

			int viewIndex, viewSize;

			if (UtilValidate.isEmpty(context.get("viewIndex"))) {
				viewIndex = 0;
			}
			else {
				try {
					viewIndex = (Integer) context.get("viewIndex");
				} catch (ClassCastException e) {
					return ServiceUtil.returnError("");
				}
			}
			if (UtilValidate.isEmpty(context.get("viewSize"))) {
				viewSize = 10;
			}
			else {
				try {
					viewSize = (Integer) context.get("viewSize");
				} catch (ClassCastException e) {
					return ServiceUtil.returnError("");
				}
			}

			// topic and type wise filtering.

			EntityCondition questionDetailCondition;
			EntityCondition topicCondition;
			EntityCondition typeCondition;

			Object questionFilterObj = context.get("questionDetailFilter");
			if (UtilValidate.isEmpty(questionFilterObj) || UtilValidate.isEmpty(questionFilterObj.toString().strip())) {
				questionDetailCondition = null;
			} else {
				questionDetailCondition = EntityCondition.makeCondition("questionDetail", EntityOperator.LIKE,
								("%" + questionFilterObj.toString() + "%"));
			}

			Object topicFilterObject = context.get("topicIds");
			if (UtilValidate.isEmpty(topicFilterObject)) {
				topicCondition = null;
			} else {
				topicCondition = EntityCondition.makeCondition("topicId", EntityOperator.IN, topicFilterObject);
			}

			Object typeFilterObject = context.get("questionTypes");
			if (UtilValidate.isEmpty(typeFilterObject)) {
				typeCondition = null;
			} else {
				typeCondition = EntityCondition.makeCondition("questionType", EntityOperator.IN, typeFilterObject);
			}

			EntityQuery eq = EntityQuery.use(delegator).from("QuestionMaster");

			List<EntityCondition> conditions = new ArrayList<>();

			if (UtilValidate.isNotEmpty(questionDetailCondition)) {
				// eq = eq.where(questionDetailCondition);
				conditions.add(questionDetailCondition);
			}

			if (UtilValidate.isNotEmpty(topicCondition)) {
				// eq = eq.where(topicCondition);
				conditions.add(topicCondition);
			}

			if (UtilValidate.isNotEmpty(typeCondition)) {
				// eq = eq.where(typeCondition);
				conditions.add(typeCondition);
			}

			eq = eq.where(EntityCondition.makeCondition(conditions, EntityOperator.AND)).cursorScrollInsensitive();

			long totalRecords = eq.queryCount();

			// EntityQuery eq = EntityQuery.use(delegator).from("QuestionMaster").maxRows(viewSize * viewIndex).cursorScrollInsensitive();

			List<GenericValue> listOfQuestions = Collections.emptyList();
			// int balanceRecord;

			// try (EntityListIterator iterator = eq.queryIterator()) {
			// listOfQuestions = iterator.getPartialList(viewIndex, viewSize);
			// balanceRecord = iterator.getResultsSizeAfterPartialList();
			// }

			listOfQuestions = eq.limit(viewSize).offset(viewSize * viewIndex).queryList();

			Map<String, Object> serviceResult = ServiceUtil.returnSuccess();

			serviceResult.put("data", listOfQuestions);
			serviceResult.put("meta", UtilMisc.toMap("viewIndex", viewIndex, "viewSize", viewSize, "totalRecords", totalRecords));
			return serviceResult;

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong! Try again later!");
		}

	}
	
	public Map<String ,? extends Object> updateQuestionWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context){
		try {
			LocalDispatcher dispatcher = dctx.getDispatcher();

			if (UtilValidate.isEmpty(dispatcher)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String errorMsg = validateQuestionData(context);

			if (UtilValidate.isNotEmpty(errorMsg)) {
				return ServiceUtil.returnError(errorMsg);
			}
			Map<String, Object> result;
			result = dispatcher.runSync("updateQuestion", context);
			if(ServiceUtil.isError(result)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}
			return ServiceUtil.returnSuccess(result.toString());
		}catch (Exception e) {
			return  ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}
		
	}

	public Map<String ,? extends Object> createQuestionWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context){
		try {
			LocalDispatcher dispatcher = dctx.getDispatcher();

			if (UtilValidate.isEmpty(dispatcher)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String errorMsg = validateQuestionData(context);

			if (UtilValidate.isNotEmpty(errorMsg)) {
				return ServiceUtil.returnError(errorMsg);
			}
			Map<String, Object> result;
			result = dispatcher.runSync("createQuestion", context);
			if(ServiceUtil.isError(result)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}
			return ServiceUtil.returnSuccess(result.toString());
		}catch (Exception e) {
			return  ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}
		
	}
	
	public Map<String, ? extends Object> deleteQuestionsWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {
			LocalDispatcher dispatcher = dctx.getDispatcher();

			@SuppressWarnings("unchecked")
			List<String> questionIds = (List<String>) context.get("questionIds");
			if (UtilValidate.isEmpty(questionIds)) {
				return ServiceUtil.returnError("Question Id ia required");
			}

			int count = 0;

			for (String qId : questionIds) {
				if (UtilValidate.isEmpty(qId)) {
					return ServiceUtil.returnError(
									"Total Deleted Questions: " + count + "For Question " + count + 1 + " Question Id is required");
				}

				Map<String, Object> result = dispatcher.runSync("deleteQuestion", UtilMisc.toMap("questionId", qId));
				if (ServiceUtil.isError(result)) {
					return result;
				}
				count++;
			}

			return ServiceUtil.returnSuccess("Questions deleted sucessfully, Total Deleted Questions: " + count);
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
		}
	}

}
