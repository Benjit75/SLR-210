package com.example;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.example.messages.CrashMsg;
import com.example.messages.HoldMsg;
import com.example.messages.LaunchMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Main {

    public static final Random rd = new Random();

    public static int N = 100;
    public static int f = 49;
    public static int tle = 1000;
    public static float alpha = (float) 1;

    public static long startTime;

    public static void main(String[] args) throws InterruptedException {

        // Getting started time
        long start = System.currentTimeMillis();
        Main.startTime = start;

        // Instantiate an actor system
        final ActorSystem system = ActorSystem.create("system");
        system.log().info("System started with N = " + N );
        system.log().info("System started with f = " + f );
        system.log().info("System started with tle = " + tle );
        system.log().info("System started with alpha = " + alpha );

        ArrayList<ActorRef> references = new ArrayList<>();
        ArrayList<ActorRef> faultyProcesses;

        for (int i = 0; i < N; i++) {
            // Instantiate processes
            final ActorRef a = system.actorOf(Process.createActor(i + 1, N), "" + i);
            references.add(a);
        }

        // give each process a view of all the other processes
        Members m = new Members(references);
        for (ActorRef actor : references) {
            actor.tell(m, ActorRef.noSender());
        }

        // Select randomly f faulty processes among the N processes
        faultyProcesses = new ArrayList<>(references);
        Collections.shuffle(faultyProcesses);
        faultyProcesses = new ArrayList<>(faultyProcesses.subList(0, f));

        // sends a special message to f random processes
        for (ActorRef faultyProcess : faultyProcesses) {
            faultyProcess.tell(new CrashMsg(), ActorRef.noSender());
        }

        // sends a special launch message to all processes
        for (ActorRef actor : references) {
            actor.tell(new LaunchMsg(), ActorRef.noSender());
        }

        // leader election : select a non-faulty process
        ActorRef selectedLeader = faultyProcesses.get(0);
        while(faultyProcesses.contains(selectedLeader)){
            selectedLeader = references.get(rd.nextInt(N));
        }

        // leader election : after a fixed timeout tle, sends a Hold message to every other process
        Thread.sleep(tle);
        for (ActorRef actor : references) {
            if (actor != selectedLeader) {
                actor.tell(new HoldMsg(), ActorRef.noSender());
            }
        }


        //OfconsProposerMsg opm = new OfconsProposerMsg(100);
        //references.get(0).tell(opm, ActorRef.noSender());
    }
}
