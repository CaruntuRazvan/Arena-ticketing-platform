package com.arena.ticketing.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "sectors")
@Getter @Setter @NoArgsConstructor
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Sector name is required")
    private String name;

    @ManyToOne
    @JoinColumn(name = "stadium_id", nullable = false)
    @JsonIgnore // deserialize to prevent circular reference
    private Stadium stadium;

    @OneToMany(mappedBy = "sector", cascade = CascadeType.ALL)
    private List<Seat> seats;
}
