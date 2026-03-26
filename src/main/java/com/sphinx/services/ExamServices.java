package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.ApiResponse;

public class ExamServices {

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
			return ApiResponse.response(false, 500, "Something went wrong try later .", null);
		}

	}

	public static Map<String, Object> createExamWrapper(DispatchContext dctx, Map<String, Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			// Debug - log what's actually in context
			System.out.println("=== createExamWrapper context keys: " + context.keySet());
			System.out.println("=== examName: " + context.get("examName"));
			System.out.println("=== noOfQuestions: " + context.get("noOfQuestions"));

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

			return ServiceUtil.returnSuccess("Exam created successfully with ID: " + examId);

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong: " + e.getMessage());
		}
	}
//	public static Map<String, ? extends Object> createExam(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			GenericValue examMaster = delegator.makeValue("ExamMaster");
//			delegator.getNextSeqId("ExamMaster");
//			examMaster.setNonPKFields(context);
//			delegator.create(examMaster);
//			return ApiResponse.response(true, 201, "Exam created sucessfully .", null);
//
//		} catch (Exception e) {
//			return ApiResponse.response(false, 500, "Something went wrong try later", null);
//		}
//	}
//
//	public static Map<String, ? extends Object> updateExam(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			GenericValue examMaster = delegator.findOne("ExamMaster", false,
//					UtilMisc.toMap("examId", context.get("examId")));
//			examMaster.setNonPKFields(context);
//			delegator.store(examMaster);
//			return ApiResponse.response(true, 200, "Exam updated sucessfully", null);
//
//		} catch (Exception e) {
//			return ApiResponse.response(false, 500, "Something went wrong try later .", null);
//		}
//	}
//
//	public static Map<String, ? extends Object> deleteExam(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			GenericValue examMaster = delegator.findOne("ExamMasterf", false,
//					UtilMisc.toMap("examId", context.get("examId")));
//			delegator.removeValue(examMaster);
//			
//			return ApiResponse.response(true, 200, "Exam deleted sucessfully", null);
//		} catch (Exception e) {
//			return ApiResponse.response(false, 500, "Something went wrong try later", null);
//		}
//	}
}
