// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StatusCode.java

package com.sun.media.rtsp.protocol;


public class StatusCode
{

    public StatusCode(int code)
    {
        this.code = code;
    }

    public static String getStatusText(int code)
    {
        String text;
        switch(code)
        {
        case 100: // 'd'
            text = "Continue";
            break;

        case 200: 
            text = "Ok";
            break;

        case 201: 
            text = "Created";
            break;

        case 250: 
            text = "Low on storage space";
            break;

        case 300: 
            text = "Multiple choices";
            break;

        case 301: 
            text = "Moved permanently";
            break;

        case 302: 
            text = "Moved temporarily";
            break;

        case 303: 
            text = "See other";
            break;

        case 304: 
            text = "Not modified";
            break;

        case 305: 
            text = "Use proxy";
            break;

        case 400: 
            text = "Bad request";
            break;

        case 401: 
            text = "Unauthorized";
            break;

        case 402: 
            text = "Payment required";
            break;

        case 403: 
            text = "Forbidden";
            break;

        case 404: 
            text = "Not found";
            break;

        case 405: 
            text = "Method not allowed";
            break;

        case 406: 
            text = "Not acceptable";
            break;

        case 407: 
            text = "Proxy authentication required";
            break;

        case 408: 
            text = "Request timed out";
            break;

        case 410: 
            text = "Gone";
            break;

        case 411: 
            text = "Length required";
            break;

        case 412: 
            text = "Precondition failed";
            break;

        case 413: 
            text = "Request entity too large";
            break;

        case 414: 
            text = "Request URI too large";
            break;

        case 415: 
            text = "Unsupported media type";
            break;

        case 451: 
            text = "Parameter not understood";
            break;

        case 452: 
            text = "Conference not found";
            break;

        case 453: 
            text = "Not enough bandwidth";
            break;

        case 454: 
            text = "Session not found";
            break;

        case 455: 
            text = "Method not valid in this state";
            break;

        case 456: 
            text = "Header field not valid";
            break;

        case 457: 
            text = "Invalid range";
            break;

        case 458: 
            text = "Parameter is read only";
            break;

        case 459: 
            text = "Aggregate operation not allowed";
            break;

        case 460: 
            text = "Only aggregate operation allowed";
            break;

        case 461: 
            text = "Unsupported transport";
            break;

        case 462: 
            text = "Destination unreachable";
            break;

        case 500: 
            text = "Internal server error";
            break;

        case 501: 
            text = "Not implemented";
            break;

        case 502: 
            text = "Bad gateway";
            break;

        case 503: 
            text = "Service unavailable";
            break;

        case 504: 
            text = "Gateway time-out";
            break;

        case 505: 
            text = "RTSP version not supported";
            break;

        case 551: 
            text = "Option not supported";
            break;

        default:
            text = "Unknown status code: " + code;
            break;
        }
        return text;
    }

    public static final int CONTINUE = 100;
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int LOW_ON_STORAGE_SPACE = 250;
    public static final int MULTIPLE_CHOICES = 300;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int MOVED_TEMPORARILY = 302;
    public static final int SEE_OTHER = 303;
    public static final int NOT_MODIFIED = 304;
    public static final int USE_PROXY = 305;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int REQUEST_TIMED_OUT = 408;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int PRECONDITION_FAILED = 412;
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int REQUEST_URI_TOO_LARGE = 414;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int PARAMETER_NOT_UNDERSTOOD = 451;
    public static final int CONFERENCE_NOT_FOUND = 452;
    public static final int NOT_ENOUGH_BANDWIDTH = 453;
    public static final int SESSION_NOT_FOUND = 454;
    public static final int METHOD_NOT_VALID_IN_THIS_STATE = 455;
    public static final int HEADER_FIELD_NOT_VALID = 456;
    public static final int INVALID_RANGE = 457;
    public static final int PARAMETER_IS_READ_ONLY = 458;
    public static final int AGGREGATE_OPERATION_NOT_ALLOWED = 459;
    public static final int ONLY_AGGREGATE_OPERATION_ALLOWED = 460;
    public static final int UNSUPPORTED_TRANSPORT = 461;
    public static final int DESTINATION_UNREACHABLE = 462;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIME_OUT = 504;
    public static final int RTSP_VERSION_NOT_SUPPORTED = 505;
    public static final int OPTION_NOT_SUPPORTED = 551;
    private int code;
}
