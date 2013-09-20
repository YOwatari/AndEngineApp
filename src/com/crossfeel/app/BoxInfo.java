package com.crossfeel.app;

public class BoxInfo {
	public int number;
	public String role;
	public boolean player;
	public boolean turn;
	public boolean playerChecked;
	
	public BoxInfo(int n, String r) {
		this.number = n;
		this.role = r;
		this.player = false;
		this.turn = false;
		this.playerChecked = false;
	}
	
}
