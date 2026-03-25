package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.LocalDispatcher;

import com.sphinx.util.ApiResponse;

public class ExamServices {

	public static Map<String, ? extends Object> getExam(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			List<GenericValue> examMaster = delegator.findAll("ExamMaster", false);
			return ApiResponse.response(true, 200, "List of exams", examMaster);
		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try later .", null);
		}

	}

	public static Map<String, ? extends Object> createExam(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			GenericValue examMaster = delegator.makeValue("ExamMaster");
			delegator.getNextSeqId("ExamMaster");
			examMaster.setNonPKFields(context);
			delegator.create(examMaster);
			LocalDispatcher dispatcher = dctx.getDispatcher();
			Map<String, Object> insertTopic = dispatcher.runSync("createExamTopics", context);
			return ApiResponse.response(true, 201, "Exam created sucessfully .", null);

		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try later", null);
		}
	}

	public static Map<String, ? extends Object> updateExam(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			GenericValue examMaster = delegator.findOne("ExamMaster", false,
					UtilMisc.toMap("examId", context.get("examId")));
			examMaster.setNonPKFields(context);
			delegator.store(examMaster);
			LocalDispatcher dispatcher = dctx.getDispatcher();
			Map<String, Object> updateTopic = dispatcher.runSync("updateExamTopics", context);
			return ApiResponse.response(true, 200, "Exam updated sucessfully", null);

		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try later .", null);
		}
	}

	public static Map<String, ? extends Object> deleteExam(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			GenericValue examMaster = delegator.findOne("ExamMasterf", false,
					UtilMisc.toMap("examId", context.get("examId")));
			delegator.removeValue(examMaster);
			LocalDispatcher dispatcher = dctx.getDispatcher();
			Map<String, Object> updateTopic = dispatcher.runSync("", context);
			
			return ApiResponse.response(true, 200, "Exam deleted sucessfully", null);
		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try later", null);
		}
	}
}
