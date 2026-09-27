# Assignment 2 — Algorithmic Analysis, Correctness and Performance Trade-offs

## 1. Overview

This project implements and analyzes three data structures in Java:

- **DynamicArray\<T\>** — an array-backed list that doubles its capacity when full.
- **MyLinkedList\<T\>** — a doubly linked list with head/tail pointers.
- **MinHeap** — an array-backed binary min-heap over `int` values.

Each structure implements the required operations (`add`, `add(index, x)`, `remove(index)`, `get(index)`,
`contains(x)` for the list-like structures; `insert`, `peekMin`, `extractMin` for the heap). The goal of the
assignment is not the implementations themselves but proving their correctness with loop invariants,
deriving their asymptotic complexity, and checking that derivation against measured performance across
four controlled workloads and input sizes n = 100, 1,000, 10,000, 100,000.

## 2. Complexity Analysis

### 2.1 DynamicArray

| Operation      | Best     | Average  | Worst    | Auxiliary space |
|----------------|----------|----------|----------|------------------|
| `add(x)`       | Θ(1)     | Θ(1)     | O(n)     | O(1) amortized, O(n) on resize |
| `add(i, x)`    | Ω(1) (i=n) | Θ(n)   | O(n) (i=0) | O(1) |
| `remove(i)`    | Ω(1) (i=n-1) | Θ(n) | O(n) (i=0) | O(1) |
| `get(i)`       | Θ(1)     | Θ(1)     | Θ(1)     | O(1) |
| `contains(x)`  | Ω(1) (found first) | Θ(n) | O(n) (not found) | O(1) |

Justification: elements are stored contiguously in a backing array, so any index is reachable by direct
offset arithmetic, giving `get(i)` constant time in every case. `add(x)` appends at the end; this is O(1)
except when the backing array is full, in which case a new array of double the size is allocated and all
n elements are copied — O(n) for that single call, but because the array doubles, the total cost of n
appends starting from empty is O(n), i.e. **O(1) amortized per call**. `add(i, x)` and `remove(i)` must shift
every element after index i by one slot to keep the array contiguous, so their cost is proportional to
`size - i`: best case is at the tail (no shifting), worst case is at the head (shifting all n elements).
`contains(x)` must scan linearly in the worst case since the array is unsorted.

### 2.2 MyLinkedList (doubly linked)

| Operation      | Best     | Average  | Worst    | Auxiliary space |
|----------------|----------|----------|----------|------------------|
| `add(x)`       | Θ(1)     | Θ(1)     | Θ(1)     | O(1) |
| `add(i, x)`    | Ω(1) (i=0 or i=n) | Θ(n) | O(n) (i=n/2) | O(1) |
| `remove(i)`    | Ω(1) (i=0 or i=n-1) | Θ(n) | O(n) (i=n/2) | O(1) |
| `get(i)`       | Ω(1) (i=0 or i=n-1) | Θ(n) | O(n) (i=n/2) | O(1) |
| `contains(x)`  | Ω(1) (found first) | Θ(n) | O(n) (not found) | O(1) |

Justification: `add(x)` appends at the tail, which is tracked by a pointer, so it is O(1) with no
amortization needed (no resizing, no copying). Every index-based operation (`get`, `add(i,x)`, `remove(i)`)
must first *locate* node i by walking pointers from whichever end (head or tail) is closer, which is
Θ(min(i, n-i)); the worst case is the middle of the list, Θ(n/2) = Θ(n). Once the node is located, the
insertion/removal itself is O(1) pointer rewiring — no shifting of other elements is needed, which is the
key structural difference from the array.

### 2.3 MinHeap

| Operation      | Best     | Average  | Worst    | Auxiliary space |
|----------------|----------|----------|----------|------------------|
| `insert(x)`    | Ω(1) (new min at bottom stays) | Θ(log n) | O(log n) | O(1) |
| `peekMin()`    | Θ(1)     | Θ(1)     | Θ(1)     | O(1) |
| `extractMin()` | Ω(1) (single element) | Θ(log n) | O(log n) | O(1) |

Justification: the heap is a complete binary tree stored in an array, so it has height ⌊log₂ n⌋.
`insert` appends the new element at the next free array slot (the next leaf position) and sifts it up
toward the root while it is smaller than its parent — at most height-many swaps, so O(log n); the best
case is when the new element is already ≥ its parent and no swap happens. `extractMin` removes the root
(the guaranteed minimum, found in O(1)), moves the last element to the root, and sifts it down by
repeatedly swapping with the smaller child — again bounded by the tree height, O(log n). `peekMin` only
reads `data[0]`.

### 2.4 Operations that look similar but are not

`get(i)` on `DynamicArray` and `get(i)` on `MyLinkedList` share a name but not a cost model: Θ(1) vs Θ(n).
Likewise `add(i, x)` costs the array Θ(n) *element copies* (cheap primitive/reference moves in a tight
array loop, very cache-friendly) while it costs the list Θ(n) *pointer hops* to locate the node (worse
cache locality, one indirection per hop) followed by an O(1) splice. Both are Θ(n) asymptotically, but
their **constant factors differ substantially** because array shifting is a sequential memory scan (fast,
vectorizable) while list traversal is a chain of pointer dereferences (slow, cache-unfriendly). This is
exactly the gap Section 9, Question 4 asks about.

## 3. Correctness — Loop Invariant Proofs

### 3.1 `DynamicArray.add(int index, T x)` — the right-shift loop

```java
for (int i = size; i > index; i--) {
    data[i] = data[i - 1];
}
data[index] = x;
```

**Loop invariant:** At the start of each iteration with loop variable `i`, the subarray
`data[i .. size]` is an exact copy of the *original* array's `data[i-1 .. size-1]`, i.e. every element
originally at position `k` for `k` in `[i-1, size-1]` has already been copied one slot to the right, to
position `k+1`, and no element with original index below `i-1` has been touched yet.

- **Initialization:** Before the first iteration, `i = size`. The claimed range `data[size .. size]` is
  empty (there is nothing at position `size` yet, since valid indices are `0..size-1`), so the invariant
  holds vacuously: no elements have been moved, and none were claimed to have been.

- **Maintenance:** Assume the invariant holds at the start of an iteration with value `i` (so
  `data[i..size]` holds the original `data[i-1..size-1]`, unmoved-below-`i-1`). The loop body executes
  `data[i] = data[i-1]`, copying the original element at position `i-1` into position `i`. Since by the
  invariant `data[i-1]` still holds its original value (it has not yet been overwritten, because the loop
  only ever writes to `data[i]` for the current, strictly decreasing `i`, and `i-1 < i`), this copy is
  exactly the element the invariant needs moved. After the copy, the range `data[i-1 .. size]` now holds
  the original `data[i-2 .. size-1]`, shifted by one — which is precisely the invariant re-stated for
  `i-1`, the value the loop variable takes next (after `i--`).

- **Termination:** The loop decrements `i` and exits when `i == index` (the loop guard `i > index` becomes
  false). At that point the invariant, with `i = index`, states that `data[index .. size]` holds the
  original `data[index-1 .. size-1]`. The loop variable strictly decreases and is bounded below by
  `index`, so the loop terminates after exactly `size - index` iterations.

- **Correctness from the invariant:** At termination, every original element with index ≥ `index` has been
  moved one slot to the right, freeing `data[index]`, and no element with index < `index` was disturbed.
  The subsequent statement `data[index] = x` then places the new element into the freed slot. The
  resulting array is therefore the original array with `x` inserted at position `index` and all later
  elements shifted right by one — exactly the specification of `add(index, x)`.

### 3.2 `MinHeap.extractMin()` — the sift-down loop

```java
private void siftDown(int i) {
    while (true) {
        int left = 2*i+1, right = 2*i+2, smallest = i;
        if (left  < size && data[left]  < data[smallest]) smallest = left;
        if (right < size && data[right] < data[smallest]) smallest = right;
        if (smallest == i) break;
        swap(i, smallest);
        i = smallest;
    }
}
```
called after `data[0] = data[size]` (last element moved to the root) inside `extractMin()`.

**Loop invariant:** At the start of each iteration, every subtree rooted at a node other than the current
node `i` satisfies the min-heap property (each parent ≤ its children), i.e. the *only* place the heap
property can currently be violated is at node `i` with respect to its own children.

- **Initialization:** Before the loop, `extractMin` has just removed the true root and moved the
  previously-last leaf into position 0. Every subtree of the original heap not touching the root already
  satisfied the heap property (the heap property held before extraction, and only position 0 was
  overwritten), so the only possible violation is at the new root, `i = 0` — matching the invariant.

- **Maintenance:** Assume the invariant holds for the current `i`: all subtrees except possibly the one
  rooted at `i` are valid min-heaps. The body identifies `smallest`, the index holding the minimum among
  `data[i]`, `data[left]`, `data[right]` (only considering children that exist, `< size`). If
  `smallest == i`, node `i` is already ≤ both its children, so the whole subtree rooted at `i` is now a
  valid min-heap and the loop breaks — invariant trivially preserved (no more possible violations exist).
  Otherwise, swapping `data[i]` and `data[smallest]` places the smaller value at `i`, so node `i`'s
  immediate children constraint is now satisfied, and the subtree not containing `smallest` (the sibling
  of the swapped child) is untouched and was already valid. The only subtree that can still be invalid is
  the one now rooted at `smallest` (since the value moved there might be greater than *its* children). The
  loop sets `i = smallest` and continues — the invariant, re-stated for the new `i`, again holds.

- **Termination:** Each iteration either breaks immediately or moves `i` strictly one level deeper in the
  complete binary tree (`i` becomes `left` or `right`, both `> i`). Since the tree has finite height
  `⌊log₂ size⌋`, `i` cannot decrease strictly deeper indefinitely and the loop must reach a leaf or a
  `smallest == i` condition within O(log size) iterations.

- **Correctness from the invariant:** At termination, the invariant states no subtree other than the one
  rooted at the final `i` can be invalid, and the break condition additionally guarantees node `i` itself
  satisfies the heap property relative to its children. Hence *no* subtree is invalid: the whole array
  `data[0 .. size-1]` is a valid min-heap. Combined with the fact that `extractMin` returned the value that
  was at the root *before* any of this (the true minimum, since a valid min-heap always has its minimum at
  the root), this proves `extractMin` both returns the correct minimum and leaves a valid min-heap behind.

## 4. Experimental Setup

- **n** (elements initially stored): 100, 1,000, 10,000, 100,000, generated with `new Random(42)` before
  timing begins (input generation is excluded from measured time).
- **m** (operations per workload): 10,000 `get` calls (Workload 1), 1,000 `contains` calls (Workload 2),
  1,000 insertions + 1,000 removals at two positions (Workload 3), n inserts + n extracts (Workload 4).
- **Repetitions:** every timed section is run 5 times per configuration and the average of the 5
  `System.nanoTime()` measurements is reported.
- **Random seed:** `new Random(42)` for every generator, so the same sequence of "random" inputs is
  reproducible across runs.
- Input data (the seed elements and the operation indices/values) is generated once per `n`, before the
  timed section starts, and reused unchanged across the 5 repetitions for that `n`.

## 5. How to Reproduce the Results

This container does not have a JDK/network available to compile and execute Java, so the numeric
tables and plots below are placeholders to be filled in after running the project — either on your own
machine or in the GitHub Actions / grader environment. Everything needed to reproduce is included:

```bash
./run.sh
```

This compiles `src/main` and `src/test`, runs `com.aitu.ds.Tests` (prints PASS/FAIL for every test),
runs `com.aitu.ds.Benchmark` (writes CSVs to `results/tables/`), and runs `plot_results.py` (writes PNGs
to `results/plots/`, requires `pip install matplotlib`). Running `com.aitu.ds.Benchmark` alone takes a few
minutes because of the n = 100,000 configurations with 5 repetitions each.

After running, paste the generated CSV rows into the tables below and embed the PNGs from
`results/plots/` in this section.

## 6. Experimental Results

### Workload 1 — Random Access (`get`)

| Structure    | n       | avg_time_ns | accesses |
|--------------|---------|-------------|----------|
| DynamicArray | 100     | *(run `./run.sh`)* | |
| LinkedList   | 100     | | |
| DynamicArray | 1,000   | | |
| LinkedList   | 1,000   | | |
| DynamicArray | 10,000  | | |
| LinkedList   | 10,000  | | |
| DynamicArray | 100,000 | | |
| LinkedList   | 100,000 | | |

*Insert `results/plots/workload1_time.png` and `workload1_accesses.png` here.*

**Analysis:** `DynamicArray.get` is Θ(1) regardless of n, so its time should stay essentially flat as n
grows. `MyLinkedList.get` is Θ(n) per call in the worst case (target near the middle), so total time over
10,000 random indices should grow roughly linearly with n. Confirm this matches the measured trend, and
note any deviation (e.g. constant-factor effects, JIT warm-up, cache effects at large n).

### Workload 2 — Search (`contains`)

| Structure    | n       | avg_time_ns | comparisons |
|--------------|---------|-------------|-------------|
| DynamicArray | 100     | | |
| LinkedList   | 100     | | |
| DynamicArray | 1,000   | | |
| LinkedList   | 1,000   | | |
| DynamicArray | 10,000  | | |
| LinkedList   | 10,000  | | |
| DynamicArray | 100,000 | | |
| LinkedList   | 100,000 | | |

*Insert `results/plots/workload2_time.png` and `workload2_comparisons.png` here.*

**Analysis:** Both structures perform Θ(n) unsorted linear search, so comparisons and time should scale
similarly for both. Any timing gap between them at the same n is attributable to constant factors
(contiguous array scan vs. pointer chasing), not to a difference in asymptotic complexity.

### Workload 3 — Insertion and Removal

| Structure    | Operation | Position | n       | avg_time_ns | movements |
|--------------|-----------|----------|---------|-------------|-----------|
| DynamicArray | insert    | begin    | 100     | | |
| DynamicArray | remove    | begin    | 100     | | |
| DynamicArray | insert    | middle   | 100     | | |
| DynamicArray | remove    | middle   | 100     | | |
| LinkedList   | insert    | begin    | 100     | | |
| LinkedList   | remove    | begin    | 100     | | |
| LinkedList   | insert    | middle   | 100     | | |
| LinkedList   | remove    | middle   | 100     | | |
| *(repeat for n = 1,000 / 10,000 / 100,000)* | | | | | |

*Insert `results/plots/workload3_time.png` and `workload3_movements.png` here.*

**Analysis:** Expect `DynamicArray` insert/remove at the **beginning** to cost Θ(n) element shifts and to
get markedly slower as n grows; at the **middle**, shifts are still Θ(n) but roughly half as many.
`MyLinkedList` insert/remove at the **beginning** should be O(1) once the head is found (which is
immediate), while at the **middle** it costs Θ(n) just to *locate* the node before an O(1) splice. So the
array is best at the tail, the list is best at the head, and both are Θ(n) in the middle — for different
reasons (copying vs. traversal).

### Workload 4 — Priority Processing (Min-Heap)

| n       | avg_insert_time_ns | avg_extract_time_ns | comparisons | sorted_correctly |
|---------|---------------------|----------------------|-------------|-------------------|
| 100     | | | | |
| 1,000   | | | | |
| 10,000  | | | | |
| 100,000 | | | | |

*Insert `results/plots/workload4_time.png` and `workload4_comparisons.png` here.*

**Analysis:** `insert` is O(log n) per call, so total time for n inserts is O(n log n); `extractMin` is the
same, O(n log n) total for n extractions. `peekMin` is not separately benchmarked here since it is a
trivial O(1) array read with no loop to measure meaningfully. `sorted_correctly` must be `true` for every
n, confirming `extractMin` always returns the current minimum.

## 7. Performance and Design Analysis

1. **Effect of increasing n:** operations with Θ(1) cost (`DynamicArray.get`, list head insert/remove) stay
   flat; Θ(n) operations (list `get`/middle insert, `contains` on either structure, array insert/remove)
   grow linearly; Θ(log n) operations (heap insert/extractMin) grow very slowly, and Θ(n log n) totals
   (Workload 4) grow just barely faster than linear.
2. **Agreement with theory:** the relative ordering of the four n values and the two structures should
   track the derived Big-O classes; small-n measurements are the least reliable due to timer resolution
   and JIT warm-up noise.
3. **Where results may differ from theory:** at very small n, constant overhead (object allocation,
   method dispatch, JIT interpretation before compilation kicks in) can dominate the asymptotic term,
   making two Θ(1) and Θ(log n) operations look similar in wall-clock time even though their comparison
   counts differ.
4. **Same Big-O, different running time:** constant factors from memory layout matter. Array shifting
   touches contiguous memory (cache-friendly, few cache misses); list traversal follows a pointer per
   step (a potential cache miss per hop). Two Θ(n) algorithms can therefore differ by a large constant
   multiplier in practice.
5. **Constant factors and implementation details:** object overhead of boxed `Integer` vs. primitive `int`,
   branch prediction, JVM JIT compilation threshold, and garbage collection pauses at large n all shift
   measured time without changing the asymptotic class.
6. **When DynamicArray is preferable:** workloads dominated by random-access reads (`get`) or append-only
   writes at the tail, where its Θ(1) direct indexing and cache-friendly layout win decisively.
7. **When LinkedList is useful:** workloads dominated by insertions/removals at a known node (e.g. the
   head/tail, or when already iterating and holding a reference), where no shifting is needed and only
   pointers are rewired.
8. **Why a heap suits priority processing:** it keeps `peekMin`/`extractMin` at O(1)/O(log n) without ever
   needing a full sort, which is far cheaper than repeatedly scanning or fully sorting a list to find the
   minimum (O(n) or O(n log n) per operation instead of O(log n)).
9. **Workload shapes the choice:** the "right" data structure is workload-dependent, not universal — the
   same logical collection of elements should be backed by different structures depending on whether the
   dominant access pattern is random reads, positional inserts/deletes, or priority extraction.

## 8. Design Recommendations

- **Random access / read-heavy workloads:** `DynamicArray`.
- **Frequent inserts/removals at the front or back with sequential access:** `MyLinkedList`.
- **Frequent inserts/removals in arbitrary/middle positions of a large collection:** neither is great in
  isolation (both are Θ(n)); consider a balanced tree or skip list if this dominates.
- **Priority-based processing (scheduling, Dijkstra, event simulation):** `MinHeap`.

## 9. Conclusion

The three structures embody a classic trade-off: `DynamicArray` favors O(1) indexed access at the cost of
O(n) positional mutation; `MyLinkedList` favors O(1) mutation *once positioned* at the cost of O(n)
positional access; `MinHeap` sacrifices general access/order entirely to guarantee O(log n) priority
operations. No structure dominates the others — the correct choice depends on which operation the
workload performs most often, which is exactly what the four experimental workloads were designed to
expose.
