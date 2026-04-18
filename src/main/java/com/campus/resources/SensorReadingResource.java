/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.campus.resources;



import com.campus.models.SensorReading;
import com.campus.models.Sensor;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.UUID;

/**
 *
 * @author harsh
 */

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    public static Map<String, List<SensorReading>> readingDatabase = new HashMap<>();

    
    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        
        Sensor targetSensor = SensorResource.sensorDatabase.get(this.sensorId);
        if (targetSensor == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Sensor not found").build();
        }

        List<SensorReading> readings = readingDatabase.getOrDefault(this.sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading newReading) {
        
        if(newReading.getId() == null || newReading.getId().isEmpty()){
            newReading.setId(java.util.UUID.randomUUID().toString());
        }
        if(newReading.getTimestamp()== 0){
            newReading.setTimestamp(System.currentTimeMillis());
        }
        
        
        Sensor targetSensor = SensorResource.sensorDatabase.get(this.sensorId);

        
        if ("MAINTENANCE".equalsIgnoreCase(targetSensor.getStatus())) {
            throw new com.campus.exceptions.SensorUnavailableException("Sensor is in maintenance and cannot accept readings.");
        }

        readingDatabase.putIfAbsent(this.sensorId, new ArrayList<>());
        readingDatabase.get(this.sensorId).add(newReading);

        
        targetSensor.setCurrentValue(newReading.getValue());

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
    
    
    }
