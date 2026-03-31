# HNSW Implementation Progress

## What We Have Built So Far

### `NeighbourArray.java`
- Stores neighbor ids (`int[] neighbours`), similarity scores (`float[] scores`), and current count (`int size`)
- Constructor takes capacity — layer 0 gets `M*2`, upper layers get `M`
- Status: **DONE**

### `HnswGraph.java`
- `NeighbourArray[][] graph` — 2D array, `graph[nodeId][layer]` = neighbor list
- `float[][] vectors` — stores each node's vector, indexed by node id
- `int entryNode = -1` — id of the graph entry point (-1 = empty graph)
- `int maxLayer = -1` — current top layer of the graph
- `int counter = -1` — sequential node id generator
- `final int maxConnectionPerNode` — M parameter
- `final int efConstruction` — beam width during insert
- `randomLevel()` — assigns a random level using `floor(-ln(uniform) * mL)`, capped at 3
- `insertNode(float[] vector)` — **DONE** — first node and subsequent nodes, bidirectional connections written
- `greedySearch(startNode, queryNode, level, ef)` — **DONE** — beam search on a single layer (Algorithm 2), two-queue pattern with visited tracking
- `cosineSimilarity(float[] a, float[] b)` — **DONE**
- Status: **IN PROGRESS** — `search()` not yet implemented

---

## Next Steps

### 1. Implement `search(float[] query, int k)` — Algorithm 5 ← YOU ARE HERE
Top-level query method. Structure:
```
ep = entryNode
for layer = maxLayer down to 1:
    ep = greedySearch(ep, query, layer, ef=1)   // greedy descent, 1 candidate per layer

results = greedySearch(ep, query, layer=0, ef=k)  // full beam search at layer 0
return top k from results
```
Note: `greedySearch` currently takes a `queryNode` (int id), but `search()` takes a raw `float[]` query vector that isn't in the graph yet. You'll need to handle that — either add the query vector temporarily or adjust the method signature.

### 2. Fix null pointer risk in `greedySearch` (line 85)
`graph[currentNode][level]` is null for nodes whose random level is lower than the layer being searched. Fix: initialize all layers 0..maxLayer for every node in `insertNode`, not just 0..nodeLevel.

### 3. Implement `selectNeighborsHeuristic` — Algorithm 4 (diversity heuristic)
Replace simple "top M by score" with diversity-aware selection. Compare recall before and after.

---

## Key Concepts Understood So Far

- Nodes exist on layers 0 through their assigned level (not just one layer)
- Connections are within a layer only — no cross-layer edges
- The same node acts as the "elevator" between layers
- `efConstruction` = beam width during build, `ef` = beam width during query
- Layer 0 gets `M*2` connections for denser base connectivity
- `randomLevel()` uses exponential decay so most nodes land on layer 0
- Graph structure: `graph[nodeId][layer]` = `NeighbourArray` of neighbor ids + scores
- Vectors stored separately in `float[][] vectors`, not inside the graph structure
