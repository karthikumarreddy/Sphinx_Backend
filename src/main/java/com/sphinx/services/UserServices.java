package com.sphinx.services;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.RandomPasswordGenerator;

public class UserServices {

	private static final String MODULE = UserServices.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";
	private static final String PARTY_PREFIX = "SPX_";
	private static final String ROLE_SPHINX_ADMIN = "SphinxAdmin";
	private static final String ROLE_SPHINX_USER = "SphinxUser";
	private static final String PARTY_STATUS_ENABLED = "PARTY_ENABLED";
	private static final int USER_PASSWORD_LEN = 6;

	public static Map<String, ? extends Object> getAllUsers(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		Delegator delegator = dctx.getDelegator();

		if (UtilValidate.isEmpty(delegator)) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

		try {
			List<GenericValue> users = EntityQuery.use(delegator).from("PartyPersonalInfo")
							.where("partyTypeId", "PERSON", "statusId", "PARTY_ENABLED", "roleTypeId", "SphinxUser", "contactMechTypeId",
											"EMAIL_ADDRESS")
					.queryList();

			Map<String, Object> result = ServiceUtil.returnSuccess("List of User");
			result.put("users", users);
			return result;

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}
	
	public static Map<String, ? extends Object> getAllUsersCount(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		Delegator delegator = dctx.getDelegator();

		if (UtilValidate.isEmpty(delegator)) {
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

		try {
			List<GenericValue> users = EntityQuery.use(delegator).from("PartyPersonalInfo")
					.where("partyTypeId", "PERSON", "statusId", "PARTY_ENABLED", "roleTypeId", "SphinxUser")
					.queryList();

			Map<String, Object> result = ServiceUtil.returnSuccess("List of User");
			result.put("count", users.size());
			return result;

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}


	public static Map<String, ? extends Object> loginUser(DispatchContext dctx, Map<String, ? extends Object> context) {
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}
			Map<String, Object> result = ServiceUtil.returnSuccess();
			GenericValue user = delegator.findOne("UserLogin", false, Map.of("userLoginId", context.get("userName")));
			if (UtilValidate.isEmpty(user))
				return ServiceUtil.returnError("No account found with the provided credentials.");

			GenericValue party = delegator.findOne("Party", false,
					UtilMisc.toMap("partyId", user.getString("partyId")));

			List<GenericValue> partyRoles = delegator.findByAnd("PartyRole",
					UtilMisc.toMap("partyId", user.getString("partyId")), null, false);

			if (UtilValidate.isEmpty(partyRoles))
				return ServiceUtil.returnError("No role assigned to this account.\n Please contact the administrator.");

			GenericValue partyRole = partyRoles.get(0);

			if (!partyRole.get("roleTypeId").equals("SphinxUser") && !partyRole.get("roleTypeId").equals("SphinxAdmin"))
				return ServiceUtil.returnError("Access denied. Your account does not have a recognized role.");

			if (user.get("enabled").equals("N"))
				return ServiceUtil.returnError(
						"Your account has been suspended. Please contact the administrator for further assistance.");

			if (user.get("currentPassword").equals(context.get("password"))) {
				result.put("successMessage", "Signed In Sucessfully!");
				result.put("partyId", user.getString("partyId"));
				return result;
			} else {
				return ServiceUtil
						.returnError("Invalid username or password. Please check your credentials and try again.");
			}

		} catch (GenericEntityException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	public static Map<String, ? extends Object> signupUser(DispatchContext dctx,
			Map<String, ? extends Object> context) {

		try {

			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			Map<String, Object> result = ServiceUtil.returnSuccess();

			String userName = (String) context.get("userName");
			String firstName = (String) context.get("firstName");
			String lastName = (String) context.get("lastName");
			String email = (String) context.get("email");
			String password = (String) context.get("password");
			String role = (String) context.get("role");
			boolean isAdmin;

			if (UtilValidate.isEmpty(role)) {
				return ServiceUtil.returnError("Role is required!");
			}

			if ("admin".equalsIgnoreCase(role)) {
				isAdmin = true;
				role = ROLE_SPHINX_ADMIN;
			} else if ("user".equalsIgnoreCase(role)) {
				isAdmin = false;
				role = ROLE_SPHINX_USER;
			} else {
				return ServiceUtil.returnError("Invalid role provided");
			}

			// when creating the user username is not comes from frontend.
			if (isAdmin && UtilValidate.isEmpty(userName)) {
				return ServiceUtil.returnError("Username is required!");
			}

			if (isAdmin) {
				GenericValue user = delegator.findOne("UserLogin", true, UtilMisc.toMap("userLoginId", userName));

				if (user != null) {
					return ServiceUtil
							.returnError("This username is already taken.  Please choose a different username.");
				}
			}

			if (UtilValidate.isEmpty(firstName)) {
				return ServiceUtil.returnError("Firstname is required!");
			}
			// if (!Pattern.matches(" ", firstName)) {
			// return ServiceUtil.returnError("Invalid first name. No White space are allowed!.");
			// }
			if (!Pattern.matches("^[A-Za-z]{2,20}$", firstName))
				return ServiceUtil
								.returnError("Invalid first name. It must be 2–20 characters and contain only letters and no space.");

			firstName = firstName.strip();

			if (UtilValidate.isEmpty(lastName)) {
				return ServiceUtil.returnError("Lastname is required!");
			}
			// if (!Pattern.matches(" ", lastName)) {
			// return ServiceUtil.returnError("Invalid last name. No White space are allowed!.");
			// }
			if (!Pattern.matches("^[A-Za-z]{1,20}$", lastName)) {
				return ServiceUtil.returnError("Invalid last name. It must be 2–20 characters and contain only letters and no space.");
			}

			lastName = lastName.strip();
			if (UtilValidate.isEmpty(email)) {
				return ServiceUtil.returnError("Email is required!");
			}
			if (!UtilValidate.isEmail(email)) {
				return ServiceUtil.returnError("Invalid Email ID format!");
			}
			if (isAdmin && UtilValidate.isEmpty(password)) {
				return ServiceUtil.returnError("Password is required!");
			}

			if (isAdmin
					&& !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
				return ServiceUtil.returnError(
						"Password should at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character");
			}

			Map<String, Object> serviceResult = null;

			/*
			 * ================== ================== BEGIN TRANSACTION ==================
			 * ==================
			 */
			TransactionUtil.begin();

			// =====================
			// Party Record Creation
			// =====================

			String partyId = PARTY_PREFIX + delegator.getNextSeqId("Party");

			serviceResult = createParty(dctx,
					UtilMisc.toMap("partyId", partyId, "partyTypeId", "PERSON", "statusId", PARTY_STATUS_ENABLED));

			if (isError(serviceResult)) {
				return handleError(serviceResult);
			}

			// =========================
			// PartyRole Record Creation
			// =========================

			serviceResult = createPartyRole(dctx,
					UtilMisc.toMap("partyId", partyId, "roleTypeId", role, "statusId", PARTY_STATUS_ENABLED));

			if (isError(serviceResult)) {
				return handleError(serviceResult);
			}

			// ======================
			// Person Record Creation
			// ======================

			serviceResult = createPerson(dctx,
					UtilMisc.toMap("partyId", partyId, "firstName", firstName, "lastName", lastName));

			if (isError(serviceResult)) {
				return handleError(serviceResult);
			}

			// =========================
			// UserLogin Record Creation
			// =========================

			serviceResult = createUserLogin(dctx, UtilMisc.toMap("userName", userName, "partyId", partyId,
					"currentPassword", password, "firstName", firstName), isAdmin);
			if (isError(serviceResult)) {
				return handleError(serviceResult);
			}

			// ===========================
			// ContactMech Record Creation
			// ===========================

			String emailContactMechId = delegator.getNextSeqId("ContactMech");

			serviceResult = createContactMech(dctx, UtilMisc.toMap("contactMechId", emailContactMechId,
					"contactMechTypeId", "EMAIL_ADDRESS", "infoString", email));

			if (isError(serviceResult)) {
				return handleError(serviceResult);
			}

			// ================================
			// PartyContactMech Record Creation
			// ================================

			serviceResult = createPartyContactMech(dctx, UtilMisc.toMap("partyId", partyId, "contactMechId",
					emailContactMechId, "fromDate", UtilDateTime.nowTimestamp()));

			if (isError(serviceResult)) {
				return handleError(serviceResult);
			}

			/*
			 * ================== ================== COMMIT TRANSACTION ==================
			 * ==================
			 */
			TransactionUtil.commit();

			result.put("successMessage", "User Created Successfully!");

			return result;

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			/*
			 * TRANSACTION ROLLED BACK
			 */
			try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException e1) {
				Debug.logError(e1, MODULE);
			}

			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}
	}

	private static Map<String, Object> createPartyContactMech(DispatchContext dctx,
			Map<String, ? extends Object> context) throws GenericServiceException {

		return dctx.getDispatcher().runSync("createPartyContactMech", context);

	}

	private static Map<String, Object> createContactMech(DispatchContext dctx, Map<String, ? extends Object> context)
			throws GenericServiceException {

		return dctx.getDispatcher().runSync("createContactMech", context);

	}

	private static Map<String, Object> createPerson(DispatchContext dctx, Map<String, ? extends Object> context)
			throws GenericServiceException {

		return dctx.getDispatcher().runSync("createPerson", context);

	}

	private static Map<String, Object> createParty(DispatchContext dctx, Map<String, ? extends Object> context)
			throws GenericServiceException {

		return dctx.getDispatcher().runSync("createParty", context);

	}

	private static Map<String, Object> createPartyRole(DispatchContext dctx, Map<String, ? extends Object> context)
			throws GenericServiceException {
		return dctx.getDispatcher().runSync("createPartyRole", context);
	}

	private static Map<String, Object> createUserLogin(DispatchContext dctx, Map<String, ? extends Object> context,
			boolean isAdmin) {

		String username;
		String partyId = (String) context.get("partyId");
		String currentPassword;
		String requirePasswordChange;

		// if not admin generate password dynamically.
		if (isAdmin) {
			currentPassword = (String) context.get("currentPassword");
			username = (String) context.get("userName");
			username = username.strip();
			requirePasswordChange = "N";

		} else {
			// if role is user we generate username and password
			String firstName = (String) context.get("firstName");
			firstName = firstName.strip();
			username = (String) context.get("firstName") + "-" + partyId;
			currentPassword = "" + RandomPasswordGenerator.generatePassword(USER_PASSWORD_LEN); // Random generation
			requirePasswordChange = "Y"; // password valid for only one session. hence this flag.
		}

		try {
			return dctx.getDispatcher().runSync("createUserLogin",
					UtilMisc.toMap("userLoginId", username, "currentPassword", currentPassword, "partyId", partyId,
							"enabled", "Y", "requirePasswordChange", requirePasswordChange));

		} catch (GenericServiceException e) {
			Debug.logError(e, MODULE);
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}

	}

	private static boolean isError(Map<String, Object> serviceResult) {
		return serviceResult.containsKey("responseMessage")
				&& "error".equalsIgnoreCase((String) serviceResult.get("responseMessage"));
	}

	private static Map<String, Object> handleError(Map<String, Object> serviceResult) {
		try {
			TransactionUtil.rollback();
		} catch (GenericTransactionException e) {
			Debug.logError(e, MODULE);
		}

		serviceResult.put("errorMessage", UNEXPECTED_ERROR_MSG);
		return serviceResult;
	}

	public static Map<String, ? extends Object> deleteUser(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		boolean transaction = false;
		try {
			Delegator delegator = dctx.getDelegator();

			if (UtilValidate.isEmpty(delegator)) {
				return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
			}

			@SuppressWarnings("unchecked")
			List<String> partyIds = (List<String>) context.get("partyIds");
			if (UtilValidate.isEmpty(partyIds)) {
				return ServiceUtil.returnError("partyIds cannot be empty");
			}

			transaction = TransactionUtil.begin();
			for (String partyId : partyIds) {

				GenericValue party = EntityQuery.use(delegator).from("Party").where("partyId", partyId).queryOne();

				if (UtilValidate.isEmpty(party)) {
					TransactionUtil.rollback(transaction, "Error occured while deleting the user", null);
					return ServiceUtil.returnError("Something went wrong try again later");
				}

				party.set("statusId", "PARTY_DISABLED");
				delegator.store(party);
			}
			TransactionUtil.commit(transaction);

			return ServiceUtil.returnSuccess("User Deleted Sucessfully");

		} catch (Exception e) {
			try {
				TransactionUtil.rollback(transaction, e.getMessage(), e);
			} catch (GenericTransactionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return ServiceUtil.returnError(UNEXPECTED_ERROR_MSG);
		}
	}
	
	
}