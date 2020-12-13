package com.changgou.goods.controller;

import com.changgou.goods.entity.Result;
import com.changgou.goods.entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class BaseExceptionHandler {

    /**
     * 异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){

        return new Result(false, StatusCode.ERROR,e.getMessage());
    }
}
