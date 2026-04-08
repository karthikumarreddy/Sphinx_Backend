package com.sphinx.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
			Locale locale = (Locale) context.get("locale");

		
			String examName = (String) context.get("examId");

			GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").queryFirst();

			if (exam == null) {
				return ServiceUtil.returnError("Invalid Exam Details! No Records found!");
			}

			String examId = (String) exam.get("examId");
			String examDescription = (String) exam.get("description");
			int noOfQuestions = (Integer) exam.get("noOfQuestions");
			int passPercentage = (Integer) exam.get("passPercentage");
			int duration = (Integer) exam.get("duration");

			List<GenericValue> assignedUsersWithCredentialAndEmail = EntityQuery.use(delegator).from("UserDetailsForEmail")
							.where("contactMechTypeId", "EMAIL_ADDRESS", "examId", examId)
							.queryList();

			if (assignedUsersWithCredentialAndEmail == null || assignedUsersWithCredentialAndEmail.isEmpty()) {
				return ServiceUtil.returnError("No assigned users found for the exam.");
			}

			GenericValue userLogin = (GenericValue) context.get("userLogin"); // who triggers the email


			// Prepare recipients
			List<String> recipientEmails = new ArrayList<>();

			for (GenericValue user : assignedUsersWithCredentialAndEmail) {
				if (user != null) {
					String email = (String) user.get("infoString");
					if (email != null) {
						recipientEmails.add(email);
					}
				}
			}

			if (recipientEmails.isEmpty()) {
				return ServiceUtil.returnError("No valid recipient emails found.");
			}

			// Prepare template data
			Map<String, Object> templateData = new HashMap<>();
			templateData.put("examName", examName);
			templateData.put("description", examDescription);
			templateData.put("noOfQuestions", noOfQuestions);
			templateData.put("passPercentage", passPercentage);
			templateData.put("duration", duration);

			// Assume username/password are in context for assigned users
			templateData.put("assignedUsers", context.get("assignedUsers")); // List<Map<String, String>> with keys: username, password,
																				// email

			// Prepare sendMail context
			Map<String, Object> sendMailContext = new HashMap<>();
			sendMailContext.put("sendTo", String.join(",", recipientEmails));
			sendMailContext.put("sendFrom", "noreply@ofbiz.org"); // change as needed
			sendMailContext.put("subject", "Exam Assigned: " + examName);
			sendMailContext.put("templateName", "component://Sphinx/template/email/ExamNotificaitonEmailTemplate.ftl");
			sendMailContext.put("templateData", templateData);
			sendMailContext.put("userLogin", userLogin);

			try {
				// Async send using OFBiz generic notification email
				dispatcher.runAsync("sendGenericNotificationEmail", sendMailContext);
			} catch (GenericServiceException e) {
				Debug.logError(e, "Failed to send exam notification email", MODULE);
				return ServiceUtil.returnError("Failed to send exam notification: " + e.getMessage());
			}
			return ServiceUtil.returnSuccess();
		} catch (GenericEntityException e) {
			Debug.logError(e, "Failed to send exam notification email", MODULE);
			return ServiceUtil.returnError("Failed to send exam notification: " + e.getMessage());
		}
	}
}
