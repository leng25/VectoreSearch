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

        // first node scenario
        if (entryNode == -1){
            entryNode = nodeId;
            maxLayer = level;
             for (int l = level; l >= 0; l--) {
                graph[nodeId][l] = new NeighbourArray(
                        l == 0 ? maxConnectionPerNode * 2: maxConnectionPerNode
                );
            }
        }
        else {

        }
    }

    private Queue<float[]> beastCandidateSearch(
            int startedNode,int queryNode, int level, int querySize){
        Queue<float[]> results = new PriorityQueue<>(Comparator.comparing((float[] a) -> a[1]));
        Queue<float[]> candidates = new PriorityQueue<>(Comparator.comparing((float[] a) -> a[1]).reversed());
        int currentNode = startedNode;
        int[] usedNodes = new int[150];
        while (true) {
            // calculating and adding node to result if applicable
            float score = cosineSimilarity(vectors[currentNode], vectors[queryNode]); // not sure who goes first here
            if (results.size() < querySize) {
                results.add(new float[]{currentNode, score});
            } else {
                assert results.peek() != null;
                if (results.peek()[1] < score) {
                    results.poll();
                    results.add(new float[]{currentNode, score});
                }
            }
            // calculating and adding candidates if applicable
            NeighbourArray neighbourNodes = graph[currentNode][level];
            for (int i = 0; i < neighbourNodes.size; i++) {
                if (usedNodes[currentNode] == 0){
                    int neighbor = neighbourNodes.neighbours[i];
                    float neighborScore = cosineSimilarity(vectors[neighbor], vectors[queryNode]);
                    candidates.add(new float[]{neighbor, neighborScore});
                    usedNodes[currentNode] = 1;
                }
            }

            //terminal conditions
            if (candidates.isEmpty()) {
                break;
            }
            if (results.size() == querySize) {
                assert candidates.peek() != null;
                assert results.peek() != null;
                if (candidates.peek()[1] < results.peek()[1]) {
                    break;
                }
            }
            // get best candidate for next loop
            currentNode = (int) Objects.requireNonNull(candidates.poll())[0];
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


