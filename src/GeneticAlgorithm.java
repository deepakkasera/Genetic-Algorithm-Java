/**
 * Created by deepak on 27/4/18.
 */
import com.sun.org.apache.bcel.internal.generic.POP;

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
        for(int i=0;i<-1;++i)
        {
            Circle temp = new Circle(circles[i]);
            System.out.println("x = "+Integer.toString(temp.x)+
                                " |y = "+Integer.toString(temp.y)+
                                " |radius = "+Integer.toString(temp.radius) + " " +
                                " |red= "+ Integer.toString(temp.red) +
                                " |green = "+ Integer.toString(temp.green)+
                                " |blue= "+Integer.toString(temp.blue)+
                                " |alpha= "+Float.toString(temp.alpha)+" " +
                                " |time= "+Integer.toString(temp.time));
        }
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


    public static void  convertColorArrayToImage(int[][] color,int generaion,String imageName,String directory){
        String current = imageName;
        String path = directory + current + "-" + Integer.toString(generaion) + ".jpg";
        BufferedImage image = new BufferedImage(color[0].length, color.length, BufferedImage.TYPE_INT_RGB);

        for(int i = 0; i < color.length; i++){
            for(int j = 0; j < color[0].length; j++){
                image.setRGB(i, j, color[i][j]);
            }
        }

        File ImageFile = new File(path);
        try {
            ImageIO.write(image, "jpg", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Initial Population initialize function
    public static void initialize(){
        population = new Genotype[POPSIZE + 1];
        newpopulation = new Genotype[POPSIZE + 1];

        Random randomGenerator = new Random();

        for(int i = 0; i <= POPSIZE; i++){
            population[i] = new Genotype();
            newpopulation[i] = new Genotype();

            population[i].count = circleCount;
            for(int j = 0; j < circleCount; j++){
                population[i].circles[j].x = new Integer(randomGenerator.nextInt(row));
                population[i].circles[j].y = new Integer(randomGenerator.nextInt(col));
                population[i].circles[j].blue = new Integer(randomGenerator.nextInt(255));
                population[i].circles[j].red = new Integer(randomGenerator.nextInt(255));
                population[i].circles[j].green = new Integer(randomGenerator.nextInt(255));
                population[i].circles[j].alpha = new Integer(randomGenerator.nextInt(255));
                population[i].circles[j].radius = new Integer(randomGenerator.nextInt(RADIUSLIMIT));
                population[i].circles[j].time = new Integer(randomGenerator.nextInt(10000));

            }

            population[i].bg_red = 255;
            population[i].bg_blue = 255;
            population[i].bg_green = 255;
            population[i].bg_alpha = 0;
        }
    }

    public static void keep_the_best(){
        int curr_best = 0;

        for(int i = 1; i <= POPSIZE; i++){
            if(population[i].fitness > population[curr_best].fitness){
                curr_best = i;
            }
        }

        population[POPSIZE] = new Genotype(population[curr_best]);
    }


    public static void evaluate(){
        for(int member = 0; member < POPSIZE; member++){
            population[member].getFitness(result);
        }
    }


    public static void elitist(){
        double best;
        int best_member = 0;
        double worst;
        int worst_member = 0;

        best = population[0].fitness;
        worst = population[0].fitness;

        for(int i = 0; i < POPSIZE; i++){
            if(population[i].fitness > best){
                best = population[i].fitness;
                best_member = i;
            }
            if(population[i].fitness < worst){
                worst = population[i].fitness;
                worst_member = i;
            }
        }

        /* If the best individual from the new population is better than
           the best individual from the previous population then copy the
           best individual from the new population; else replace the worst
           individual from the current population with the best one from
           the previous generation
         */

        if(best >= population[POPSIZE].fitness){
            population[POPSIZE] = new Genotype(population[best_member]);
        }
        else{
            population[worst_member] = new Genotype(population[POPSIZE]);
        }
    }


    public static void selector(){

        //find the total fitness of the population
        double sum = 0.0;
        for(int member = 0; member < POPSIZE; member++){
            sum = sum + population[member].fitness;
        }

        //find the relative fitness
        for(int member = 0; member < POPSIZE; member++){
            population[member].rfitness = population[member].fitness/sum;
        }

        //Find the cummulative fitness
        population[0].cfitness = population[0].rfitness;
        for(int member = 1; member < POPSIZE; member++){
            population[member].cfitness = population[member-1].cfitness + population[member].rfitness;
        }

        // Select survivors using cumulative fitness.

        for(int i = 0; i < POPSIZE; i++){
            Random randomGenerator = new Random();
            double p = (randomGenerator.nextInt(1000)) / 1000.0;
            if(p < population[0].cfitness){
                newpopulation[i] = new Genotype(population[0]);
            }
            else{
                for(int j = 0; j < POPSIZE; j++){
                    if(p >= population[j].fitness && p < population[j+1].fitness){
                        newpopulation[i] = new Genotype(population[j+1]);
                        break;
                    }
                }
            }
        }

        //	Once a new population is created, copy it back
        //
        // System.out.println("New pop");
        // for(i = 0; i < POPSIZE; i++) {
        // 	System.out.println(newpopulation[i].fitness);
        // 	population[i] = new Genotype(newpopulation[i]);
        // }
    }


//    public static void convertColorArrayToImage(){
//        File ImageFile = new File();
//    }


    public static void main(String[] args) throws IOException {
        PrintWriter printWriter = new PrintWriter(args[1] + "info.txt", "UTF-8");

        printWriter.println("First line");
        printWriter.println("POPSIZE=" + Integer.toString(POPSIZE));
        printWriter.println("MAXGENS=" + Integer.toString(MAXGENS));
        printWriter.println("circleCount=" + Integer.toString(circleCount));
        printWriter.println("RADIUSLIMIT=" + Integer.toString(RADIUSLIMIT));
        printWriter.println("PXOVER=" + Double.toString(PXOVER));
        printWriter.println("PMUTATION=" + Double.toString(PMUTATION));
        printWriter.println("PMUTATIONCOLOR=" + Double.toString(PMUTATIONCOLOR));

        printWriter.close();


        BufferedImage image = ImageIO.read(GeneticAlgorithm.class.getResource(args[0] + ".jpg"));
        result = convertTo2D(image);
        row = result.length;
        col = result[0].length;
        Genotype best = null;


        //Iniatilazation of population
        initialize();

        System.out.println("After Initialization");
        best = new Genotype(population[POPSIZE]);
        best.Print();

        //evaluation of fitness of members
        evaluate();
        System.out.println("After evaluate");
        best = new Genotype(population[POPSIZE]);
        best.Print();

        keep_the_best();
        System.out.println("After keep_the_best");
        best = new Genotype(population[POPSIZE]);
        best.Print();

        double old_fitness = -1;
        for(int generations = 0; generations < MAXGENS; generations++){
            selector();
            
        }

    }
}
