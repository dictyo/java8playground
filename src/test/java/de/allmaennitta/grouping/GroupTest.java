package de.allmaennitta.grouping;

import de.allmaennitta.TestUtils;
import de.allmaennitta.grouping.model.Person;
import de.allmaennitta.grouping.model.Pet;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

public class GroupTest {
    List<Person> persons;

    @Before
    public void setUp() {
        persons = TestUtils.buildPersonsList();
    }

    @Test
    public void testMultiLevelGrouping() {
        final Map<String, List<Person>> singleLevelGrouping =
                persons.stream()
                        .collect(groupingBy(Person::getCountry));
        System.out.println(singleLevelGrouping);
        assertThat(singleLevelGrouping.get("USA").get(0).getCountry()).isEqualTo("USA");

        final Map<String, Map<String, List<Person>>> personsByCountryAndCity =
                persons.stream()
                        .collect(groupingBy(Person::getCountry, groupingBy(Person::getCity)
                        ));
        System.out.println(singleLevelGrouping);
        assertThat(personsByCountryAndCity.get("USA").get("NYC").get(0).getCity()).isEqualTo("NYC");

        final Map<String, Map<String, Map<String, List<Person>>>> personsByCountryCityAndPetName =
                persons.stream()
                        .collect(groupingBy(Person::getCountry,
                                groupByCityAndPetName())
        );
        assertThat(personsByCountryCityAndPetName.get("USA").get("NYC").get("Max").get(0).getName()).isEqualTo("John");

        final Map<String, List<Pet>> petsGroupedByCity = persons.stream()
                .collect(
                groupingBy(
                        Person::getCity,
                        mapping(Person::getPet, toList())
                )
        );
        assertThat(petsGroupedByCity.get("NYC").get(0).getName()).isEqualTo("Max");

        final Map<String, Pet> olderPetOfEachCity = persons.stream().collect(
                groupingBy(
                        Person::getCity,
                        collectOlderPet()
                )
        );
        assertThat(olderPetOfEachCity.get("NYC").getName()).isEqualTo("Buddy");
    }

    private Collector<Person, ?, Map<String, Map<String, List<Person>>>> groupByCityAndPetName() {
        return groupingBy(Person::getCity, groupingBy(p -> p.getPet().getName()));
    }
    private Collector<Person, ?, Pet> collectOlderPet() {
        return collectingAndThen(
                mapping(
                        Person::getPet,
                        Collectors.maxBy(Comparator.comparingInt(Pet::getAge))
                ), Optional::get);
    }
}