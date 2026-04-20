package com.ga.petadoption.controller;

import com.ga.petadoption.model.Volunteer;
import com.ga.petadoption.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/volunteer/")
public class VolunteerController {
    private VolunteerService volunteerService;

    @Autowired
    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @PostMapping("new")
    public Volunteer createVolunteer(@RequestBody Volunteer volunteer) {
        System.out.println("calling createVolunteer ==> ");
        return volunteerService.createVolunteer(volunteer);
    }

    @GetMapping("all")
    public List<Volunteer> getAllVolunteers() {
        System.out.println("calling getAllVolunteers ==> ");
        return volunteerService.getAllVolunteers();
    }

    @GetMapping(path = "{volunteerId}")
    public Volunteer getVolunteerById(@PathVariable Long volunteerId) {
        System.out.println("calling getVolunteerById ==> ");
        return volunteerService.getVolunteerById(volunteerId);
    }

    @PatchMapping(path = "{volunteerId}")
    public Volunteer updateVolunteer(@PathVariable Long volunteerId, @RequestBody Volunteer volunteerObject) {
        System.out.println("calling updateVolunteerById ==> ");
        return volunteerService.updateVolunteer(volunteerId, volunteerObject);
    }

    @DeleteMapping("{volunteerId}")
    public void deleteVolunteer(@PathVariable("volunteerId") Long volunteerId) {
        System.out.println("calling deleteVolunteerById ==> ");
        volunteerService.deleteVolunteer(volunteerId);
    }
}
