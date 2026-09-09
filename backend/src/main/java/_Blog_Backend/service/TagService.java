package _Blog_Backend.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _Blog_Backend.dto.TagTrendDto;
import _Blog_Backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagTrendDto> getTrendingTags(int limit) {
        return tagRepository.findTrendingTags(PageRequest.of(0, limit))
                .stream()
                .map(projection -> new TagTrendDto(
                        projection.getId(),
                        projection.getName(),
                        projection.getPostCount()))
                .toList();
    }
}
