/**
 * Created by deepak on 27/4/18.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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

    public Circle() {

    }

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

        Iterator it = treeMap.entrySet().iterator();
        double color_red = bg_red, color_green = bg_green, color_blue = bg_blue, color_alpha = bg_alpha;

        while(it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            Circle circle = (Circle) entry.getValue();
            double t = (double) (circle.alpha / 255.0);
            double new_alpha = (double) (t + (1.0 - t) * color_alpha);

            color_red = (t*circle.red + (1.0 - t) * color_red * color_alpha)/new_alpha;
            color_green = (t*circle.green + (1.0-t)*color_green*color_alpha)/new_alpha;
            color_blue = (t*circle.blue + (1.0-t)*color_blue*color_alpha)/new_alpha;
            color_alpha = new_alpha;
        }

        Color color = new Color((int)color_red, (int)color_green, (int)color_blue, (int)(255*color_alpha) );

        return color;
    }


    public int idnt(int diff) {
        if (diff == 0)
            return 1;
        return 0;
    }

    public double getFitness(int[][] result){
        double ans = 0;
        for(int i = 0; i < result.length; i++){
            for(int j = 0; j < result[0].length; j++){
                Color m = getColorOfPoint(i,j);

                Color n = new Color(result[i][j]);

                ans += ( Math.pow(2,abs(m.getRed() - n.getRed())/255.0) +
                         Math.pow(2,abs(m.getBlue() - n.getBlue())/255.0) +
                         Math.pow(2,abs(m.getGreen() - n.getGreen())/255.0) );
            }
        }
        this.fitness = 100000.0 / ans;
        return this.fitness;
    }

    public void Print(){

    }
}

public class GeneticAlgorithm {


    public static int POPSIZE = 15;             // Population size
    public static int MAXGENS = 150000;         // number of generations
    public static double PXOVER = 0.8;          //Probability of Crossover
    public static double PMUTATION = 0.1;       //Probability of Mutation
    public static Genotype population[];
    public static Genotype newpopulation[];
    public static int row, col;
    public static int circleCount = 50;         // Number of circles a member of a population can have
    public static int[][] result;
    public static int RADIUSLIMIT=175;
    public static double PMUTATIONCOLOR = .2;

    //Function to convert an image to 2D matrix
    //source for writing this function :- https://stackoverflow.com/a/17175454
    private static int[][] convertTo2D(BufferedImage image) {
        final byte[] pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;
        int[][] result = new int[height][width];

        //getAlphaRaster() function returns null if if there is no alpha channel associated with the ColorModel in this image
        //getAlphaRaster() -> a WritableRaster or null if this BufferedImage has no alpha channel associated with its ColorModel.
        if(image.getAlphaRaster() == null){
            final int pixelLength = 3;
            for(int pixel = 0, row = 0, col = 0; pixelLength < pixels.length; pixel += pixelLength){
                int argument = 0;
                argument += ((int)pixels[pixel + 1] & 0xff);            //blue
                argument += (((int)pixels[pixel + 2] & 0xff ) << 8);    //green
                argument += (((int)pixels[pixel + 3] & 0xff ) << 16);   //red
                result[row][col] = argument;
                col++;
                if(col == width){
                    row++;
                    col = 0;
                }
            }
        }
        else{
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argument = 0;
                argument += ((int) pixels[pixel] & 0xff);				   // blue
                argument += (((int) pixels[pixel + 1] & 0xff) << 8);		// green
                argument += (((int) pixels[pixel + 2] & 0xff) << 16);	   // red
                result[row][col] = argument;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }
//        return;
        return result;
    }

    

//    public static void convertColorArrayToImage(){
//        File ImageFile = new File();
//    }

}
