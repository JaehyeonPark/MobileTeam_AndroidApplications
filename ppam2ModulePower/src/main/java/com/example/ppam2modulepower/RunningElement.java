package com.example.ppam2modulepower;

import java.io.Serializable;


class RunningElement implements Serializable {
	private int avgPower;
	private String partName;
	private SensorState partState;

	public int getAvgPower() {
		return avgPower;
	}

	public void setAvgPower(int avgPower) {
		this.avgPower = avgPower;
	}

	public SensorState getPartState() {
		return partState;
	}

	public void setPartState(SensorState partState) {
		this.partState = partState;
	}

	public RunningElement() {
		// TODO Auto-generated constructor stub
		partState = new SensorState();
		avgPower=0;
		partName="";
	}

	public RunningElement(RunningElement lastOne) {
		// TODO Auto-generated constructor stub
		partState = new SensorState();
		partState.copyState(lastOne.partState);
		avgPower=0;
		partName="";
	}
	
	void setPartName (){
		partName=partState.makeStateName();
	}
	
	String getPartName (){
		return partName;
	}

	@Override	
	public String toString(){
		setPartName ();
		return (partName + avgPower + "mW");
	}
	
	public String stateToString(){
		String accIsOperating = "";
		String gpsIsOperating = "";
		String networkIsOperating = "";
		String gpsMode="";
		String networkMode="";
		String accMode="";
		
		if(this.getPartState().getAcc().isOperating){
			accIsOperating = ",TRUE";
		} else
		{
			accIsOperating = ",FALSE";
		}

		if(this.getPartState().getGps().isOperating){
			gpsIsOperating = ",TRUE";
		} else
		{
			gpsIsOperating = ",FALSE";
		}

		if(this.getPartState().getNetwork().isOperating){
			networkIsOperating = ",TRUE";
		} else
		{
			networkIsOperating = ",FALSE";
		}
		
		switch(this.getPartState().getAcc().getMode())
		{
		case 0: accMode = ",FASTEST"; break;
		case 1: accMode = ",GAME"; break;
		case 2: accMode = ",UI"; break;
		case 3: accMode = ",NORMAL"; break;
		}
		
		switch(this.getPartState().getGps().getState())
		{
		case 0: gpsMode += ",GPS_NOT_SETTED"; break;
		case 1: gpsMode += ",GPS_EVENT_STARTED"; break;
		case 2: gpsMode += ",GPS_EVENT_STOPPED"; break;
		case 3: gpsMode += ",GPS_EVENT_FIRST_FIX"; break;
		case 4: gpsMode += ",GPS_EVENT_SATELLITE_STATUS"; break;
		}
		
		gpsMode += ",Dis_" + getPartState().getGps().disFreq + ",Time_" + getPartState().getGps().timeFreq;
		networkMode += ",Dis_" + getPartState().getNetwork().disFreq + ",Time_" + getPartState().getNetwork().timeFreq;
		
		return ("ACC" + accIsOperating + accMode + "\n" + 
				"GPS" + gpsIsOperating + gpsMode + "\n" +
				"Network" + networkIsOperating + networkMode + "\n" );
	}
}

class SensorState implements Serializable {
	AccState acc;
	GpsState gps;
	NetworkState network;
	public AccState getAcc() {
		return acc;
	}
	public void setAcc(AccState acc) {
		this.acc = acc;
	}
	public void setGps(GpsState gps) {
		this.gps = gps;
	}
	public GpsState getGps() {
		return gps;
	}
	public void setNetwork(NetworkState network) {
		this.network = network;
	}
	public NetworkState getNetwork() {
		return network;
	}
	public SensorState() {
		// TODO Auto-generated constructor stub
		acc = new AccState();
		gps = new GpsState();
		network = new NetworkState();
	}
	void copyState(SensorState copyOne){
		acc.copyState(copyOne.acc);
		gps.copyState(copyOne.gps);
		network.copyState(copyOne.network);
	}
	String makeStateName (){
		String temp= "";
		if(acc.getIsOperating())
			temp += ("acc_mode"+acc.getMode()+"\n");
		if(gps.getIsOperating())
			temp += ("gps_mode"+"_state"+gps.getState()+"_dist"+gps.getDisFreq()+"_time"+gps.getTimeFreq() + "\n");
		if(network.getIsOperating())
			temp += ("network_mode"+"_dist"+network.getDisFreq()+"_time"+network.getTimeFreq() + "\n");
//			temp += ("network_mode"+"_state"+network.getState()+"_dist"+network.getDisFreq()+"_time"+network.getTimeFreq() + "\n");
		return temp;
	}
	public boolean anyoneOperating (){
		if(acc.getIsOperating()||gps.getIsOperating()||network.getIsOperating())
			return true;
		else 
			return false;
	}
}

class AccState implements Serializable {
	boolean isOperating;
	int mode;
	
	public boolean getIsOperating() {
		return isOperating;
	}

	public void setIsOperating(boolean isOperating) {
		this.isOperating = isOperating;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public AccState() {
		// TODO Auto-generated constructor stub
		this.isOperating=false;
		this.mode=-1;
	}
	
	public boolean isDiff (AccState diffOne){
		if(	this.isOperating!=diffOne.isOperating ||
			this.mode!=diffOne.mode)
			return true;
		else
			return false;
	}
	
	public void copyState(AccState copyOne){
		this.isOperating = copyOne.isOperating;
		this.mode = copyOne.mode;
	}
}

class GpsState implements Serializable {
	boolean isOperating;
	int state;
	int timeFreq;
	int disFreq;
	
	public boolean getIsOperating() {
		return isOperating;
	}

	public void setIsOperating(boolean isOperating) {
		this.isOperating = isOperating;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getTimeFreq() {
		return timeFreq;
	}

	public void setTimeFreq(int timeFreq) {
		this.timeFreq = timeFreq;
	}

	public int getDisFreq() {
		return disFreq;
	}

	public void setDisFreq(int disFreq) {
		this.disFreq = disFreq;
	}

	public GpsState() {
		// TODO Auto-generated constructor stub
		this.isOperating=false;
		this.state=-1;
		this.timeFreq=0;
		this.disFreq=0;
	}
	
	public boolean isDiff (GpsState diffOne){
		if(	this.isOperating    != diffOne.isOperating	||
			this.state   		!= diffOne.state		||
			this.timeFreq		!= diffOne.timeFreq 	||
			this.disFreq 		!= diffOne.disFreq		)
			return true;
		else
			return false;
	}
	
	void copyState(GpsState copyOne){
		this.isOperating = copyOne.isOperating;
		this.state = copyOne.state;
		this.timeFreq = copyOne.timeFreq;
		this.disFreq = copyOne.disFreq;
	}
}

class NetworkState implements Serializable {
	boolean isOperating;
	int state;
	int timeFreq;
	int disFreq;
	
	public boolean getIsOperating() {
		return isOperating;
	}

	public void setIsOperating(boolean isOperating) {
		this.isOperating = isOperating;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getTimeFreq() {
		return timeFreq;
	}

	public void setTimeFreq(int timeFreq) {
		this.timeFreq = timeFreq;
	}

	public int getDisFreq() {
		return disFreq;
	}

	public void setDisFreq(int disFreq) {
		this.disFreq = disFreq;
	}

	public NetworkState() {
		// TODO Auto-generated constructor stub
		this.isOperating=false;
		this.state=-1;
		this.timeFreq=0;
		this.disFreq=0;
	}
	
	public boolean isDiff (NetworkState diffOne){
		if(	this.isOperating    != diffOne.isOperating	||
			this.state   		!= diffOne.state		||
			this.timeFreq		!= diffOne.timeFreq 	||
			this.disFreq 		!= diffOne.disFreq		)
			return true;
		else
			return false;
	}
	
	void copyState(NetworkState copyOne){
		this.isOperating = copyOne.isOperating;
		this.state = copyOne.state;
		this.timeFreq = copyOne.timeFreq;
		this.disFreq = copyOne.disFreq;
	}
}