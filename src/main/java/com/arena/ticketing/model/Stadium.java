package com.arena.ticketing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "stadiums")
@Getter
@Setter
@NoArgsConstructor
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele stadionului este obligatoriu")
    private String name;

    private String location;

    @OneToMany(mappedBy = "stadium", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Sector> sectors;
}
