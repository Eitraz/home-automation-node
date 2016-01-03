package com.eitraz.automation.api;

import com.eitraz.automation.api.tellstick.TellstickDevice;
import com.eitraz.automation.api.tellstick.TellstickRawDeviceEvent;
import com.eitraz.automation.tellstick.TellstickAutomation;
import com.eitraz.tellstick.core.device.Device;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/tellstick")
@Produces(MediaType.APPLICATION_JSON)
public class TellstickResource {
    private final TellstickAutomation tellstick;

    public TellstickResource(TellstickAutomation tellstick) {
        this.tellstick = tellstick;
    }

    @GET
    @Path("/devices")
    public List<TellstickDevice> devices() {
        return tellstick.getTellstick().getTellstick().getDeviceHandler()
                .getDevices().stream()
                .map(d -> new TellstickDevice(d, tellstick.getDeviceStatus(d.getName())))
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/device/{name}")
    public TellstickDevice device(@PathParam("name") String name) {
        Device device = tellstick.getTellstick().getTellstick().getDeviceByName(name);
        if (device != null)
            return new TellstickDevice(device, tellstick.getDeviceStatus(device.getName()));
        else
            throw new NotFoundException("No such device");
    }

    @GET
    @Path("/rawEvents")
    public List<TellstickRawDeviceEvent> rawDeviceEvents() {
        return tellstick
                .getRawDeviceEventsCache().stream()
                .map(TellstickRawDeviceEvent::new)
                .sorted((o1, o2) -> Long.compare(o2.getTime().getTime(), o1.getTime().getTime()))
                .collect(Collectors.toList());
    }
}
