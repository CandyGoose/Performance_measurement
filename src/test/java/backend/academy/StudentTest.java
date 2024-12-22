package backend.academy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class StudentTest {
    @Test
    void name() {
        Student student = new Student("Ivan", "Ivanov");
        assertThat(student.name())
            .isEqualTo("Ivan");
    }
    @Test
    void surname() {
        Student student = new Student("Ivan", "Ivanov");
        assertThat(student.surname())
            .isEqualTo("Ivanov");
    }
}
