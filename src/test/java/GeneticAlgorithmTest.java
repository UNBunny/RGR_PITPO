import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class GeneticAlgorithmTest {

    @Test
    void testOneMaxFitness() {
        // Тестирование функции oneMaxFitness
        List<Integer> individual = List.of(1, 1, 1, 0, 0);
        int expectedFitness = 3;
        assertEquals(expectedFitness, GeneticAlgorithm.oneMaxFitness(individual));
    }

    @Test
    void testIndividualCreator() {
        // Тестирование функции individualCreator
        GeneticAlgorithm.Individual individual = GeneticAlgorithm.individualCreator();
        assertEquals(GeneticAlgorithm.ONE_MAX_LENGTH, individual.bitString.size());
        assertTrue(individual.bitString.stream().allMatch(bit -> bit == 0 || bit == 1));
    }

    @Test
    void testPopulationCreator() {
        // Тестирование функции populationCreator
        List<GeneticAlgorithm.Individual> population = GeneticAlgorithm.populationCreator(10);
        assertEquals(10, population.size());
        for (GeneticAlgorithm.Individual individual : population) {
            assertEquals(GeneticAlgorithm.ONE_MAX_LENGTH, individual.bitString.size());
            assertTrue(individual.bitString.stream().allMatch(bit -> bit == 0 || bit == 1));
        }
    }

    @Test
    void testSelTournament() {
        // Тестирование функции selTournament
        List<GeneticAlgorithm.Individual> population = GeneticAlgorithm.populationCreator(10);
        List<GeneticAlgorithm.Individual> selected = GeneticAlgorithm.selTournament(population, population.size());
        assertEquals(population.size(), selected.size());
        assertTrue(selected.stream().allMatch(individual -> individual instanceof GeneticAlgorithm.Individual));
    }

    @Test
    void testCxOnePoint() {
        // Тестирование функции cxOnePoint
        List<Integer> parent1 = List.of(0, 0, 0, 0, 0);
        List<Integer> parent2 = List.of(1, 1, 1, 1, 1);
        List<Integer> parent1Before = new ArrayList<>(parent1);
        List<Integer> parent2Before = new ArrayList<>(parent2);
        GeneticAlgorithm.cxOnePoint((GeneticAlgorithm.Individual) parent1, (GeneticAlgorithm.Individual) parent2);
        assertNotEquals(parent1Before, parent1);
        assertNotEquals(parent2Before, parent2);
    }

    @Test
    void testMutFlipBit() {
        // Тестирование функции mutFlipBit
        List<Integer> individual = List.of(0, 0, 0, 0, 0);
        List<Integer> individualBefore = new ArrayList<>(individual);
        GeneticAlgorithm.mutFlipBit((GeneticAlgorithm.Individual) individual, 1.0);
        assertNotEquals(individualBefore, individual);
    }
}
