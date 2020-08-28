package com.quintonvr.tjoptjop.machinevision;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;

import java.util.Comparator;

public class Digit implements Comparable<Digit>{
    private MatOfPoint contour;
    private Rect boundingBox;
    public Digit(MatOfPoint c, Rect box){
        contour = c;
        boundingBox = box;
    }

    MatOfPoint getContour(){
        return contour;
    }

    Rect getBoundingBox(){
        return boundingBox;
    }

    @Override
    public int compareTo(Digit o) {
        if (boundingBox.x > o.boundingBox.x){
            return 1;
        }else if (boundingBox.x == o.boundingBox.x){
            return 0;
        }else{
            return -1;
        }
    }
}
