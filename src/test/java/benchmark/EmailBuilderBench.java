package benchmark;

import com.mimecast.robin.mime.EmailBuilder;
import com.mimecast.robin.smtp.MessageEnvelope;
import com.mimecast.robin.smtp.session.Session;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@State(Scope.Benchmark)
public class EmailBuilderBench {

    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Setup(Level.Iteration)
    public void setup() {
        outputStream.reset();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        outputStream.close();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
    public void defaultHeaders() throws IOException {
        EmailBuilder emailBuilder = new EmailBuilder(new Session(), new MessageEnvelope())
                .writeTo(outputStream);
    }
}
