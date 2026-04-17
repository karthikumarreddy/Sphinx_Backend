package com.sphinx.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class UserExamServices {

	private static String MODULE = UserExamServices.class.getName();

	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";

	public static Map<String, ? extends Object> startExamWrapper(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) dctx.getDispatcher();

			if (UtilValidate.isEmpty(dispatcher)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			String partyId = (String) context.get("partyId");
			String examId = (String) context.get("examId");

			long totalAnswered = (Integer) context.get("totalAnswered");
			long totalRemaining = (Integer) context.get("totalRemaining");
			long isExamActive = (Integer) context.get("isExamActive");
			if (totalAnswered < 0) {
				return ServiceUtil.returnError("Total Answered can not be negative");
			}
			if (totalRemaining < 0) {
				return ServiceUtil.returnError("Total Remaining can not be negative");
			}
			if (!(isExamActive == 0 || isExamActive == 1)) {
				return ServiceUtil.returnError("ExamActive value is invalid");
			}

			long sum = totalAnswered + totalRemaining;

			GenericValue exam = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId", partyId, "examId", examId).queryOne();
			if (UtilValidate.isEmpty(exam)) {
				return ServiceUtil.returnError("Invalid Credential Cannot start exam");
			}

			GenericValue totalQuestions = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId)
					.queryOne();
			long noOfQuestions = totalQuestions.getLong("noOfQuestions");

			if (sum > noOfQuestions) {
				return ServiceUtil.returnError("Inproper totalAnswers and totalRemaining");
			}

			GenericValue isExamLaunched = EntityQuery.use(delegator).from("InProgressParty")
					.where("partyId", partyId, "examId", examId).queryOne();
			if (!UtilValidate.isEmpty(isExamLaunched)) {
				return ServiceUtil.returnError("Exam is already started can not start again");
			}

			Map<String, Object> result = dispatcher.runSync("startExam", context);
			if (ServiceUtil.isError(result)) {
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}
	}

	public static Map<String, ? extends Object> getAllExamQuestions(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			String examId = (String) context.get("examId");
			String partyId = (String) context.get("partyId");
			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Exam Id is required ");

			}
			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Party Id is required ");
			}

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			GenericValue person = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId", partyId, "examId", examId).queryOne();
			if (UtilValidate.isEmpty(person)) {
				return ServiceUtil.returnError("Exam is Not assigned to the user");
			}

			List<GenericValue> exams = EntityQuery.use(delegator).from("QuestionBankMaster").where("examId", examId)
					.orderBy("questionDetail").queryList();

			Map<String, Object> result = ServiceUtil.returnSuccess();
			result.put("data", exams);
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	public static Map<String, Object> getAllExamAssignedForUser(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User!");
			}

			List<GenericValue> assignedExams = EntityQuery.use(delegator).from("AssignedExamDetails")
					.where("partyId", partyId).queryList();

			Map<String, Object> result = ServiceUtil.returnSuccess("Exam Details!");
			result.put("data", assignedExams);
			return result;

		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input values!");
		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	public static Map<String, ? extends Object> getAllPartyExam(DispatchContext dctx, Map<String, Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			String partyId = (String) context.get("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Party Id is required");
			}
			List<GenericValue> examIds = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("partyId", partyId).queryList();

			List<GenericValue> data = new ArrayList<>();

			for (GenericValue examId : examIds) {
				GenericValue exam = EntityQuery.use(delegator).from("ExamMaster")
						.where("examId", examId.getString("examId")).queryOne();
				if (UtilValidate.isNotEmpty(exam)) {
					data.add(exam);
				}

			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			return result;

		} catch (Exception e) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);

		}
	}

	public static Map<String, Object> submitExam(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();

		try {
			String examId = (String) context.get("examId");
			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(examId) || UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Missing examId or partyId.");
			}

			// fetchexam details
			GenericValue examMaster = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId).queryOne();

			if (examMaster == null) {
				return ServiceUtil.returnError("Exam not found.");
			}

			double passPercentage = examMaster.getDouble("passPercentage") != null
					? examMaster.getDouble("passPercentage")
					: 50.0;

			boolean negativeEnabled = examMaster.getLong("allowNegativeMarks") != null
					&& examMaster.getLong("allowNegativeMarks") == 1;

			double negativeMark = examMaster.getDouble("negativeMarkValue") != null
					? examMaster.getDouble("negativeMarkValue")
					: 0.0;

			// fetch questions
			List<GenericValue> questions = EntityQuery.use(delegator).from("QuestionBankMaster").where("examId", examId)
					.queryList();

			Map<String, GenericValue> questionMap = new HashMap<>();
			double totalMaxMarks = 0.0;

			for (GenericValue q : questions) {
				questionMap.put(String.valueOf(q.get("qId")), q);
				String val = q.getString("answerValue");
				totalMaxMarks += val != null ? Double.parseDouble(val) : 1.0;
			}

			// getsubmitted answers
			List<GenericValue> answers = EntityQuery.use(delegator).from("AnswerMaster")
					.where("examId", examId, "partyId", partyId).queryList();

			int totalCorrect = 0;
			int totalWrong = 0;
			double totalScore = 0.0;

			Map<String, int[]> topicStats = new HashMap<>();
			Map<String, Double> topicScores = new HashMap<>();
			Map<String, Double> topicMaxMarks = new HashMap<>();

			for (GenericValue ans : answers) {
				String qId = String.valueOf(ans.get("qId"));
				String submitted = ans.getString("submittedAnswer");

				GenericValue question = questionMap.get(qId);
				if (question == null) {
					continue;
				}

				String correct = question.getString("answer");
				String topicId = question.getString("topicId");
				String type = question.getString("questionType");

				double marks = Double.parseDouble(
						question.getString("answerValue") != null ? question.getString("answerValue") : "1");

				topicStats.putIfAbsent(topicId, new int[] { 0, 0 });
				topicScores.putIfAbsent(topicId, 0.0);
				topicMaxMarks.putIfAbsent(topicId, 0.0);

				topicStats.get(topicId)[1]++;
				topicMaxMarks.put(topicId, topicMaxMarks.get(topicId) + marks);

				if (UtilValidate.isEmpty(submitted))
					continue;

				boolean correctAns = submitted.trim().equalsIgnoreCase(correct.trim());

				if (correctAns) {
					totalCorrect++;
					totalScore += marks;
					topicStats.get(topicId)[0]++;
					topicScores.put(topicId, topicScores.get(topicId) + marks);
				} else {
					totalWrong++;
					if (negativeEnabled) {
						totalScore -= negativeMark;
						topicScores.put(topicId, topicScores.get(topicId) - negativeMark);
					}
				}
			}

			double percentage = totalMaxMarks > 0 ? (totalScore / totalMaxMarks) * 100 : 0;

			int userPassed = percentage >= passPercentage ? 1 : 0;

			// attempt Count
			GenericValue rel = EntityQuery.use(delegator).from("PartyExamRelationship")
					.where("examId", examId, "partyId", partyId).queryOne();

			long attempts = rel != null && rel.getLong("noOfAttempts") != null ? rel.getLong("noOfAttempts") + 1 : 1;

			long performanceId = Long.parseLong(delegator.getNextSeqId("PartyPerformance"));

			// Create PartyPerformanc
			Map<String, Object> perfCtx = new HashMap<>();
			perfCtx.put("performanceId", performanceId);
			perfCtx.put("examId", examId);
			perfCtx.put("partyId", partyId);
			perfCtx.put("score", totalScore);
			perfCtx.put("date", UtilDateTime.nowTimestamp());
			perfCtx.put("noOfQuestions", (long) questions.size());
			perfCtx.put("totalCorrect", (long) totalCorrect);
			perfCtx.put("totalWrong", (long) totalWrong);
			perfCtx.put("userPassed", (long) userPassed);
			perfCtx.put("attemptNo", attempts);

			dispatcher.runSync("createPartyPerformance", perfCtx);

			// topic wise Performance
			for (Map.Entry<String, int[]> entry : topicStats.entrySet()) {
				String topicId = entry.getKey();

				double topicScore = topicScores.get(topicId);
				double topicMax = topicMaxMarks.get(topicId);

				double topicPct = topicMax > 0 ? (topicScore / topicMax) * 100 : 0;

				long detailId = Long.parseLong(delegator.getNextSeqId("DetailedPartyPerformance"));

				Map<String, Object> topicCtx = new HashMap<>();
				topicCtx.put("detailedPerformanceId", detailId);
				topicCtx.put("performanceId", performanceId);
				topicCtx.put("examId", examId);
				topicCtx.put("partyId", partyId);
				topicCtx.put("topicId", topicId);
				topicCtx.put("userTopicPercentage", topicPct);
				topicCtx.put("correctQuestionsInthisTopic", (long) entry.getValue()[0]);
				topicCtx.put("totalQuestionsInThisTopic", (long) entry.getValue()[1]);
				topicCtx.put("userPassedThisTopic", topicPct >= passPercentage ? 1L : 0L);

				dispatcher.runSync("createDetailedPartyPerformance", topicCtx);
			}

			// update attempt tracking
			if (rel != null) {
				Map<String, Object> updateCtx = new HashMap<>();
				updateCtx.put("examId", examId);
				updateCtx.put("partyId", partyId);
				updateCtx.put("noOfAttempts", attempts);
				updateCtx.put("lastPerformanceDate", UtilDateTime.nowTimestamp());

				dispatcher.runSync("updatePartyExamRelationship", updateCtx);
			}

			// deactivate exam
			Map<String, Object> inProgressCtx = new HashMap<>();
			inProgressCtx.put("examId", examId);
			inProgressCtx.put("partyId", partyId);
			inProgressCtx.put("isExamActive", 0L);

			dispatcher.runSync("updateInProgressParty", inProgressCtx);

			Map<String, Object> result = ServiceUtil.returnSuccess("Exam submitted successfully.");
			result.put("performanceId", performanceId);
			result.put("totalCorrect", totalCorrect);
			result.put("totalWrong", totalWrong);
			result.put("score", totalScore);
			result.put("scorePercentage", percentage);
			result.put("userPassed", userPassed);
			result.put("attemptNo", attempts);

			return result;

		} catch (Exception e) {
			Debug.logError(e, "Error in submitExam", MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	public static Map<String, Object> getExamResult(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();

		try {
			String examId = (String) context.get("examId");
			String partyId = (String) context.get("partyId");
			Long performanceId = (Long) context.get("performanceId");

			if (UtilValidate.isEmpty(examId) || UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("examId and partyId are required.");
			}

			GenericValue performance = null;

			// Fetch specific attempt if performanceId is provided
			if (performanceId != null) {
				performance = EntityQuery.use(delegator).from("PartyPerformance")
						.where("performanceId", performanceId, "examId", examId, "partyId", partyId).queryOne();
			} else {
				// Fetch latest attempt
				List<GenericValue> performances = EntityQuery.use(delegator).from("PartyPerformance")
						.where("examId", examId, "partyId", partyId).orderBy("-attemptNo").queryList();

				if (UtilValidate.isNotEmpty(performances)) {
					performance = performances.get(0);
				}
			}

			if (performance == null) {
				return ServiceUtil.returnError("No exam result found.");
			}

			Long resolvedPerformanceId = performance.getLong("performanceId");

			// Fetch topic-wise details
			List<GenericValue> topicDetails = EntityQuery.use(delegator).from("DetailedPartyPerformance")
					.where("performanceId", resolvedPerformanceId).queryList();

			// Calculate percentage
			Double score = performance.getDouble("score");
			Long totalQuestions = performance.getLong("noOfQuestions");

			double percentage = 0.0;
			if (score != null && totalQuestions != null && totalQuestions > 0) {
				percentage = (score / totalQuestions) * 100;
			}

			// Determine pass/fail status
			Long passed = performance.getLong("userPassed");
			String resultStatus = (passed != null && passed == 1) ? "PASS" : "FAIL";

			// Prepare response
			Map<String, Object> result = ServiceUtil.returnSuccess("Exam result fetched successfully.");
			result.put("performance", performance);
			result.put("topicDetails", topicDetails);
			result.put("scorePercentage", percentage);
			result.put("resultStatus", resultStatus);

			return result;

		} catch (Exception e) {
			Debug.logError(e, "Error in getExamResult service", MODULE);
			return ServiceUtil.returnError("Error fetching exam result: " + e.getMessage());
		}
	}

}
