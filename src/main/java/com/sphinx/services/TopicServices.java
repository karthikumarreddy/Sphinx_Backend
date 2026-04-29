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
			String partyId=(String) context.get("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid details can not fetch the count");
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();
			

			List<GenericValue> topics = EntityQuery.use(delegator).from("TopicMaster").where("partyId", partyId).select("topicId")
							.queryList();
			// if (UtilValidate.isEmpty(topics)) {
			// return ServiceUtil.returnError("Cannot find the data ");
			// }
			result.put("topicList", topics);
			return result;
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	public static Map<String, ? extends Object> getAllTopicsCount(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();
			String partyId = (String) context.get("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid details can not fetch the count");
			}

			List<GenericValue> topics = EntityQuery.use(delegator).from("TopicMaster").where("partyId", partyId)
					.queryList();
			if (UtilValidate.isEmpty(topics)) {
				return ServiceUtil.returnError("Cannot find the data ");
			}
			result.put("count", topics.size());
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
			String topicId = (String) context.get("topicId");
			if (UtilValidate.isEmpty(topicId)) {
				return ServiceUtil.returnError("Topic Id is required");
			}

			GenericValue topic = delegator.findOne("TopicMaster", false, Map.of("topicId", topicId));
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
			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(topicName)) {
				return ServiceUtil.returnError("Topic details are required ");
			}
			GenericValue isPresent = EntityQuery.use(delegator).from("TopicMaster")
					.where(EntityCondition
							.makeCondition(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
									EntityOperator.AND,
									EntityCondition.makeCondition(EntityFunction.upperField("topicName"),
											EntityOperator.LIKE, EntityFunction.upper("%" + topicName + "%"))))
					.queryFirst();

			if (UtilValidate.isEmpty(isPresent)) {
				return dctx.getDispatcher().runSync("createTopic",
						UtilMisc.toMap("topicId", topicName, "topicName", topicName,"partyId",partyId));
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

	public static Map<String, ? extends Object> updateTopicWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			LocalDispatcher dispatcher = dctx.getDispatcher();
			if (UtilValidate.isEmpty(dispatcher)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String partyId = (String) context.get("partyId");
			String topicId = (String) context.get("topicId");
			String topicName = (String) context.get("topicName");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Requiresd Details are invalid ");
			}

			if (UtilValidate.isEmpty(topicId) || UtilValidate.isEmpty(topicName)) {
				return ServiceUtil.returnError("Topic Details are invalid ");
			}
			if (UtilValidate.isEmpty(topicName)) {
				return ServiceUtil.returnError("Topic Details are invalid ");
			}
			Map<String, Object> result = dispatcher.runSync("updateTopic", context);
			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	public static Map<String, ? extends Object> deleteTopicWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			LocalDispatcher dispatcher = dctx.getDispatcher();
			if (UtilValidate.isEmpty(dispatcher)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String topicId = (String) context.get("topicId");
			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(topicId)) {
				return ServiceUtil.returnError("Topic Details are invalid ");
			}

			Map<String, Object> result = dispatcher.runSync("deleteTopic", context);
			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

}
