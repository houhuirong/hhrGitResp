package com.company.quchong;

/**
 * @Auther: hhr
 * @Date: 2020/8/20 - 08 - 20 - 17:20
 * @Description: com.company.quchong
 * @version: 1.0
 */
public class  U {
    private  Integer id;
    private  String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**必须重写hashcode 和 equals 方法 */
    @Override
    public int hashCode() {

        return name.hashCode(); // 重写此属性值的hashcode，不然就是判断这个对象的hashcode
    }


    @Override
    public boolean equals(Object obj) {

        if (obj instanceof  U){
            U o = (U)obj;
            if (o.getName().equals(name)){ //根据属性值进行判断
                return true;
            }
        }

        return super.equals(obj);
    }
}
