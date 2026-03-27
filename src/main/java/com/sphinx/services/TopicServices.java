package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.ApiResponse;

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

	//
//	public static Map<String, ? extends Object> createTopic(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			String topicId = delegator.getNextSeqId("TopicMaster");
//			GenericValue topicMaster = delegator.makeValue("TopicMaster");
//			topicMaster.set("topicId", topicId);
//			topicMaster.set("topicName", context.get("topicName"));
//			delegator.create(topicMaster);
//			return ApiResponse.response(true, 200, "Topic created sucessfully", null);
//		} catch (Exception e) {
//			return ApiResponse.response(false, 500, "Something went wrong try again later", null);
//		}
//	}
//
//	public static Map<String, ? extends Object> updateTopic(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			GenericValue topicMaster = delegator.findOne("TopicMaster", false,
//					UtilMisc.toMap("topicId", context.get("topicId")));
//			topicMaster.set("topicName", context.get("topicName"));
//			delegator.store(topicMaster);
//			return ApiResponse.response(true, 200, "Topic name updated sucessfully .", null);
//		} catch (Exception e) {
//			// TODO: handle exception
//			return ApiResponse.response(false, 500, "Something went wrong try again later .", null);
//		}
//
//	}
//
//	public static Map<String, ? extends Object> deleteTopic(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			GenericValue topicMaster = delegator.findOne("TopicMaster", false,
//					UtilMisc.toMap("topicId", context.get("topicId")));
//			if (topicMaster == null) {
//				return ApiResponse.response(false, 400, "Topic not found", null);
//			}
//			delegator.removeValue(topicMaster);
//			return ApiResponse.response(true, 200, "Topic deleted", null);
//		} catch (Exception e) {
//			return ApiResponse.response(false, 500, "Something went wrong try later", null);
//
//		}
//	}
//

}
