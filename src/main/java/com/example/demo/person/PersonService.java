package com.example.demo.person;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {
    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository){
        this.personRepository = personRepository;
    }

    public List<Person> loadAllPersons(){
        return personRepository.findAll();
    }

    public void saveAllPersons(List<Person> persons){
        personRepository.saveAllAndFlush(persons);
    }

    public Person savePerson(Person person){
        return personRepository.saveAndFlush(person);
    }
}
