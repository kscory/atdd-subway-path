package subway.fixtures;

import subway.domain.command.LineCommand;
import subway.domain.entity.line.Line;
import subway.domain.entity.line.LineSections;

import java.util.UUID;

public class LineFixture {
    public static Line prepareRandom(Long upStationId, Long downStationId) {
        return Line.init(new LineCommand.CreateLine(UUID.randomUUID().toString(), UUID.randomUUID().toString(), upStationId, downStationId, 10L));
    }

    public static Line prepareLineOne(Long firstStationId, Long lastStationId) {
        Line line = new Line("1호선", "#0052A4", new LineSections());
        for (long i = firstStationId; i<lastStationId; i++) {
            line.addSection(i, i+1, 10L);
        }
        return line;
    }
}
