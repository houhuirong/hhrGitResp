package com.company.quchong;

import java.util.Objects;

/**
 * @Auther: hhr
 * @Date: 2020/8/20 - 08 - 20 - 10:21
 * @Description: com.company.quchong
 * @version: 1.0
 */
public class Person {
    private String name;
    private String phoenNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(getName(), person.getName()) &&
                Objects.equals(getPhoenNumber(), person.getPhoenNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getPhoenNumber());
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", phoenNumber='" + phoenNumber + '\'' +
                '}';
    }

    public Person(String name, String phoenNumber) {
        this.name = name;
        this.phoenNumber = phoenNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoenNumber() {
        return phoenNumber;
    }

    public void setPhoenNumber(String phoenNumber) {
        this.phoenNumber = phoenNumber;
    }
}
