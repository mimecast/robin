package benchmark;

import com.mimecast.robin.mime.EmailParser;
import com.mimecast.robin.smtp.io.LineInputStream;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@State(Scope.Benchmark)
public class EmailParserBench {

    static final String mime = "MIME-Version: 1.0\r\n" +
            "From: Lady Robin <lady.robin@example.com>\r\n" +
            "To: Sir Robin <sir.robin@example.com>\r\n" +
            "Date: Thu, 28 Jan 2021 20:27:09 +0000\r\n" +
            "Message-ID: <twoRobinsMakeAFamily@example.com>\r\n" +
            "Subject: Robin likes\r\n" +
            "Content-Type: text/plain; charset=\"ISO-8859-1\",\r\n\tname=robin.txt,\r\n\tlanguage='en_UK';\r\n" +
            "Content-Disposition: inline charset='ISO-8859-1'\r\n\tfilename=robin.txt;\r\n\tlanguage=en_UK,";

    @State(Scope.Thread)
    public static class Input {
        public LineInputStream inputStream = new LineInputStream(new ByteArrayInputStream(mime.getBytes()), 1024);
    }

    @TearDown(Level.Iteration)
    public void tearDown(Input state) throws IOException {
        state.inputStream.close();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
    public void headers(Input state) throws IOException {
        EmailParser parser = new EmailParser(state.inputStream)
                .parse(true);
    }
}
