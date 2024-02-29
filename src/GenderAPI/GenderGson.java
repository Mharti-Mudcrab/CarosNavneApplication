/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GenderAPI;


class GenderGson {
    private String name;
    private String gender;
    private String error;
    private double probability;
    private int count;

    public GenderGson(String name, String gender, String error, double probability, int count) {
        this.name = name;
        this.gender = gender;
        this.probability = probability;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }
    
    public String getError() {
        return error;
    }

    public double getProbability() {
        return probability;
    }

    public int getCount() {
        return count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public void setError(String error) {
        this.error = error;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
