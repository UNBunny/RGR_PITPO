import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {

    // Constants of the problem
    static final int ONE_MAX_LENGTH = 100; // Length of the bit string to be optimized

    // Constants of the genetic algorithm
    static final int POPULATION_SIZE = 200; // Number of individuals in the population
    static final double P_CROSSOVER = 0.9; // Probability of crossover
    static final double P_MUTATION = 0.1; // Probability of mutation of an individual
    static final int MAX_GENERATIONS = 50; // Maximum number of generations

    static final int RANDOM_SEED = 42;
    static final Random random = new Random(RANDOM_SEED);

    static class FitnessMax {
        int value;

        FitnessMax(int value) {
            this.value = value;
        }
    }

    static class Individual {
        List<Integer> bitString;
        FitnessMax fitness;

        Individual(List<Integer> bitString) {
            this.bitString = bitString;
            this.fitness = new FitnessMax(0);
        }
    }

    static int oneMaxFitness(List<Integer> bitString) {
        int sum = 0;
        for (Integer bit : bitString) {
            sum += bit;
        }
        return sum;
    }

    static Individual individualCreator() {
        List<Integer> bitString = new ArrayList<>();
        for (int i = 0; i < ONE_MAX_LENGTH; i++) {
            bitString.add(random.nextInt(2));
        }
        return new Individual(bitString);
    }

    static List<Individual> populationCreator(int n) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            population.add(individualCreator());
        }
        return population;
    }

    static List<Individual> selTournament(List<Individual> population, int p_len) {
        List<Individual> offspring = new ArrayList<>();
        for (int n = 0; n < p_len; n++) {
            int i1, i2, i3;
            do {
                i1 = random.nextInt(p_len);
                i2 = random.nextInt(p_len);
                i3 = random.nextInt(p_len);
            } while (i1 == i2 || i1 == i3 || i2 == i3);

            Individual selected = population.get(i1);
            if (population.get(i2).fitness.value > selected.fitness.value) {
                selected = population.get(i2);
            }
            if (population.get(i3).fitness.value > selected.fitness.value) {
                selected = population.get(i3);
            }
            offspring.add(selected);
        }
        return offspring;
    }

    static void cxOnePoint(Individual child1, Individual child2) {
        int s = random.nextInt(ONE_MAX_LENGTH - 3) + 2;
        for (int i = s; i < ONE_MAX_LENGTH; i++) {
            int temp = child1.bitString.get(i);
            child1.bitString.set(i, child2.bitString.get(i));
            child2.bitString.set(i, temp);
        }
    }

    static void mutFlipBit(Individual mutant, double indpb) {
        for (int i = 0; i < ONE_MAX_LENGTH; i++) {
            if (random.nextDouble() < indpb) {
                mutant.bitString.set(i, mutant.bitString.get(i) == 1 ? 0 : 1);
            }
        }
    }

    public static void main(String[] args) {
        List<Individual> population = populationCreator(POPULATION_SIZE);
        int generationCounter = 0;

        List<Integer> fitnessValues = new ArrayList<>();
        for (Individual individual : population) {
            individual.fitness.value = oneMaxFitness(individual.bitString);
            fitnessValues.add(individual.fitness.value);
        }

        List<Integer> maxFitnessValues = new ArrayList<>();
        List<Double> meanFitnessValues = new ArrayList<>();

        while (fitnessValues.stream().max(Integer::compare).orElse(0) < ONE_MAX_LENGTH && generationCounter < MAX_GENERATIONS) {
            generationCounter++;
            List<Individual> offspring = selTournament(population, population.size());
            List<Individual> offspringClone = new ArrayList<>();
            for (Individual ind : offspring) {
                List<Integer> bitStringClone = new ArrayList<>(ind.bitString);
                offspringClone.add(new Individual(bitStringClone));
            }

            for (int i = 0; i < offspring.size() - 1; i += 2) {
                if (random.nextDouble() < P_CROSSOVER) {
                    cxOnePoint(offspringClone.get(i), offspringClone.get(i + 1));
                }
            }

            for (Individual mutant : offspringClone) {
                if (random.nextDouble() < P_MUTATION) {
                    mutFlipBit(mutant, 1.0 / ONE_MAX_LENGTH);
                }
            }

            List<Integer> freshFitnessValues = new ArrayList<>();
            for (Individual individual : offspringClone) {
                int fitness = oneMaxFitness(individual.bitString);
                individual.fitness.value = fitness;
                freshFitnessValues.add(fitness);
            }

            population.clear();
            population.addAll(offspringClone);

            fitnessValues.clear();
            for (Individual individual : population) {
                fitnessValues.add(individual.fitness.value);
            }

            int maxFitness = fitnessValues.stream().max(Integer::compare).orElse(0);
            double meanFitness = fitnessValues.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
            maxFitnessValues.add(maxFitness);
            meanFitnessValues.add(meanFitness);
            System.out.printf("Generation %d: Max fitness = %d, Mean fitness = %.2f\n", generationCounter, maxFitness, meanFitness);

            int bestIndex = fitnessValues.indexOf(maxFitness);
            System.out.print("Best individual = ");
            for (int bit : population.get(bestIndex).bitString) {
                System.out.print(bit + " ");
            }
            System.out.println();
        }

        XYSeries maxFitnessSeries = new XYSeries("Max Fitness");
        XYSeries meanFitnessSeries = new XYSeries("Mean Fitness");

        for (int i = 0; i < maxFitnessValues.size(); i++) {
            maxFitnessSeries.add(i, maxFitnessValues.get(i));
            meanFitnessSeries.add(i, meanFitnessValues.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(maxFitnessSeries);
        dataset.addSeries(meanFitnessSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Fitness over Generations", // chart title
                "Generation", // x axis label
                "Fitness", // y axis label
                dataset, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );

        JFrame frame = new JFrame("Genetic Algorithm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ChartPanel chartPanel = new ChartPanel(chart);
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
