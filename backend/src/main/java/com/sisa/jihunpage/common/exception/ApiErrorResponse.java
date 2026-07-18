package com.sisa.jihunpage.common.exception;

import java.util.Map;

public record ApiErrorResponse(
	String code,
	String message,
	Map<String, String> errors
) {
}
