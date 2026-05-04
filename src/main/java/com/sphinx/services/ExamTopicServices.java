package com.sphinx.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class ExamTopicServices {
	private static final String MODULE = ExamTopicServices.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";

	public static Map<String, ? extends Object> addExamTopicsWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			GenericValue userLogin = (GenericValue) context.get("userLogin");
			if (UtilValidate.isEmpty(userLogin)) {
				return ServiceUtil.returnError("User Login Not Found!");
			}

			String examId = (String) context.get("examId");
			String topicId = (String) context.get("topicId");
			String topicName = (String) context.get("topicName");
			String percentage = (String) context.get("percentage");
			String topicPassPercentage = (String) context.get("topicPassPercentage");
			Boolean savePermanently = (Boolean) context.get("savePermanently");

			if (UtilValidate.isEmpty(examId)) {
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
			if (UtilValidate.isEmpty(savePermanently)) {
				return ServiceUtil.returnError("Save Permanently is required ");
			}

			String existingMsg = "";

			if (savePermanently) {
				GenericValue topicMasterRecord = EntityQuery.use(delegator).from("TopicMaster").where("topicId", topicId).queryFirst();
				if (UtilValidate.isEmpty(topicMasterRecord)) {
					Map<String, Object> result = dctx.getDispatcher().runSync("createTopic",
									UtilMisc.toMap("topicId", topicId, "topicName", topicName, "partyId", userLogin.getString("partyId")));
					if (ServiceUtil.isError(result)) {
						return ServiceUtil.returnError("Failed to Save the topic Permanently!");
					}

					existingMsg = "Topic Saved Permanently";
				} else {
					existingMsg = "Topic Already Saved Permanently";
				}
			}

			// GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId)
			// .select("noOfQuestions").queryFirst();

			GenericValue exam = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryFirst();

			if (UtilValidate.isEmpty(exam)) {
				return ServiceUtil.returnError("Assessment Not found On Records!");
			}

			// long totalQuestions = exam.getLong("noOfQuestions");
			// int totalQuestionsInTopic = (int) (totalQuestions * Integer.valueOf(percentage)) / 100;
			// long questionCount = EntityQuery.use(delegator).from("QuestionMaster").where("topicId", topicId)
			// .maxRows(totalQuestionsInTopic).queryCount();
			// if (totalQuestionsInTopic > questionCount) {
			// return ServiceUtil.returnError((totalQuestionsInTopic - questionCount)
			// + " question needed for the Topic to add in Assessment! Please Add Questions to the Topic!");
			// }

			GenericValue topic = delegator.findOne("ExamTopicDetails", true, UtilMisc.toMap("topicId", topicId));


			if (UtilValidate.isEmpty(topic)) {

				Map<String, Object> input = new HashMap<String, Object>();
				input.put("examId", examId);
				input.put("topicId", topicId);
				input.put("topicName", topicName);
				input.put("percentage", percentage);
				input.put("topicPassPercentage", topicPassPercentage);

				LocalDispatcher dispatcher = dctx.getDispatcher();
				Map<String, Object> result = dispatcher.runSync("updateExamTopics", context);
				if (ServiceUtil.isError(result)) {
					return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
				}
				result.put("successMessage", existingMsg + " " + "Topic Assigned to the Assessment !");
				return result;

			} else {
				long existingPercentage = topic.getLong("percentage");
				double existingPassPercentage = topic.getDouble("topicPassPercentage");
				if (Long.valueOf(percentage) + existingPercentage > 100) {
					return ServiceUtil.returnError("Topic Already Exists, Cannot Update further, Cummulative Percentage exceeds 100%");
				}
				if (Double.valueOf(topicPassPercentage) + existingPassPercentage > 100) {
					return ServiceUtil.returnError(
									"Topic Already Exists, Cannot Update further, Cummulative Topic Pass Percentage exceeds 100%");
				}

				topic.set("percentage", Long.valueOf(percentage) + existingPercentage);
				topic.set("topicPassPercentage", Double.valueOf(topicPassPercentage) + existingPassPercentage);

				delegator.store(topic);

				return ServiceUtil.returnSuccess(existingMsg + " " + "Topic Updated Successfully!");
			}

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
		}
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

			if (UtilValidate.isEmpty(topics)) {
				return ServiceUtil.returnError("topics is null");
			}
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
	//
	// public static Map<String, ? extends Object> generateExamQuestions(DispatchContext dctx,
	// Map<String, ? extends Object> context) {
	//
	// Delegator delegator = dctx.getDelegator();
	// try {
	// if (UtilValidate.isEmpty(delegator)) {
	// return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
	// }
	//
	// String examId = (String) context.get("examId");
	// String partyId = (String) context.get("partyId");
	//
	// if (UtilValidate.isEmpty(examId)) {
	// return ServiceUtil.returnError("examId is required");
	// }
	// if (UtilValidate.isEmpty(partyId)) {
	// return ServiceUtil.returnError("partyId is required");
	// }
	//
	// // ── Exam validation ───────────────────────────────────────────────
	// GenericValue exam = delegator.findOne("ExamMaster", false, UtilMisc.toMap("examId", examId));
	// if (UtilValidate.isEmpty(exam)) {
	// return ServiceUtil.returnError("Exam not found for examId: " + examId);
	// }
	//
	// Long setupProper = exam.getLong("examSetupProper");
	// if (setupProper != null && setupProper.equals(1L)) {
	// return ServiceUtil.returnError("Exam already launched. Cannot regenerate questions");
	// }
	//
	// if (exam.get("noOfQuestions") == null) {
	// return ServiceUtil.returnError("Exam has no question count set");
	// }
	//
	// int totalQuestions = exam.getLong("noOfQuestions").intValue();
	// if (totalQuestions <= 0) {
	// return ServiceUtil.returnError("Exam question count must be greater than 0");
	// }
	//
	// List<GenericValue> topics = delegator.findByAnd("ExamTopicDetails",
	// UtilMisc.toMap("examId", examId, "partyId", partyId), null, false);
	//
	// if (UtilValidate.isEmpty(topics)) {
	// return ServiceUtil.returnError("No topics found. Add topics before generating questions");
	// }
	//
	// int totalPercentage = 0;
	// for (GenericValue topic : topics) {
	// if (topic.get("percentage") == null) {
	// return ServiceUtil.returnError("Topic " + topic.getString("topicId") + " has no percentage set");
	// }
	// totalPercentage += topic.getLong("percentage").intValue();
	// }
	//
	// if (totalPercentage != 100) {
	// return ServiceUtil
	// .returnError("Topic percentages must add up to 100. Current total: " + totalPercentage);
	// }
	//
	// Long questionsRandomized = exam.getLong("questionsRandomized");
	// boolean shouldShuffle = questionsRandomized != null && questionsRandomized.equals(1L);
	//
	// for (GenericValue topic : topics) {
	// String topicId = topic.getString("topicId");
	// int percentage = topic.getLong("percentage").intValue();
	// int questionCount = (totalQuestions * percentage) / 100;
	//
	// if (questionCount <= 0) {
	// return ServiceUtil.returnError("Topic " + topicId + " percentage too low — results in 0 questions");
	// }
	//
	// EntityCondition condition = EntityCondition.makeCondition(
	// EntityCondition.makeCondition("topicId", EntityOperator.EQUALS, topicId), EntityOperator.AND,
	// EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
	//
	// long availableCount = EntityQuery.use(delegator).from("QuestionMaster").where(condition).queryCount();
	//
	// if (availableCount == 0) {
	// return ServiceUtil.returnError("No questions found for topic: " + topicId);
	// }
	// if (availableCount < questionCount) {
	// return ServiceUtil.returnError("Topic " + topicId + " needs " + questionCount
	// + " questions but only " + availableCount + " available");
	// }
	// }
	//
	// // Remove existing generated questions for this exam + party
	// delegator.removeByCondition("QuestionBankMasterB",
	// EntityCondition.makeCondition(
	// EntityCondition.makeCondition("examId", EntityOperator.EQUALS, examId), EntityOperator.AND,
	// EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId)));
	//
	// int totalSaved = 0;
	//
	// for (GenericValue topic : topics) {
	// String topicId = topic.getString("topicId");
	// int percentage = topic.getLong("percentage").intValue();
	// int questionCount = (totalQuestions * percentage) / 100;
	//
	// EntityCondition condition = EntityCondition.makeCondition(
	// EntityCondition.makeCondition("topicId", EntityOperator.EQUALS, topicId), EntityOperator.AND,
	// EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
	//
	// List<GenericValue> allQuestions = EntityQuery.use(delegator).from("QuestionMaster").where(condition)
	// .queryList();
	//
	// if (shouldShuffle) {
	// Collections.shuffle(allQuestions);
	// }
	//
	// List<GenericValue> selectedQuestions = allQuestions.subList(0, questionCount);
	//
	// for (GenericValue q : selectedQuestions) {
	// String newQid = delegator.getNextSeqId("QuestionBankMasterB");
	// GenericValue draft = delegator.makeValue("QuestionBankMasterB");
	//
	// draft.set("qId", newQid);
	// draft.set("examId", examId);
	// draft.set("partyId", partyId);
	// draft.set("topicId", q.get("topicId"));
	// draft.set("questionDetail", q.get("questionDetail"));
	// draft.set("optionA", q.get("optionA"));
	// draft.set("optionB", q.get("optionB"));
	// draft.set("optionC", q.get("optionC"));
	// draft.set("optionD", q.get("optionD"));
	// draft.set("optionE", q.get("optionE"));
	// draft.set("answer", q.get("answer"));
	// draft.set("numAnswers", q.getLong("numAnswers"));
	// draft.set("questionType", q.get("questionType")); // fixed case
	// draft.set("difficultyLevel", q.get("difficultyLevel"));
	// draft.set("answerValue", q.get("answerValue"));
	// draft.set("negativeMarkValue", exam.getLong("negativeMarkValue"));
	//
	// delegator.create(draft);
	// totalSaved++;
	// }
	// }
	//
	// return ServiceUtil.returnSuccess("Questions generated successfully. Total saved: " + totalSaved);
	//
	// } catch (Exception e) {
	// return ServiceUtil.returnError("Failed to generate questions: " + e.getMessage());
	// }
	// }

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


	public static Map<String, ? extends Object> getAllAssessmentTopics(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			// String partyId = (String) context.get("partyId");
			String examId = (String) context.get("examId");

			// if (UtilValidate.isEmpty(partyId)) {
			// return ServiceUtil.returnError("Invalid details can not fetch the Questions!");
			// }

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid details can not fetch the Questions!");
			}

			List<GenericValue> topics = EntityQuery.use(delegator).from("ExamTopicDetails").where("examId", examId).select("topicId")
							.queryList();

			Map<String, Object> result = ServiceUtil.returnSuccess();

			result.put("data", topics);
			return result;

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	public static Map<String, ? extends Object> saveMandatoryQuestionsForTopic(DispatchContext dctx,
					Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();


			String examId = (String) context.get("examId");
			String topicId = (String) context.get("topicId");
			String questionIds = (String) context.get("questionIds");

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid details can not Save Mandatory Questions!");
			}

			if (UtilValidate.isEmpty(topicId)) {
				return ServiceUtil.returnError("Invalid details can not Save Mandatory Questions!");
			}

			if (UtilValidate.isEmpty(questionIds)) {
				return ServiceUtil.returnError("Invalid details can not Save Mandatory Questions!");
			}


			GenericValue topic = EntityQuery.use(delegator).from("ExamTopicDetails").where("examId", examId, "topicId", topicId).queryOne();

			if (UtilValidate.isEmpty(topic)) {
				return ServiceUtil.returnError("Select Topic not found on Records!");
			}

			topic.set("mandatoryQuestionIds", questionIds);
			delegator.store(topic);

			return ServiceUtil.returnSuccess("Topic Details Saved!");


		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

}
