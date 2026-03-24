package com.sphinx.resourse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/questions")
@Produces(MediaType.APPLICATION_JSON)
public class QuestionResource {

	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addQuestions(Map<String, Object> input) {

		return null;
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadQuestions(@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileDetail) {

		String fileName = fileDetail.getFileName();

		// file type check
		if (!fileName.endsWith(".xlsx")) {
			// error only xlsx files are allowed
		}

		// file size check
		long sizeInMb = fileDetail.getSize() / 1024;

		// maximum limit.
		if (sizeInMb > 5) {
			// error size limit less than 5 mb
		}

		// process the excel file

		try {
			Workbook workbook = WorkbookFactory.create(file);

			Sheet sheet = workbook.getSheetAt(0);
			
			List<Map<String, ? extends Object>> questions = new ArrayList<Map<String, ? extends Object>>();
			
			// starts from 1, skip header
			for (int i = 1; i < sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				
				if (row == null)
					continue;

			}

		} catch (EncryptedDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
