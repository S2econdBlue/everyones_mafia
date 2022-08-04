package com.ssafy.mafia.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class RoomInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roomSeq;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = true, length = 1000)
    private String password;

    private int capacity;

    private int hostUser;


    @JsonIgnore
    @OneToOne(mappedBy = "roomInfoSeq", fetch = FetchType.LAZY)
    private GameInfo roomInfoSeq;




}
