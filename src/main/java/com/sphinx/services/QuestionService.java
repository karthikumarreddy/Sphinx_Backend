package com.sphinx.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sphinx.util.QuestionColumnConfigUtil;
import com.sphinx.util.QuestionColumnConfigUtil.ColumnConfig;

public class QuestionService {

	private static final String MODULE = QuestionService.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";


	// QUESTION SERVICE

	// public Map<String, ? extends Object> getUploadFormatDocument(DispatchContext
	// dctx, Map<String, ? extends Object> context) {
	//
	// List<GenericValue> topics =
	// EntityQuery.use(null).from("TopicMaster").queryList();
	// List<GenericValue> types = EntityQuery.use(null).from("Enumeration")
	// .where(EntityCondition.makeCondition(EntityFunction.upperField("enumTypeId"),
	// EntityOperator.LIKE,
	// EntityFunction.upper("%SPHINX_Q_TYPE%")))
	// .queryList();
	//
	// Workbook workbook = new XSSFWorkbook();
	//
	// // 2. Hidden sheet holding the dropdown values
	// Sheet refSheet = workbook.createSheet("_ref");
	// workbook.setSheetHidden(workbook.getSheetIndex("_ref"), true);
	//
	// for (int i = 0; i < types.size(); i++)
	// refSheet.createRow(i).createCell(0).setCellValue(types.get(i).getString("enumId"));
	//
	// for (int i = 0; i < topics.size(); i++) {
	// Row row = refSheet.getRow(i) != null ? refSheet.getRow(i) :
	// refSheet.createRow(i);
	// row.createCell(1).setCellValue(topics.get(i).getString("topicName"));
	// }
	//
	// // 3. Main data sheet
	// Sheet sheet = workbook.createSheet("Questions");
	//
	// // Header row
	// Row header = sheet.createRow(0);
	// header.createCell(0).setCellValue("Question Text");
	// header.createCell(1).setCellValue("Question Type"); // dropdown
	// header.createCell(2).setCellValue("Question Topic"); // dropdown
	// header.createCell(3).setCellValue("Marks");
	//
	// // Style the header
	// CellStyle headerStyle = workbook.createCellStyle();
	// Font font = workbook.createFont();
	// font.setBold(true);
	// headerStyle.setFont(font);
	// headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
	// headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	// for (Cell cell : header)
	// cell.setCellStyle(headerStyle);
	//
	// // 4. Apply dropdowns to rows 2-1001 (1000 question rows)
	// DataValidationHelper dvHelper = sheet.getDataValidationHelper();
	//
	// // Type dropdown — references _ref column A
	// String typeFormula = "_ref!$A$1:$A$" + types.size();
	// String topicFormula = "_ref!$B$1:$B$" + topics.size();
	//
	// addDropdown(sheet, dvHelper, typeFormula, 1, 1, 1000, 1); // col B
	// addDropdown(sheet, dvHelper, topicFormula, 1, 1, 1000, 2); // col C
	//
	// // Auto-size columns
	// for (int i = 0; i < 4; i++)
	// sheet.autoSizeColumn(i);
	//
	// // 5. Return as downloadable response
	// ByteArrayOutputStream out = new ByteArrayOutputStream();
	// workbook.write(out);
	// workbook.close();
	//
	// return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
	// "attachment; filename=questions_template.xlsx")
	// .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
	// .body(out.toByteArray());
	// }
	//
	// private void addDropdown(Sheet sheet, DataValidationHelper dvHelper, String
	// formula, int firstRow, int lastRow, int firstCol,
	// int lastCol) {
	// DataValidationConstraint constraint =
	// dvHelper.createFormulaListConstraint(formula);
	// CellRangeAddressList range = new CellRangeAddressList(firstRow, lastRow,
	// firstCol, lastCol);
	// DataValidation dv = dvHelper.createValidation(constraint, range);
	// dv.setShowErrorBox(true);
	// dv.createErrorBox("Invalid value", "Please select from the dropdown list.");
	// dv.setShowDropDown(false); // false = show the arrow
	// sheet.addValidationData(dv);
	// }

	public Map<String, ? extends Object> getTemplateDocument(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {

			Map<String, Object> result = ServiceUtil.returnSuccess();

			Workbook workbook = new XSSFWorkbook();
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

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			workbook.write(out);

			byte[] bytes = out.toByteArray();

			out.close();

			workbook.close();

			result.put("bytes", bytes);

			return result;

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Unexpected error occured, try again after sometime!");
		}

	}

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

	public Map<String, ? extends Object> createQuestion(DispatchContext dctx, Map<String, ? extends Object> context) {

		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			GenericValue questionRecord = delegator.makeValue("QuestionMaster");

			questionRecord.setNextSeqId();

			String topicId = (String) context.get("topicId");

			GenericValue topic = delegator.findOne("TopicMaster", true, UtilMisc.toMap("topicId", topicId));

			if (UtilValidate.isEmpty(topic)) {
				return ServiceUtil.returnError("Question Topic was not a valid one!");
			}

			questionRecord.setNonPKFields(context);

			questionRecord.create();

			return ServiceUtil.returnSuccess("Question addedd successfully!");
		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Unexpected error occured");
		}
	}

	public Map<String, ? extends Object> createBulkQuestions(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {
			

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			GenericValue questionRecord = delegator.makeValue("QuestionMaster");

			questionRecord.setNextSeqId();

			String topicId = (String) context.get("topicId");

			GenericValue topic = delegator.findOne("TopicMaster", true, UtilMisc.toMap("topicId", topicId));

			if (UtilValidate.isEmpty(topic)) {
				return ServiceUtil.returnError("Question Topic was not a valid one!");
			}

			questionRecord.setNonPKFields(context);

			questionRecord.create();

			return ServiceUtil.returnSuccess("Question addedd successfully!");
		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Unexpected error occured");
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

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

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

}
