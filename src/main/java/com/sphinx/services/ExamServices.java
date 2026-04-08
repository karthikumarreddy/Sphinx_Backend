package com.sphinx.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
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

import com.sphinx.util.ServiceHelper;

public class ExamServices {
	private static final String MODULE = ExamServices.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";

	public static Map<String, ? extends Object> getExam(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Map<String, Object> result = ServiceUtil.returnSuccess();
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			List<GenericValue> examList = delegator.findAll("ExamMaster", false);
			if (UtilValidate.isEmpty(examList)) {
				return ServiceUtil.returnError("no exam created to display");
			}
			result.put("examList", examList);
			return result;
		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try again later ");
		}

	}

	public static Map<String, Object> createExam(DispatchContext dctx, Map<String, Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String examId = delegator.getNextSeqId("ExamMaster");
			String partyId = (String) context.get("partyId");

			GenericValue examMaster = delegator.makeValue("ExamMaster");
			examMaster.set("examId", examId);
			examMaster.set("examName", context.get("examName"));
			examMaster.set("description", context.get("description"));
			examMaster.set("noOfQuestions", Long.parseLong(context.get("noOfQuestions").toString()));
			examMaster.set("duration", Long.parseLong(context.get("duration").toString()));
			examMaster.set("passPercentage", Long.parseLong(context.get("passPercentage").toString()));
			examMaster.set("questionsRandomized",
					context.get("questionsRandomized") != null
							? Long.parseLong(context.get("questionsRandomized").toString())
							: 0L);
			examMaster.set("answersMust",
					context.get("answersMust") != null ? Long.parseLong(context.get("answersMust").toString()) : 0L);
			examMaster.set("allowNegativeMarks",
					context.get("allowNegativeMarks") != null
							? Long.parseLong(context.get("allowNegativeMarks").toString())
							: 0L);
			examMaster.set("negativeMarkValue", context.get("negativeMarkValue"));
			examMaster.set("fromDate", UtilDateTime.nowTimestamp());
			examMaster.set("thruDate", null);
			examMaster.set("examSetupProper", 0L);

			GenericValue userLogin = (GenericValue) context.get("userLogin");
			if (UtilValidate.isEmpty(userLogin)) {
				examMaster.set("createdByUserLogin", userLogin.getString("userLoginId"));
				examMaster.set("lastModifiedByUserLogin", userLogin.getString("userLoginId"));
			}

			delegator.create(examMaster);

			GenericValue adminPartRel = delegator.makeValue("AdminPartyExamRel");
			adminPartRel.set("examId", examId);
			adminPartRel.set("partyId", partyId);
			delegator.create(adminPartRel);

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("examId", examId);
			result.put("partyId", partyId);
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong: " + e.getMessage());
		}
//		
//		try {
//			Delegator delegator = dctx.getDelegator();
//			String examId = delegator.getNextSeqId("ExamMaster");
//			String partyId = (String) context.get("partyId");
//			context.put("examId", examId);
//			LocalDispatcher dispatcher = dctx.getDispatcher();
//			Map<String, Object> examResult = dispatcher.runSync("createExam", context);
//			if (ServiceUtil.isError(examResult)) {
//				return ServiceUtil.returnError("A porblem occured while creating the exam");
//			}
//			Map<String, Object> adminPartyRel = dispatcher.runSync("AdminPartyRel",
//					UtilMisc.toMap("examId", examId, "partyId", partyId));
//			if (ServiceUtil.isError(adminPartyRel)) {
//				return ServiceUtil.returnError("A problem occured while creating the exam");
//			}
//			Map<String, Object> result = ServiceUtil.returnSuccess();
//			result.put("examId", examId);
//			return result;
//
//		} catch (Exception e) {
//			return ServiceUtil.returnError("Something went wrong try again later");
//		}
	}

	public static Map<String, Object> updateExam(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			GenericValue examMaster = delegator.findOne("ExamMaster", false,
					UtilMisc.toMap("examId", context.get("examId")));

			if (UtilValidate.isEmpty(examMaster)) {
				return ServiceUtil.returnError("Exam not found");
			}

			examMaster.set("examName", context.get("examName"));
			examMaster.set("description", context.get("description"));
			examMaster.set("noOfQuestions", Long.parseLong(context.get("noOfQuestions").toString()));
			examMaster.set("duration", Long.parseLong(context.get("duration").toString()));
			examMaster.set("passPercentage", Long.parseLong(context.get("passPercentage").toString()));
			examMaster.set("questionsRandomized",
					context.get("questionsRandomized") != null
							? Long.parseLong(context.get("questionsRandomized").toString())
							: 0L);
			examMaster.set("answersMust",
					context.get("answersMust") != null ? Long.parseLong(context.get("answersMust").toString()) : 0L);
			examMaster.set("allowNegativeMarks",
					context.get("allowNegativeMarks") != null
							? Long.parseLong(context.get("allowNegativeMarks").toString())
							: 0L);
			examMaster.set("negativeMarkValue", context.get("negativeMarkValue"));
			examMaster.set("fromDate", UtilDateTime.nowTimestamp());
			examMaster.set("thruDate", null);
			examMaster.set("examSetupProper", 0L);

			GenericValue userLogin = (GenericValue) context.get("userLogin");
			if (UtilValidate.isEmpty(userLogin)) {
				examMaster.set("createdByUserLogin", userLogin.getString("userLoginId"));
				examMaster.set("lastModifiedByUserLogin", userLogin.getString("userLoginId"));
			}

			delegator.store(examMaster);

			return ServiceUtil.returnSuccess("Updated successfully");

		} catch (Exception e) {
			e.printStackTrace(); // helpful for debugging
			return ServiceUtil.returnError("Something went wrong, try later");
		}
	}

	public static Map<String, Object> assignUsersToExam(DispatchContext dctx, Map<String, ? extends Object> context) {

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) dctx.getDispatcher();

			if (UtilValidate.isEmpty(dispatcher)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			List<Map<String, Object>> users = (List<Map<String, Object>>) context.get("users");

			// ================ BEGIN TRANSACTION ==================================
			TransactionUtil.begin();

			boolean firstTime = true;

			for (Map<String, Object> user : users) {

				String partyId = (String) user.get("partyId");
				String examId = (String) user.get("examId");
				int allowedAttempts = (Integer) user.get("allowedAttempts");
				int timeoutDays = (Integer) user.get("timeoutDays");

				GenericValue party = EntityQuery.use(dctx.getDelegator()).from("Party").where("partyId", partyId)
						.queryFirst();

				if (UtilValidate.isEmpty(examId)) {
					return ServiceUtil.returnError("Give Exam Details is Invalid!");
				}

				if (UtilValidate.isEmpty(partyId)) {
					return ServiceUtil.returnError("Give User Details is Invalid!");
				}

				if (UtilValidate.isEmpty(party)) {
					return ServiceUtil.returnError("Invalid User Details! Record Not Found!");
				}

				if (firstTime) {
					GenericValue exam = EntityQuery.use(dctx.getDelegator()).from("ExamMaster").where("examId", examId)
							.queryFirst();

					if (UtilValidate.isEmpty(exam)) {
						return ServiceUtil.returnError("Invalid Exam Details! Record Not Found!");
					}
				}

				if (allowedAttempts <= 0 && allowedAttempts > 5) {
					return ServiceUtil.returnError("Invalid Allowed Attempts! Should in between 0 and 5");
				}

				if (timeoutDays < 0 && timeoutDays > 5) {
					return ServiceUtil.returnError("Invalid Timeout Days! Should in between 0 and 5");
				}

				Map<String, Object> result = dispatcher.runSync("createPartyExamRelationship",
						UtilMisc.toMap("partyId", partyId, "examId", examId, "allowedAttempts", allowedAttempts,
								"timeoutDays", timeoutDays, "noOfAttempts", 0));

				if (ServiceUtil.isError(result)) {
					result.put("errorMessage", UNEXPECTED_ERROR_MSG);
					// ================ ROLLBACK TRANSACTION ==================================
					TransactionUtil.rollback();
					return result;
				}

			}
			// ================ COMMIT TRANSACTION ==================================
			TransactionUtil.commit();

			return ServiceUtil.returnSuccess("Users Assigned Successfully!");

		} catch (ClassCastException e) {
			try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException e1) {
				Debug.logError(e, MODULE);
			}

			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input values!");
		} catch (GenericServiceException | GenericEntityException e) {
			try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException e1) {
				Debug.logError(e, MODULE);
			}
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}
	}

	public static Map<String, Object> getAllAssignedUsersForExam(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String examId = (String) context.get("examId");

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid Exam Id!");
			}

			List<GenericValue> assignedUsers = EntityQuery.use(delegator).from("PersonWithExam").where("partyTypeId",
					"PERSON", "statusId", "PARTY_ENABLED", "roleTypeId", "SphinxUser", "examId", examId).queryList();

			Map<String, Object> result = ServiceUtil.returnSuccess("User Details!");
			result.put("data", assignedUsers);
			return result;

		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input values!");
		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	public static Map<String, Object> getAllExamAssignedForUser(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User!");
			}

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			List<GenericValue> assignedExams = EntityQuery.use(delegator).from("AssignedExamDetails")
					.where("partyId", partyId).queryList();

			Map<String, Object> result = ServiceUtil.returnSuccess("Exam Details!");
			result.put("data", assignedExams);
			return result;

		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input values!");
		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	public static Map<String, Object> removeAssignedUserFromExam(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {

			LocalDispatcher dispatcher = (LocalDispatcher) dctx.getDispatcher();

			if (UtilValidate.isEmpty(dispatcher)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String partyId = (String) context.get("partyId");
			String examId = (String) context.get("examId");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User!");
			}

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid User!");
			}

			GenericValue assignedUserRecord = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId", partyId, "examId", examId).queryFirst();

			if (assignedUserRecord == null) {
				return ServiceUtil.returnSuccess("User already Removed!");
			}

			Map<String, Object> result = dispatcher.runSync("removeAssignedUserFromExam",
					UtilMisc.toMap("partyId", partyId, "examId", examId));

			if (result.containsKey("responseMessage") && result.get("responseMessage").equals("success")) {
				result.put("successMessage", "User removed from Exam Successfully!");
			}
			return result;

		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input values!");
		} catch (GenericEntityException | GenericServiceException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	public static Map<String, ? extends Object> adminExamList(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String partyId = (String) context.get("partyId");

			// List<GenericValue> adminExams =
			// EntityQuery.use(delegator).from("AdminPartyExamRel").where("partyId",
			// partyId).queryList();

			List<GenericValue> listOfExams = EntityQuery.use(delegator).from("ExamByAdminDetails")
					.where("partyId", partyId).queryList();

			Map<String, Object> serviceResult = ServiceUtil.returnSuccess("Admin Exam List!");

			serviceResult.put("data", listOfExams);

			return serviceResult;

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}
	}

	public static Map<String, ? extends Object> updateAssignedUserWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		Delegator delegator = dctx.getDelegator();

		if (UtilValidate.isEmpty(delegator)) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

		String partyId = (String) context.get("partyId");
		String examId = (String) context.get("examId");
		Object allowedAttemptsStr = (String) context.get("allowedAttempts");
		Object timeoutDaysStr = (String) context.get("timeoutDays");
		Integer allowedAttempts;
		Integer timeoutDays;

		if (UtilValidate.isEmpty(partyId)) {
			return ServiceUtil.returnError("Invalid User Info!");
		}

		if (UtilValidate.isEmpty(examId)) {
			return ServiceUtil.returnError("Invalid Exam Info!");
		}

		if (UtilValidate.isEmpty(allowedAttemptsStr)) {
			return ServiceUtil.returnError("Invalid Allowed Attempts!");
		} else {
			try {
				allowedAttempts = Integer.valueOf((String) allowedAttemptsStr);
			} catch (ClassCastException | NumberFormatException e) {
				return ServiceUtil.returnError("Invalid Allowed Attempts!");
			}
		}

		if (UtilValidate.isEmpty(timeoutDaysStr)) {
			return ServiceUtil.returnError("Invalid Exam Timeout Period!");
		} else {
			try {
				timeoutDays = Integer.valueOf((String) timeoutDaysStr);
			} catch (NumberFormatException | ClassCastException e) {
				return ServiceUtil.returnError("Invalid Exam Timeout Period!");
			}
		}

		GenericValue assignUser;
		try {
			assignUser = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId", partyId, "examId", examId).queryOne();
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError("Something Went Wrong! Try again later!");
		}

		if (assignUser == null) {
			return ServiceUtil.returnError("Given User not Assigned to this Exam!");
		}

		try {
			Map<String, Object> result = dctx.getDispatcher().runSync("updateAssignedUser", UtilMisc.toMap("partyId",
					partyId, "examId", examId, "allowedAttempts", allowedAttempts, "timeoutDays", timeoutDays));

			if (!ServiceHelper.isError(result)) {
				result.put("successMessage", "Assigned User Info Updated Successfully!");
			}

			return result;
		} catch (GenericServiceException e) {
			return ServiceUtil.returnError("Something Went Wrong! Try again later!");
		}

	}

	public static Map<String, ? extends Object> deleteExamWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		LocalDispatcher dispatcher = (LocalDispatcher) dctx.getDispatcher();

		if (UtilValidate.isEmpty(dispatcher)) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

		Delegator delegator = dctx.getDelegator();

		if (UtilValidate.isEmpty(delegator)) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}
		String examId = (String) context.get("examId");

		if (UtilValidate.isEmpty(examId)) {
			return ServiceUtil.returnError("examId is required to delete an exam.");
		}

		try {

			EntityCondition questionBCondition = EntityCondition.makeCondition("examId", EntityOperator.EQUALS, examId);
			int dataRemoved = delegator.removeByCondition("QuestionBankMasterB", questionBCondition);

			// delete QuestionBankMaster
			EntityCondition qbCondition = EntityCondition.makeCondition("examId", EntityOperator.EQUALS, examId);
			delegator.removeByCondition("QuestionBankMaster", qbCondition);
			Debug.logInfo("Deleted all QuestionBankMaster records for examId: " + examId, MODULE);

			// delete ExamTopicDetails it has a composite PK examId , topicId
			// so we fetch all topics for this exam first then delete each.

			List<GenericValue> examTopics = delegator.findByAnd("ExamTopicDetails", UtilMisc.toMap("examId", examId),
					null, false);

			if (UtilValidate.isNotEmpty(examTopics)) {
				for (GenericValue examTopic : examTopics) {
					Map<String, Object> deleteTopicCtx = new HashMap<>();
					deleteTopicCtx.put("examId", examId);
					deleteTopicCtx.put("topicId", examTopic.getString("topicId"));

					Map<String, Object> deleteTopicResult = dispatcher.runSync("deleteExamTopics", deleteTopicCtx);

					if (ServiceUtil.isError(deleteTopicResult)) {
						Debug.logError("Failed to delete ExamTopicDetails for examId: " + examId + ", topicId: "
								+ examTopic.getString("topicId"), MODULE);
						return ServiceUtil.returnError(
								"Failed to delete exam topic: " + ServiceUtil.getErrorMessage(deleteTopicResult));
					}
				}
				Debug.logInfo("exam topics deleted", MODULE);
			} else {
				Debug.logInfo("No ExamTopicDetails found for examId: " + examId, MODULE);
			}

			// delete the ExamMaster record
			Map<String, Object> deleteExamCtx = new HashMap<>();
			deleteExamCtx.put("examId", examId);
			Map<String, Object> deleteExamResult = dispatcher.runSync("deleteExam", deleteExamCtx);

			if (ServiceUtil.isError(deleteExamResult)) {
				Debug.logError("Failed to delete ExamMaster for examId: " + examId, MODULE);
				return ServiceUtil
						.returnError("Failed to delete exam: " + ServiceUtil.getErrorMessage(deleteExamResult));
			}

			Debug.logInfo("Exam deleted successfully for examId: " + examId, MODULE);
			return ServiceUtil.returnSuccess("Exam and all related data deleted successfully.");

		} catch (GenericEntityException e) {
			Debug.logError(e, "Entity error while deleting exam: " + examId, MODULE);
			return ServiceUtil.returnError("Database error: " + e.getMessage());
		} catch (GenericServiceException e) {
			return ServiceUtil.returnError("Database error: " + e.getMessage());
		}

	}

	public static Map<String, ? extends Object> getAllExamQuestions(DispatchContext dctx,
			Map<String, ? extends Object> contaxt) {
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String examId = (String) contaxt.get("examId");
			List<GenericValue> examQuestions = EntityQuery.use(delegator).from("QuestionBankMasterB")
					.where("examId", examId).queryList();
			if (examQuestions == null || examQuestions.isEmpty()) {
				return ServiceUtil.returnError("Questions is not assigned to the exam");
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("data", examQuestions);
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try again later");
		}
	}
}
