package com.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.RandomPasswordGenerator;

public class SecurityCodeService {

	private static final String MODULE = SecurityCodeService.class.getName();

	public static Map<String, ? extends Object> generateSecurityCode(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			String examId = (String) context.get("examId");
			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid Exam details!");
			}

			String partyId = (String) context.get("partyId");
			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User details!");
			}

			GenericValue secCode = EntityQuery.use(delegator).from("ExamSecurityCode").where("examId", examId, "partyId", partyId)
							.queryFirst();

			String otp = RandomPasswordGenerator.generateSecurityCode(6);

			if (UtilValidate.isEmpty(secCode)) {

				GenericValue securityCodeRecord = delegator.makeValue("ExamSecurityCode");

				securityCodeRecord.set("examId", examId);
				securityCodeRecord.set("partyId", partyId);
				securityCodeRecord.set("securityCode", otp);

				delegator.create(securityCodeRecord);
			} else {
				secCode.set("securityCode", otp);
				delegator.store(secCode);
			}

			Map<String, Object> result = ServiceUtil.returnSuccess("Security Code Generated Successfully!");
			result.put("securityCode", secCode);
			return result;

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
		}
		
	}

	public static Map<String, ? extends Object> sendSecurityCode(DispatchContext dctx, Map<String, ? extends Object> context) {

		try {

			LocalDispatcher dispatcher = dctx.getDispatcher();

			String partyId = (String) context.get("partyId");
			GenericValue examRecord = (GenericValue) context.get("examRecord");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User Details!");
			}

			if (UtilValidate.isEmpty(examRecord)) {
				String examId = (String) context.get("examId");
				if (UtilValidate.isEmpty(examId)) {
					return ServiceUtil.returnError("Invalid Assessment Details!");
				}

				examRecord = EntityQuery.use(dctx.getDelegator()).from("ExamMaster").where("examId", examId).queryOne();

				if (UtilValidate.isEmpty(examRecord)) {
					return ServiceUtil.returnError("Assessment Not Found! Contact Administrator for more details!");
				}
			}

			Map<String, Object> result = dispatcher.runSync("generateSecurityCode",
							UtilMisc.toMap("partyId", partyId, "examId", examRecord.get("examId")));

			GenericValue securityCode = (GenericValue) result.get("securityCode");

			return dispatcher.runSync("sendExamNotification", UtilMisc.toMap("examRecord", examRecord, "securityCode", securityCode));

		} catch (GenericServiceException | GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
		}
	}

	public static Map<String, ? extends Object> verifySecurityCode(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			String partyId = (String) context.get("partyId");
			String examId = (String) context.get("examId");
			String securityCode = (String) context.get("securityCode");

			if (UtilValidate.isEmpty(partyId)) {
				return ServiceUtil.returnError("Invalid User Details!");
			}

			if (UtilValidate.isEmpty(examId)) {
				return ServiceUtil.returnError("Invalid Exam Details!");
			}

			if (UtilValidate.isEmpty(securityCode)) {
				return ServiceUtil.returnError("Invalid Security Code!");
			}

			GenericValue securityCodeRecord = EntityQuery.use(delegator).from("ExamSecurityCode")
							.where("examId", examId, "partyId", partyId).queryFirst();

			if (UtilValidate.isEmpty(securityCodeRecord)) { // security record not present in ExamSecurityCode table.

				return ServiceUtil.returnError("Security Code Verification Failed!, Please contact your Administrator!");
			}

			String codeFromDb = securityCodeRecord.getString("securityCode");

			if (UtilValidate.isEmpty(codeFromDb)) {
				return ServiceUtil.returnError("Security Code Verification Failed!, Please contact your Administrator!");
			}

			if ("Y".equalsIgnoreCase(securityCodeRecord.getString("securityCodeVerified"))) {
				return ServiceUtil.returnSuccess("Security Code already Verified!");
			}

			if (!codeFromDb.equals(securityCode)) {
				return ServiceUtil.returnError("Incorrect Security Code!");
			}

			securityCodeRecord.setString("securityCodeVerified", "Y");

			delegator.store(securityCodeRecord);

			return ServiceUtil.returnSuccess("Security Code Verified!");

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
		}

	}

}
