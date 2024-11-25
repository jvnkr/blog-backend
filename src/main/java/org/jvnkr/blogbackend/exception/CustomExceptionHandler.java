package org.jvnkr.blogbackend.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(APIException.class)
  public ResponseEntity<Map<String, String>> handleFieldErrorException(APIException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", ex.getMessage());
    return new ResponseEntity<>(errorResponse, ex.getStatus());
  }
}
