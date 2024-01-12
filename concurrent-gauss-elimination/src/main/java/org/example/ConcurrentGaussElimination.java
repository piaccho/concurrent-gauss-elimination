package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;


public class ConcurrentGaussElimination {
    private final int N; // matrix size
    private final double[][] matrix; // NxN matrix with RHS
    private List<Action> alphabet;
    private List<Relation> dependencies;
    private Map<Action, List<Action>> dependenciesMap;
    private List<List<Action>> foatNormalForm;
    private Graph graph;

    public ConcurrentGaussElimination(Matrix matrix) {
        this.N = matrix.getSize();
        this.matrix = new double[N][N + 1];
        for(int i = 0; i < N; i++)
            for(int j = 0; j < N; j++)
                this.matrix[i][j] = matrix.getMatrix()[i][j];
        for(int i = 0; i < N; i++)
            this.matrix[i][N] = matrix.getRHS()[i][0];
        this.alphabet = null;
        this.dependencies = null;
        this.dependenciesMap = null;
        this.foatNormalForm = null;
        this.graph = null;
    }

    private void createAlphabetAndFoatNormalForm() {
        List<Action> actionsAlphabet = new ArrayList<>();
        List<List<Action>> foatNormalForm = new ArrayList<>();
        for (int i = 0; i < N - 1; i++) {
            List<Action> foatGroupA = new ArrayList<>();
            List<Action> foatGroupB = new ArrayList<>();
            List<Action> foatGroupC = new ArrayList<>();
            for (int k = i + 1; k < N; k++) {
                Action actionA = new Action('A', i, -1, k);
                actionsAlphabet.add(actionA);
                foatGroupA.add(actionA);
                for (int j = i; j < N + 1; j++) {
                    Action actionB = new Action('B', i, j, k);
                    actionsAlphabet.add(actionB);
                    foatGroupB.add(actionB);

                    Action actionC = new Action('C', i, j, k);
                    actionsAlphabet.add(actionC);
                    foatGroupC.add(actionC);
                }
            }
            foatNormalForm.add(foatGroupA);
            foatNormalForm.add(foatGroupB);
            foatNormalForm.add(foatGroupC);
        }
        this.alphabet = actionsAlphabet;
        this.foatNormalForm = foatNormalForm;
    }

    private void createDependencies() {
        List<Relation> dependencies = new ArrayList<>();
        for (int i = 0; i < alphabet.size(); i++) {
            for (int j = i + 1; j < alphabet.size(); j++) {
                Action firstAction = alphabet.get(i);
                Action secondAction = alphabet.get(j);

                if (firstAction.isDependent(secondAction)) {
                    dependencies.add(new Relation(firstAction, secondAction));
                }
            }
        }
        this.dependencies = dependencies;
    }

    private void createDependenciesMap() {
        Map<Action, List<Action>> dependenciesMap = new HashMap<>();

        for (Relation relation : dependencies) {
            Action key = relation.getFirstAction();
            Action value = relation.getSecondAction();

            if (!dependenciesMap.containsKey(key)) {
                dependenciesMap.put(key, new ArrayList<>());
            }
            dependenciesMap.get(key).add(value);
            if (!dependenciesMap.containsKey(value)) {
                dependenciesMap.put(value, new ArrayList<>());
            }
            dependenciesMap.get(value).add(key);
        }

        this.dependenciesMap = dependenciesMap;
    }

    private void createGraph() {
        Graph graph = new Graph(this.alphabet.size());

        // Add edges
        for (int i = 0; i < this.alphabet.size(); i++) {
            for (int j = i + 1; j < this.alphabet.size(); j++) {
                for (Action dependent : dependenciesMap.get(this.alphabet.get(j))) {
                    if (dependent == this.alphabet.get(i)) {
                        graph.addEdge(i, j);
                        break;
                    }
                }
            }
        }

        // Remove transitivity
        for (int i = 0; i < alphabet.size(); i++) {
            int start = i;
            List<Integer> neighbours = graph.getNeighbors(i);
            List<Integer> visited = new ArrayList<>();
            Queue<Integer> queue = new LinkedList<>(neighbours);

            while (!queue.isEmpty()) {
                int node = queue.remove();
                graph.getNeighbors(node).forEach(next -> {
                    if (next.equals(start)) {
                        throw new RuntimeException("Cycle detected!");
                    }
                    if(!visited.contains(next)) {
                        visited.add(next);
                        queue.add(next);
                    }
                });
            }

            List<Integer> toRemove = new ArrayList<>();
            neighbours.forEach(s -> {
                if(visited.contains(s))
                    toRemove.add(s);
            });

            toRemove.forEach(node -> graph.removeEdge(start, node));
        }



        this.graph = graph;
    }

    public void createModel() {
        createAlphabetAndFoatNormalForm();
        printAlphabet();

        createDependencies();
        printDependencies();

        createDependenciesMap();
//        printDependenciesMap();

        printFoatNormalForm();

        createGraph();
//        printDotGraph();
        saveDotGraphToFile();
    }

    public void solveWithSchedulers(String outputFilename) throws InterruptedException {
        System.out.println("\nSolving concurrently...\n");

        double[] matrixA = new double[N];
        double[][] matrixB = new double[N][N+1];

        // Schedulers
        for (int i = 0; i < N - 1; i++){
            List<Thread> tasksA = new ArrayList<>();
            List<Thread> tasksB = new ArrayList<>();
            List<Thread> tasksC = new ArrayList<>();
            int finalI = i;

            // A tasks
            for (int k = i + 1; k < N; k++) {
                int finalK = k;
                tasksA.add(new Thread(()-> {
                    matrixA[finalK] = matrix[finalK][finalI] / matrix[finalI][finalI];
                }));
            }

            // B tasks
            for(int k = i + 1; k < N; k++){
                for(int j=i; j<N+1; j++){
                    int finalJ = j;
                    int finalK = k;
                    tasksB.add(new Thread(()-> {
                        matrixB[finalK][finalJ] = matrix[finalI][finalJ] * matrixA[finalK];
                    }));
                }
            }

            // C tasks
            for(int k = i + 1; k < N; k++){
                for(int j=i; j<N+1; j++){
                    int finalJ = j;
                    int finalK = k;
                    tasksC.add(new Thread(()-> {
                        if (finalI == finalJ){
                            matrix[finalK][finalJ] = 0;
                            return;
                        }
                        matrix[finalK][finalJ] -= matrixB[finalK][finalJ];
                    }));
                }
            }

            for (Thread thread : tasksA) {
                thread.start();
                thread.join();
            }
            for (Thread thread : tasksB) {
                thread.start();
                thread.join();
            }
            for (Thread thread : tasksC) {
                thread.start();
                thread.join();
            }
        }

        // Solve linear equations - to identity matrix
        solveLinearEquations();

        saveSolutionToFile(outputFilename);
    }
    private void solveLinearEquations() {
        for (int i = N - 1; i >= 0; i--) {
            double m;

            m = matrix[i][i];
            matrix[i][i] /= m;
            matrix[i][N] /= m;

            for (int i1 = i - 1; i1 >= 0; i1--) {
                m = matrix[i1][i];
                for (int j1 = N; j1 > i1; j1--) {
                    matrix[i1][j1] -= m * matrix[i][j1];
                }
            }
        }
    }
    private void saveSolutionToFile(String filename) {
        System.out.println("\nSaving solution...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            int N = matrix.length;

            writer.write(Integer.toString(N));
            writer.newLine();

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    writer.write(matrix[i][j] + " ");
                }
                writer.newLine();
            }

            for (int i = 0; i < N; i++) {
                writer.write(matrix[i][N] + " ");
            }
            System.out.println("\nSolution saved to file: " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while saving solution to file: " + filename);
            e.printStackTrace();
        }
    }
    private void saveDotGraphToFile() {
        if(this.graph != null) {
            System.out.println("\nSaving dot graph...");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("graph.dot"))) {
                writer.write("digraph G {");
                writer.newLine();
                String colorScheme = "set19";
                writer.write("node [colorscheme=" + colorScheme + "]");
                writer.newLine();
                for(int i = 0; i < this.alphabet.size(); i++) {
                    int finalI = i;
                    this.graph.getNeighbors(i).forEach(j -> {
                        try {
                            writer.write(finalI + "->" + j);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    writer.write(i + "[label=\"" + this.alphabet.get(i) + "\"]");
                    writer.newLine();
                }
                for(int i = 0; i < this.foatNormalForm.size(); i++) {
                    List<Action> group = this.foatNormalForm.get(i);
                    Random rand = new Random();

//                    int red = rand.nextInt(256);
//                    int green = rand.nextInt(256);
//                    int blue = rand.nextInt(256);
//                    String colorHEX = String.format("#%02X%02X%02X", red, green, blue);
                    int colorIndex = (i + 1) % 11;

                    for(Action action: group) {
                        writer.write(this.alphabet.indexOf(action) + "[shape=circle, style=filled, color=" + colorIndex + "]");
//                        writer.write(this.alphabet.indexOf(action) + "[shape=circle, style=filled, fillcolor=\"" + colorHEX + "\"]");
                        writer.newLine();
                    }
                }
                writer.write("}");

                System.out.println("Dot Graph saved to file: " + "graph.dot");
            } catch (IOException e) {
                System.out.println("An error occurred while saving Dot Graph to file: "+ " graph.dot");
                e.printStackTrace();
            }
            try {
                MutableGraph g = new Parser().read(new File("graph.dot"));
                Graphviz.fromGraph(g).width(800).render(Format.PNG).toFile(new File("graph.png"));
                System.out.println("Rendered dot Graph saved to file: " + "graph.png\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // PRINTS

    private void printLinearEquation() {
        for (int i = 0; i < N; i++) {
            System.out.print("[ ");
            for (int j = 0; j < N; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println("| " + matrix[i][N] + " ]");
        }
    }

    private void printAlphabet() {
        if(this.alphabet != null) {
            System.out.println("\nAlphabet/Word (" + this.alphabet.size() + ") :");
            System.out.println(this.alphabet);
        }
    }

    private void printDependencies() {
        if(this.dependencies != null) {
            System.out.println("\nDependencies - without identity and symmetry (" + this.dependencies.size() + ") :");
            System.out.println(this.dependencies);
        }
    }

    private void printDependenciesMap() {
        if(this.dependenciesMap != null) {
            System.out.println("\nDependencies Map - all dependencies :");
            Comparator<Map.Entry<Action, List<Action>>> comparator = Comparator
                    .comparing((Map.Entry<Action, List<Action>> entry) -> entry.getKey().getType())
                    .thenComparing(entry -> entry.getKey().getI())
                    .thenComparing(entry -> entry.getKey().getK())
                    .thenComparing(entry -> entry.getKey().getJ());

            this.dependenciesMap.entrySet().stream().sorted(comparator).forEach(entry -> {
                System.out.print(entry.getKey() + "\t -> ");
                System.out.println(entry.getValue());
            });
        }
    }

    private void printFoatNormalForm() {
        System.out.println("\nFoat Normal Form: ");
        for(List<Action> group: this.foatNormalForm) {
            System.out.println(group);
        }
    }

    private void printDotGraph() {
        if(this.graph != null) {
            System.out.println("\nDot Graph: ");
            System.out.println("digraph G {");
            for(int i = 0; i < this.alphabet.size(); i++) {
                int finalI = i;
                this.graph.getNeighbors(i).forEach(j -> System.out.println(finalI + "->" + j));
                System.out.println(i + "[label=\"" + this.alphabet.get(i) + "\"]");
            }
            // Coloring
            for(List<Action> group: this.foatNormalForm) {
                Random rand = new Random();

                int red = rand.nextInt(256);
                int green = rand.nextInt(256);
                int blue = rand.nextInt(256);
                String colorHEX = String.format("#%02X%02X%02X", red, green, blue);

                for(Action action: group) {
                    System.out.println(this.alphabet.indexOf(action) + "[shape=circle, style=filled, fillcolor=\"" + colorHEX + "\"]");
                }
            }
            System.out.println("}");
        }
    }
    public Matrix getMatrix() {
        double[][] matrix = new double[N][N];
        for(int i = 0; i < N; i++)
            for(int j = 0; j < N; j++)
                matrix[i][j] = this.matrix[i][j];
        double[][] RHS = new double[N][1];
        for(int i = 0; i < N; i++)
            RHS[i][0] = this.matrix[i][N];
        return new Matrix(N, matrix, RHS);
    }
}
