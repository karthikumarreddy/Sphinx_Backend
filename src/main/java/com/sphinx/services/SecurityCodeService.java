package com.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.RandomPasswordGenerator;

public class SecurityCodeService {

	private static final String MODULE = SecurityCodeService.class.getName();

	public static Map<String, ? extends Object> generateSecurityCode(DispatchContext dctx, Map<String, ? extends Object> context) {
		// generate security Code.

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

			String otp = RandomPasswordGenerator.generateSecurityCode(6);

			GenericValue securityCodeRecord = delegator.makeValue("ExamSecurityCode");

			securityCodeRecord.set("examId", examId);
			securityCodeRecord.set("partyId", partyId);
			securityCodeRecord.set("securityCode", otp);

			delegator.create(securityCodeRecord);

			return ServiceUtil.returnSuccess("Security Code Generated Successfully!");

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError("Something went wrong try again later");
		}
		
	}

	public static Map<String, ? extends Object> verifySecurityCode(DispatchContext dctx, Map<String, ? extends Object> context) {
		// generate security Code.

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

			// if ("Y".equalsIgnoreCase(securityCodeRecord.getString("securityCodeVerified"))) {
			// return ServiceUtil.returnError("Security Code already Verified!");
			// }

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
