package com.sphinx.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.ApiResponse;

public class ExamTopicServices {
	private static final String MODULE = ExamTopicServices.class.getName();

	public static Map<String, ? extends Object> getAllExamTopics(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();
			List<GenericValue> topics = EntityQuery.use(delegator).from("ExamTopicDetails")
					.where("examId", context.get("examId")).queryList();
			if (topics.isEmpty()) {
				return ServiceUtil.returnError("topics is null");
			}
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("examTopicList", topics);
			return result;
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

	        // 1. Fetch exam
	        GenericValue exam = delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId", examId));
	        if (exam == null) {
	            return ApiResponse.response(false, 404, "Exam not found", null);
	        }

	        // 2. Check if already launched
	        Long setupProper = exam.getLong("examSetupProper");
	        if (setupProper != null && setupProper == 1) {
	            return ApiResponse.response(false, 400, "Exam already launched. Cannot regenerate questions", null);
	        }

	        // 3. Validate question count
	        if (exam.get("noOfQuestions") == null) {
	            return ApiResponse.response(false, 400, "Exam has no question count set", null);
	        }
	        int totalQuestions = exam.getLong("noOfQuestions").intValue();
	        if (totalQuestions <= 0) {
	            return ApiResponse.response(false, 400, "Exam question count must be greater than 0", null);
	        }

	        // 4. Fetch topics for this exam
	        List<GenericValue> topics = delegator.findByAnd("ExamTopicDetails",
	                UtilMisc.toMap("examId", examId), null, false);
	        if (topics == null || topics.isEmpty()) {
	            return ApiResponse.response(false, 400, "No topics found. Add topics before generating questions", null);
	        }

	        // 5. Validate percentages sum to 100
	        int totalPercentage = 0;
	        for (GenericValue topic : topics) {
	            if (topic.get("percentage") == null) {
	                return ApiResponse.response(false, 400,
	                        "Topic " + topic.getString("topicId") + " has no percentage set", null);
	            }
	            totalPercentage += topic.getLong("percentage").intValue();
	        }
	        if (totalPercentage != 100) {
	            return ApiResponse.response(false, 400,
	                    "Topic percentages must add up to 100. Current total: " + totalPercentage, null);
	        }

	        // 6. Clear existing generated questions for this exam
	        delegator.removeByCondition("QuestionBankMasterB",
	                EntityCondition.makeCondition("examId", examId));

	        // 7. Resolve shuffle flag once, outside the loop
	        Long questionsRandomized = exam.getLong("questionsRandomized");
	        boolean shouldShuffle = questionsRandomized != null && questionsRandomized == 1;

	        int totalSaved = 0;

	        // 8. For each topic, fetch questions from QuestionMaster and copy to QuestionBankMasterB
	        for (GenericValue topic : topics) {
	            String topicId = topic.getString("topicId");
	            int percentage = topic.getLong("percentage").intValue();
	            int questionCount = (totalQuestions * percentage) / 100;

	            if (questionCount <= 0) {
	                return ApiResponse.response(false, 400,
	                        "Topic " + topicId + " percentage too low — results in 0 questions", null);
	            }

	            EntityCondition condition = EntityCondition.makeCondition("topicId", EntityOperator.EQUALS, topicId);
	            List<GenericValue> questions = EntityQuery.use(delegator)
	                    .from("QuestionMaster")
	                    .where(condition)
	                    .limit(questionCount)
	                    .queryList();

	            // 9. Validate before shuffle
	            if (questions == null || questions.isEmpty()) {
	                return ApiResponse.response(false, 400, "No questions found for topic: " + topicId, null);
	            }
	            if (questions.size() < questionCount) {
	                return ApiResponse.response(false, 400, "Topic " + topicId + " needs " + questionCount
	                        + " questions but only " + questions.size() + " available", null);
	            }

	            // 10. Shuffle after validation if flag is set
	            if (shouldShuffle) {
	                Collections.shuffle(questions);
	            }

	            // 11. Copy each question into QuestionBankMasterB
	            for (GenericValue q : questions) {
	                String newQid = delegator.getNextSeqId("QuestionBankMasterB");
	                GenericValue draft = delegator.makeValue("QuestionBankMasterB");

	                draft.set("qId", newQid);
	                draft.set("examId", examId);
	                draft.set("topicId", q.get("topicId"));
	                draft.set("questionDetail", q.get("questionDetail"));
	                draft.set("optionA", q.get("optionA"));
	                draft.set("optionB", q.get("optionB"));
	                draft.set("optionC", q.get("optionC"));
	                draft.set("optionD", q.get("optionD"));
	                draft.set("optionE", q.get("optionE"));
	                draft.set("answer", q.get("answer"));
	                draft.set("numAnswers", q.get("numAnswers"));
	                draft.set("questionType", q.get("questionType")); // fixed casing
	                draft.set("difficultyLevel", q.get("difficultyLevel"));
	                draft.set("answerValue", q.get("answerValue"));
	                draft.set("negativeMarkValue", exam.get("negativeMarkValue"));

	                delegator.create(draft);
	                totalSaved++;
	            }
	        }

	        return ApiResponse.response(true, 200,
	                "Questions generated successfully. Total saved: " + totalSaved, null);

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
	
	public static Map<String, ? extends Object> getExamById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator=dctx.getDelegator();
			GenericValue exam=delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId",context.get("examId")));
			if(exam == null || exam.isEmpty()) {
			 return ServiceUtil.returnError("Exam is not available ");
			}
			Map<String,Object>result= ServiceUtil.returnSuccess();
			result.put("exam", exam);
		}catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try later");
		}
		return ServiceUtil.returnError("Exam is not available");
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
