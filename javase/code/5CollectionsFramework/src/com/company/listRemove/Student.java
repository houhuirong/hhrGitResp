package com.company.listRemove;

/**
 * @Auther: hhr
 * @Date: 2020/7/29 - 07 - 29 - 10:25
 * @Description: com.company.listRemove
 * @version: 1.0
 */
public class Student {
    private int id;
    private String stuNo;
    private String name;

    public Student(int id, String stuNo, String name) {
        this.id = id;
        this.stuNo = stuNo;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", stuNo='" + stuNo + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStuNo() {
        return stuNo;
    }

    public void setStuNo(String stuNo) {
        this.stuNo = stuNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
