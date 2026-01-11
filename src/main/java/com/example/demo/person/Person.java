package com.example.demo.person;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import com.example.demo.user.User;

@Data // Fügt automatisch Constructor, Getter, Setter, Equals, Hashcode und toString Methoden ein.
// Erstellt automatisch einen Builder, mit dem verschiedene Objekte mit verschiedenen Kombinationen an
// Attributen erstellt werden können, ohne jeweils eigene Konstruktoren zu benötigen. Diskussion auf
// Stackoverflow: https://stackoverflow.com/questions/29881135/difference-between-builder-pattern-and-constructor
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @Nullable
    private Integer alter;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date geburtsdatum;
    private String geburtsort;
    private String profilePictureURI;
    @OneToOne
    private User user;

}
