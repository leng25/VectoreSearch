package org.example.HNSW;

public class NeighbourArray{
    int[] neighbours; // neighbor ids
    float[] score; //similarity scores
    int size; // how many are filled
    int maxConnection;

    public NeighbourArray(int maxConnectionPerNode){
        this.neighbours = new int[maxConnectionPerNode];
        this.score = new float[maxConnectionPerNode];
        this.size = 0;
        this.maxConnection = maxConnectionPerNode;
    }

    public void addNeighbour(int nodeId, float score){
        if(size < maxConnection){
            System.out.println("adding neighbour: " + nodeId);
            neighbours[size] = nodeId;
            size++;
            return;
        }
        System.out.println("size is greater than maxConnetion neighbour was not added");
    }
}
