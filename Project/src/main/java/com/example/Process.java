package com.example;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.example.messages.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Process extends UntypedAbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);// Logger attached to actor
    private final int N;//number of processes
    private final int id;//id of current process
    private ArrayList<ActorRef> processes;//other processes' references
    private boolean hold;
    private boolean faultProneMode = false;
    private boolean silentMode = false;
    private final float alpha = 0;

    private Integer proposal;
    private int ballot;
    private int readBallot;
    private int imposeBallot;
    private Integer estimate;
    private HashMap<ActorRef, State> states;

    private int nbOfAck = 0;

    public Process(int ID, int nb) {
        this.N = nb;
        this.id = ID;

        this.ballot = this.id - this.N;
        this.proposal = null;
        this.readBallot = 0;
        this.imposeBallot = this.id - this.N;
        this.estimate = null;
        this.states = new HashMap<ActorRef, State>();
    }
    
    public String toString() {
        return "Process{" + "id=" + this.id ;
    }

    /**
     * Static function creating actor
     */
    public static Props createActor(int ID, int nb) {
        return Props.create(Process.class, () -> {
            return new Process(ID, nb);
        });
    }
    
    
    private void propose(Integer v) {
        this.proposal = v;
        this.ballot = this.ballot + this.N;
        this.states.clear();
        for (ActorRef actor : this.processes) {
            if (actor.equals(getSelf())) {
                continue;
            }
            actor.tell(new ReadMsg(this.ballot), this.getSelf());
            this.log.info("Read ballot " + this.ballot + " msg: p" + self().path().name() + " -> p" + actor.path().name());
        }
    }
    
    private void readReceived(int newBallot, ActorRef pj) {
        if((this.readBallot > newBallot || this.imposeBallot > newBallot) & (!this.hold)){
            this.log.info("Sending ABORT message ("+newBallot+") to "+ pj.path().name());
            pj.tell(new AbortMsg(newBallot), this.getSelf());
        }else{
            this.log.info("Sending GATHER message ("+newBallot+", "+this.imposeBallot+", "+this.estimate+") to "+ pj.path().name());
            this.readBallot = newBallot;
            pj.tell(new GatherMsg(newBallot, this.imposeBallot, this.estimate), this.getSelf());
        }
    }

    private void abortReceived(int ballot){
        // TODO : rien
    }

    private void gatherReceived(int newBallot, int estBallot, Integer estimate, ActorRef pj){
        this.states.put(pj, new State(estimate, estBallot));
        //  Il faut regarder si on a reçu plus de n/2 states
        if (this.states.size() > this.N/2){
            int highestEstBallot = 0;
            Integer proposal = 0;
            for (State s : this.states.values()){
                if (s.getEstballot() > highestEstBallot){
                    highestEstBallot = s.getEstballot();
                    proposal = s.getEst();
                }
            }
            if (proposal > 0){
                this.proposal = proposal;
            }
            this.states.clear();
            for (ActorRef actor : this.processes){
                    actor.tell(new ImposeMsg(newBallot, this.proposal), getSelf());
            }

        }
    }

    private void imposeReceived(int newBallot, int v, ActorRef pj){
        if((this.readBallot > newBallot || this.imposeBallot > newBallot)&(!this.hold)){
            this.log.info("Sending ABORT message ("+newBallot+") to "+ pj.path().name());
            pj.tell(new AbortMsg(newBallot), getSelf());
        }else{
            this.estimate = v;
            this.imposeBallot = newBallot;
            this.log.info("Sending ACK message ("+newBallot+") to "+ pj.path().name());
            pj.tell(new AckMsg(newBallot), getSelf());
        }
    }

    private void ackReceived(int ballot){
        this.nbOfAck++;

        if(this.nbOfAck > (N/2)){
            this.nbOfAck = 0;
            log.info("p"+self().path().name()+" received ACK from a majority" + " (b="+ ballot + ")");
            for(ActorRef actor : this.processes){
                actor.tell(new DecideMsg(this.proposal), getSelf());
            }
        }
    }

    private void decideReceived(int v){
        this.silentMode = true; // Once we decided on a value, we stop listening (for better logs)
        long time = System.currentTimeMillis() - Main.startTime;
        log.info("p"+self().path().name()+" received DECIDE from p" + getSender().path().name() + " (value="+ v + ")"+ " | time: " + time);
        for(ActorRef actor : this.processes){
            actor.tell(new DecideMsg(v), getSelf());
        }
    }

    public void onReceive(Object message) throws Throwable {
        // If the process is in silent mode, it does not respond any request anymore
        if(this.silentMode){
            return;
        }
        // If the proc is in fault prone mode, there is a probability alpha that the process might enter silent mode.
        if(this.faultProneMode){
            boolean crash = (((new Random()).nextFloat()) < this.alpha);
            if(crash){
                this.silentMode = true;
                return;
            }
        }
        if (message instanceof Members) {//save the system's info
            Members m = (Members) message;
            this.processes = m.getReferences();
            this.log.info("p" + self().path().name() + " received processes info");
        } else if (message instanceof HoldMsg){
            this.log.info("p" + self().path().name() + " received HOLD from p"+getSender().path().name());
            this.hold = true;
        } else if (message instanceof LaunchMsg) {
            if (this.hold){
                return;
            }
            this.log.info("p" + self().path().name() + " received LAUNCH.");
            int v = (new Random()).nextInt(2);
            this.propose(v);
        } else if (message instanceof CrashMsg) {
            this.log.info("p" + self().path().name() + " received CRASH.");
            this.faultProneMode = true;
        } else if (message instanceof OfconsProposerMsg) {
            OfconsProposerMsg m = (OfconsProposerMsg) message;
            this.propose(m.getV());
        } else if (message instanceof ReadMsg) {
            this.log.info("p" + self().path().name() + " received READ");
            ReadMsg m = (ReadMsg) message;
            this.readReceived(m.getBallot(), getSender());
        } else if (message instanceof AbortMsg) {
            this.log.info("p"+self().path().name()+" received ABORT from p" + getSender().path().name() );
            AbortMsg m = (AbortMsg) message;
            this.abortReceived(m.getBallot());
        } else if (message instanceof GatherMsg) {
            this.log.info("p"+self().path().name()+" received GATHER from p" + getSender().path().name() );
            GatherMsg m = (GatherMsg) message;
            this.gatherReceived(m.getBallot(), m.getImposeBallot(), m.getEstimate(), getSender());
        } else if (message instanceof ImposeMsg){
            this.log.info("p"+self().path().name()+" received IMPOSE from p" + getSender().path().name() );
            ImposeMsg m = (ImposeMsg) message;
            this.imposeReceived(m.ballot, m.proposal, getSender());
        } else if (message instanceof AckMsg){
            this.log.info("p"+self().path().name()+" received ACK from p" + getSender().path().name() );
            AckMsg m = (AckMsg) message;
            this.ackReceived(m.ballot);
        } else if (message instanceof DecideMsg){
            this.log.info("p"+self().path().name()+" received DECIDE from p" + getSender().path().name() );
            DecideMsg m = (DecideMsg) message;
            this.decideReceived(m.v);
        }
    }

    class State {
        private Integer est;
        private int estballot;

        public State(Integer est, int estballot) {
            this.est = est;
            this.estballot = estballot;
        }

        public Integer getEst(){return this.est;}

        public int getEstballot(){return this.estballot;}
    }
}
