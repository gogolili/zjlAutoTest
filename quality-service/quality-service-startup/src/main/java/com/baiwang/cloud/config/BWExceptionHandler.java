package com.baiwang.cloud.config;

import com.baiwang.cloud.common.model.ServiceResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//全局异常处理器
@ControllerAdvice
public class BWExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ServiceResponse<String> parameterExceptionHandler(MethodArgumentNotValidException e) {
        // 获取异常信息
        BindingResult exceptions = e.getBindingResult();
        // 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty()) {
                // 这里列出了全部错误参数，按正常逻辑，只需要第一条错误即可
                FieldError fieldError = (FieldError) errors.get(0);
                return ServiceResponse.error(fieldError.getDefaultMessage(), 200);
            }
        }
        return ServiceResponse.error("系统异常，请稍后再试",900);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ServiceResponse<String> handleException(Exception exception) {
        ServiceResponse<String> error = ServiceResponse.error(exception.getMessage(), 200);
        return error;
    }
}

