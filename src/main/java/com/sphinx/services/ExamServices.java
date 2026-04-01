package com.sphinx.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
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

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("examId", examId);
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong: " + e.getMessage());
		}
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

	public static Map<String, Object> assignUserToExam(DispatchContext dctx, Map<String, ? extends Object> context) {

		try {
			String partyId = (String) context.get("partyId");
			String examId = (String) context.get("examId");
			long allowedAttempts = (long) context.get("allowedAttempts");
			long noOfAttempts = (long) context.get("noOfAttempts");
			long timeoutDays = (long) context.get("timeoutDays");
			Timestamp fromDate = (Timestamp) context.get("fromDate");
			Timestamp thruDate = (Timestamp) context.get("thruDate");

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Give Exam Details is Invalid!");
			}

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Give User Details is Invalid!");
			}

			if (allowedAttempts <= 0 && allowedAttempts > 5) {
				return ServiceUtil.returnError("Invalid Allowed Attempts! Should in between 0 and 5");
			}

			if (noOfAttempts < 0 && noOfAttempts > allowedAttempts) {
				return ServiceUtil.returnError("Invalid Number of Attempts!");
			}

			if (timeoutDays < 0 && timeoutDays > 5) {
				return ServiceUtil.returnError("Invalid Timeout Days! Should in between 0 and 5");
			}

			if (fromDate.before(Timestamp.valueOf(LocalDateTime.now()))) {
				return ServiceUtil.returnError("Invalid From Date!");
			}

			if (thruDate.before(Timestamp.valueOf(LocalDateTime.now()))) {
				return ServiceUtil.returnError("Invalid Thru Date!");
			}


			LocalDispatcher dispatcher = dctx.getDispatcher();

			if (dispatcher == null) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			
			return dispatcher.runSync("createPartyExamRelationship", context);


		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input values!");
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}


	}

	// public static Map<String, ? extends Object> updateExam(DispatchContext dctx,
	// Map<String, ? extends Object> context) {
	// try {
	// Delegator delegator = dctx.getDelegator();
	// GenericValue examMaster = delegator.findOne("ExamMaster", false,
	// UtilMisc.toMap("examId", context.get("examId")));
	// examMaster.setNonPKFields(context);
	// delegator.store(examMaster);
	// return ApiResponse.response(true, 200, "Exam updated sucessfully", null);
	//
	// } catch (Exception e) {
	// return ApiResponse.response(false, 500, "Something went wrong try later .",
	// null);
	// }
	// }
	//
	// public static Map<String, ? extends Object> deleteExam(DispatchContext dctx,
	// Map<String, ? extends Object> context) {
	// try {
	// Delegator delegator = dctx.getDelegator();
	// GenericValue examMaster = delegator.findOne("ExamMasterf", false,
	// UtilMisc.toMap("examId", context.get("examId")));
	// delegator.removeValue(examMaster);
	//
	// return ApiResponse.response(true, 200, "Exam deleted sucessfully", null);
	// } catch (Exception e) {
	// return ApiResponse.response(false, 500, "Something went wrong try later",
	// null);
	// }
	// }
}
