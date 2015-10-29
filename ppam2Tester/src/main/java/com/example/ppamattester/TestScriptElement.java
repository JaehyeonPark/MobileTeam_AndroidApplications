package com.example.ppamattester;

import java.io.Serializable;

class TestScriptElement implements Serializable {
	private int dueTime=1;
	private String ElementName="Default";
	
	public TestScriptElement (){
	}
	public TestScriptElement (int time){
		setDueTime(time);
	}
	public String getElementName() {
		return ElementName;
	}

	public void setElementName(String elementName) {
		ElementName = elementName;
	}

	public int getDueTime() {
		return dueTime;
	}

	public void setDueTime(int dueTime) {
		this.dueTime = dueTime;
	}

	@Override
	public String toString() {
		return 	getElementName()+" "+
				"dueTime("+getDueTime()+")";
	}
}

class NewElement extends TestScriptElement {
	public NewElement() {
		// TODO Auto-generated constructor stub
		this.setElementName("Newfile");
	}
}

class StrElement extends TestScriptElement {
	public StrElement() {
		// TODO Auto-generated constructor stub
		this.setElementName("Start");
	}
}

class EndElement extends TestScriptElement {
	public EndElement() {
		// TODO Auto-generated constructor stub
		this.setElementName("End");
	}
}

class AccElement extends TestScriptElement {

	private int mode;

	public AccElement(int dueTime, int mode) {
		// TODO Auto-generated constructor stub
		this.setElementName("Accelerometer");
		this.setMode(mode);
		this.setDueTime(dueTime);
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public String toString() {
		return 	getElementName()+" "+
				"mode("+getMode()+") "+
				"dueTime("+getDueTime()+")";
	}
}

class AccEndElement extends TestScriptElement {
	public AccEndElement() {
		// TODO Auto-generated constructor stub
		this.setElementName("AccEnd");
	}
	public AccEndElement(int time) {
		// TODO Auto-generated constructor stub
		this.setDueTime(time);
		this.setElementName("AccEnd");
	}
}

class LightElement extends TestScriptElement {

	private int mode;

	public LightElement(int dueTime, int mode) {
		// TODO Auto-generated constructor stub
		this.setElementName("Illumination");
		this.setMode(mode);
		this.setDueTime(dueTime);
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public String toString() {
		return 	getElementName()+" "+
				"mode("+getMode()+") "+
				"dueTime("+getDueTime()+")";
	}
}

class LightEndElement extends TestScriptElement {
	public LightEndElement() {
		// TODO Auto-generated constructor stub
		this.setElementName("LightEnd");
	}
	public LightEndElement(int time) {
		// TODO Auto-generated constructor stub
		this.setDueTime(time);
		this.setElementName("LightEnd");
	}
}

class GpsElement extends TestScriptElement {

	private int minTime;
	private int minDistance;
	
	public GpsElement(int dueTime, int minTime, int minDistance) {
		// TODO Auto-generated constructor stub
		this.setElementName("GPS");
		this.setDueTime(dueTime);
		this.setMinTime(minTime);
		this.setMinDistance(minDistance);
	}

	public int getMinTime() {
		return minTime;
	}

	public void setMinTime(int minTime) {
		this.minTime = minTime;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(int minDistance) {
		this.minDistance = minDistance;
	}

	@Override
	public String toString() {
		return 	getElementName()+" freq("+
				getMinTime()+"_"+getMinDistance()+") "+
				"dueTime("+getDueTime()+")";
	}
}

class GpsEndElement extends TestScriptElement {
	public GpsEndElement() {
		// TODO Auto-generated constructor stub
		this.setElementName("GPSEnd");
	}
	public GpsEndElement(int time) {
		// TODO Auto-generated constructor stub
		this.setDueTime(time);
		this.setElementName("GPSEnd");
	}
}

class NetworkElement extends TestScriptElement {

	private int minTime;
	private int minDistance;
	
	public NetworkElement(int dueTime, int minTime, int minDistance) {
		// TODO Auto-generated constructor stub
		this.setElementName("Network");
		this.setDueTime(dueTime);
		this.setMinTime(minTime);
		this.setMinDistance(minDistance);
	}

	public int getMinTime() {
		return minTime;
	}

	public void setMinTime(int minTime) {
		this.minTime = minTime;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(int minDistance) {
		this.minDistance = minDistance;
	}

	@Override
	public String toString() {
		return 	getElementName()+" freq("+
				getMinTime()+"_"+getMinDistance()+") "+
				"dueTime("+getDueTime()+")";
	}
}

class NetworkEndElement extends TestScriptElement {
	public NetworkEndElement() {
		// TODO Auto-generated constructor stub
		this.setElementName("NetworkEnd");
	}
	public NetworkEndElement(int time) {
		// TODO Auto-generated constructor stub
		this.setDueTime(time);
		this.setElementName("NetworkEnd");
	}
}