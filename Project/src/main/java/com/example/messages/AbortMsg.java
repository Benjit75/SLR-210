package com.example.messages;

public class AbortMsg {
    private final int ballot;

    public AbortMsg(int ballot) {
        this.ballot = ballot;
    }

    public int getBallot() {
        return this.ballot;
    }

    @Override
    public String toString() {
        return "AbortMsg{" +
                "ballot=" + ballot +
                '}';
    }
}
