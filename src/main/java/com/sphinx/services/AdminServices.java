package com.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class AdminServices {
	private static final String MODULE = AdminServices.class.getName();

	public static Map<String, ? extends Object> approveUser(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			if (UtilValidate.isEmpty(context.get("userName")))
				return ServiceUtil.returnError("userName is required");

			Delegator delegator = dctx.getDelegator();
			GenericValue user = delegator.findOne("UserLogin", false,
					UtilMisc.toMap("userLoginId", context.get("userName")));
			if (UtilValidate.isEmpty(user))
				return ServiceUtil.returnError("user not found");
			
			user.set("enabled", "Y");
			delegator.store(user);

			GenericValue party = delegator.findOne("Party", false, UtilMisc.toMap("partyId", user.getString("partyId")));
			if (UtilValidate.isEmpty(party))
				return ServiceUtil.returnError("party not found");
			
			party.set("statusId", "PARTY_ENABLED");
			delegator.store(party);
			return ServiceUtil.returnSuccess("User approved successfully");

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}
}
