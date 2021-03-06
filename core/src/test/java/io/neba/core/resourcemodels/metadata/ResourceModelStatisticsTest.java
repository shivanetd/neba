/*
  Copyright 2013 the original author or authors.

  Licensed under the Apache License, Version 2.0 the "License";
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.neba.core.resourcemodels.metadata;

import org.junit.Before;
import org.junit.Test;

import static java.lang.Integer.MAX_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olaf Otto
 */
public class ResourceModelStatisticsTest {
    private double median;
    private double average;
    private double maximumDuration;
    private double minimumDuration;

    private ResourceModelStatistics testee;
    private long numberOfMappings;

    @Before
    public void setUp() {
        this.testee = new ResourceModelStatistics();
    }

    @Test
    public void testMedianCalculationWithEvenMappingCountAndNormalDistribution() {
        withDurations(1, 1,   // Interval [1, 2)
                      2, 2,   // Interval [2, 4) -> mean is 3
                      3, 3);  // Interval [2, 4)
        calculateMedian();
        // Median is the mean of 3 + 3 (the center most samples)
        assertMedianIs(3D);
    }

    @Test
    public void testMedianCalculationWithEvenMappingCountAndUnevenDistribution() {
        withDurations(1, 1,               // Interval [1, 2)
                      2, 2, 3, 3,         // Interval [2, 4) -> mean 3
                      108, 108, 109, 109, // Interval [64, 128) -> mean 96
                      250, 250            // Interval [128, 256)
                      );
        calculateMedian();
        // Median is the mean of 3 + 96 (the average of the center most intervals)
        assertMedianIs(49.5);
    }

    @Test
    public void testMedianCalculationWithUnEvenMappingCountAndNormalDistribution() {
        withDurations(1,         // Interval [1, 2)
                      2, 3,      // Interval [2, 4)
                      4, 5, 6, 7 // Interval [4, 8) -> mean 6
                      );
        calculateMedian();
        // Median is the center element (6)
        assertMedianIs(6D);
    }

    @Test
    public void testMedianCalculationWithUnevenMappingCountAndUnevenDistribution() {
        withDurations(1, 1, 1, 1, 1,   // Interval [1, 2)
                      2, 3,            // Interval [2, 4) -> mean 3
                      4, 5, 6, 7       // Interval [4, 8)
                      );
        calculateMedian();
        // Median is the center element (3)
        assertMedianIs(3D);
    }

    @Test
    public void testAverageCalculation() {
        withDurations(1,          // interval [1, 2) -> average 1.5
                      2, 3,       // interval [2, 4) -> average 3
                      4, 5, 6, 7  // interval [4, 8) -> average 6
                        );
        calculateAverage();
        // = (1 * 1.5 + 2 * 3 + 4 * 6) / 7
        assertAverageIs(4.5D);
    }

    @Test
    public void testAverageCalculationWithZeroDuration() {
        withDurations(0, 0, // interval [0, 1) -> average .5
                1, 1,       // interval [1, 2) -> average 1.5
                4, 5, 6, 7  // interval [4, 8) -> average 6
        );
        calculateAverage();
        // = (2 * 0.5 + 2 * 1.5 + 4 * 6) / 8
        assertAverageIs(3.5D);
    }

    @Test
    public void testMaximumMappingDurationCalculation() {
        withDurations(0, 2, 250, 0, 5, 6, 7, 199);
        calculateMaximumMappingDuration();
        // The maximum is the average of the maximum interval ([128, 256))
        assertMaximumMappingDurationIs(192);
    }

    @Test
    public void testMaximumMappingDurationCalculationWithoutAnyElements() {
        calculateMaximumMappingDuration();
        assertMaximumMappingDurationIs(0);
    }

    @Test
    public void testMinimumMappingDurationCalculation() {
        withDurations(1, 2, 3, 4, 250, 6, 7, 1);
        calculateMinimumMappingDuration();
        assertMinimumMappingDurationIs(1.5);
    }

    @Test
    public void testMinimumMappingDurationCalculationWithoutAnyElements() {
        calculateMinimumMappingDuration();
        assertMinimumMappingDurationIs(0);
    }

    @Test
    public void testTotalMappingTimeCalculation() {
        int[] durations = {290, 180, 290, 300, 300, 310, 330, 270, 270, 180, 330};
        withDurations(durations);

        // The total duration is not precise, at it is based on interval average,ut
        // it ha a finite error given a sufficiently distributed data set.
        assertThat(this.testee.getTotalMappingDuration()).isEqualTo(3840);
    }

    @Test
    public void testMappingCount() {
        withMappings(114);
        calculateNumberOfMappings();
        assertNumberOfMappingsIs(114);
    }

    @Test
    public void testFallbackWhenMappingDurationExceedsFrequencyTableBoundaries() {
        withDurations(1, 1, MAX_VALUE);
        calculateMaximumMappingDuration();
        // The average of [2^14, 2^15), the right-most interval.
        assertMaximumMappingDurationIs(24576);
    }

    @Test
    public void testInstantiationCount() {
        assertNumberOfInstantiationsIs(0);

        countInstantiation();
        assertNumberOfInstantiationsIs(1);

        countInstantiation();
        assertNumberOfInstantiationsIs(2);
    }

    @Test
    public void testCacheHitCount() {
        assertNumberOfCacheHitsIs(0);

        countCacheHit();

        assertNumberOfCacheHitsIs(1);

        countCacheHit();

        assertNumberOfCacheHitsIs(2);
    }

    @Test
    public void testReset() {
        countCacheHit();
        countInstantiation();
        withDurations(1, 1);
        withMappings(2);

        calculateMinimumMappingDuration();
        calculateNumberOfMappings();

        assertNumberOfInstantiationsIs(1);
        assertNumberOfCacheHitsIs(1);
        assertMinimumMappingDurationIs(1.5F);
        assertNumberOfMappingsIs(2);

        reset();

        calculateMinimumMappingDuration();
        calculateNumberOfMappings();

        assertNumberOfInstantiationsIs(0);
        assertNumberOfCacheHitsIs(0);
        assertMinimumMappingDurationIs(0);
        assertNumberOfMappingsIs(0);
    }

    private void reset() {
        this.testee.reset();
    }

    private void countCacheHit() {
        this.testee.countCacheHit();
    }

    private void assertNumberOfCacheHitsIs(int expected) {
        assertThat(this.testee.getCacheHits()).isEqualTo(expected);
    }

    private void countInstantiation() {
        this.testee.countInstantiation();
    }

    private void assertNumberOfInstantiationsIs(long expected) {
        assertThat(this.testee.getInstantiations()).isEqualTo(expected);
    }

    private void assertNumberOfMappingsIs(int expected) {
        assertThat(this.numberOfMappings).isEqualTo(expected);
    }

    private void assertMinimumMappingDurationIs(double duration) {
        assertThat(this.minimumDuration).isEqualTo(duration);
    }

    private void calculateMinimumMappingDuration() {
        this.minimumDuration = this.testee.getMinimumMappingDuration();
    }

    private void assertMaximumMappingDurationIs(double duration) {
        assertThat(this.maximumDuration).isEqualTo(duration);
    }

    private void calculateMaximumMappingDuration() {
        this.maximumDuration = this.testee.getMaximumMappingDuration();
    }

    private void assertMedianIs(double median) {
        assertThat(this.median).isEqualTo(median);
    }

    private void calculateMedian() {
        this.median = this.testee.getMappingDurationMedian();
    }

    private void assertAverageIs(double average) {
        assertThat(this.average).isEqualTo(average);
    }

    private void calculateAverage() {
        this.average = this.testee.getAverageMappingDuration();
    }

    private void withDurations(int... durations) {
        for (int duration : durations) {
            this.testee.countMappingDuration(duration);
        }
    }

    private void withMappings(int mappings) {
        for (int i = 0; i < mappings; ++i) {
            this.testee.countSubsequentMapping();
        }
    }

    private void calculateNumberOfMappings() {
        this.numberOfMappings = this.testee.getNumberOfMappings();
    }
}
