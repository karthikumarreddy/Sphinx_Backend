package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class UserExamServices {

	private static String MODULE = UserExamServices.class.getName();

	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";

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

	public static Map<String, Object> getAllExamAssignedForUser(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {

			Delegator delegator = dctx.getDelegator();

			String partyId = (String) context.get("partyId");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User!");
			}

			List<GenericValue> assignedExams = EntityQuery.use(delegator).from("AssignedExamDetails").where("partyId", partyId).queryList();

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
}
