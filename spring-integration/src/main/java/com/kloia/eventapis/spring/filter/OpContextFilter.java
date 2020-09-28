package com.kloia.eventapis.spring.filter;

import com.kloia.eventapis.common.OperationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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


    public static final List<String> CHANGE_METHODS = Collections.unmodifiableList(Arrays.asList("POST", "PUT", "DELETE"));
    public static final String OP_ID_HEADER = "X-OPID";
    public static final String OP_TIMEOUT_HEADER = "X-OP-TIMEOUT";
    public static final String OP_START_TIME_HEADER = "X-OP-START-TIME";
    public static final String PARENT_OP_ID_HEADER = "X-PARENT-OPID";
    private OperationContext operationContext;

    @Autowired
    public OpContextFilter(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (CHANGE_METHODS.contains(httpServletRequest.getMethod())) {

                String parentOpId = httpServletRequest.getHeader(OP_ID_HEADER);
                String olderOpIds = httpServletRequest.getHeader(PARENT_OP_ID_HEADER);
                if (StringUtils.hasText(parentOpId) && StringUtils.hasText(olderOpIds)) {
                    parentOpId = parentOpId + OperationContext.PARENT_OP_ID_DELIMITER + olderOpIds;
                }
                String opId = operationContext.generateContext(StringUtils.hasText(parentOpId) ? parentOpId : null, true);
                httpServletResponse.setHeader(OperationContext.OP_ID, opId); //legacy
                httpServletResponse.setHeader(OP_ID_HEADER, opId);
                operationContext.getContext().getPreGenerationConsumers().add(generatedContext -> {
                    httpServletResponse.setHeader(OP_TIMEOUT_HEADER, String.valueOf(generatedContext.getCommandTimeout()));
                    httpServletResponse.setHeader(OP_START_TIME_HEADER, String.valueOf(generatedContext.getStartTime()));
                });
            }
        } finally {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            operationContext.clearContext();
        }
    }


}
