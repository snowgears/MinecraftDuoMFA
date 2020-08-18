package com.snowgears.duomfa.duojavaclient;

import org.json.JSONObject;

public class AuthClient {
  private static final String ENROLL_URL   = "/auth/v2/enroll";
  private static final String PREAUTH_URL  = "/auth/v2/preauth";
  private static final String AUTH_URL     = "/auth/v2/auth";
  private static final String AUTH_STATUS_URL     = "/auth/v2/auth_status";

  private final String ikey;
  private final String skey;
  private final String hostname;

  public AuthClient(String ikey, String skey, String hostname) {
    this.ikey = ikey;
    this.skey = skey;
    this.hostname = hostname;
  }

  /**
   * Enroll new user with Duo authentication.
   * @return JSONObject with all activation information
   */
  public JSONObject enroll() {
    try {
      Http request = new Http("POST", hostname, ENROLL_URL);
      request.signRequest(ikey, skey);
      return (JSONObject) request.executeRequest();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Preauth a user to determine if it is authorized to log in.
   * @param username account username
   * @return JSONObject with user's available authentication factors
   */
  public JSONObject preauth(String username) {
    try {
      Http request = new Http("POST", hostname, PREAUTH_URL);
      request.addParam("username", username);
      request.signRequest(ikey, skey);
      return (JSONObject) request.executeRequest();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Performs authentication for a user by sending a push factor to the device.
   * @param username account username
   * @param factor factor for second-factor auth, for example: push, passcode, sms, or phone
   * @param device device id for the push capability
   * @param async 1 for async, 0 for sync
   * @return JSONObject with auth status
   */
  public JSONObject auth(String username, String factor, String device, int async) {
    try {
      Http request = new Http("POST", hostname, AUTH_URL);
      request.addParam("username", username);
      request.addParam("factor", factor);
      request.addParam("device", device);
      request.addParam("async", ""+async);
      request.signRequest(ikey, skey);
      return (JSONObject) request.executeRequest();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
    /**
     * Checks authentication status for a user.
     * @param txid transaction id of a previous authentication request
     * @return JSONObject with auth status
     */
    public JSONObject auth_status(String txid) {
        try {
            Http request = new Http("GET", hostname, AUTH_STATUS_URL);
            request.addParam("txid", txid);
            request.signRequest(ikey, skey);
            return (JSONObject) request.executeRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
