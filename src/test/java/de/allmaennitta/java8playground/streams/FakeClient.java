package de.allmaennitta.java8playground.streams;

import java.io.Serializable;

public class FakeClient implements Serializable {
	private static final long serialVersionUID = -6358742378177948329L;

	private int waitTime;
	private double purchases;
	
	public FakeClient() {}
	
	public FakeClient(int waitTime, double purchases) {
		this.waitTime = waitTime;
		this.purchases = purchases;
	}

	public int getWaitTime() {
		return waitTime;
	}
	
	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public double getPurchases() {
	    long waitTime = (long) this.waitTime;
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Sleep went wrong");
		}
		return purchases;
	}

	public void setPurchases(double purchases) {
		this.purchases = purchases;
	}
}
