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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.sphinx.util.ApiResponse;
import com.sphinx.util.QuestionColumnConfigUtil;
import com.sphinx.util.QuestionColumnConfigUtil.ColumnConfig;

@Path("/questions")
public class QuestionResource {

	private static final String MODULE = QuestionResource.class.getName();

	@Context
	private HttpServletRequest request;

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

	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addQuestions(Map<String, Object> input) {
		try {

			Map<String, Object> result = getDispatcher().runSync("createQuestion", input);
			System.out.println("============== RESULT ======================" + result);
			return Response.status(201).entity(result).build();

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ApiResponse.response(false, 500, e.getMessage(), null)).build();
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
			return Response.status(400).entity(ApiResponse.response(false, 400, "Only Excel file with .xlsx files are allowed", null))
							.build();
		}

		// file size check
		long sizeInMb = fileDetail.getSize() / 1024;

		// maximum limit.
		if (sizeInMb > 5) {
			return Response.status(400)
							.entity(ApiResponse.response(false, 400,
											"File size exceeds 5 mb, only file less than that are allowed.", null))
							.build();
		}

		// process the excel file

		try {
			Workbook workbook = WorkbookFactory.create(file);
			Sheet sheet = workbook.getSheetAt(0);
			List<Map<String, Object>> questions = new ArrayList<>();

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
										.entity(ApiResponse.response(false, 400,
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

						if (numVal == Math.floor(numVal)) {
							question.put(col.field, (int) numVal);
						} else {
							question.put(col.field, numVal);
						}
						break;

					case STRING:
						String strVal = cell.getStringCellValue();
						question.put(col.field, strVal != null ? strVal.trim() : null);
						break;

					case BOOLEAN:
						question.put(col.field, cell.getBooleanCellValue());
						break;

					case FORMULA:
						// Evaluate formula and get result
						FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
						CellValue evaluated = evaluator.evaluate(cell);
						switch (evaluated.getCellType()) {
						case NUMERIC:
							question.put(col.field, (int) evaluated.getNumberValue());
							break;
						case STRING:
							question.put(col.field, evaluated.getStringValue());
							break;
						case BOOLEAN:
							question.put(col.field, evaluated.getBooleanValue());
							break;
						default:
							question.put(col.field, null);
						}
						break;

					case BLANK:
					default:
						question.put(col.field, null);
						break;
					}
				}
				questions.add(question);
			}

			Map<String, Object> serviceResult = getDispatcher().runSync("createBulkQuestions",
							UtilMisc.toMap("listOfQuestions", questions));

			return Response.status(201).entity(ApiResponse.response(true, 201, "Questions uploaded successfully!", serviceResult)).build();

		} catch (EncryptedDocumentException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ApiResponse.response(false, 500, e.getMessage(), null)).build();
		} catch (IOException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ApiResponse.response(false, 500, e.getMessage(), null)).build();
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return Response.status(500).entity(ApiResponse.response(false, 500, e.getMessage(), null)).build();
		}

	}

}
