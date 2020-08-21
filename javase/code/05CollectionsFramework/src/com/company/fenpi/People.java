package com.company.fenpi;

public class People {
	private int id;
	private String name;
	private double inCome;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getInCome() {
		return inCome;
	}
	public void setInCome(double inCome) {
		this.inCome = inCome;
	}
	@Override
    public String toString() {
        return "People{" +"id=" + id +", name='"
                + name + '\'' +", inCome=" + inCome +'}';
    }


}
