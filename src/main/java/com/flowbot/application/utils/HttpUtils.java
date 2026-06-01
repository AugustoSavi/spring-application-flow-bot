package com.flowbot.application.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.function.Predicate;

public final class HttpUtils {


    public static final Predicate<HttpStatusCode> isNotFound = HttpStatus.NOT_FOUND::equals;

    public static final Predicate<HttpStatusCode> is4xx = HttpStatusCode::is4xxClientError;

    public static final Predicate<HttpStatusCode> is5xx = HttpStatusCode::is5xxServerError;
}
