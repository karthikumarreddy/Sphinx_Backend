package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;

import com.sphinx.util.ApiResponse;

public class ExamTopicServices {

	public static Map<String, ? extends Object> createExamTopics(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			GenericValue examTopicDetails = delegator.makeValue("ExamTopicDetails");
			examTopicDetails.setNonPKFields(context);
			delegator.create(examTopicDetails);
			return ApiResponse.response(true, 200, "Exam created sucessfully", null);
		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try later.", null);
		}
	}

	public static Map<String, ? extends Object> updateExamTopics(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			GenericValue examTopicDetails = delegator.findOne("ExamTopicDetails", false,
					UtilMisc.toMap("examId", context.get("examId")));
			examTopicDetails.setNonPKFields(context);
			delegator.store(examTopicDetails);
			return ApiResponse.response(true, 200, "Exam updated sucessfully", null);
		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try later.", null);
		}
	}

	public static Map<String, ? extends Object> deleteExamTopics(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			GenericValue examTopicDetails = delegator.findOne("ExamTopicDetails", false,
					UtilMisc.toMap("examId", context.get("examId")));
			delegator.removeValue(examTopicDetails);
			return ApiResponse.response(true, 200, "Exam topic removed sucessfully", null);
		} catch (Exception e) {
			return ApiResponse.response(false, 500, "something went rong try later .", null);
		}
	}

	public static Map<String, ? extends Object> getExamTop(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			List<GenericValue> topics = delegator.findAll("TopicMaster", false);
			if (topics.isEmpty()) {
				return ApiResponse.response(false, 400, "No topics available", null);
			}
			return ApiResponse.response(true, 200, "Available topics", topics);
		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try again later", null);
		}
	}

}
