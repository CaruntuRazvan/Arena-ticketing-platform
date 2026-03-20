package com.arena.ticketing.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "seats")
@Getter @Setter @NoArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Row number must be at least 1")
    private Integer rowNumber;

    @Min(value = 1, message = "Seat number must be at least 1")
    private Integer seatNumber;

    @ManyToOne
    @JoinColumn(name = "sector_id", nullable = false)
    @JsonIgnore // deserialize to prevent circular reference
    private Sector sector;
}

