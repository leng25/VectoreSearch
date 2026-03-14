package org.example.HNSW;

public class NeighbourArray{
    int[] neighbours; // neighbor ids
    float[] score; //similarity scores
    int size; // how many are filled

    public NeighbourArray(int maxConnectionPerNode){
        this.neighbours = new int[maxConnectionPerNode];
        this.score = new float[maxConnectionPerNode];
        this.size = 0;
    }
}
