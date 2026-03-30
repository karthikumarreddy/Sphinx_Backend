package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityFunction;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.ServiceUtil;

public class TopicServices {
	private static final String MODULE = TopicServices.class.getName();

	public static Map<String, ? extends Object> getAllTopic(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Map<String, Object> result = ServiceUtil.returnSuccess();
			Delegator delegator = dctx.getDelegator();
			List<GenericValue> topics = delegator.findAll("TopicMaster", false);
			if (topics.isEmpty()) {
				return ServiceUtil.returnError("Cannot find the data ");
			}
			result.put("topicList", topics);
			return result;
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());		}
	}

	public static Map<String, Object> getTopicById(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		try {
			Map<String, Object> result = ServiceUtil.returnSuccess();
			GenericValue topic = delegator.findOne("TopicMaster", false, Map.of("topicId", context.get("topicId")));
			if (topic == null) {
				return ServiceUtil.returnError("topic is empty");
			}
			result.put("topic", topic);
			return result;
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());		}
	}


	public static Map<String, Object> createTopic(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		try {

			String topicName = (String) context.get("topicName");
			GenericValue isPresent = EntityQuery.use(delegator).from("TopicMaster")
							.where(EntityCondition.makeCondition(EntityFunction.upperField("topicName"), EntityOperator.LIKE,
											EntityFunction.upper("%" + topicName + "%")))
							.queryFirst();
			

			if (isPresent == null) {
				return dctx.getDispatcher().runSync("createTopic", UtilMisc.toMap("topicName", (String) context.get("topicName")));
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
