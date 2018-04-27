/**
 * Created by deepak on 27/4/18.
 */
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;

class Circle{
    // x and y are the coordinates of center of the circle
    public int x;
    public int y;
    public int radius;                  // Radius of the circle
    public int red,green,blue;          //RGB values for the color of the circle
    public int alpha;                   //Alpha value of the color(Opacity and transparency factor)
    public int time;                    //the time when this circle was added to the member to which it belongs

    public boolean isPointInside(int i,int j){
        return ((this.x - i) * (this.x - i) - (this.y - j) * (this.y - j) <= this.radius * this.radius);
    }

    //Constructor for the circle class
    public Circle(Circle other){
        this.x = other.x ;
        this.y = other.y ;
        this.red = other.red ;
        this.green = other.green ;
        this.blue = other.blue ;
        this.alpha = other.alpha ;
        this.time = other.time ;
        this.radius = other.radius;
    }

}

class Genotype{
    public Circle circles[] = new Circle[GeneticAlgorithm.circleCount];     //each member of the population contains a number of circles
    public int count;										// stores the number of circles in the present member of population
    public int bg_red, bg_green, bg_blue;					// the RGB values of the background
    public int bg_alpha;									// the alpha value of the background
    public double fitness;
    public double cfitness;
    public double rfitness;


    public Genotype(){
        for(int i = 0; i < circles.length; i++){
            this.circles[i] = new Circle();
        }
    }

    public Genotype(Genotype other){
        for(int i = 0; i < circles.length; i++){
            this.circles[i] = new Circle(other.circles[i]);
        }
        this.count = other.count;
        this.bg_red = other.bg_red ;
        this.bg_green = other.bg_green ;
        this.bg_blue = other.bg_blue ;
        this.bg_alpha = other.bg_alpha ;
        this.fitness = other.fitness ;
        this.cfitness = other.cfitness ;
        this.rfitness = other.rfitness ;
    }

    public int abs(int difference){
        if(difference < 0) return -1*difference;
        return difference;
    }

    public Color getColorOfPoint(int i,int j){
        TreeMap<Integer, Circle> treeMap = new TreeMap<Integer, Circle> ();
//        TreeMap <Integer, Circle> treemap = new TreeMap <Integer, Circle> ();
        for(int k = 0; k < circles.length; k++){
            if(circles[k].isPointInside(i,j)){
                treeMap.put(circles[k].time, circles[k]);
            }
        }

    }
}

public class GeneticAlgorithm {
}
