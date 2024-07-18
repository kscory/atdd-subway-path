package subway.domain.entity.line;

import lombok.NonNull;
import subway.domain.exception.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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

        // 구간의 상행역이 노선에 존재하지 않는 경우
        if (!getAllStationIds().contains(upStationId)) {
            // 하행역이 상행종착역이라면 구간 가장 앞에 추가
            if (getFirstSection().getUpStationId().equals(downStationId)) {
                data.add(0, new LineSection(line, upStationId, downStationId, distance, data.get(0).getPosition() - 1));
                return;
            }
            // 그 외의 경우 에러
            throw new InvalidUpStationException(upStationId);
        }

        // 구간의 상행역이 노선의 하행종창역이라면 구간 가장 뒤에 추가
        if (!getAllStationIds().contains(downStationId) && getLastSection().getDownStationId().equals(upStationId)) {
            data.add(new LineSection(line, upStationId, downStationId, distance, data.get(data.size()-1).getPosition() + 1));
            return;
        }

        // 새로운 구간의 하행역이 이미 노선에 포함되어 있는 경우 에러
        verifyDownStationAlreadyExisted(downStationId);

        // 조건을 모두 통과했다면 중간에 추가
        Optional<Integer> sameIdx = findSameUpStationIdx(upStationId);
        if (sameIdx.isPresent()) {
            int idx = sameIdx.get();

            // 같은 position 으로 추가
            LineSection changed = data.get(idx);
            data.add(idx, new LineSection(line, upStationId, downStationId, distance, data.get(idx).getPosition()));
            data.add(idx+1, new LineSection(line, downStationId, changed.getDownStationId(), distance, data.get(idx).getPosition()));
            data.remove(idx+2);

            // position 들 1씩 추가
            for (int i=idx+1; i<data.size(); i++) {
                data.get(i).increasePosition();
            }
        }

    }

    private Optional<Integer> findSameUpStationIdx(Long upStationId) {
        for (int i=0; i<data.size(); i++) {
            if (data.get(i).getUpStationId().equals(upStationId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
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
