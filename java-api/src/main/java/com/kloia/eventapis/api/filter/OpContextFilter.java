package com.kloia.eventapis.api.filter;

import com.kloia.eventapis.api.impl.OperationContext;
import lombok.extern.slf4j.Slf4j;
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
import java.util.UUID;

@Slf4j
public class OpContextFilter extends OncePerRequestFilter {


    private OperationContext operationContext;


    @Autowired
    public OpContextFilter(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    public static final List<String> changeMethods = Collections.unmodifiableList(Arrays.asList("POST", "PUT", "DELETE"));


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (changeMethods.contains(httpServletRequest.getMethod()))
            try {
                String opIdStr = httpServletRequest.getHeader("opId");
                UUID opId;
                if (opIdStr != null) {
                    opId = UUID.fromString(opIdStr);
                } else {
                    opId = UUID.randomUUID();
                }
                operationContext.switchContext(opId);
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } finally {
                operationContext.clearContext();
            }
    }


}
