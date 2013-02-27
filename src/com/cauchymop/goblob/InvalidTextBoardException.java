package com.cauchymop.goblob;

@SuppressWarnings("serial")
public class InvalidTextBoardException extends Exception {
  public enum ERROR_CODE {
    InvalidSize;
  }

  private ERROR_CODE errorCode;

  public InvalidTextBoardException(ERROR_CODE errorCode) {
    this.errorCode = errorCode;
  }

  public ERROR_CODE getErrorCode() {
    return errorCode;
  }
  
  @Override
  public String getMessage() {
    return "Invalid Text Board: " + errorCode;
  }
}
