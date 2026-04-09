package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityFunction;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class TopicServices {
	private static final String MODULE = TopicServices.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";


	public static Map<String, ? extends Object> getAllTopic(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			
			Map<String, Object> result = ServiceUtil.returnSuccess();
			
			List<GenericValue> topics = delegator.findAll("TopicMaster", false);
			if (UtilValidate.isEmpty(topics)) {
				return ServiceUtil.returnError("Cannot find the data ");
			}
			result.put("topicList", topics);
			return result;
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	public static Map<String, Object> getTopicById(DispatchContext dctx, Map<String, Object> context) {
		
		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			Map<String, Object> result = ServiceUtil.returnSuccess();
			GenericValue topic = delegator.findOne("TopicMaster", false, Map.of("topicId", context.get("topicId")));
			if (UtilValidate.isEmpty(topic)) {
				return ServiceUtil.returnError("topic is empty");
			}
			result.put("topic", topic);
			return result;
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	public static Map<String, Object> createTopic(DispatchContext dctx, Map<String, Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String topicName = context.get("topicName").toString().toUpperCase();
			GenericValue isPresent = EntityQuery.use(delegator).from("TopicMaster")
					.where(EntityCondition.makeCondition(EntityFunction.upperField("topicName"), EntityOperator.LIKE,
							EntityFunction.upper("%" + topicName + "%")))
					.queryFirst();

			if (UtilValidate.isEmpty(isPresent)) {
				return dctx.getDispatcher().runSync("createTopic",
						UtilMisc.toMap("topicId", topicName, "topicName", topicName));
			} else {
				return ServiceUtil.returnError("Given Topic is Already Present!");
			}

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Unexpected Error Occured! Try Again sometime!");
		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Unexpected Error Occured! Try Again sometime!");
		}

	}

}
