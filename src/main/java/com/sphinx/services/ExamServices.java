package com.sphinx.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamServices {
	private static final String MODULE = ExamServices.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";

	public static Map<String, ? extends Object> getExam(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Map<String, Object> result = ServiceUtil.returnSuccess();
			Delegator delegator = dctx.getDelegator();
			List<GenericValue> examList = delegator.findAll("ExamMaster", false);
			if (examList == null || examList.isEmpty()) {
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

			String examId = delegator.getNextSeqId("ExamMaster");
			String partyId=(String)context.get("partyId");

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
			if (userLogin != null) {
				examMaster.set("createdByUserLogin", userLogin.getString("userLoginId"));
				examMaster.set("lastModifiedByUserLogin", userLogin.getString("userLoginId"));
			}

			delegator.create(examMaster);
			
			GenericValue adminPartRel=delegator.makeValue("AdminPartyExamRel");
			adminPartRel.set("examId",examId );
			adminPartRel.set("partyId", partyId);
			delegator.create(adminPartRel);

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("examId", examId);
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

			GenericValue examMaster = delegator.findOne("ExamMaster", false,
					UtilMisc.toMap("examId", context.get("examId")));

			if (examMaster == null) {
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
			if (userLogin != null) {
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

			LocalDispatcher dispatcher = dctx.getDispatcher();

			if (dispatcher == null) {
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

				GenericValue party = EntityQuery.use(dctx.getDelegator()).from("Party").where("partyId", partyId).queryFirst();

				if (UtilValidate.isEmpty(examId)) {
					return ServiceUtil.returnError("Give Exam Details is Invalid!");
				}

				if (UtilValidate.isEmpty(partyId)) {
					return ServiceUtil.returnError("Give User Details is Invalid!");
				}

				if (party == null) {
					return ServiceUtil.returnError("Invalid User Details! Record Not Found!");
				}

				if (firstTime) {
					GenericValue exam = EntityQuery.use(dctx.getDelegator()).from("ExamMaster").where("examId", examId).queryFirst();

					if (exam == null) {
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


	public static Map<String, Object> getAllAssignedUsersForExam(DispatchContext dctx, Map<String, ? extends Object> context) {


		try {
			String examId = (String) context.get("examId");

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid Exam Id!");
			}

			Delegator delegator = dctx.getDelegator();

			if (delegator == null) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			List<GenericValue> assignedUsers = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("examId", examId).queryList();

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
			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User!");
			}

			Delegator delegator = dctx.getDelegator();

			if (delegator == null) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			List<GenericValue> assignedExams = EntityQuery.use(delegator).from("PartyExamRelationship")
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

			Delegator delegator = dctx.getDelegator();
			LocalDispatcher dispatcher = dctx.getDispatcher();

			if (delegator == null || dispatcher == null) {
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

			return result;

		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input values!");
		} catch (GenericEntityException | GenericServiceException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	public static Map<String, ? extends Object> getUserAssignedExams(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			String partyId = (String) context.get("partyId");
			Map<String, Object> result = ServiceUtil.returnSuccess();

			List<GenericValue> assignedExams = delegator.findByAnd("PartyExamRelationship",
					UtilMisc.toMap("partyId", partyId), null, false);

			List<Map<String, Object>> examList = new ArrayList<>();

			for (GenericValue assigned : assignedExams) {
				String examId = assigned.getString("examId");

				// Problem 1 fix — queryFirst() returns one GenericValue, not a List
				GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryFirst();

				// Problem 2 fix — skip instead of error
				if (exam == null)
					continue;

				// Problem 3 fix — merge both entities into one flat map
				Map<String, Object> examMap = new HashMap<>();

				examMap.put("examId", examId);
				examMap.put("examName", exam.getString("examName"));
				examMap.put("description", exam.getString("description"));
				examMap.put("duration", exam.getLong("duration"));
				examMap.put("noOfQuestions", exam.getLong("noOfQuestions"));
				examMap.put("passPercentage", exam.getBigDecimal("passPercentage"));

				examMap.put("allowedAttempts", assigned.getLong("allowedAttempts"));
				examMap.put("noOfAttempts", assigned.getLong("noOfAttempts"));
				examMap.put("fromDate", assigned.getTimestamp("fromDate"));
				examMap.put("thruDate", assigned.getTimestamp("thruDate"));
				examMap.put("canSplitExams", assigned.getLong("canSplitExams"));
				examMap.put("canSeeDetailedResults", assigned.getLong("canSeeDetailedResults"));
				examMap.put("lastPerformanceDate", assigned.getTimestamp("lastPerformanceDate"));

				GenericValue inProgress = EntityQuery.use(delegator).from("InProgressParty")
						.where("partyId", partyId, "examId", examId).queryFirst();

				long noOfAttempts = assigned.getLong("noOfAttempts") != null ? assigned.getLong("noOfAttempts") : 0L;
				long allowedAttempts = assigned.getLong("allowedAttempts") != null ? assigned.getLong("allowedAttempts")
						: 1L;
				Timestamp today = UtilDateTime.nowTimestamp();
				Timestamp thruDate = assigned.getTimestamp("thruDate");

				String examStatus;
				if (inProgress != null && Long.valueOf(1L).equals(inProgress.getLong("isExamActive"))) {
					examStatus = "IN_PROGRESS";
				} else if (thruDate != null && today.after(thruDate)) {
					examStatus = "EXPIRED";
				} else if (noOfAttempts >= allowedAttempts) {
					examStatus = "ATTEMPTS_EXHAUSTED";
				} else {
					examStatus = "AVAILABLE";
				}

				examMap.put("examStatus", examStatus);
				examList.add(examMap);
			}

			result.put("examList", examList);
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong, please try later.");
		}
	}
	
	public static Map<String,? extends Object> adminExamList(DispatchContext dctx,Map<String,? extends Object> context){
		try {
			Delegator delegator=dctx.getDelegator();
			String partyId=(String)context.get("partyId");
			
			List<GenericValue> adminExams=EntityQuery.use(delegator).from("AdminPartyExamRel").where("partyId",partyId).queryList();
			Map<String,Object> result=ServiceUtil.returnSuccess();
			
			List<Object> exams=new ArrayList<Object>();
			for(GenericValue list:adminExams) {
				GenericValue data=delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId",list.get("examId")));
				exams.add(data);
			}
			result.put("data", exams);
			return result;
			
			
		}catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try later");
		}
	}


}
