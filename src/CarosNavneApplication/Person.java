/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CarosNavneApplication;

/**
 *
 * @author madsw
 */
public class Person {
    
    private String name;
    private String sex;
    private int firstIndex;

    public Person(String name, String sex) {
        this.name = name;
        this.sex = sex;
    }

    public Person(String name, String sex, int firstIndex) {
        this.name = name;
        this.sex = sex;
        this.firstIndex = firstIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    @Override
    public String toString() {
        return name + ", " + sex + ", " + (firstIndex == 0 ? "Null" : firstIndex);
    }

    protected Person clone() {
        return new Person(this.name, this.sex, this.firstIndex);
    }
    
    
}
