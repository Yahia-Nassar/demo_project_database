package com.example.demo.user;

import com.example.demo.person.Person;
import com.example.demo.person.PersonService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;
    private final PersonService personService;

    public ProfileController(UserService userService, PersonService personService) {
        this.userService = userService;
        this.personService = personService;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = userService.currentUser();
        Person person = user.getPerson();
        if (person == null) {
            person = new Person();
            person.setName(user.getName());
            person.setUser(user);
            person = personService.savePerson(person);
            user.setPerson(person);
        }
        model.addAttribute("user", user);
        model.addAttribute("person", person);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("person") Person submitted) {
        User user = userService.currentUser();
        Person person = user.getPerson();
        if (person == null) {
            person = new Person();
            person.setUser(user);
        }
        person.setName(submitted.getName());
        person.setAlter(submitted.getAlter());
        person.setGeburtsdatum(submitted.getGeburtsdatum());
        person.setGeburtsort(submitted.getGeburtsort());
        personService.savePerson(person);
        return "redirect:/profile";
    }
}