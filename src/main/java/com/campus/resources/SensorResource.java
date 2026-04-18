/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.campus.resources;

import com.campus.models.Sensor;
import com.campus.models.Room;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.PathParam;
import java.util.UUID;


/**
 *
 * @author harsh
 */
@Path("/sensors")
public class SensorResource {

    public static Map<String, Sensor> sensorDatabase = new HashMap<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = sensorDatabase.values();

        if (type != null && !type.trim().isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor newSensor) {
        
        if(newSensor.getId() == null || newSensor.getId().isEmpty()){
            newSensor.setId(java.util.UUID.randomUUID().toString());
        }

        Room targetRoom = RoomResource.roomDatabase.get(newSensor.getRoomId());

        if (targetRoom == null) {
            throw new com.campus.exceptions.LinkedResourceNotFoundException("Cannot create sensor: Room ID '" + newSensor.getRoomId() + "' does not exist.");
        }

        sensorDatabase.put(newSensor.getId(), newSensor);

        targetRoom.getSensorIds().add(newSensor.getId());

        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadings(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorDatabase.get(sensorId);

        if (sensor == null) {
            throw new javax.ws.rs.NotFoundException("Sensor with ID '" + sensorId + "' not found.");
        }

        return new SensorReadingResource(sensorId);
    }
}
