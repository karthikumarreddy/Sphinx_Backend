package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;

import com.sphinx.util.ApiResponse;

public class ExamTopicServices {
	public static Map<String, ? extends Object> getAllExamTopics(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			List<GenericValue> topics = delegator.findAll("ExamTopicDetails", false);
			if (topics.isEmpty()) {
				return ApiResponse.response(false, 400, "No topics available", null);
			}
			return ApiResponse.response(true, 200, "Available topics", topics);
		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try again later", null);
		}
	}

	public static Map<String, Object> getExamTopicById(DispatchContext ctx, Map<String, Object> context) {
		Delegator delegator = ctx.getDelegator();
		String examId = (String) context.get("examId");
		String topicId = (String) context.get("topicId");

		try {
			GenericValue examTopic = EntityQuery.use(delegator).from("ExamTopicDetails")
					.where("examId", examId, "topicId", topicId).queryOne();

			if (examTopic == null) {
				return ApiResponse.response(false, 400, "No topics available", null);
			}

			return ApiResponse.response(true, 200, "topics", examTopic);
		} catch (Exception e) {

			return ApiResponse.response(false, 400, "No topics available", null);
		}

	}

	public static Map<String, ? extends Object> addExamTopics(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			GenericValue examTopicDetails = delegator.makeValue("ExamTopicDetails");
			examTopicDetails.set("examId", context.get("examId"));
			examTopicDetails.set("topicId", context.get("topicId"));
			examTopicDetails.set("topicName", context.get("topicName"));
			examTopicDetails.set("percentage", context.get("percentage"));
			examTopicDetails.set("questionsPerExam", context.get("questionsPerExam"));
			examTopicDetails.set("startingQid", context.get("startingQid"));
			examTopicDetails.set("endingQid", context.get("endingQid"));
			examTopicDetails.set("topicPassPercentage", context.get("topicPassPercentage"));
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
			List<GenericValue> examTopicDetails = delegator.findByAnd("ExamTopicDetails",
					UtilMisc.toMap("examId", context.get("examId"), "topicId", context.get("topicId")), null, false);
			delegator.removeAll(examTopicDetails);
			return ApiResponse.response(true, 200, "Exam deleted sucessfully .", null);
		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong try again later .", null);
		}
	}

	public static Map<String, ? extends Object> generateExamQuestions(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			GenericValue examMaster = delegator.findOne("ExamMaster", false,
					UtilMisc.toMap("examId", context.get("examId")));

			int totalQuestions = Integer.parseInt(context.get("noOfQuestions").toString());

			List<GenericValue> topics = delegator.findByAnd("ExamTopicDetails",
					UtilMisc.toMap("examId", context.get("examId")), null, false);
			if (topics.isEmpty() || topics == null) {
				return ApiResponse.response(false, 400, "Topics not available add topics to the exam ", null);
			} 

			for (GenericValue topic : topics) {
				int percentage = Integer.parseInt(context.get("percentage").toString());

				int questionCount = (totalQuestions * percentage) / 100;
				List<GenericValue> questions = EntityQuery.use(delegator).from("QuestionMaser")
						.where("topicId", context.get("topicId")).limit(questionCount).queryList();

				if (questions.isEmpty() || questions == null) {
					return ApiResponse.response(false, 400, "No Question available", null);
				}

				for (GenericValue value : questions) {
					GenericValue questionBankMasterB = delegator.makeValue("QuestionBankMasterB");
					questionBankMasterB.setNonPKFields(context);
				}
			}
			return ApiResponse.response(true, 200, "Questions added Sucesssfully to the exam", null);

		} catch (Exception e) {
			// TODO: handle exception
			return ApiResponse.response(false, 500,"Something went wrong try again later", null);
		}
	}

}
