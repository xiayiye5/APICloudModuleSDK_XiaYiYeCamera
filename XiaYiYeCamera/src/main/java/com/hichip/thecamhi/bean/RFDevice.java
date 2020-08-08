package com.hichip.thecamhi.bean;


import java.io.Serializable;

@SuppressWarnings("serial")
public class RFDevice implements Serializable {
	public  String name;
	public  String type;
	public  String code;
	public  int    u32Index;
	public int enable;
    
	public RFDevice(String name, String type, String code,int u32Index,int enable) {
		super();
		this.name = name;
		this.type = type;
		this.code = code;
		this.u32Index = u32Index;
		this.enable=enable;
	}
	



	public int getEnable() {
		return enable;
	}




	public void setEnable(int enable) {
		this.enable = enable;
	}




	public int getU32Index() {
		return u32Index;
	}


	public void setU32Index(int u32Index) {
		this.u32Index = u32Index;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
    
    



}
