/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mareteneedlemanwunsch;

/**
 *
 * @author hwikgren
 */
public class Node {

    private int i;
    private int j;
    private int points;
    private String direction;
    private String previousMdc;
    private String previousTrans;
    private int previousPoints;
    
    
    public Node(int i, int j) {
        this.i = i;
        this.j = j;
        //this.points = points;
    }

    public Node() {
    }

    public Node(int points, String direction) {
        this.points = points;
        this.direction = direction;
    }

    public Node(int i, int j, String direction) {
        this.i = i;
        this.j = j;
        //this.points = points;
        this.direction = direction;
    }

    public void setNode(int i, int j) {
        this.i = i;
        this.j = j;
        //this.points = points;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setPreviousPoints(int previousPoints) {
        this.previousPoints = previousPoints;
    }

    public void setPreviousString(String mdc, String trans) {
        this.previousMdc = mdc;
        this.previousTrans = trans;
    }

    public int getPoints() {
        return points;
    }

    public String getDirection() {
        return direction;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getPreviousPoints() {
        return previousPoints;
    }

    public String getPreviousMdc() {
        return previousMdc;
    }

    public String getPreviousTrans() {
        return previousTrans;
    }
    
    
}
