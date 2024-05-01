package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
    // константы задачи
    private static final int ONE_MAX_LENGTH = 100; // длина подлежащей оптимизации битовой строки

    // константы генетического алгоритма
    private static final int POPULATION_SIZE = 200; // количество индивидуумов в популяции
    private static final double P_CROSSOVER = 0.9; // вероятность скрещивания
    private static final double P_MUTATION = 0.1; // вероятность мутации индивидуума
    private static final int MAX_GENERATIONS = 50; // максимальное количество поколений

    private static final int RANDOM_SEED = 42;
    private static final Random random = new Random(RANDOM_SEED);

    static class FitnessMax {
        int value;

        FitnessMax(int value) {
            this.value = value;
        }
    }

    static class Individual {
        List<Integer> genes;
        FitnessMax fitness;

        Individual(List<Integer> genes) {
            this.genes = genes;
            this.fitness = new FitnessMax(0);
        }
    }

    static int oneMaxFitness(Individual individual) {
        int sum = 0;
        for (Integer gene : individual.genes) {
            sum += gene;
        }
        return sum;
    }

    static Individual individualCreator() {
        List<Integer> genes = new ArrayList<>();
        for (int i = 0; i < ONE_MAX_LENGTH; i++) {
            genes.add(random.nextInt(2));
        }
        return new Individual(genes);
    }

    static List<Individual> populationCreator(int n) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            population.add(individualCreator());
        }
        return population;
    }

    static List<Individual> selTournament(List<Individual> population) {
        List<Individual> offspring = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            int i1, i2, i3;
            i1 = random.nextInt(population.size());
            do {
                i2 = random.nextInt(population.size());
            } while (i2 == i1);
            do {
                i3 = random.nextInt(population.size());
            } while (i3 == i1 || i3 == i2);

            Individual ind1 = population.get(i1);
            Individual ind2 = population.get(i2);
            Individual ind3 = population.get(i3);

            Individual best = (ind1.fitness.value > ind2.fitness.value) ? ind1 : ind2;
            best = (best.fitness.value > ind3.fitness.value) ? best : ind3;

            offspring.add(best);
        }
        return offspring;
    }

    static void cxOnePoint(Individual child1, Individual child2) {
        int s = random.nextInt(ONE_MAX_LENGTH - 2) + 1;
        for (int i = s; i < ONE_MAX_LENGTH; i++) {
            int temp = child1.genes.get(i);
            child1.genes.set(i, child2.genes.get(i));
            child2.genes.set(i, temp);
        }
    }

    static void mutFlipBit(Individual mutant, double indpb) {
        for (int i = 0; i < ONE_MAX_LENGTH; i++) {
            if (random.nextDouble() < indpb) {
                mutant.genes.set(i, (mutant.genes.get(i) == 1) ? 0 : 1);
            }
        }
    }

    public static void main(String[] args) {
        List<Individual> population = populationCreator(POPULATION_SIZE);
        int generationCounter = 0;

        List<Integer> maxFitnessValues = new ArrayList<>();
        List<Double> meanFitnessValues = new ArrayList<>();

        while (generationCounter < MAX_GENERATIONS) {
            generationCounter++;

            List<Individual> offspring = selTournament(population);
            List<Individual> offspringCopy = new ArrayList<>();
            for (Individual ind : offspring) {
                List<Integer> genesCopy = new ArrayList<>(ind.genes);
                offspringCopy.add(new Individual(genesCopy));
            }

            for (int i = 0; i < offspring.size(); i += 2) {
                if (random.nextDouble() < P_CROSSOVER) {
                    cxOnePoint(offspringCopy.get(i), offspringCopy.get(i + 1));
                }
            }

            for (Individual mutant : offspringCopy) {
                mutFlipBit(mutant, 1.0 / ONE_MAX_LENGTH);
            }

            List<Integer> freshFitnessValues = new ArrayList<>();
            for (Individual individual : offspringCopy) {
                int fitnessValue = oneMaxFitness(individual);
                freshFitnessValues.add(fitnessValue);
                individual.fitness.value = fitnessValue;
            }

            population = offspringCopy;

            int maxFitness = freshFitnessValues.stream().max(Integer::compareTo).orElse(0);
            double meanFitness = freshFitnessValues.stream().mapToInt(Integer::intValue).average().orElse(0);

            maxFitnessValues.add(maxFitness);
            meanFitnessValues.add(meanFitness);

            System.out.println("Поколение " + generationCounter + ": Макс приспособ. = " + maxFitness + ", Средняя приспособ.= " + meanFitness);

            int bestIndex = freshFitnessValues.indexOf(maxFitness);
            System.out.print("Лучший индивидуум = ");
            for (Integer gene : population.get(bestIndex).genes) {
                System.out.print(gene + " ");
            }
            System.out.println("\n");
        }

        XYSeries maxFitnessSeries = new XYSeries("Максимальная приспособленность");
        XYSeries meanFitnessSeries = new XYSeries("Средняя приспособленность");
        for (int i = 0; i < maxFitnessValues.size(); i++) {
            maxFitnessSeries.add(i, maxFitnessValues.get(i));
            meanFitnessSeries.add(i, meanFitnessValues.get(i));
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(maxFitnessSeries);
        dataset.addSeries(meanFitnessSeries);

        // Создание графика
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Зависимость максимальной и средней приспособленности от поколения",
                "Поколение", "Приспособленность", dataset);

        // Отображение графика в окне
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Генетический алгоритм");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            ChartPanel chartPanel = new ChartPanel(chart);
            frame.add(chartPanel, BorderLayout.CENTER);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
