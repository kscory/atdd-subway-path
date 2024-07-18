package subway.domain.entity.line;

import lombok.NonNull;
import subway.domain.exception.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Embeddable
public class LineSections implements Iterable<LineSection> {

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<LineSection> data = new ArrayList<>();

    protected void addSection(Line line, Long upStationId, Long downStationId, Long distance) {
        if (data.isEmpty()) {
            data.add(new LineSection(line, upStationId, downStationId, distance, 0L));
            return;
        }

        // 구간의 상행역이 노선에 존재하지 않고 하행역이 상행종착역이라면 구간 가장 앞에 추가
        if (!getAllStationIds().contains(upStationId) && getFirstSection().getUpStationId().equals(downStationId)) {
            data.add(0, new LineSection(line, upStationId, downStationId, distance, data.get(0).getPosition() - 1));
            return;
        }

        // 구간의 상행역이 노선의 하행종창역이 아니도록 구간인 경우 에러
        if (!getLastSection().getDownStationId().equals(upStationId)) {
            throw new InvalidUpStationException(upStationId);
        }

        // 새로운 구간의 하행역이 이미 노선에 포함되어 있는 경우 에러
        verifyDownStationAlreadyExisted(downStationId);


        data.add(new LineSection(line, upStationId, downStationId, distance, getLastSection().getPosition() + 1));
    }

    private void verifyDownStationAlreadyExisted(Long downStationId) {
        if (getAllStationIds().contains(downStationId)) {
            throw new InvalidDownStationException(downStationId);
        }
    }

    protected void deleteSection(Long stationId) {
        if (size() <= 1) {
            throw new SubwayDomainException(SubwayDomainExceptionType.INVALID_SECTION_SIZE);
        }

        // 삭제할 역이 노선의 하행종창역이 아닌 경우 에러
        if (!getLastSection().getDownStationId().equals(stationId)) {
            throw new InvalidStationException(stationId);
        }

        // 마지막 section 에서 제거
        data.remove(size() -1);
    }

    public Stream<LineSection> stream() {
        return data.stream();
    }

    public int size() {
        return data.size();
    }

    public LineSection getFirstSection() {
        return data.isEmpty() ? null : data.get(0);
    }

    public LineSection getLastSection() {
        return data.isEmpty() ? null : data.get(size() - 1);
    }

    private List<Long> getAllUpStationIds() {
        return data.stream()
                .map(LineSection::getUpStationId)
                .collect(Collectors.toList());
    }

    public List<Long> getAllStationIds() {
        // 모든 상행역 id 가져오기
        List<Long> stationIds = getAllUpStationIds();

        // 마지막 하행역 id 추가
        if (getLastSection() != null) {
            stationIds.add(getLastSection().getDownStationId());
        }
        return stationIds;
    }

    @Override
    @NonNull
    public Iterator<LineSection> iterator() {
        return data.iterator();
    }
}
