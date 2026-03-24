package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;

import com.sphinx.util.ApiResponse;

public class QuestionService {

	private static final String MODULE = QuestionService.class.getName();

	// QUESTION SERVICE

	public Map<String, ? extends Object> createQuestion(DispatchContext dctx, Map<String, ? extends Object> context) {

		try {
			Delegator delegator = dctx.getDelegator();

			GenericValue questionRecord = delegator.makeValue("QuestionMaster");

			questionRecord.setNextSeqId();

			String topicId = (String) context.get("topicId");

			GenericValue topic = delegator.findOne("TopicMaster", true, UtilMisc.toMap("topicId", topicId));

			if (topic == null) {
				return ApiResponse.response(false, 400, "Question Topic was not a valid one!", null);
			}

			questionRecord.setNonPKFields(context);

			questionRecord.create();

			return ApiResponse.response(true, 201, "Question addedd successfully!", null);

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ApiResponse.response(true, 500, "Unexpected error occured", null);
		}
	}

	public Map<String, ? extends Object> createBulkQuestions(DispatchContext dctx, Map<String, ? extends Object> context) {

		try {

			Delegator delegator = dctx.getDelegator();

			List<Map<String, ? extends Object>> questions = (List<Map<String, ? extends Object>>) context.get("listOfQuestions");

			for (Map<String, ? extends Object> question : questions) {

				GenericValue questionRecord = delegator.makeValue("QuestionMaster");

				questionRecord.setNextSeqId();

				questionRecord.setNonPKFields(question);

				questionRecord.create();

			}

			return ApiResponse.response(true, 201, "Questions addedd successfully!", null);

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ApiResponse.response(true, 500, "Unexpected error occured", null);
		}

	}

}
