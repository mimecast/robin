package benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Benchmarks {

    public static void main(String[] args) throws RunnerException {
        final Options options = new OptionsBuilder()
                .include(EmailBuilderBench.class.getSimpleName())
                .include(EmailParserBench.class.getSimpleName())
                .threads(4)
                .forks(1)
                .build();

        new Runner(options).run();
    }
}
