package com.rentoss.core.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Builder
public class SliceResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final boolean hasNext;
    private final boolean first;
    private final boolean last;

    public static <T> SliceResponse<T> from(Slice<T> slice) {
        return SliceResponse.<T>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .first(slice.isFirst())
                .last(slice.isLast())
                .build();
    }
}
