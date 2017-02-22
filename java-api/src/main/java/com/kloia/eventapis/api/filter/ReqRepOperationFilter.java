package com.kloia.eventapis.api.filter;

import com.kloia.eventapis.api.StoreApi;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.api.pojos.Event;
import com.kloia.eventapis.api.pojos.EventState;
import com.kloia.eventapis.api.pojos.IEventType;
import com.kloia.eventapis.api.pojos.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class ReqRepOperationFilter extends AbstractRequestLoggingFilter {

    private static final Logger logger = LoggerFactory.getLogger(ReqRepOperationFilter.class);

    private StoreApi storeApi;

    public ReqRepOperationFilter(StoreApi storeApi) {
        this.storeApi = storeApi;
    }

    public static final List<String> aggregateMethods = Collections.unmodifiableList(Arrays.asList("POST", "PUT", "DELETE"));


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String method = httpServletRequest.getMethod();
        String requestURI = httpServletRequest.getRequestURI();

        if ( (aggregateMethods.contains(method)))
            try {
                OperationRepository operationRepository = storeApi.getOperationRepository();

                StringBuilder logBuilder = new StringBuilder(150);
                logBuilder.append("Request : method=");
                logBuilder.append(method);
                logBuilder.append(",requestUri=");
                logBuilder.append(requestURI);
                logBuilder.append(",requestBody=");
                logBuilder.append(" ");

                String opIdStr = httpServletRequest.getHeader("opId");
                UUID opId = null;
                Operation operation;
                if (opIdStr != null) {
                    opId = UUID.fromString(opIdStr);
                    operation = operationRepository.getOperation(opId);
                } else {
                    Map.Entry<UUID, Operation> orderCreate = operationRepository.createOperation(requestURI);
                    opId = orderCreate.getKey();
                    operation = orderCreate.getValue();
                }
                operationRepository.switchContext(opId, operation);
                Event event = new Event(UUID.randomUUID(), IEventType.EXECUTE, EventState.CREATED, null);
                operationRepository.appendEvent(opId, event);

                try {
                    filterChain.doFilter(httpServletRequest, httpServletResponse);
                    logger.info(logBuilder.toString());
                    operationRepository.updateEvent(opId,event.getEventId(),event1 -> event1.setEventState(EventState.SUCCEDEED));
                } finally {
                    operationRepository.clearContext();
                }
            } catch (IOException e) {
                logger.error("IOException : " + e.getMessage(), e.getStackTrace());
            } catch (ServletException e) {
                logger.error("ServletException : " + e.getMessage(), e.getStackTrace());
            }
        else
            filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {

    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {

    }

    @Override
    public void destroy() {
    }


}
