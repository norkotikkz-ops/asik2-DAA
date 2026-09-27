import csv
import os
import matplotlib.pyplot as plt

TABLES = "results/tables"
PLOTS = "results/plots"
os.makedirs(PLOTS, exist_ok=True)


def read_csv(name):
    path = os.path.join(TABLES, name)
    with open(path, newline="") as f:
        return list(csv.DictReader(f))


def plot_workload1():
    rows = read_csv("workload1_random_access.csv")
    ns = sorted(set(int(r["n"]) for r in rows))
    for metric, ylabel, fname in [
        ("avg_time_ns", "Average time (ns)", "workload1_time.png"),
        ("accesses", "Element accesses", "workload1_accesses.png"),
    ]:
        plt.figure()
        for structure in ["DynamicArray", "LinkedList"]:
            ys = [int(r[metric]) for r in rows if r["structure"] == structure]
            plt.plot(ns, ys, marker="o", label=structure)
        plt.xscale("log")
        plt.yscale("log")
        plt.xlabel("n")
        plt.ylabel(ylabel)
        plt.title("Workload 1 - Random Access")
        plt.legend()
        plt.grid(True, which="both", ls="--", alpha=0.4)
        plt.savefig(os.path.join(PLOTS, fname), bbox_inches="tight")
        plt.close()


def plot_workload2():
    rows = read_csv("workload2_search.csv")
    ns = sorted(set(int(r["n"]) for r in rows))
    for metric, ylabel, fname in [
        ("avg_time_ns", "Average time (ns)", "workload2_time.png"),
        ("comparisons", "Comparisons", "workload2_comparisons.png"),
    ]:
        plt.figure()
        for structure in ["DynamicArray", "LinkedList"]:
            ys = [int(r[metric]) for r in rows if r["structure"] == structure]
            plt.plot(ns, ys, marker="o", label=structure)
        plt.xscale("log")
        plt.yscale("log")
        plt.xlabel("n")
        plt.ylabel(ylabel)
        plt.title("Workload 2 - Search")
        plt.legend()
        plt.grid(True, which="both", ls="--", alpha=0.4)
        plt.savefig(os.path.join(PLOTS, fname), bbox_inches="tight")
        plt.close()


def plot_workload3():
    rows = read_csv("workload3_insert_remove.csv")
    ns = sorted(set(int(r["n"]) for r in rows))
    combos = sorted(set((r["structure"], r["operation"], r["position"]) for r in rows))
    for metric, ylabel, fname in [
        ("avg_time_ns", "Average time (ns)", "workload3_time.png"),
        ("movements", "Element movements", "workload3_movements.png"),
    ]:
        plt.figure()
        for structure, operation, position in combos:
            ys = [
                int(r[metric])
                for r in rows
                if r["structure"] == structure and r["operation"] == operation and r["position"] == position
            ]
            plt.plot(ns, ys, marker="o", label=f"{structure}-{operation}-{position}")
        plt.xscale("log")
        plt.yscale("log")
        plt.xlabel("n")
        plt.ylabel(ylabel)
        plt.title("Workload 3 - Insertion and Removal")
        plt.legend(fontsize=7)
        plt.grid(True, which="both", ls="--", alpha=0.4)
        plt.savefig(os.path.join(PLOTS, fname), bbox_inches="tight")
        plt.close()


def plot_workload4():
    rows = read_csv("workload4_heap.csv")
    ns = [int(r["n"]) for r in rows]

    plt.figure()
    plt.plot(ns, [int(r["avg_insert_time_ns"]) for r in rows], marker="o", label="insert (n ops)")
    plt.plot(ns, [int(r["avg_extract_time_ns"]) for r in rows], marker="o", label="extractMin (n ops)")
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel("n")
    plt.ylabel("Average total time (ns)")
    plt.title("Workload 4 - Priority Processing (time)")
    plt.legend()
    plt.grid(True, which="both", ls="--", alpha=0.4)
    plt.savefig(os.path.join(PLOTS, "workload4_time.png"), bbox_inches="tight")
    plt.close()

    plt.figure()
    plt.plot(ns, [int(r["comparisons"]) for r in rows], marker="o", label="comparisons")
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel("n")
    plt.ylabel("Comparisons")
    plt.title("Workload 4 - Priority Processing (comparisons)")
    plt.legend()
    plt.grid(True, which="both", ls="--", alpha=0.4)
    plt.savefig(os.path.join(PLOTS, "workload4_comparisons.png"), bbox_inches="tight")
    plt.close()


if __name__ == "__main__":
    plot_workload1()
    plot_workload2()
    plot_workload3()
    plot_workload4()
    print("Plots written to", PLOTS)
