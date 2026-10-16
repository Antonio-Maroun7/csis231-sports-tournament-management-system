package com.antonio.sportstournament.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "venue_availability")
@Getter
@Setter
public class VenueAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id",nullable = false)
    private Venue venue;

    @Column(name = "available_date",nullable = false)
    private LocalDate availableDate;

    @Column(name = "start_time",nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time",nullable = false)
    private LocalTime endTime;


}
