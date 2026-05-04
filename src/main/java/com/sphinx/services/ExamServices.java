package com.sphinx.services;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public static Map<String, Object> getExamByName(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String examName = (String) context.get("examName");

			List<GenericValue> examList;
			if (UtilValidate.isEmpty(examName)) {
				examList = EntityQuery.use(delegator).from("ExamMaster").queryList();
			} else {
				examList = EntityQuery.use(delegator).from("ExamMaster")
						.where(EntityCondition.makeCondition("examName", EntityOperator.LIKE, "%" + examName + "%"))
						.queryList();
			}

			if (UtilValidate.isEmpty(examList)) {
				return ServiceUtil.returnError("No exams found");
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("examList", examList);
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong: " + e.getMessage());
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
			String description = (String) context.get("description");

			if (description.length() > 500) {
				return ServiceUtil.returnError("Description should be less than 500 letters");
			}

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
			if (!UtilValidate.isEmpty(userLogin)) {
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
			if (!UtilValidate.isEmpty(userLogin)) {
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

				if (allowedAttempts < 0 && allowedAttempts > 5) {
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

			List<String> partyIds = (List<String>) context.get("partyId");
			String examId = (String) context.get("examId");

			if (UtilValidate.isEmpty(partyIds)) {
				return ServiceUtil.returnError("Invalid Users!");
			}

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid Assessment Information!");
			}

			// GenericValue assignedUserRecord =
			// EntityQuery.use(delegator).from("PartyExamRelationship")
			// .where("partyId", partyId, "examId", examId).queryFirst();

			List<GenericValue> assignedUserRecord = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where(EntityCondition.makeCondition("partyId", EntityOperator.IN, partyIds),
							EntityCondition.makeCondition("examId", EntityOperator.EQUALS, examId))
					.queryList();

			if (assignedUserRecord == null) {
				return ServiceUtil.returnSuccess("Users already Removed!");
			}

			int count = assignedUserRecord.size();
			delegator.removeAll(assignedUserRecord);

			// Map<String, Object> result = dispatcher.runSync("removeAssignedUserFromExam",
			// UtilMisc.toMap("partyId", partyId, "examId", examId));

			// if (result.containsKey("responseMessage") &&
			// result.get("responseMessage").equals("success")) {
			// result.put("successMessage", "User removed from Exam Successfully!");
			// }
			// return result;

			return ServiceUtil.returnSuccess(count + " Users Removed from Assessment!");

		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input values!");
		} catch (GenericEntityException e) {
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

	public static Map<String, ? extends Object> adminExamListCount(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid Admin Details!");
			}

			List<GenericValue> listOfExams = EntityQuery.use(delegator).from("ExamByAdminDetails")
					.where("partyId", partyId).queryList();

			Map<String, Object> serviceResult = ServiceUtil.returnSuccess("Admin Exam List!");

			serviceResult.put("count", listOfExams.size());

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

	public static Map<String, ? extends Object> generateQuestionsForExam(DispatchContext dctx,
			Map<String, ? extends Object> contaxt) {
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String examId = (String) contaxt.get("examId");
			String partyId = (String) contaxt.get("partyId");

			if (UtilValidate.isEmpty(examId)) {
				Debug.logError("Exam Id is Invalid from from frontend Exam Id => " + examId, MODULE);
				return ServiceUtil.returnError("Invalid Exam Details!");
			}

			if (UtilValidate.isEmpty(partyId)) {
				Debug.logError("Party Id is Invalid from from frontend Party Id => " + partyId, MODULE);
				return ServiceUtil.returnError("Invalid User Details!");
			}

//			List<GenericValue> examQuestions = new ArrayList<>();

			// find all topics for the exam.
			List<GenericValue> examTopics = EntityQuery.use(delegator).from("ExamTopicDetails").where("examId", examId)
					.queryList();

			if (UtilValidate.isEmpty(examTopics)) {
				return ServiceUtil.returnError("No Topics Assigend for this Assessment!");
			}

			GenericValue examRecord = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId)
					.queryFirst();

			if (UtilValidate.isEmpty(examRecord)) {
				return ServiceUtil.returnError("Assessment not found!");
			}

			long totalQuestions = examRecord.getLong("noOfQuestions");

			// Get all questions from questionMaster and put in the QuestionBankMaster
			for (GenericValue examTopic : examTopics) {
				String topicId = examTopic.getString("topicId");
				if (UtilValidate.isEmpty(topicId)) {
					Debug.logError("Topic Id is Invalid from DB topicId => " + topicId + ", Exam Id => " + examId,
							MODULE);
					continue;
				}
				String mandatoryQuestionsIds = examTopic.getString("mandatoryQuestionIds");

				List<String> mandatoryQuestions = Collections.emptyList();

				if (UtilValidate.isNotEmpty(mandatoryQuestionsIds)) {
					mandatoryQuestions = Arrays.asList(mandatoryQuestionsIds.split(","));
				}

				// total percentage of questions.
				Long percentage = examTopic.getLong("percentage");

				// long totalRecords = delegator.findCountByCondition("QuestionMaster",
				// EntityCondition.makeCondition("topicId", EntityOperator.EQUALS, topicId),
				// null, null);

				int requiredQuestionsInTopic = (int) (totalQuestions * percentage) / 100;

				// totalQuestions = totalQuestions + totalQuestionsInTopic;

				// List<GenericValue> topicWiseQuestions =
				// EntityQuery.use(delegator).from("QuestionMaster").where("topicId", topicId)
				// .maxRows(totalQuestionsInTopic).queryList();

				// List<GenericValue> topicWiseQuestions =
				// EntityQuery.use(delegator).from("QuestionMaster").where("topicId", topicId)
				// .maxRows(totalQuestionsInTopic).queryList();

				List<GenericValue> topicWiseQuestions = EntityQuery.use(delegator).from("QuestionMaster")
						.where(EntityCondition.makeCondition("topicId", EntityOperator.EQUALS, topicId),
								EntityCondition.makeCondition("questionId", EntityOperator.NOT_IN, mandatoryQuestions))
						.queryList();

				if (UtilValidate.isEmpty(topicWiseQuestions) || topicWiseQuestions.size() < requiredQuestionsInTopic) {
					return ServiceUtil.returnError("Required Questions Not Found in Question Bank");
				}
				// int totalQuestionsInTopicInDb = topicWiseQuestions.size();

				List<GenericValue> selectedQuestions = new ArrayList<>();

				Collections.shuffle(topicWiseQuestions, new SecureRandom());

				if (topicWiseQuestions.size() < (requiredQuestionsInTopic - mandatoryQuestions.size())) {
					return ServiceUtil.returnError("Required Questions was Not found in Records!");
				}

				selectedQuestions
						.addAll(topicWiseQuestions.subList(0, requiredQuestionsInTopic - mandatoryQuestions.size()));

				topicWiseQuestions = EntityQuery.use(delegator).from("QuestionMaster")
						.where(EntityCondition.makeCondition("topicId", EntityOperator.EQUALS, topicId),
								EntityCondition.makeCondition("questionId", EntityOperator.IN, mandatoryQuestions))
						.queryList();

				selectedQuestions.addAll(topicWiseQuestions);

				// SecureRandom rand = new SecureRandom();
				// // get random number.
				// int randomNumber = 0;
				// Set<Integer> questionIdx = new HashSet<Integer>();
				// // generate unique question index with SET.
				// for (int i = 1; < ; i++) {
				// // randomNumber = rand.nextInt(totalQuestionsInTopic);
				// randomNumber = (int) (Math.random() * totalQuestionsInTopicInDb - 1) + 1; //
				// get random number.
				//
				// // questionIdx.add(rand.nextInt(totalQuestionsInTopicInDb - 1));
				// questionIdx.add(randomNumber);
				// if (questionIdx.size() == totalQuestionsInTopic) {
				// break;
				// }
				// }

				// int totalQuestionsInTopicInDb = topicWiseQuestions.size();

				// add to question bank master;
				for (GenericValue question : selectedQuestions) {
					// GenericValue question = topicWiseQuestions.get(i);
					GenericValue questionBank = delegator.makeValue("QuestionBankMasterB");
					questionBank.set("qId", question.getString("questionId"));
					questionBank.set("examId", examId);
					questionBank.set("partyId", partyId);
					questionBank.set("topicId", question.get("topicId"));
					questionBank.set("questionDetail", question.get("questionDetail"));
					questionBank.set("optionA", question.get("optionA"));
					questionBank.set("optionB", question.get("optionB"));
					questionBank.set("optionC", question.get("optionC"));
					questionBank.set("optionD", question.get("optionD"));
					questionBank.set("optionE", question.get("optionE"));
					questionBank.set("answer", question.get("answer"));
					questionBank.set("numAnswers", (Long) question.get("numAnswers"));
					questionBank.set("questionType", question.get("questionType"));
					questionBank.set("difficultyLevel", question.get("difficultyLevel"));
					questionBank.set("answerValue", question.get("answerValue"));
					questionBank.set("negativeMarkValue", 0);

					delegator.create(questionBank);
				}

//				topicWiseQuestions

				// insert all question in the question Bank Master.
				/*
				 * for (GenericValue question : topicWiseQuestions) { GenericValue questionBank
				 * = delegator.makeValue("QuestionBankMaster"); questionBank.set("qId",
				 * question.getString("questionId")); questionBank.set("examId", examId);
				 * questionBank.set("partyId", partyId); questionBank.set("topicId",
				 * question.get("topicId")); questionBank.set("questionDetail",
				 * question.get("questionDetail")); questionBank.set("optionA",
				 * question.get("optionA")); questionBank.set("optionB",
				 * question.get("optionB")); questionBank.set("optionC",
				 * question.get("optionC")); questionBank.set("optionD",
				 * question.get("optionD")); questionBank.set("optionE",
				 * question.get("optionE")); questionBank.set("answer", question.get("answer"));
				 * questionBank.set("numAnswers", (Long) question.get("numAnswers"));
				 * questionBank.set("questionType", question.get("questionType"));
				 * questionBank.set("difficultyLevel", question.get("difficultyLevel"));
				 * questionBank.set("answerValue", question.get("answerValue"));
				 * questionBank.set("negativeMarkValue", 0);
				 * 
				 * delegator.create(questionBank); }
				 * 
				 * examQuestions.addAll(topicWiseQuestions);
				 */

			}

			Map<String, Object> result = ServiceUtil.returnSuccess("Exam and Questions are Ready!");
			result.put("totalQuestions", totalQuestions);
			return result;

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
		}
	}

	public static Map<String, ? extends Object> setupExam(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			LocalDispatcher dispatcher = dctx.getDispatcher();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String examId = (String) context.get("examId");
			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid Assessment details!");
			}

			GenericValue examRecord = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();

			if (UtilValidate.isEmpty(examRecord)) {
				return ServiceUtil.returnError("Assessment Not Found!");
			}

			examRecord.set("fromDate", Timestamp.valueOf(LocalDateTime.now()));

			delegator.store(examRecord);

			GenericValue examSetupAlready = EntityQuery.use(delegator).from("ExamSetupDetails").where("examId", examId)
					.queryFirst();

			if (UtilValidate.isNotEmpty(examSetupAlready)) {
				return ServiceUtil.returnError("This assessment has already started. You cannot start it again.");
			}

			List<GenericValue> assignedTopics = EntityQuery.use(delegator).from("ExamTopicDetails")
					.where("examId", examId).queryList();

			if (UtilValidate.isEmpty(assignedTopics)) {
				return ServiceUtil.returnError(
						"No topics have been assigned to this assessment. Please assign at least one topic to continue.");
			}

			List<GenericValue> assignedUsersList = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("examId", examId).queryList();

			if (UtilValidate.isEmpty(assignedUsersList)) {
				return ServiceUtil.returnError(
						"No users have been assigned to this assessment. Please assign at least one user to continue.");
			}

			// ExamSetupDetails

			GenericValue examSetup = delegator.makeValue("ExamSetupDetails");

			examSetup.set("examId", examId);
			examSetup.set("setupType", "N/A");
			examSetup.set("setupDetails", "Exam Setup Successfully!");

			delegator.create(examSetup);

			for (GenericValue assignedUser : assignedUsersList) {
				if (UtilValidate.isNotEmpty(assignedUser)) {

					LocalDateTime now = LocalDateTime.now();
					assignedUser.set("fromDate", Timestamp.valueOf(now));
					long timeoutDays = assignedUser.getLong("timeoutDays");
					int timeoutDaysInt = (int) timeoutDays;
					if (timeoutDays < 0) {
						return ServiceUtil.returnError("Invalid Timeout Days");
					}
					assignedUser.set("thruDate",
							UtilDateTime.addDaysToTimestamp(Timestamp.valueOf(now), timeoutDaysInt));

					delegator.store(assignedUser);
				}
			}

			return ServiceUtil.returnSuccess("Assesment Available to Available Users!");

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
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
			GenericValue exam = EntityQuery.use(delegator).from("InProgressParty")
					.where("examId", examId, "isExamActive", 1L).queryFirst();

			if (!UtilValidate.isEmpty(exam)) {
				return ServiceUtil.returnError("Can not delete the exam a user is attending the exam try again later ");
			}

			EntityCondition condition = EntityCondition.makeCondition("examId", EntityOperator.EQUALS, examId);

			delegator.removeByCondition("DetailedPartyPerformance", condition);
			delegator.removeByCondition("PartyPerformance", condition);
			delegator.removeByCondition("AnswerMaster", condition);
			delegator.removeByCondition("ExamSecurityCode", condition);

			delegator.removeByCondition("PartyExamRelationship", condition);
			delegator.removeByCondition("AdminPartyExamRel", condition);
			delegator.removeByCondition("InProgressParty", condition);
			delegator.removeByCondition("ExamSetupDetails", condition);
			delegator.removeByCondition("QuestionBankMaster", condition);

			List<GenericValue> examTopics = delegator.findByAnd("ExamTopicDetails", UtilMisc.toMap("examId", examId),
					null, false);

			for (GenericValue examTopic : examTopics) {
				dispatcher.runSync("deleteExamTopics",
						UtilMisc.toMap("examId", examId, "topicId", examTopic.getString("topicId")));
			}

			dispatcher.runSync("deleteExam", UtilMisc.toMap("examId", examId));

			return ServiceUtil.returnSuccess("Deleted successfully");

		} catch (Exception e) {

			return ServiceUtil.returnError(e.getMessage());
		}

	}

	public static Map<String, Object> generateReport(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			String partyId = (String) context.get("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Required details are missing: partyId");
			}

			List<GenericValue> partyExamRels = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId", partyId).queryList();

			if (UtilValidate.isEmpty(partyExamRels)) {
				return ServiceUtil.returnError("No assessments assigned to this user");
			}

			Set<String> assignedExamIds = new HashSet<>();
			for (GenericValue rel : partyExamRels) {
				assignedExamIds.add(rel.getString("examId"));
			}

			List<GenericValue> allAssignedExams = EntityQuery.use(delegator).from("ExamMaster")
					.where(EntityCondition.makeCondition("examId", EntityOperator.IN, assignedExamIds)).queryList();

			List<GenericValue> performances = EntityQuery.use(delegator).from("PartyPerformance")
					.where("partyId", partyId).orderBy("-attemptNo").queryList();

			Map<String, GenericValue> attendedExamMap = new HashMap<>();
			for (GenericValue perf : performances) {
				String examId = perf.getString("examId");
				if (!attendedExamMap.containsKey(examId)) {
					attendedExamMap.put(examId, perf);
				}
			}

			List<Map<String, Object>> assignedNotAttended = new ArrayList<>();

			List<Map<String, Object>> attendedWithResults = new ArrayList<>();

			for (GenericValue exam : allAssignedExams) {
				String examId = exam.getString("examId");

				Map<String, Object> examInfo = new HashMap<>();
				examInfo.put("examId", examId);
				examInfo.put("examName", exam.getString("examName"));
				examInfo.put("description", exam.getString("description"));
				examInfo.put("noOfQuestions", exam.getLong("noOfQuestions"));
				examInfo.put("duration", exam.getLong("duration"));
				examInfo.put("passPercentage", exam.getLong("passPercentage"));

				if (attendedExamMap.containsKey(examId)) {
					GenericValue perf = attendedExamMap.get(examId);

					examInfo.put("score", perf.getBigDecimal("score"));
					examInfo.put("totalCorrect", perf.getLong("totalCorrect"));
					examInfo.put("totalWrong", perf.getLong("totalWrong"));
					examInfo.put("attemptNo", perf.getLong("attemptNo"));
					examInfo.put("date", perf.getTimestamp("date"));
					examInfo.put("performanceId", perf.getLong("performanceId"));

					Long userPassed = perf.getLong("userPassed");
					examInfo.put("userPassed", userPassed);
					examInfo.put("status", (userPassed != null && userPassed == 1L) ? "PASSED" : "FAILED");

					Long noOfQuestions = perf.getLong("noOfQuestions");
					if (noOfQuestions != null && noOfQuestions > 0) {
						double scoreVal = perf.getBigDecimal("score") != null
								? perf.getBigDecimal("score").doubleValue()
								: 0.0;
						double scorePct = (scoreVal / noOfQuestions) * 100.0;
						examInfo.put("scorePercentage", Math.round(scorePct * 100.0) / 100.0);
					} else {
						examInfo.put("scorePercentage", 0.0);
					}

					attendedWithResults.add(examInfo);

				} else {
					examInfo.put("status", "PENDING");
					assignedNotAttended.add(examInfo);
				}
			}

			Map<String, Object> result = ServiceUtil.returnSuccess("Report generated successfully");
			result.put("partyId", partyId);
			result.put("assignedExams", assignedNotAttended);
			result.put("attendedExams", attendedWithResults);
			result.put("totalAssigned", allAssignedExams.size());
			result.put("totalAttended", attendedWithResults.size());
			result.put("totalPending", assignedNotAttended.size());
			return result;

		} catch (Exception e) {
			Debug.logError(e, "Error in generateReport", MODULE);
			return ServiceUtil.returnError("Error generating report: " + e.getMessage());
		}
	}

	public static Map<String, ? extends Object> getUsersNotAssignedToExam(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			String examId=(String) context.get("examId");
			if(UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Required details are missing ");
			}
			List<GenericValue> examUsers = EntityQuery.use(delegator).from("PartyExamRelationship").where("examId",examId).queryList();
			List<GenericValue> users = EntityQuery
					.use(delegator).from("PartyPersonalInfo").where("partyTypeId", "PERSON", "statusId",
							"PARTY_ENABLED", "roleTypeId", "SphinxUser", "contactMechTypeId", "EMAIL_ADDRESS")
					.queryList();		
			Set<String> examUserPartyIds = new HashSet<>();

			for (GenericValue examUser : examUsers) {
				examUserPartyIds.add(examUser.getString("partyId"));
			}
			Iterator<GenericValue> iterator = users.iterator();
			while (iterator.hasNext()) {
				GenericValue user = iterator.next();
				if (examUserPartyIds.contains(user.getString("partyId"))) {
					iterator.remove();
				}
			}
			Map<String, Object> result = ServiceUtil.returnSuccess("Un Assigned Users List");
			result.put("data", users);
			return result;

		} catch (Exception e) {
			Debug.logError(e, "Error in generateReport", MODULE);
			return ServiceUtil.returnError("Error generating report: " + e.getMessage());
		}
	}

}
