package com.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.ApiResponse;

public class AdminServices {
	private static final String MODULE = AdminServices.class.getName();

	public static Map<String, ? extends Object> approveUser(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			if (context.get("userName") == null)
				return ApiResponse.response(false, 400, "userName is required", null);

			Delegator delegator = dctx.getDelegator();
			GenericValue user = delegator.findOne("UserLogin", false,
					UtilMisc.toMap("userLoginId", context.get("userName")));
			if (user == null)
				return ApiResponse.response(false, 400,"user not found", null);
			user.set("enabled", "Y");
			delegator.store(user);

			GenericValue party = delegator.findOne("Party", false, UtilMisc.toMap("partyId", user.getString("partyId")));
			if (party == null)
				return ApiResponse.response(false, 400, "party not found", null);
			
			party.set("statusId", "PARTY_ENABLED");
			delegator.store(party);

			return ApiResponse.response(true, 200, "User approved successfully", null);

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(e.getMessage());
		}
	}
}
