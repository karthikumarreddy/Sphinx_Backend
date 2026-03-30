package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.ApiResponse;

public class QuestionService {

	private static final String MODULE = QuestionService.class.getName();

	// QUESTION SERVICE

	public Map<String, ? extends Object> getAllQuestionByTopic(DispatchContext dctx, Map<String, ? extends Object> context) {

		Delegator delegator = dctx.getDelegator();

		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {

			String topicId = (String) context.get("topicId");
			if(topicId == null) {
				return ServiceUtil.returnError("Invalid topic id.");
			}
			
			List<GenericValue> questionsByCategory = EntityQuery.use(delegator).from("QuestionMaster")
							.where("topicId", topicId).queryList();
			result.put("questions", questionsByCategory);
			return result;

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}


	}

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

	public Map<String, ? extends Object> getAllQuestionTypes(DispatchContext dctx, Map<String, ? extends Object> context) {

		Delegator delegator = dctx.getDelegator();
		try {
			Map<String, Object> result = ServiceUtil.returnSuccess();
			List<GenericValue> questionTypes = EntityQuery.use(delegator).from("Enumeration")
							.where(EntityCondition.makeCondition("enumTypeId", EntityOperator.LIKE, "%SPHINX_Q_TYPE%")).queryList();

			result.put("data", questionTypes);
			return result;
		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

}
