package com.example.emmisiontracker.domain.stats;

import com.example.emmisiontracker.constants.TravelMethod;
import com.example.emmisiontracker.domain.travel.Travel;
import com.example.emmisiontracker.domain.travel.TravelStop;
import com.example.emmisiontracker.repository.TravelRepository;
import io.leangen.graphql.annotations.GraphQLQuery;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Stream;

public class OverallStats {

    private final TravelRepository travelRepository;
    public  OverallStats(TravelRepository travelRepository) {
        this.travelRepository = travelRepository;
    }

    @GraphQLQuery(name = "totalEmission")
    public double getTotalEmission() {
        return travelRepository.getStream().mapToDouble(Travel::emission).sum();
    }

    @GraphQLQuery(name = "totalDistance")
    public double getTotalDistance() {
        return travelRepository.getStream().mapToDouble(Travel::distance).sum();
    }

    @GraphQLQuery(name = "totalTravels")
    public int getTravelCount() {
        return travelRepository.getCount();
    }

    @GraphQLQuery(name = "methodsUsed")
    public TravelMethod[] getAllMethodsUsed() {
        ArrayList<TravelMethod> travelMethods = new ArrayList<>();
        travelRepository.getStream().forEach(t -> travelMethods.addAll(t.travelMethods()));

        return travelMethods.stream().distinct().toArray(TravelMethod[]::new);
    }

    @GraphQLQuery(name = "methodDistribution")
    public double[] getMethodDistribution() {
        return getDistribution(s -> s.mapToDouble(TravelStop::distance).sum());
    }

    @GraphQLQuery(name = "emissionDistribution")
    public double[] getEmissionDistribution() {
        return getDistribution(s -> s.mapToDouble(TravelStop::emission).sum());
    }

    private double[] getDistribution(Function<Stream<TravelStop>, Double> reduce)
    {
        TravelMethod[] usedMethods = getAllMethodsUsed();
        double[] methodDistribution = new double[usedMethods.length];

        for (int i = 0; i < usedMethods.length; i++) {
            final TravelMethod method = usedMethods[i];
            double total = travelRepository.getStream().mapToDouble(t ->
                    reduce.apply(t.getStopsWithMethod(method).stream())).sum();

            methodDistribution[i] = total;
        }

        return methodDistribution;
    }

}