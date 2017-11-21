package com.kloia.eventapis.spring.filter;

import com.kloia.eventapis.common.OperationContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class OpContextFilter extends OncePerRequestFilter {


    public static final List<String> changeMethods = Collections.unmodifiableList(Arrays.asList("POST", "PUT", "DELETE"));
    private OperationContext operationContext;

    @Autowired
    public OpContextFilter(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (changeMethods.contains(httpServletRequest.getMethod())) {

                String opId = httpServletRequest.getHeader("opId");
                if (opId != null) {
                    operationContext.switchContext(opId);
                } else {
                    opId = operationContext.generateContext();
                    httpServletResponse.setHeader("opId", opId);
                }
            }
        } finally {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            operationContext.clearContext();
        }
    }


}
