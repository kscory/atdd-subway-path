package subway.domain.entity.line;

import lombok.Getter;

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

    public LineSection(Line line, Long upStationId, Long downStationId, Long distance, Long position) {
        this.line = line;
        this.upStationId = upStationId;
        this.downStationId = downStationId;
        this.distance = distance;
        this.position = position;
    }
}
