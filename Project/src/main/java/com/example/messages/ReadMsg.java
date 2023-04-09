package com.example.messages;

public class ReadMsg {
    private final int ballot;

    public ReadMsg(int ballot) {
        this.ballot = ballot;
    }

    public int getBallot() {
        return this.ballot;
    }

    @Override
    public String toString() {
        return "ReadMsg{" +
                "ballot=" + ballot +
                '}';
    }
}
