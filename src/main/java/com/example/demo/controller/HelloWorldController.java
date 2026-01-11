package com.example.demo.controller;

import com.example.demo.person.Person;
import com.example.demo.person.PersonService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Controller
public class HelloWorldController {
    public final static String UPLOAD_DIRECTORY = "data/files/images/";
    PersonService personService;

    public HelloWorldController(PersonService personService){
        this.personService = personService;
    }

    @GetMapping("/hello")
    public String hello(Model model){
        model.addAttribute("message", "Hello World from Java!"); // Hinzufügen der message zum Model

        ArrayList<Person> persons = (ArrayList<Person>) personService.loadAllPersons();
        model.addAttribute("addedPerson", new Person());


        model.addAttribute("persons", persons);
        return "hello"; // Im Hintergrund wird hierdurch das Template mit dem Namen "hello.html" zurückgegeben, welches die Daten aus dem Model erhält.
    }

    @PostMapping(path="/hello/addPerson", consumes = {MULTIPART_FORM_DATA_VALUE})
    public String addPerson(@ModelAttribute("addedPerson") Person addedPerson, @RequestParam("image") MultipartFile file) throws IOException {
        addedPerson = personService.savePerson(addedPerson);
        Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, addedPerson.getId() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
        addedPerson.setProfilePictureURI(addedPerson.getId() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
        Files.write(fileNameAndPath, file.getBytes());
        personService.savePerson(addedPerson);

        return "redirect:/hello";
    }

    @GetMapping(value = "/images/{fileId}")
    public @ResponseBody Resource showImage(@PathVariable String fileId) throws IOException {
        byte[] image = Files.readAllBytes(Paths.get(UPLOAD_DIRECTORY, fileId));

        return new ByteArrayResource(image);
    }
}
