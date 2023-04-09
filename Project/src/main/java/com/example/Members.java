package com.example;

import akka.actor.ActorRef;

import java.util.ArrayList;

/**
 * Class containing the processes' references
 */
public class Members {
    private final ArrayList<ActorRef> references;
    private final String data;

    public Members(ArrayList<ActorRef> references) {
        this.references = references;
        StringBuilder s = new StringBuilder("[ ");
        for (ActorRef a : references) {
            s.append(a.path().name()).append(" ");
        }
        s.append("]");
        this.data = s.toString();
    }

    public ArrayList<ActorRef> getReferences(){
        return this.references;
    }

    public String getData(){
        return this.data;
    }
            
}
