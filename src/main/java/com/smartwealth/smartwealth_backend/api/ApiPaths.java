package com.smartwealth.smartwealth_backend.api;

public final class ApiPaths {
    private ApiPaths() {
    }

    // Base API Path
    public static final String API = "/api";

    // User
    public static final String USERS = "/users";
    public static final String CUSTOMER_ID = "/{customerId}";
    public static final String KYC_STATUS = "/kyc-status";
    public static final String USER_BY_ID = USERS + CUSTOMER_ID;
    public static final String USER_KYC_UPDATE = USER_BY_ID + KYC_STATUS;
    public static final String API_USERS  = API + USERS;
    public static final String USER_RISK_PROFILE = "/risk-profile";

    // Admin
    public static final String ADMIN = "/admin";
    public static final String API_ADMIN = API + ADMIN;
    public static final String API_ADMIN_USERS = API_ADMIN + USERS;

    // Auth
    public static final String AUTH = "/auth";
    public static final String API_AUTH  = API + AUTH;
    public static final String AUTH_LOGIN  = "/login";
    public static final String AUTH_REFRESH  = "/refresh";

    // wallet
    public static final String WALLET = "/wallet";
    public static final String API_WALLET  = API + WALLET;
    public static final String WALLET_BALANCE = "/balance";
    public static final String WALLET_CREDIT = "/credit";
    public static final String WALLET_DEBIT = "/debit";
}
