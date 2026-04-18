/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.campus.exceptions;

import com.campus.models.ErrorMessage;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author harsh
 */
@Provider
public class CampusApiErrorMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        
        
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message = exception.getMessage();

        
        if (exception instanceof WebApplicationException) {
            status = ((WebApplicationException) exception).getResponse().getStatus();
        }

        
        ErrorMessage customError = new ErrorMessage(
                message != null ? message : "An unexpected server error occurred.",
                status,
                "http://localhost:8080/CampusAPI/docs"
        );

        return Response.status(status).entity(customError).build();
    }
}
