package com.smartwealth.smartwealth_backend.api;

public final class ApiPaths {
    private ApiPaths() {
    }

    // Base API Path
    public static final String API = "/api";
    public static final String PUBLIC_API = API + "/public";

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
    public static final String AUTH_ADMIN_LOGIN = "/admin-login";
    public static final String API_AUTH  = API + AUTH;
    public static final String AUTH_LOGIN  = "/login";
    public static final String AUTH_REFRESH  = "/refresh";

    // wallet
    public static final String WALLET = "/wallet";
    public static final String API_WALLET  = API + WALLET;
    public static final String WALLET_BALANCE = "/balance";
    public static final String WALLET_CREDIT = "/credit";
    public static final String WALLET_DEBIT = "/debit";
    public static final String WALLET_TRANSACTIONS = "/transactions";

    // schemes
    public static final String SCHEMES = "/schemes";
    public static final String API_SCHEMES = PUBLIC_API + SCHEMES;


    // plans
    public static final String PLANS = "/plans";
    public static final String API_PLANS = PUBLIC_API + PLANS;
    public static final String PLAN_ID = "/{planId}";

    // nav
    public static final String NAV_HISTORY = "/nav-history";
    public static final String NAVS_BY_DATE = "/by-date";
    public static final String API_NAV_HISTORY = PUBLIC_API + NAV_HISTORY;

    // filters
    public static final String FILTERS = "/filters";
    public static final String API_FILTERS = PUBLIC_API + FILTERS;

    // Investment
    public static final String INVESTMENT = "/investment";
    public static final String LUMPSUM = "/lumpsum";
    public static final String SELL = "/sell";
    public static final String ORDER_HISTORY = "/order-history";
    public static final String API_INVESTMENT = API + INVESTMENT;

    // SIP
    public static final String SIP = "/sips";
    public static final String API_SIP = API + SIP;
    public static final String SIP_CREATE = "/create";
    public static final String GET_ALL_SIPS = "/all";
    public static final String SIP_PAUSE = "/pause";
    public static final String SIP_CANCEL = "/cancel";
    public static final String SIP_RESUME = "/resume";

    // portfolio
    public static final String PORTFOLIO = "/portfolio";
    public static final String HOLDINGS = "/holdings";
    public static final String PLAN = "/plan";
    public static final String TRANSACTIONS = "/transactions";
    public static final String API_PORTFOLIO = API + PORTFOLIO;
    public static final String FAMILY_PORTFOLIO = "/family";

    // family
    public static final String FAMILY = "/family";
    public static final String REQUEST = "/request";
    public static final String PENDING_REQUESTS = "/request/pending";
    public static final String ACCEPT = "/accept";
    public static final String REVOKE = "/revoke";
    public static final String MEMBERS = "/members";
    public static final String API_FAMILY = API + FAMILY;
    public static final String ACCEPT_REQUEST = REQUEST + "/{id}" + ACCEPT;
}
