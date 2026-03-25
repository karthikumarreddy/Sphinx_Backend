package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
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

	public static Map<String, ? extends Object> generateExamQuestions(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			String examId = (String) context.get("examId");

			if (examId == null || examId.trim().isEmpty()) {
				return ApiResponse.response(false, 400, "examId is required", null);
			}

			GenericValue exam = delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId", examId));
			if (exam == null) {
				return ApiResponse.response(false, 404, "Exam not found", null);
			}

			Object setupProper = exam.get("examSetupProper");
			if (setupProper != null && ((Long) setupProper).intValue() == 1) {
				return ApiResponse.response(false, 400, "Exam already launched. Cannot regenerate questions", null);
			}

			if (exam.get("noOfQuestions") == null) {
				return ApiResponse.response(false, 400, "Exam has no question count set", null);
			}
			int totalQuestions = ((Long) exam.get("noOfQuestions")).intValue();
			if (totalQuestions <= 0) {
				return ApiResponse.response(false, 400, "Exam question count must be greater than 0", null);
			}

			List<GenericValue> topics = delegator.findByAnd("ExamTopicDetails", UtilMisc.toMap("examId", examId), null,
					false);
			if (topics == null || topics.isEmpty()) {
				return ApiResponse.response(false, 400, "No topics found. Add topics before generating questions",
						null);
			}

			int totalPercentage = 0;
			for (GenericValue topic : topics) {
				if (topic.get("percentage") == null) {
					return ApiResponse.response(false, 400,
							"Topic " + topic.getString("topicId") + " has no percentage set", null);
				}
				totalPercentage += ((Long) topic.get("percentage")).intValue();
			}
			if (totalPercentage != 100) {
				return ApiResponse.response(false, 400,
						"Topic percentages must add up to 100. Current total: " + totalPercentage, null);
			}

			delegator.removeByCondition("QuestionBankMasterB", EntityCondition.makeCondition("examId", examId));

			int totalSaved = 0;

			for (GenericValue topic : topics) {
				String topicId = topic.getString("topicId");
				int percentage = ((Long) topic.get("percentage")).intValue();
				int questionCount = (totalQuestions * percentage) / 100;

				if (questionCount <= 0) {
					return ApiResponse.response(false, 400,
							"Topic " + topicId + " percentage too low — results in 0 questions", null);
				}
				Long startingQid = (Long) topic.get("startingQid");
				Long endingQid = (Long) topic.get("endingQid");

				EntityCondition condition = EntityCondition.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("topicId", EntityOperator.EQUALS, topicId),
						EntityCondition.makeCondition("questionId", EntityOperator.GREATER_THAN_EQUAL_TO, startingQid),
						EntityCondition.makeCondition("questionId", EntityOperator.LESS_THAN_EQUAL_TO, endingQid)),
						EntityOperator.AND);

				List<GenericValue> questions = EntityQuery.use(delegator).from("QuestionMaster").where(condition)
						.orderBy("RANDOM()").limit(questionCount).queryList();

				if (questions == null || questions.isEmpty()) {
					return ApiResponse.response(false, 400, "No questions found for topic: " + topicId, null);
				}
				if (questions.size() < questionCount) {
					return ApiResponse.response(false, 400, "Topic " + topicId + " needs " + questionCount
							+ " questions but only " + questions.size() + " available", null);
				}

				for (GenericValue q : questions) {

					Long newQid = Long.parseLong(delegator.getNextSeqId("QuestionBankMasterB"));

					GenericValue draft = delegator.makeValue("QuestionBankMasterB");
					draft.set("examId", examId);
					draft.set("qId", newQid);
					draft.set("topicId", q.get("topicId"));
					draft.set("questionDetail", q.get("questionDetail"));
					draft.set("optionA", q.get("optionA"));
					draft.set("optionB", q.get("optionB"));
					draft.set("optionC", q.get("optionC"));
					draft.set("optionD", q.get("optionD"));
					draft.set("optionE", q.get("optionE"));
					draft.set("answer", q.get("answer"));
					draft.set("numAnswers", q.get("numAnswers"));
					draft.set("questiontype", q.get("questionType"));
					draft.set("difficultyLevel", q.get("difficultyLevel"));
					draft.set("answerValue", q.get("answerValue"));
					draft.set("negativeMarkValue", exam.get("negativeMarkValue"));

					delegator.create(draft);
					totalSaved++;
				}
			}

			return ApiResponse.response(true, 200, "Questions generated successfully. Total saved: " + totalSaved,
					null);

		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong: " + e.getMessage(), null);
		}
	}

	public static Map<String, ? extends Object> launchExam(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			String examId = (String) context.get("examId");

			if (examId == null || examId.trim().isEmpty()) {
				return ApiResponse.response(false, 400, "examId is required", null);
			}

			GenericValue exam = delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId", examId));
			if (exam == null) {
				return ApiResponse.response(false, 404, "Exam not found", null);
			}

			Object setupProper = exam.get("examSetupProper");
			if (setupProper != null && ((Long) setupProper).intValue() == 1) {
				return ApiResponse.response(false, 400, "Exam is already launched", null);
			}

			List<GenericValue> draftQuestions = delegator.findByAnd("QuestionBankMasterB",
					UtilMisc.toMap("examId", examId), null, false);
			if (draftQuestions == null || draftQuestions.isEmpty()) {
				return ApiResponse.response(false, 400, "No draft questions found. Run generateExamQuestions first",
						null);
			}

			int expectedTotal = ((Long) exam.get("noOfQuestions")).intValue();
			if (draftQuestions.size() != expectedTotal) {
				return ApiResponse.response(false, 400, "Draft has " + draftQuestions.size()
						+ " questions but exam expects " + expectedTotal + ". Please regenerate questions", null);
			}

			delegator.removeByCondition("QuestionBankMaster", EntityCondition.makeCondition("examId", examId));

			for (GenericValue draft : draftQuestions) {
				GenericValue finalQ = delegator.makeValue("QuestionBankMaster");

				finalQ.set("examId", draft.get("examId"));
				finalQ.set("qId", draft.get("qId"));
				finalQ.set("topicId", draft.get("topicId"));
				finalQ.set("questionDetail", draft.get("questionDetail"));
				finalQ.set("optionA", draft.get("optionA"));
				finalQ.set("optionB", draft.get("optionB"));
				finalQ.set("optionC", draft.get("optionC"));
				finalQ.set("optionD", draft.get("optionD"));
				finalQ.set("optionE", draft.get("optionE"));
				finalQ.set("answer", draft.get("answer"));
				finalQ.set("numAnswers", draft.get("numAnswers"));
				finalQ.set("questionType", draft.get("questiontype"));
				finalQ.set("difficultyLevel", draft.get("difficultyLevel"));
				finalQ.set("answerValue", draft.get("answerValue"));
				finalQ.set("negativeMarkValue", draft.get("negativeMarkValue"));

				delegator.create(finalQ);
			}

			exam.set("examSetupProper", 1);
			delegator.store(exam);

			return ApiResponse.response(true, 200,
					"Exam launched successfully. " + draftQuestions.size() + " questions ready", null);

		} catch (Exception e) {
			return ApiResponse.response(false, 500, "Something went wrong: " + e.getMessage(), null);
		}
	}

//	public static Map<String, ? extends Object> addExamTopics(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			GenericValue examTopicDetails = delegator.makeValue("ExamTopicDetails");
//			examTopicDetails.set("examId", context.get("examId"));
//			examTopicDetails.set("topicId", context.get("topicId"));
//			examTopicDetails.set("topicName", context.get("topicName"));
//			examTopicDetails.set("percentage", context.get("percentage"));
//			examTopicDetails.set("questionsPerExam", context.get("questionsPerExam"));
//			examTopicDetails.set("startingQid", context.get("startingQid"));
//			examTopicDetails.set("endingQid", context.get("endingQid"));
//			examTopicDetails.set("topicPassPercentage", context.get("topicPassPercentage"));
//			delegator.create(examTopicDetails);
//			return ApiResponse.response(true, 200, "Exam created sucessfully", null);
//		} catch (Exception e) {
//			return ApiResponse.response(false, 500, "Something went wrong try later.", null);
//		}
//	}
//
//	public static Map<String, ? extends Object> updateExamTopics(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			GenericValue examTopicDetails = delegator.findOne("ExamTopicDetails", false,
//					UtilMisc.toMap("examId", context.get("examId")));
//			examTopicDetails.setNonPKFields(context);
//			delegator.store(examTopicDetails);
//			return ApiResponse.response(true, 200, "Exam updated sucessfully", null);
//		} catch (Exception e) {
//			return ApiResponse.response(false, 500, "Something went wrong try later.", null);
//		}
//	}

//	public static Map<String, ? extends Object> deleteExamTopics(DispatchContext dctx,
//			Map<String, ? extends Object> context) {
//		try {
//			Delegator delegator = dctx.getDelegator();
//			List<GenericValue> examTopicDetails = delegator.findByAnd("ExamTopicDetails",
//					UtilMisc.toMap("examId", context.get("examId"), "topicId", context.get("topicId")), null, false);
//			delegator.removeAll(examTopicDetails);
//			return ApiResponse.response(true, 200, "Exam deleted sucessfully .", null);
//		} catch (Exception e) {
//			return ApiResponse.response(false, 500, "Something went wrong try again later .", null);
//		}
//	}

}
