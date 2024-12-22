package backend.academy;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State(Scope.Thread)
public class ReflectionBenchmark {
    private Student student;
    private Method method;
    private MethodHandle handle;
    private Function<Student, String> getter;

    @SuppressWarnings({"UncommentedMain", "MagicNumber"})
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(ReflectionBenchmark.class.getSimpleName())
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.NANOSECONDS)
            .forks(1)
            .warmupForks(1)
            .warmupIterations(2)
            .warmupTime(TimeValue.seconds(2))
            .measurementIterations(3)
            .measurementTime(TimeValue.seconds(10))
            .build();


        new Runner(options).run();
    }

    @Setup
    @SuppressWarnings("MultipleStringLiterals")
    public void setup() throws Throwable {
        student = new Student("Name", "Surname");

        method = Student.class.getMethod("name");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(String.class);
        handle = lookup.findVirtual(Student.class, "name", methodType);

        MethodType interfaceType = MethodType.methodType(Function.class);
        MethodType invokeType = MethodType.methodType(Object.class, Object.class);
        MethodType actualType = MethodType.methodType(String.class, Student.class);
        CallSite apply = LambdaMetafactory.metafactory(
            lookup,
            "apply",
            interfaceType,
            invokeType,
            handle,
            actualType
        );
        getter = (Function<Student, String>) apply.getTarget().invokeExact();
    }

    @Benchmark
    public void directAccess(Blackhole bh) {
        String name = student.name();
        bh.consume(name);
    }

    @Benchmark
    public void reflection(Blackhole bh) throws InvocationTargetException, IllegalAccessException {
        String name = (String) method.invoke(student);
        bh.consume(name);
    }

    @Benchmark
    public void handle(Blackhole bh) throws Throwable {
        String name = (String) handle.invoke(student);
        bh.consume(name);
    }

    @Benchmark
    public void lambdaMetafactory(Blackhole bh) {
        String name = getter.apply(student);
        bh.consume(name);
    }
}
