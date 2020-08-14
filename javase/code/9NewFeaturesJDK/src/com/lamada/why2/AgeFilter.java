package com.lamada.why2;

import com.lamada.why.Student;

/**
 * @author: 马士兵教育
 * @create: 2019-10-13 16:40
 */
public class AgeFilter implements StudentFilter {
    @Override
    public boolean compare(Student student) {
        return student.getAge()>14;
    }
}
