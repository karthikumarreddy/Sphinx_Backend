package com.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class AdminServices {

	public static Map<String, ? extends Object> approveUser(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		try {
			if (context.get("userName") == null)
				return ServiceUtil.returnError("userName is required");

			Delegator delegator = dctx.getDelegator();
			GenericValue user = delegator.findOne("UserLogin", false,
					UtilMisc.toMap("userLoginId", context.get("userName")));
			if (user == null)
				return ServiceUtil.returnError("user not found");

			user.set("enabled", "Y");
			delegator.store(user);

			GenericValue party = delegator.findOne("Party", false, UtilMisc.toMap("partyId", user.getString("partyId")));
			if (party == null)
				return ServiceUtil.returnError("party not found");

			party.set("statusId", "PARTY_ENABLED");
			delegator.store(party);

			return ServiceUtil.returnSuccess("User approved successfully");

		} catch (Exception e) {
			return ServiceUtil.returnError("something went wrong try later");
		}
	}
}
