package com.sphinx.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamEmailServices {

	private static final String MODULE = ExamEmailServices.class.getName();


	public static Map<String, Object> sendExamNotification(DispatchContext ctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = ctx.getDelegator();
			LocalDispatcher dispatcher = ctx.getDispatcher();
			// Locale locale = (Locale) context.get("locale");

			GenericValue exam = (GenericValue) context.get("examRecord");

			// GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryFirst();

			if (exam == null) {
				return ServiceUtil.returnError("Invalid Exam Details! No Records found!");
			}

			String examName = (String) exam.get("examName");
			// String examDescription = (String) exam.get("description");
			// long noOfQuestions = (Long) exam.get("noOfQuestions");
			// long passPercentage = (Long) exam.get("passPercentage");
			// long duration = (Long) exam.get("duration");

			List<GenericValue> assignedUsersListsWithEmails = EntityQuery.use(delegator).from("UserDetailsForEmail")
							.where("contactMechTypeId", "EMAIL_ADDRESS", "examId", exam.get("examId"))
							.queryList();

			if (assignedUsersListsWithEmails.isEmpty()) {
				return ServiceUtil.returnError("No assigned users found for the exam.");
			}

			// // Prepare template data
			// Map<String, Object> templateData = new HashMap<>();
			// templateData.put("examName", examName);
			// templateData.put("description", examDescription);
			// templateData.put("noOfQuestions", noOfQuestions);
			// templateData.put("passPercentage", passPercentage);
			// templateData.put("duration", duration);
			//
			// // Assume username/password are in context for assigned users
			// templateData.put("assignedUsers", context.get("assignedUsers")); // List<Map<String, String>> with keys: username, password,
			// // email
			// // Prepare sendMail context
			// Map<String, Object> sendMailContext = new HashMap<>();
			//
			// sendMailContext.put("sendFrom", "vasudevantmail@gmail.com");
			// sendMailContext.put("subject", "Exam Assigned: " + examName);
			// sendMailContext.put("templateName", "component://Sphinx/template/email/ExamNotificaitonEmailTemplate.ftl");
			// sendMailContext.put("templateData", templateData);

			Map<String, Object> emailContext = new HashMap<>();
			emailContext.put("subject", "Exam Assignment and Access Details");

			String emailBody = "Dear Candidate,\n\n" + "You have been successfully assigned to the following examination: " + examName
							+ "\n\nPlease find your login credentials below: \n\n" + "Username:  %s \n" + "Security Code: %s \n\n"
							+ "Kindly use the above credentials to access the Sphinx application and commence your examination.\n\nShould you require any assistance, please do not hesitate to contact the administrator."
							+ "Best regards,\nSphinx Administrator";

			emailContext.put("contentType", "text/plain");

			for (GenericValue assignedUser : assignedUsersListsWithEmails) {

				emailContext.put("sendTo", assignedUser.getString("infoString"));
				emailContext.put("body",
								String.format(emailBody, assignedUser.getString("userLoginId"), assignedUser.getString("securityCode")));

				try {
					dispatcher.runAsync("sendMail", emailContext);
				} catch (GenericServiceException e) {
					Debug.logError(e, "Failed to send exam notification email", MODULE);
					// return ServiceUtil.returnError("Failed to send exam notification: " + e.getMessage());
				}

			}


			return ServiceUtil.returnSuccess("Mail Notificaiton Initiated! The Users will recieve the Email shortly!");

		} catch (GenericEntityException e) {
			Debug.logError(e, "Failed to send exam notification email", MODULE);
			return ServiceUtil.returnError("Failed to send exam notification: " + e.getMessage());
		} catch (Exception e) {
			Debug.logError(e, "Failed to send exam notification email", MODULE);
			return ServiceUtil.returnError("Failed to send exam notification: " + e.getMessage());
		}
	}
}
