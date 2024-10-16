package com.dylibso.examples.kafka.simulation;

import com.dylibso.examples.kafka.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Random;

/**
 * Simulates Orders within a given range.
 */
public class Simulator {
    private static final Logger LOGGER = Logger.getLogger(Simulator.class);

    private final Random priceGen;
    private final Random volGen;
    private final double minPrice;
    private final double spanPrice;
    private final int maxVol;

    Simulator(double minPrice, double maxPrice, int maxVol) throws IOException {
        this.priceGen = new Random();
        this.volGen = new Random();
        this.minPrice = minPrice;
        this.spanPrice = maxPrice - minPrice;
        this.maxVol = maxVol;
    }

    Order next() throws IOException {
        return new Order(
                ZonedDateTime.now(),
                generatePrice(),
                volGen.nextInt(1, maxVol));
    }

    private static final double scale = 1e5;

    private double generatePrice() {
        double v = minPrice +
                priceGen.nextGaussian(.5, .15) * spanPrice;
        return Math.round(v * scale) / scale;
    }
}
