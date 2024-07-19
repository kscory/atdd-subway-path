package subway.domain.entity.line;

import lombok.NonNull;
import org.springframework.data.util.Pair;
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

    public LineSection get(int idx) {
        return data.get(idx);
    }

    private boolean containsStation(Long stationId) {
        return getAllStationIds().contains(stationId);
    }

    protected void addSection(LineSection section) {
        if (data.isEmpty()) {
            data.add(section);
            return;
        }

        // 상행역이 노선에 없는데 가장 앞에 추가되지 않는 경우
        if (!containsStation(section.getUpStationId()) && !section.isPrevSection(getFirstSection())) {
            throw new InvalidStationException("상행역이 노선에 존재하지 않는다면 가장 앞 구간으로 추가해야 합니다.");
        }

        // 하행역이 노선에 있는데 가장 앞에 추가되지 않는 경우
        if (containsStation(section.getDownStationId()) && !section.isPrevSection(getFirstSection())) {
            throw new InvalidStationException("하행역이 노선에 존재하다면 가장 앞 구간으로 추가해야 합니다.");
        }

        // 상행역이 존재하는데 가장 앞에 추가되는 경우
        if (containsStation(section.getUpStationId()) && section.isPrevSection(getFirstSection())) {
            throw new InvalidStationException("상행역이 노선에 존재한다면 가장 앞 구간으로 추가할 수 없습니다.");
        }


        // 추가되는 구간이 가장 앞인 경우
        if (section.isPrevSection(getFirstSection())) {
            addFirstSectionAndArrange(section);
            return;
        }

        // 추가되는 구간이 가장 뒤인 경우
        if (section.isNextSection(getLastSection())) {
            addLastSectionAndArrange(section);
            return;
        }

        // 추가되는 구간이 중간인 경우
        findSameUpStationIdx(section.getUpStationId()).ifPresent(idx -> addMiddleSectionAndArrange(section, idx));
    }

    private void addFirstSectionAndArrange(LineSection section) {
        data.add(0, section);
        arrangePosition();
    }

    private void addLastSectionAndArrange(LineSection section) {
        data.add(section);
        arrangePosition();
    }

    private Optional<Integer> findSameUpStationIdx(Long upStationId) {
        for (int i=0; i<data.size(); i++) {
            if (data.get(i).getUpStationId().equals(upStationId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private void addMiddleSectionAndArrange(LineSection section, int idx) {
        Pair<LineSection, LineSection> split = data.get(idx).splitSection(section.getDownStationId(), section.getDistance());
        data.add(idx, split.getFirst());
        data.add(idx+1, split.getSecond());
        data.remove(idx+2);
        arrangePosition();
    }

    private void arrangePosition() {
        for (int i=0; i<data.size(); i++) {
            data.get(i).changePosition((long) i);
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
