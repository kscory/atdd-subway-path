package subway.unit.entity;

import autoparams.AutoSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import subway.domain.command.LineCommand;
import subway.domain.entity.line.Line;
import subway.domain.entity.line.LineSections;
import subway.domain.exception.SubwayDomainException;
import subway.domain.exception.SubwayDomainExceptionType;
import subway.fixtures.LineFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

public class LineTest {
    @DisplayName("init")
    @Nested
    class Init {
        @ParameterizedTest
        @AutoSource
        public void sut_returns_new_line(LineCommand.CreateLine command) {
            // when
            Line actual = Line.init(command);

            // then
            assertAll("assert init",
                    () -> assertThat(actual.getName()).isEqualTo(command.getName()),
                    () -> assertThat(actual.getColor()).isEqualTo(command.getColor()),

                    // section
                    () -> assertThat(actual.getSections().size()).isEqualTo(1),
                    () -> assertThat(actual.getSections().getLastSection().getUpStationId()).isEqualTo(command.getUpStationId()),
                    () -> assertThat(actual.getSections().getLastSection().getDownStationId()).isEqualTo(command.getDownStationId()),
                    () -> assertThat(actual.getSections().getLastSection().getDistance()).isEqualTo(command.getDistance())
            );
        }
    }

    @DisplayName("update")
    @Nested
    class Update {
        @ParameterizedTest
        @AutoSource
        public void sut_updated(Line sut, LineCommand.UpdateLine command) {
            // when
            sut.update(command);


            // then
            assertThat(sut.getName()).isEqualTo(command.getName());
            assertThat(sut.getColor()).isEqualTo(command.getColor());
        }
    }

    @DisplayName("addSection")
    @Nested
    class AddSection {
        @Test
        public void sut_throws_if_invalid_up_station_section() {
            // given
            Line sut = LineFixture.prepareLineOne(1L, 4L);

            // when
            SubwayDomainException actual = (SubwayDomainException) catchThrowable(() -> sut.addSection(5L, 6L, 10L));

            // then
            assertThat(actual.getExceptionType()).isEqualTo(SubwayDomainExceptionType.INVALID_UP_STATION);
        }

        @Test
        public void sut_throws_if_downStation_already_existed() {
            // given
            Line sut = LineFixture.prepareLineOne(1L, 4L);

            // when
            SubwayDomainException actual = (SubwayDomainException) catchThrowable(() -> sut.addSection(4L, 3L, 10L));

            // then
            assertThat(actual.getExceptionType()).isEqualTo(SubwayDomainExceptionType.INVALID_DOWN_STATION);
        }

        @Test
        public void sut_add_section_if_empty() {
            // given
            Line sut = new Line("11", "cc", new LineSections());

            // when
            sut.addSection(1L, 2L, 10L);

            // then
            assertThat(sut.getSections().size()).isEqualTo(1);
        }

        @Test
        public void sut_add_section_if_firstSection() {
            // given
            Line sut = LineFixture.prepareLineOne(1L, 4L);

            // when
            sut.addSection(99L, 1L, 10L);

            // then
            assertThat(sut.getSections().getAllStationIds().get(0)).isEqualTo(99L);
            assertThat(sut.getSections().getAllStationIds().get(1)).isEqualTo(1L);
        }

        @Test
        public void sut_add_section_if_lastSection() {
            // given
            Line sut = LineFixture.prepareLineOne(1L, 4L);

            // when
            sut.addSection(4L, 5L, 10L);

            // then
            assertThat(sut.getSections().getLastSection().getUpStationId()).isEqualTo(4L);
            assertThat(sut.getSections().getLastSection().getDownStationId()).isEqualTo(5L);
        }
    }
}
