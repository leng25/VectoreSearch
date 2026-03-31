package org.example.HNSW;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

public class HnswGraph {

    float[][] vectors = new float[150][];
    NeighbourArray[][] graph = new NeighbourArray[150][4];
    int entryNode = -1;
    int maxLayer = -1;
    int counter = -1;

    final int maxConnectionPerNode;
    final int efConstruction;

    public HnswGraph(int maxConnectionPerNode, int efConstruction){
        this.maxConnectionPerNode = maxConnectionPerNode;
        this.efConstruction = efConstruction;
    }

    public void insertNode(float[] vectors){

        counter++;
        int nodeId = counter;
        int level = randomLevel();

        this.vectors[nodeId] = vectors;

        // first node oh new leve, mark node as Entry node and update the leve
        if (entryNode == -1 || maxLayer < level){
            entryNode = nodeId;
            maxLayer = level;
            // adding NeighbourArray for the nodeId at each level
            // because this if the first node it will by added with empty neighbours
            for (int l = level; l >= 0; l--) {
                graph[nodeId][l] = new NeighbourArray(l == 0 ? maxConnectionPerNode * 2: maxConnectionPerNode);
            }
        }
        else {
            // Here we search for the beast candidates at each level using greedySearch
            // then we add those canidates to this new Node NeighboursArray
            // finnaly we add the new node back into each of those candidates creating a bidireaction relationship
            for(int l = level; l>=0; l--){
                Queue<float[]> beastCandidates = greedySearch(entryNode, nodeId, l, efConstruction);
                NeighbourArray neighbourArray = new NeighbourArray(l == 0 ? maxConnectionPerNode * 2: maxConnectionPerNode);
                while(!beastCandidates.isEmpty()){
                    float[] candidateFloat = beastCandidates.poll();
                    int candidateId = (int) candidateFloat[0];
                    float score = (float) candidateFloat[1];
                    neighbourArray.addNeighbour(candidateId, score);
                    graph[candidateId][l].addNeighbour(nodeId, score);
                }
                graph[nodeId][l] = neighbourArray;
            }
        }
    }

    private Queue<float[]> greedySearch(
            int startedNode,int queryNode, int level, int querySize){
        Queue<float[]> results = new PriorityQueue<>(Comparator.comparing((float[] a) -> a[1]));
        Queue<float[]> candidates = new PriorityQueue<>(Comparator.comparing((float[] a) -> a[1]).reversed());
        System.out.println("START SEARCHING on level: " + level + " Query NodeID: " + queryNode);
        int currentNode = startedNode;
        int[] usedNodes = new int[150];
        while (true) {
            // calculating and adding node to result if applicable
            usedNodes[currentNode] = 1;
            System.out.println("Searching from NodeID: " + currentNode);
            float score = cosineSimilarity(vectors[currentNode], vectors[queryNode]); // not sure who goes first here
            System.out.println("Vectore calculation: currentNodeId: " + currentNode + " score: " + vectors[currentNode] + " | queryNodeId " + queryNode + " score: " + vectors[queryNode] + " |  == " + score);
            if (results.size() < querySize) {
                System.out.println("Result Queue no full Adding current Node to Results Queue NodeID: " + currentNode);
                results.add(new float[]{currentNode, score});
            } else {
                assert results.peek() != null;
                if (results.peek()[1] < score) {
                    System.out.println("Worst Value from Result Queue lower than score Adding current Node to Result Queue: " + currentNode);
                    results.poll();
                    results.add(new float[]{currentNode, score});
                }
            }
            // calculating and adding candidates if applicable
            NeighbourArray neighbourNodes = graph[currentNode][level];
            for (int i = 0; i < neighbourNodes.size; i++) {
                int neighbor = neighbourNodes.neighbours[i];
                if (usedNodes[neighbor] == 0){
                    float neighborScore = cosineSimilarity(vectors[neighbor], vectors[queryNode]);
                    System.out.println("Adding to Candidate Queue nodeID" + neighbor);
                    candidates.add(new float[]{neighbor, neighborScore});
                    usedNodes[neighbor] = 1;
                }
            }

            //terminal condition all candidates are agotated
            if (candidates.isEmpty()) {
                System.out.println("candidate is Empty hit breakingPoint");
                break;
            }
            //terminal condition if worst result is better than beast candidates
            if (results.size() == querySize) {
                assert candidates.peek() != null;
                assert results.peek() != null;
                if (candidates.peek()[1] < results.peek()[1]) {
                    break;
                }
            }
            // get best candidate for next loop
            currentNode = (int) Objects.requireNonNull(candidates.poll())[0];
            System.out.println("picking beast candidate for currentNode: " + currentNode);
        }

        return results;
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, magA = 0, magB = 0;
        for (int i = 0; i < a.length; i++) {
            dot  += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }
        if (magA == 0 || magB == 0) return 0f;
        return dot / (float)(Math.sqrt(magA) * Math.sqrt(magB));
    }


    private int randomLevel(){
        double ml = 1.0 / Math.log(maxConnectionPerNode);
        int level = (int) (-Math.log(Math.random()) * ml);
        return Math.min(level, 3); // cap at Max level 3;
    }
}


