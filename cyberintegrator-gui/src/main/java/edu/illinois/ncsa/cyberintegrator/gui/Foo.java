package edu.illinois.ncsa.cyberintegrator.gui;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Foo {
    @GeneratedValue
    @Id
    public long   id;

    public String bar;
}
