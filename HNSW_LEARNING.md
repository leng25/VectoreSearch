# HNSW — Learning Guide & Project Plan

## The Paper

**"Efficient and robust approximate nearest neighbor search using Hierarchical Navigable Small World graphs"**
Yury Malkov, Dmitry Yashunin (2016/2018)

- Local PDF: [paper_1603.09320.pdf](./paper_1603.09320.pdf)
- arXiv: https://arxiv.org/abs/1603.09320

Read the paper before writing any code. The five core algorithms (1–5) are the blueprint for everything below.

---

## What Problem Are We Solving?

Given a large collection of high-dimensional vectors (e.g. text embeddings, image features), find the `k` most similar vectors to a query vector **fast**, without comparing every vector (brute force).

HNSW solves this with a multi-layer proximity graph that enables greedy logarithmic-complexity traversal — similar in spirit to a skip list, but for vector similarity.

---

## Apache Lucene's HNSW Architecture

Lucene is the reference implementation to study. The source lives at:
`/Users/luisnegrin/IdeaProjects/lucene/lucene/core/src/java/org/apache/lucene/util/hnsw`

### Core Classes

---

### `HnswGraph` (abstract)
**File:** `util/hnsw/HnswGraph.java`

The abstract API for an HNSW graph. It exposes a *cursor-style* iterator interface:
- `seek(int level, int node)` — point the cursor at a node on a given layer
- `nextNeighbor()` — iterate its neighbors one by one

**Why a cursor and not a List?** Because the graph can be stored on disk (off-heap). The same abstract API works for both in-memory and disk-backed graphs without changing the search code.

**What to learn:** The separation between the graph data structure and its traversal. Lucene never exposes a `List<Integer>` of neighbors — it always goes through this streaming interface.

---

### `OnHeapHnswGraph`
**File:** `util/hnsw/OnHeapHnswGraph.java`

The in-memory concrete implementation of `HnswGraph`. The internal layout is:

```
graph[nodeId][level] = NeighborArray
```

A 2D array: first dimension is node ordinal, second is layer. Each cell holds a `NeighborArray` (a sorted array of neighbor ids + their scores).

Key facts:
- Level 0 has a larger neighbor budget: `M * 2` slots
- Upper levels have `M` slots
- The entry node (the single entry point on the top layer) is stored as an `AtomicReference<EntryNode>` to support concurrent builds

**What to learn:** How the hierarchical structure is laid out in memory. Why level 0 gets double the connections (the paper explains this — denser connectivity at the base improves recall).

---

### `HnswGraphBuilder`
**File:** `util/hnsw/HnswGraphBuilder.java`

Builds the graph by inserting vectors one at a time. Implements **Algorithm 1** from the paper.

Key parameters (Lucene naming → paper naming):
| Lucene | Paper | Meaning |
|---|---|---|
| `M` | `M` | Max neighbors per node on upper layers |
| `beamWidth` | `efConstruction` | Candidate queue size during insertion |
| `ml` | `mL` | Level normalisation factor: `1 / ln(M)` |

Key methods:
- `addGraphNode(int node)` — insert one vector into the graph
- `getRandomGraphLevel(ml, random)` — assigns a random max-layer to the new node using `floor(-ln(uniform) * mL)`. This is the exponential decay probability from the paper.
- `addDiverseNeighbors(...)` — applies the **diversity heuristic (Algorithm 4)**: prefer neighbors that are closer to the new node than to each other, rather than just picking the M closest. This is what makes HNSW robust on clustered data.
- `selectAndLinkDiverse(...)` — inner loop of the heuristic

**What to learn:** Why the diversity heuristic matters. A naive "pick the M closest" (Algorithm 3) creates dense local clusters but poor long-range connectivity. Algorithm 4 explicitly ensures the neighbors "cover different directions."

---

### `HnswGraphSearcher`
**File:** `util/hnsw/HnswGraphSearcher.java`

Searches the graph for the nearest neighbors of a query vector. Implements **Algorithm 5** from the paper (and Algorithm 2 for per-layer search).

Key methods:
- `findBestEntryPoint(...)` — greedy descent through layers `numLevels-1` down to layer 1, always moving to the neighbor with the best score. Only tracks 1 candidate per level here.
- `searchLevel(...)` — beam search on a single layer. Maintains:
  - `candidates` min-heap (ordered by score ascending) — nodes to explore next
  - `results` max-heap (ordered by score descending, capped at `ef`) — best found so far
  - `visited` bitset — avoid revisiting nodes

The termination condition: stop when the best unexplored candidate is worse than the worst result already collected (the beam has converged).

**What to learn:** The two-queue pattern. Why you need both a *candidates* heap and a *results* heap, and why they are ordered differently.

---

### `NeighborArray`
**File:** `util/hnsw/NeighborArray.java`

A compact, sorted array of `(nodeId, score)` pairs. Used as the adjacency list for each `(node, level)` slot. Keeps itself sorted by score to make pruning efficient when the array is full and a new neighbor must evict an existing one.

**What to learn:** The tradeoff between using a sorted array vs a heap for a bounded neighbor list.

---

### `NeighborQueue`
**File:** `util/hnsw/NeighborQueue.java`

A bounded priority queue used during search for the candidate beam. Can operate as a min-heap (for candidates — explore the best next) or max-heap (for results — evict the worst when full).

**What to learn:** How the same underlying heap is reused for two different purposes just by flipping the comparator.

---

## The Five Algorithms in the Paper

Before writing any code, understand what each algorithm does:

| Algorithm | Name | When used |
|---|---|---|
| 1 | `INSERT` | Adding a new vector to the index |
| 2 | `SEARCH-LAYER` | Beam search on one layer (used by both insert and query) |
| 3 | `SELECT-NEIGHBORS-SIMPLE` | Naive: pick the M closest candidates |
| 4 | `SELECT-NEIGHBORS-HEURISTIC` | Diverse: pick neighbors that cover different directions |
| 5 | `KNN-SEARCH` | Query: find top-k nearest neighbors |

Algorithm 2 is the building block called by both 1 and 5. Algorithm 4 is what separates HNSW from simpler graph methods.

---

## Project Goal — Mini HNSW in Java

We will build a minimal, readable Java implementation of HNSW from scratch inside this project, in the package `org.example.hnsw`.

The goal is **understanding**, not production performance. The code should mirror the paper's algorithms directly so you can trace each line back to the paper.

### Planned Classes

| Class | Responsibility |
|---|---|
| `DistanceFunction` | Euclidean, cosine, dot-product similarity |
| `HnswNode` | Stores a vector and its per-layer neighbor lists |
| `HnswIndex` | The index: `insert()` and `search()` |

### How We Will Work

- You write the code, not me.
- I will ask you questions to guide you toward the right design decisions.
- When you are stuck I will give hints, not full solutions.
- After you write each method we will compare it to the equivalent in Lucene and discuss what Lucene does differently and why.

### Suggested Order

1. Implement `DistanceFunction` — get comfortable with similarity vs distance
2. Implement `HnswNode` — think about how to represent per-layer neighbors
3. Implement `SEARCH-LAYER` (Algorithm 2) — the core beam search loop
4. Implement `KNN-SEARCH` (Algorithm 5) — top-level search using Algorithm 2
5. Implement `SELECT-NEIGHBORS-SIMPLE` (Algorithm 3) — naive neighbor selection
6. Implement `INSERT` (Algorithm 1) — full insertion using Algorithms 2 and 3
7. Replace Algorithm 3 with `SELECT-NEIGHBORS-HEURISTIC` (Algorithm 4) — observe the recall improvement
8. Compare every step against the corresponding Lucene class

---

## Questions to Think About Before Starting

These are questions you should be able to answer after reading the paper. We will revisit them as we implement each piece:

1. Why does the graph have multiple layers instead of just one flat layer?
2. Why is the entry point always on the top layer and there is only one of them?
3. Why does level 0 get `M * 2` neighbors while upper layers get `M`?
4. In Algorithm 2, why do you need both a *candidates* heap and a *results* heap?
5. What is the difference between `efConstruction` (used during build) and `ef` (used during query)?
6. Why does the diversity heuristic (Algorithm 4) outperform the simple one (Algorithm 3) on clustered data?
7. How does the level assignment formula `floor(-ln(uniform) * mL)` produce an exponentially decaying distribution?
