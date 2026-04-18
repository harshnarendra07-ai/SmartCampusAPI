/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.campus.resources;

import com.campus.models.Room;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 *
 * @author harsh
 */
@Path("/rooms")
public class RoomResource {

    public static Map<String, Room> roomDatabase = new HashMap<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        Collection<Room> allRooms = roomDatabase.values();
        return Response.ok(allRooms).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room newRoom) {
        
        if(newRoom.getId() == null || newRoom.getId().isEmpty()){
            newRoom.setId(java.util.UUID.randomUUID().toString());
        }
        
        roomDatabase.put(newRoom.getId(), newRoom);

        return Response.status(Response.Status.CREATED).entity(newRoom).build();
    }

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {

        Room room = roomDatabase.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        Room room = roomDatabase.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new com.campus.exceptions.RoomNotEmptyException("Cannot delete room: Sensors are still active inside it.");
        }

        roomDatabase.remove(roomId);

        return Response.noContent().build();
    }
}
