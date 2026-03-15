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
- `insertNode(float[] vector)` — first node case done (assigns id, level, initializes graph slots, sets entry point)
- Status: **IN PROGRESS** — else branch (non-first node) not yet written

---

## Next Steps

### 1. Implement `searchLayer(float[] query, int entryPoint, int ef, int layer)` — Algorithm 2
This is the core beam search. Called by both `insertNode` and `search`.

It needs:
- `query` — the vector we are searching for
- `entryPoint` — node id to start from
- `ef` — how many candidates to keep in the beam
- `layer` — which layer of the graph to search

It returns a list of the best `ef` node ids found.

Internals:
- `candidates` min-heap — nodes to explore next (best score at top)
- `results` max-heap — best found so far, capped at `ef` (worst at top for easy eviction)
- `visited` set — avoid revisiting nodes
- Loop: pop best candidate, explore its neighbors, add unvisited ones to both heaps
- Stop when best candidate is worse than worst result

### 2. Implement `insertNode` else branch — Algorithm 1 (non-first node)
Using `searchLayer`:

```
ep = entryNode
for layer = maxLayer down to newNode.level + 1:
    ep = searchLayer(query, ep, ef=1, layer)   // phase 1: zoom in, 1 candidate

for layer = newNode.level down to 0:
    candidates = searchLayer(query, ep, efConstruction, layer)  // phase 2: full search
    neighbors = selectBestM(candidates, M or M*2 at layer 0)
    connect newNode <-> each neighbor bidirectionally
    ep = best candidate from this layer
```

### 3. Implement `selectNeighbors` — Algorithm 3 (simple version first)
Just pick the M closest from the candidate list. Used inside the insert else branch.

### 4. Implement `search(float[] query, int k)` — Algorithm 5
Top-level query method. Same structure as insert but only does Phase 1 top-down, then full beam search at layer 0, returns top k results.

### 5. Implement `selectNeighborsHeuristic` — Algorithm 4 (diversity heuristic)
Replace Algorithm 3 with the diversity-aware version. Compare recall before and after.

### 6. Add a distance function
Need `cosineSimilarity(float[] a, float[] b)` or `euclidean(float[] a, float[] b)` to score nodes during search. Currently no similarity computation exists anywhere.

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
