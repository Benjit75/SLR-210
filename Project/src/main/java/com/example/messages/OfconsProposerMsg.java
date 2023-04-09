package com.example.messages;

public class OfconsProposerMsg {

	private final int v;

	// v is the value to be proposed (in this project, v is 0 or 1, therefore an integer)
	public OfconsProposerMsg(int v) {
		this.v = v;
	}

	public int getV() {
		return this.v;
	}

	@Override
	public String toString() {
		return "OfconsProposerMsg{" +
				"v=" + this.v +
				'}';
	}
}
