package org.example.HNSW;

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

    private int randomLevel(){
        double ml = 1.0 / Math.log(maxConnectionPerNode);
        int level = (int) (-Math.log(Math.random()) * ml);
        return Math.min(level, 3); // cap at Max level 3;
    }
}


