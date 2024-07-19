package subway.domain.entity.line;

import lombok.Getter;
import org.springframework.data.util.Pair;
import subway.domain.exception.SubwayDomainException;
import subway.domain.exception.SubwayDomainExceptionType;

import javax.persistence.*;

@Getter
@Entity(name = "line_sections")
public class LineSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private Line line;

    @Column(nullable = false)
    private Long upStationId;

    @Column(nullable = false)
    private Long downStationId;

    @Column(nullable = false)
    private Long distance;

    @Column(nullable = false)
    private Long position;

    protected LineSection() {}

    public LineSection(Line line, Long upStationId, Long downStationId, Long distance) {
        this.line = line;
        this.upStationId = upStationId;
        this.downStationId = downStationId;
        this.distance = distance;
        this.position = 0L;
    }

    public void changePosition(Long position) {
        this.position = position;
    }

    public Pair<LineSection, LineSection> splitSection(Long middleStationId, Long firstDistance) {
        long secondDistance = distance - firstDistance;
        if (secondDistance < 0) {
            throw new SubwayDomainException(SubwayDomainExceptionType.INVALID_SECTION_DISTANCE);
        }

        LineSection first = new LineSection(line, upStationId, middleStationId, firstDistance);
        LineSection second = new LineSection(line, middleStationId, downStationId, secondDistance);

        return Pair.of(first, second);
    }

    public boolean isPrevSection(LineSection section) {
        return downStationId.equals(section.getUpStationId());
    }

    public boolean isNextSection(LineSection section) {
        return upStationId.equals(section.getDownStationId());
    }
}
