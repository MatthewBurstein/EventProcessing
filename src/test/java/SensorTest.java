import eventprocessing.models.Reading;
import eventprocessing.models.Sensor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SensorTest {

    private Sensor sensor;

    @Before
    public void createSensor() {
        sensor = new Sensor("id", 1.0, 2.0);
    }

    @Test
    public void addReading_increasesNumberOfReadingsBy1() {
        Reading reading = buildReadingWithValue(1.3);
        sensor.addReading(reading);
        assertThat(sensor.getNumberOfReadings()).isEqualTo(1);
    }

    @Test
    public void addReading_addsReadingValueToTotalValue() {
        Reading reading = buildReadingWithValue(1.3);
        sensor.addReading(reading);
        assertThat(sensor.getTotalValue()).isEqualTo(1.3);
    }

    private Reading buildReadingWithValue(double value) {
        Reading reading = Mockito.mock(Reading.class);
        when(reading.getValue()).thenReturn(value);
        return reading;
    }
}
