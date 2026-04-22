package com.sphinx.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import com.ctc.wstx.shaded.msv.org_isorelax.dispatcher.Dispatcher;

public class ExamTopicServices {
	private static final String MODULE = ExamTopicServices.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";

	public static Map<String,? extends Object> addExamTopicsWrapper(DispatchContext dctx,Map<String,? extends Object> context){
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String examId = (String) context.get("examId");
			String topicId = (String) context.get("topicId");
			String topicName = (String) context.get("topicName");
			String percentage = (String) context.get("percentage");
			String topicPassPercentage = (String) context.get("topicPassPercentage");

			if (UtilValidate.isEmail(examId)) {
				return ServiceUtil.returnError("Exam id is empty ");
			}
			if (UtilValidate.isEmpty(topicId)) {
				return ServiceUtil.returnError("TopicId is Required");
			}
			if (UtilValidate.isEmpty(topicName)) {
				return ServiceUtil.returnError("Topic name is required ");
			}
			if (UtilValidate.isEmpty(percentage)) {
				return ServiceUtil.returnError("Question percentage is required ");
			}
			if (UtilValidate.isEmpty(topicPassPercentage)) {
				return ServiceUtil.returnError("Topic pass percentage is required ");
			}
			GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).select("noOfQuestions").queryFirst();
			long totalQuestions = exam.getLong("noOfQuestions");
			int totalQuestionsInTopic = (int) (totalQuestions * Integer.valueOf(percentage)) / 100;
			long questionCount = EntityQuery.use(delegator).from("QuestionMaster").where("topicId", topicId).maxRows(totalQuestionsInTopic)
							.queryCount();
			if (totalQuestionsInTopic > questionCount) {
				return ServiceUtil.returnError((totalQuestionsInTopic - questionCount)+ " question needed for the Topic to add in Assessment! Please Add Questions to the Topic!");
			}
			GenericValue topic=delegator.findOne("ExamTopicDetails", true, UtilMisc.toMap("topicId",topicName));
			if(UtilValidate.isNotEmpty(topic)) {
				int passper=Integer.parseInt(topicPassPercentage)+Integer.parseInt(topic.getString(topicPassPercentage));
				topic.set(topicPassPercentage,passper);
				delegator.store(topic);
				
				return ServiceUtil.returnSuccess("Topic Pass Percentage Updated");
			}

			Map<String, Object> input = new HashMap<String, Object>();
			input.put("examId", examId);
			input.put("topicId", topicId);
			input.put("topicName", topicName);
			input.put("percentage", percentage);
			input.put("topicPassPercentage", topicPassPercentage);
			
			LocalDispatcher dispatcher= dctx.getDispatcher();
			Map<String,Object> result=dispatcher.runSync("updateExamTopics", context);
			if(ServiceUtil.isError(result)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}
			return result;
			
			
		}catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try again later");		}
	}

	public static Map<String, ? extends Object> getAllExamTopics(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			List<GenericValue> topics = EntityQuery.use(delegator).from("ExamTopicDetails")
					.where("examId", context.get("examId")).queryList();
			if (UtilValidate.isEmpty(topics)) {
				return ServiceUtil.returnError("topics is null");
			}
			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("examTopicList", topics);
			return result;
		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try again later");
		}
	}

	public static Map<String, Object> getExamTopicsByExamId(DispatchContext dctx, Map<String, Object> context) {

		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String examId = (String) context.get("examId");

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("ExamId is required");
			}

			List<GenericValue> topics = EntityQuery.use(delegator).from("ExamTopicDetails").where("examId", examId)
					.queryList();

			List<Map<String, Object>> resultList = new ArrayList<>();

			for (GenericValue gv : topics) {
				resultList.add(gv.getAllFields());
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("examTopicList", resultList);
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong: " + e.getMessage());
		}
	}

	public static Map<String, ? extends Object> generateExamQuestions(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String examId = (String) context.get("examId");

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("examId is required");
			}

			GenericValue exam = delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId", examId));
			if (UtilValidate.isEmpty(exam)) {
				return ServiceUtil.returnError("Exam not found");
			}

			Long setupProper = exam.getLong("examSetupProper");
			if (setupProper != null && setupProper == 1) {
				return ServiceUtil.returnError("Exam already launched. Cannot regenerate questions");
			}

			if (exam.get("noOfQuestions") == null) {
				return ServiceUtil.returnError("Exam has no question count set");
			}
			int totalQuestions = exam.getLong("noOfQuestions").intValue();
			if (totalQuestions <= 0) {
				return ServiceUtil.returnError("Exam question count must be greater than 0");
			}

			List<GenericValue> topics = delegator.findByAnd("ExamTopicDetails", UtilMisc.toMap("examId", examId), null,
					false);
			if (topics == null || topics.isEmpty()) {
				return ServiceUtil.returnError("No topics found. Add topics before generating questions");
			}

			int totalPercentage = 0;
			for (GenericValue topic : topics) {
				if (topic.get("percentage") == null) {
					return ServiceUtil.returnError("Topic " + topic.getString("topicId"));
				}
				totalPercentage += topic.getLong("percentage").intValue();
			}
			if (totalPercentage != 100) {
				return ServiceUtil
						.returnError("Topic percentages must add up to 100. Current total: " + totalPercentage);
			}

			delegator.removeByCondition("QuestionBankMasterB", EntityCondition.makeCondition("examId", examId));

			Long questionsRandomized = exam.getLong("questionsRandomized");
			boolean shouldShuffle = questionsRandomized != null && questionsRandomized == 1;

			int totalSaved = 0;

			for (GenericValue topic : topics) {
				String topicId = topic.getString("topicId");
				int percentage = topic.getLong("percentage").intValue();
				int questionCount = (totalQuestions * percentage) / 100;

				if (questionCount <= 0) {
					return ServiceUtil.returnError("Topic " + topicId + " percentage too low — results in 0 questions");
				}

				EntityCondition condition = EntityCondition.makeCondition("topicId", EntityOperator.EQUALS, topicId);
				List<GenericValue> questions = EntityQuery.use(delegator).from("QuestionMaster").where(condition)
						.limit(questionCount).queryList();

				if (questions == null || questions.isEmpty()) {
					return ServiceUtil.returnError("No questions found for topic: ");
				}
				if (questions.size() < questionCount) {
					return ServiceUtil.returnError("Topic " + topicId + " needs " + questionCount
							+ " questions but only " + questions.size() + " available");
				}

				if (shouldShuffle) {
					Collections.shuffle(questions);
				}

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
					draft.set("numAnswers", (Long) q.get("numAnswers"));
					draft.set("questiontype", q.get("questionType"));
					draft.set("difficultyLevel", q.get("difficultyLevel"));
					draft.set("answerValue", q.get("answerValue"));
					draft.set("negativeMarkValue", (Long) exam.get("negativeMarkValue"));

					delegator.create(draft);
					totalSaved++;
				}
			}
			return ServiceUtil.returnSuccess("Questions generated successfully. Total saved: " + totalSaved);

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try again later ");
		}
	}

	public static Map<String, ? extends Object> launchExam(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String examId = (String) context.get("examId");

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("examId is required");
			}

			GenericValue exam = delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId", examId));
			if (UtilValidate.isEmpty(exam)) {
				return ServiceUtil.returnError("Exam not found");
			}

			Object setupProper = exam.get("examSetupProper");
			if (setupProper != null && ((Long) setupProper).intValue() == 1) {
				return ServiceUtil.returnError("Exam is already launched");
			}

			List<GenericValue> draftQuestions = delegator.findByAnd("QuestionBankMasterB",
					UtilMisc.toMap("examId", examId), null, false);
			if (UtilValidate.isEmpty(draftQuestions)) {
				return ServiceUtil.returnError("No draft questions found. Run generateExamQuestions first");
			}

			int expectedTotal = ((Long) exam.get("noOfQuestions")).intValue();
			if (draftQuestions.size() != expectedTotal) {
				return ServiceUtil.returnError("Draft has " + draftQuestions.size() + " questions but exam expects "
						+ expectedTotal + ". Please regenerate questions");
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

			return ServiceUtil
					.returnSuccess("Exam launched successfully. " + draftQuestions.size() + " questions ready");

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try again later ");
		}
	}

	public static Map<String, ? extends Object> getExamById(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String examId = (String) context.get("examId");
			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("ExamId is required");
			}

			GenericValue exam = delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId", examId));

			if (exam == null) {
				return ServiceUtil.returnError("Exam is not available");
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("exam", exam.getAllFields());
			result.put("partyId", partyId);

			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError("Something went wrong try later");
		}
	}

}
