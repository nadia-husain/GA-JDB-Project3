package com.ga.petadoption.controller;

import com.ga.petadoption.model.VolunteerEvent;
import com.ga.petadoption.service.VolunteerEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/volunteerEvent/")
public class VolunteerEventsController {
    private VolunteerEventService volunteerEventService;

    @Autowired
    public void setVolunteerEventService(VolunteerEventService volunteerEventService) {
        this.volunteerEventService = volunteerEventService;
    }

    @PostMapping("new")
    public VolunteerEvent createVolunteerEvent(@RequestBody VolunteerEvent volunteerEvent) {
        System.out.println("calling createVolunteerEvent ==> ");
        return volunteerEventService.createVolunteerEvent(volunteerEvent);
    }

    @GetMapping("all")
    public List<VolunteerEvent> getAllVolunteerEvents() {
        System.out.println("calling getAllVolunteerEvents ==> ");
        return volunteerEventService.getAllVolunteerEvents();
    }

    @GetMapping(path = "{volunteerEventId}")
    public VolunteerEvent getVolunteerEventById(@PathVariable Long volunteerEventId) {
        System.out.println("calling getVolunteerEventById ==> ");
        return volunteerEventService.getVolunteerEventById(volunteerEventId);
    }

    @PatchMapping(path = "{volunteerEventId}")
    public VolunteerEvent updateVolunteerEvent(@PathVariable Long volunteerEventId, @RequestBody VolunteerEvent volunteerEventObject) {
        System.out.println("calling updateVolunteerEventById ==> ");
        return volunteerEventService.updateVolunteerEvent(volunteerEventId, volunteerEventObject);
    }

    @DeleteMapping("{volunteerEventId}")
    public void deleteVolunteerEvent(@PathVariable("volunteerEventId") Long volunteerEventId) {
        System.out.println("calling deleteVolunteerEventById ==> ");
        volunteerEventService.deleteVolunteerEvent(volunteerEventId);
    }
}
