package com.sphinx.services;

import java.math.BigDecimal;
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
			LocalDispatcher dispatcher = dctx.getDispatcher();

			Delegator delegator = dctx.getDelegator();

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
				return ServiceUtil.returnError("You are Not Assigned to the Exam!");
			}

			GenericValue totalQuestions = EntityQuery.use(delegator).from("ExamMaster").where("examId", examId)
					.queryOne();
			long noOfQuestions = totalQuestions.getLong("noOfQuestions");

			if (sum > noOfQuestions) {
				return ServiceUtil.returnError("Invalid Total Answered and Remaining Questions!");
			}

			GenericValue isExamLaunched = EntityQuery.use(delegator).from("InProgressParty")
					.where("partyId", partyId, "examId", examId).queryOne();
			if(!UtilValidate.isEmpty(isExamLaunched)) {
				return ServiceUtil.returnError("Exam is already started can not start again");
			}

			Map<String, Object> result = dispatcher.runSync("startExam", context);
			if (ServiceUtil.isError(result)) {
				return result;
			}

			result = dispatcher.runSync("generateQuestionsForExam", context);

			if (ServiceUtil.isError(result)) {
				return result;
			}

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
	        String examId  = (String) context.get("examId");
	        String partyId = (String) context.get("partyId");

	        if (UtilValidate.isEmpty(examId) || UtilValidate.isEmpty(partyId)) {
	            return ServiceUtil.returnError("Missing examId or partyId.");
	        }

	        // Fetch exam
	        GenericValue examMaster = EntityQuery.use(delegator)
	                .from("ExamMaster")
	                .where("examId", examId)
	                .queryOne();

	        if (examMaster == null) {
	            return ServiceUtil.returnError("Exam not found.");
	        }

	        long passPercentage = examMaster.getLong("passPercentage") != null
	                ? examMaster.getLong("passPercentage") : 50L;

	        boolean negativeEnabled = examMaster.getLong("allowNegativeMarks") != null
	                && examMaster.getLong("allowNegativeMarks") == 1L;

	        long negativeMarkValue = examMaster.getLong("negativeMarkValue") != null
	                ? examMaster.getLong("negativeMarkValue") : 0L;

	        // Questions
	        List<GenericValue> questions = EntityQuery.use(delegator)
	                .from("QuestionBankMaster")
	                .where("examId", examId)
	                .queryList();

	        Map<String, GenericValue> questionMap = new HashMap<>();
	        for (GenericValue q : questions) {
	            questionMap.put(String.valueOf(q.get("qId")), q);
	        }

	        double totalMaxMarks = questions.size();

	        // Answers
	        List<GenericValue> answers = EntityQuery.use(delegator)
	                .from("AnswerMaster")
	                .where("examId", examId, "partyId", partyId)
	                .queryList();

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
	            if (question == null) continue;


	            String correct = question.getString("answer");
	            String topicId = question.getString("topicId");


	            double marks = 1.0;

	            topicStats.putIfAbsent(topicId, new int[]{0, 0});
	            topicScores.putIfAbsent(topicId, 0.0);
	            topicMaxMarks.putIfAbsent(topicId, 0.0);

	            topicStats.get(topicId)[1]++;
	            topicMaxMarks.put(topicId, topicMaxMarks.get(topicId) + marks);

	            if (UtilValidate.isEmpty(submitted)) continue;

	            boolean isCorrect = submitted.trim().equalsIgnoreCase(correct.trim());

	            if (isCorrect) {
	                totalCorrect++;
	                totalScore += marks;
	                topicStats.get(topicId)[0]++;
	                topicScores.put(topicId, topicScores.get(topicId) + marks);
	            } else {
	                totalWrong++;
	                if (negativeEnabled) {
	                    totalScore -= negativeMarkValue;
	                    topicScores.put(topicId, topicScores.get(topicId) - negativeMarkValue);
	                }
	            }
	        }

	        double percentage = totalMaxMarks > 0 ? (totalScore / totalMaxMarks) * 100.0 : 0.0;
	        int userPassed = percentage >= passPercentage ? 1 : 0;

	        // Attempt count
	        GenericValue rel = EntityQuery.use(delegator)
	                .from("PartyExamRelationship")
	                .where("examId", examId, "partyId", partyId)
	                .queryOne();

	        long attempts = (rel != null && rel.getLong("noOfAttempts") != null)
	                ? rel.getLong("noOfAttempts") + 1L : 1L;

	        String performanceId = delegator.getNextSeqId("PartyPerformance");

	        Map<String, Object> perfCtx = new HashMap<>();
	        perfCtx.put("performanceId", performanceId);
	        perfCtx.put("examId", examId);
	        perfCtx.put("partyId", partyId);
	        perfCtx.put("score", BigDecimal.valueOf(totalScore));
	        perfCtx.put("date", UtilDateTime.nowTimestamp());
	        perfCtx.put("noOfQuestions", (long) questions.size());
	        perfCtx.put("totalCorrect", (long) totalCorrect);
	        perfCtx.put("totalWrong", (long) totalWrong);
	        perfCtx.put("userPassed", (long) userPassed);
	        perfCtx.put("attemptNo", attempts);

	        Map<String, Object> perfResult = dispatcher.runSync("createPartyPerformance", perfCtx);
	        if (ServiceUtil.isError(perfResult)) {
	            return perfResult;
	        }

	        // Topic performance
	        for (Map.Entry<String, int[]> entry : topicStats.entrySet()) {
	            String topicId = entry.getKey();
	            double topicScore = topicScores.get(topicId);
	            double topicMax = topicMaxMarks.get(topicId);
	            double topicPct = topicMax > 0 ? (topicScore / topicMax) * 100.0 : 0.0;

	            String detailId = delegator.getNextSeqId("DetailedPartyPerformance");

	            Map<String, Object> topicCtx = new HashMap<>();
	            topicCtx.put("detailedPerformanceId", detailId);
	            topicCtx.put("performanceId", performanceId);
	            topicCtx.put("examId", examId);
	            topicCtx.put("partyId", partyId);
	            topicCtx.put("topicId", topicId);
	            topicCtx.put("userTopicPercentage", BigDecimal.valueOf(topicPct)); // ✅ FIX
	            topicCtx.put("correctQuestionsInthisTopic", (long) entry.getValue()[0]);
	            topicCtx.put("totalQuestionsInThisTopic", (long) entry.getValue()[1]);
	            topicCtx.put("userPassedThisTopic", topicPct >= passPercentage ? 1L : 0L);

	            Map<String, Object> topicResult = dispatcher.runSync("createDetailedPartyPerformance", topicCtx);
	            if (ServiceUtil.isError(topicResult)) {
	                return topicResult;
	            }
	        }

	        // Update attempts
	        if (rel != null) {
	            Map<String, Object> updateCtx = new HashMap<>();
	            updateCtx.put("examId", examId);
	            updateCtx.put("partyId", partyId);
	            updateCtx.put("noOfAttempts", attempts);
	            updateCtx.put("lastPerformanceDate", UtilDateTime.nowTimestamp());

	            Map<String, Object> updateResult = dispatcher.runSync("updatePartyExamRelationship", updateCtx);
	            if (ServiceUtil.isError(updateResult)) {
	                return updateResult;
	            }
	        }

	        // deactivate exam
	        Map<String, Object> inProgressCtx = new HashMap<>();
	        inProgressCtx.put("examId", examId);
	        inProgressCtx.put("partyId", partyId);
	        inProgressCtx.put("isExamActive", 0L);

	        Map<String, Object> inProgressResult = dispatcher.runSync("updateInProgressParty", inProgressCtx);
	        if (ServiceUtil.isError(inProgressResult)) {
	            return inProgressResult;
	        }

	     
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

	// getAllExamQuestions
	public static Map<String, ? extends Object> getUserExamQuestion(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();


			String partyId = (String) context.get("partyId");
			String examId = (String) context.get("examId");
			Integer questionNumber = (Integer) context.get("questionNumber");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User Details!");
			}

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid Exam Details!");
			}

			// EntityCondition examMatchingContion = EntityCondition.makeCondition("examId", EntityOperator.EQUALS, examId);
			// EntityCondition partyMatchingCondition = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId);

			EntityQuery baseCondition = EntityQuery.use(delegator).from("ExamQuestionsWithAnswer")
							.where("examId", examId, "partyId", partyId);

			long totalRecords = baseCondition.queryCount();

			if (totalRecords <= 0) {
				return ServiceUtil.returnError("No Questions Found, Please Ensure the Details are Correct!");
			}

			if (questionNumber <= 0 || questionNumber > totalRecords) {
				return ServiceUtil.returnError("Requested Question Number exceeds Total Number of questions!");
			}

			GenericValue question = baseCondition.orderBy("qId").limit(1).offset(questionNumber - 1).queryOne();

			Map<String, Object> result = ServiceUtil.returnSuccess();

			result.put("end", false);
			if (UtilValidate.isEmpty(question)) {
				result.put("successMessage", "End of Questions!");
				result.put("end", true);
			}

			result.put("data", question);

			return result;

		} catch (ClassCastException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Invalid Input Values!");
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
		}
	}

}
